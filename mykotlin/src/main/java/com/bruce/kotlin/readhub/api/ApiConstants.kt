package com.bruce.kotlin.readhub.api

/**
 * Created by yangjianan on 2018/8/7.
 */
object ApiConstants {
    val TOPIC_PAGE_SIZE = 20 // 热门话题,每页请求数
    val APP_HOST = "https:/api.readhub.cn/"//URL
    val TEST_HOST = "https:/api.readhub.cn/"//URL
    val SINGLE_LOGININ_RESTRICT_CODE = "10004" //单点登录码

    /**
     * 获取对应的host
     *
     * @param hostType host类型
     * @return host
     */
    fun getHost(hostType: Int): String {
        val host: String
        when (hostType) {
            HostType.TYPE_TEST -> host = TEST_HOST
            HostType.TYPE_APP -> host = APP_HOST
            HostType.TYPE_LOGIN -> host = APP_HOST
            else -> host = ""
        }
        return host
    }
}
