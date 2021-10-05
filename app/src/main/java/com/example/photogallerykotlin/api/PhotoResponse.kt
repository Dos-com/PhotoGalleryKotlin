package com.example.photogallerykotlin.api

import com.example.photogallerykotlin.GalleryItem
import com.google.gson.annotations.SerializedName

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List <GalleryItem>
}