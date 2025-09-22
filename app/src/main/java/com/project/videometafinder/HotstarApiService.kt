package com.project.videometafinder

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface HotstarApiService {

    @GET("api/internal/bff/v2/pages/1021/spaces/3853")
    fun getHotstarContent(
        @Query("content_id") contentId: String,
        @Query("mode") mode: String = "default",
        @Query("offset") offset: Int = 0,
        @Query("page_enum") pageEnum: String = "watch",
        @Query("search_query") searchQuery: String, // Example: "house"
        @Query("size") size: Int = 10,
        @Query("tabName") tabName: String = "episode"
    ): Call<HotstarResponse>
}