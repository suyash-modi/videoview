package com.droid.videoview

import retrofit2.Call
import retrofit2.http.GET

interface ApiInterface {
    @GET("index.php?p=showAllVideos")
    fun getAllVideos(): Call<MyData>
}