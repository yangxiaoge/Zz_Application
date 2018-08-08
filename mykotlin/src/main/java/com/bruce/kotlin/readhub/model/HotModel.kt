package com.bruce.kotlin.readhub.model


import com.bruce.kotlin.readhub.api.Api
import com.bruce.kotlin.readhub.api.HostType
import com.bruce.kotlin.readhub.contract.HotContract
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by yangjianan on 2018/8/8.
 */
class HotModel : HotContract.Model {
    override fun getTopics(params: Map<String, String>): Observable<String> {
        return Api.getDefault(HostType.TYPE_APP)
                .getTopic(params)
                .map { responseBody -> responseBody.string() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}
