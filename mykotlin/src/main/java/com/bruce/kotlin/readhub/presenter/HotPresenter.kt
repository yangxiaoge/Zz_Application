package com.bruce.kotlin.readhub.presenter

import com.alibaba.fastjson.JSON
import com.bruce.kotlin.readhub.bean.HotTopic
import com.bruce.kotlin.readhub.contract.HotContract
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


/**
 * Created by yangjianan on 2018/8/8.
 */
class HotPresenter : HotContract.Presenter() {
    override fun getTopics(params: Map<String, String>) {
        mView.showLoading("加载中")
        mRxManage.add(mModel.getTopics(params).subscribe({ s ->
            val backJO = JSON.parseObject(s)
            val data = backJO.getString("data")
            val hotTopicList = Gson().fromJson<List<HotTopic>>(data, object : TypeToken<List<HotTopic>>() {
            }.type)
            if (hotTopicList.isNotEmpty()) {
                mView.getDataSuccess(hotTopicList)
            } else {
                mView.showErrorTip("暂无热门新闻")
            }
        }) { e -> mView.showErrorTip(e.message) })
    }
}
