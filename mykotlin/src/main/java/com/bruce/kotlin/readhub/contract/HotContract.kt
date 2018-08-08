package com.bruce.kotlin.readhub.contract


import com.bruce.kotlin.readhub.bean.HotTopic
import com.yjn.common.base.BaseModel
import com.yjn.common.base.BasePresenter
import com.yjn.common.base.BaseView

import io.reactivex.Observable

/**
 * Created by yangjianan on 2018/8/8.
 * Desc: MVP 的桥梁
 */
interface HotContract {
    interface Model : BaseModel {
        fun getTopics(params: Map<String, String>): Observable<String>
    }

    interface View : BaseView {
        fun getDataSuccess(data: List<HotTopic>)
    }

    abstract class Presenter : BasePresenter<View, Model>() {
        abstract fun getTopics(params: Map<String, String>)
    }
}
