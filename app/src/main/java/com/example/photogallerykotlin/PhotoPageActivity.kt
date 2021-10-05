package com.example.photogallerykotlin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

private const val TAG = "PhotoPageActivity"

class PhotoPageActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_page)

        val fm = supportFragmentManager
        val currentFragment = fm.findFragmentById(R.id.fragment_container)

        if (currentFragment == null){
            val fragment = PhotoPageFragment.newInstance(intent.data as Uri)
            fm.beginTransaction().add(R.id.fragment_container,fragment).commit()
        }
    }



    companion object{
        fun newIntent(context: Context, pagePageUri: Uri): Intent{
            return Intent(context, PhotoPageActivity::class.java).apply {
                data = pagePageUri
            }
        }
    }
}