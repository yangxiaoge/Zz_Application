package com.bruce.kotlin.readhub.api

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * Created by yangjianan on 2018/8/7.
 */
interface ApiService {
    //热门话题
    @GET("topic")
    fun getTopic(@QueryMap qureMap: Map<String, String>):
    //    Observable<ResponseBody> getTopic(@Query("lastCursor") String lastCursor, @Query("pageSize") int pageSize);
            Observable<ResponseBody>

    //话题详情
    @GET("topic/{topicId}")
    fun getTopicDetail(@Path("topicId") topicId: String): Observable<ResponseBody>

    //科技动态
    @GET("news")
    fun getTechNews(@Query("lastCursor") lastCursor: String, @Query("pageSize") pageSize: Int): Observable<ResponseBody>

    //开发者资讯
    @GET("technews")
    fun getDevNews(@Query("lastCursor") lastCursor: String, @Query("pageSize") pageSize: Int): Observable<ResponseBody>

    //区块链资讯
    @GET("blockchain")
    fun getapiBlockNews(@Query("lastCursor") lastCursor: String, @Query("pageSize") pageSize: Int): Observable<ResponseBody>
}
