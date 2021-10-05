package com.example.photogallerykotlin

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

class ThumbnailDownloader<in T> (private val responseHandler:Handler, private val onThumbnailDownloaded: (T, Bitmap) -> Unit): HandlerThread(TAG), LifecycleObserver {
    private var hasQuit = false
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()
    val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver{

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setUp(){
            Log.i(TAG, "Starting background thread")
            start()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown(){
            Log.i(TAG, "Destroy backgroud thread")
            quit()
        }
    }

    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue(){
            Log.i(TAG, "Clearing all requests from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    fun queueThumbnail(target: T, url: String){
        Log.i(TAG, "Got a Url $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }


    @Suppress("UNCHECKED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        super.onLooperPrepared()
        requestHandler = object : Handler(){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)

                if (msg.what == MESSAGE_DOWNLOAD){
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL: ${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T){
        var url = requestMap[target] ?: return

        val bitmap = flickrFetchr.fetchPhoto(url) ?: return

        responseHandler.post(Runnable {
            Log.d(TAG, "handleRequest: bitmap ${bitmap == null}")
            Log.d(TAG, "handleRequest: url ${requestMap[target] != null}")

            if (requestMap[target] != url || hasQuit) {
                return@Runnable
            }

            requestMap.remove(target)

            onThumbnailDownloaded(target, bitmap)
        })
    }

}