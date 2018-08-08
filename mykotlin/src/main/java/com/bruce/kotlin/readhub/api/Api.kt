package com.bruce.kotlin.readhub.api

import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.SparseArray
import com.alibaba.fastjson.JSON
import com.bruce.kotlin.readhub.Application
import com.yjn.common.base.AppManager
import com.yjn.common.util.NetWorkUtils
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.EOFException
import java.io.File
import java.lang.Long
import java.nio.charset.Charset
import java.nio.charset.UnsupportedCharsetException
import java.util.concurrent.TimeUnit

/**
 * Created by yangjianan on 2018/8/7.
 */
class Api//构造方法私有
private constructor(hostType: Int) {
    var retrofit: Retrofit
    var movieService: ApiService
    var okHttpClient: OkHttpClient

    /**
     * 云端响应头拦截器，用来配置缓存策略
     * Dangerous interceptor that rewrites the server's cache-control header.
     */
    private val mRewriteCacheControlInterceptor = Interceptor { chain ->
        var request = chain.request()
        val cacheControl = request.cacheControl().toString()
        if (!NetWorkUtils.isNetConnected(Application.getAppContext())) {
            request = request.newBuilder()//
                    .cacheControl(if (TextUtils.isEmpty(cacheControl)) CacheControl.FORCE_NETWORK else CacheControl.FORCE_CACHE)//
                    .build()
        }
        val originalResponse = chain.proceed(request)
        if (NetWorkUtils.isNetConnected(Application.getAppContext())) {
            //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置

            originalResponse.newBuilder()//
                    .header("Cache-Control", cacheControl)//
                    .removeHeader("Pragma")//
                    .build()
        } else {
            originalResponse.newBuilder()//
                    .header("Cache-Control", "public, only-if-cached, max-stale=$CACHE_STALE_SEC")//
                    .removeHeader("Pragma")//
                    .build()
        }
    }


    /**
     * @Description 网络拦截器
     */
    private// Buffer the entire body.
    // 单点登录
    //ActivityUtils.logout();
    val ins: Interceptor
        get() = Interceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)

            val responseBody = response.body()
            val contentLength = responseBody!!.contentLength()


            if (!bodyEncoded(response.headers())) {
                val source = responseBody.source()
                source.request(Long.MAX_VALUE)
                val buffer = source.buffer()

                var charset: Charset? = UTF8
                val contentType = responseBody.contentType()
                if (contentType != null) {
                    try {
                        charset = contentType.charset(UTF8)
                    } catch (e: UnsupportedCharsetException) {
                        return@Interceptor response
                    }

                }

                if (!isPlaintext(buffer)) {
                    return@Interceptor response
                }

                if (contentLength != 0L) {
                    val result = buffer.clone().readString(charset!!)
                    if (ApiConstants.SINGLE_LOGININ_RESTRICT_CODE == JSON.parseObject(result).getString("code")) {
                        val curActivity = AppManager.getAppManager().currentActivity()
                        curActivity!!.runOnUiThread {
                            AlertDialog.Builder(curActivity)
                                    .setTitle("提醒")
                                    .setMessage("您的账号在其他地方登陆。您将退出登录")
                                    .setPositiveButton("好的") { dialog, which -> }
                                    .setCancelable(false)
                                    .create()
                                    .show()
                        }

                    }


                }

            }
            response
        }


    init {
        //开启Log
        val logInterceptor = HttpLoggingInterceptor()
        logInterceptor.level = HttpLoggingInterceptor.Level.BODY
        //缓存
        val cacheFile = File(Application.getAppContext().cacheDir, "cache")
        val cache = Cache(cacheFile, (1024 * 1024 * 100).toLong()) //100Mb
        //增加头部信息
        val headerInterceptor = Interceptor { chain ->
            val builder = chain.request().newBuilder()
                                    .addHeader("channel", HeaderConfig.channel)//
                                    .addHeader("deviceNo", HeaderConfig.getDeviceNo1())//
            //                        .addHeader("token", HeaderConfig.token);
            if (hostType == HostType.TYPE_LOGIN) {
                //                    builder.addHeader("Content-Type", "multipart/form-data");
                builder.addHeader("Content-Type", "application/x-www-form-urlencoded")
            } else {
                builder.addHeader("Content-Type", "application/json")
            }
            chain.proceed(builder.build())
        }

        okHttpClient = OkHttpClient.Builder()//
                .readTimeout(READ_TIME_OUT.toLong(), TimeUnit.MILLISECONDS)//
                .connectTimeout(CONNECT_TIME_OUT.toLong(), TimeUnit.MILLISECONDS)//
                .addInterceptor(mRewriteCacheControlInterceptor)//
                .addNetworkInterceptor(mRewriteCacheControlInterceptor)//
                .addInterceptor(headerInterceptor)//
                .addInterceptor(logInterceptor)//
                .addInterceptor(ins)//
                .cache(cache)//
                .build()

        //        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").serializeNulls().create();
        retrofit = Retrofit.Builder()//
                .client(okHttpClient)//
                //                .addConverterFactory(GsonConverterFactory.create(gson))//
                .addConverterFactory(GsonConverterFactory.create())//
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//
                .baseUrl(ApiConstants.getHost(hostType))//
                .build()
        movieService = retrofit.create(ApiService::class.java)
    }

    private fun bodyEncoded(headers: Headers): Boolean {
        val contentEncoding = headers.get("Content-Encoding")
        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
    }

    companion object {
        //读超时长，单位：毫秒
        val READ_TIME_OUT = 7676
        //连接时长，单位：毫秒
        val CONNECT_TIME_OUT = 7676
        private val sRetrofitManager = SparseArray<Api>(HostType.TYPE_COUNT)

        /*************************缓存设置 */
        /*
   1. noCache 不使用缓存，全部走网络

    2. noStore 不使用缓存，也不存储缓存

    3. onlyIfCached 只使用缓存

    4. maxAge 设置最大失效时间，失效则不使用 需要服务器配合

    5. maxStale 设置最大失效时间，失效则不使用 需要服务器配合 感觉这两个类似 还没怎么弄清楚，清楚的同学欢迎留言

    6. minFresh 设置有效时间，依旧如上

    7. FORCE_NETWORK 只走网络

    8. FORCE_CACHE 只走缓存*/

        /**
         * 设缓存有效期为两天
         */
        private val CACHE_STALE_SEC = (60 * 60 * 24 * 2).toLong()
        /**
         * 查询缓存的Cache-Control设置，为if-only-cache时只查询缓存而不会请求服务器，max-stale可以配合设置缓存失效时间
         * max-stale 指示客户机可以接收超出超时期间的响应消息。如果指定max-stale消息的值，那么客户机可接收超出超时期指定值之内的响应消息。
         */
        private val CACHE_CONTROL_CACHE = "only-if-cached, max-stale=$CACHE_STALE_SEC"
        /**
         * 查询网络的Cache-Control设置，头部Cache-Control设为max-age=0
         * (假如请求了服务器并在a时刻返回响应结果，则在max-age规定的秒数内，浏览器将不会发送对应的请求到服务器，数据由缓存直接返回)时则不会使用缓存而请求服务器
         */
        private val CACHE_CONTROL_AGE = "max-age=0"


        /**
         * @param hostType TEST_NUM：1 (测试的host)
         */
        fun getDefault(hostType: Int): ApiService {
            var retrofitManager: Api? = sRetrofitManager.get(hostType)
            if (retrofitManager == null) {
                retrofitManager = Api(hostType)
                sRetrofitManager.put(hostType, retrofitManager)
            }
            return retrofitManager.movieService
        }


        /**
         * 根据网络状况获取缓存的策略
         */
        val cacheControl: String
            get() = if (NetWorkUtils.isNetConnected(Application.getAppContext())) CACHE_CONTROL_AGE else CACHE_CONTROL_CACHE


        private val UTF8 = Charset.forName("UTF-8")

        @Throws(EOFException::class)
        internal fun isPlaintext(buffer: Buffer): Boolean {
            try {
                val prefix = Buffer()
                val byteCount = if (buffer.size() < 64) buffer.size() else 64
                buffer.copyTo(prefix, 0, byteCount)
                for (i in 0..15) {
                    if (prefix.exhausted()) {
                        break
                    }
                    val codePoint = prefix.readUtf8CodePoint()
                    if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                        return false
                    }
                }
                return true
            } catch (e: EOFException) {
                return false
            }

        }
    }
}
