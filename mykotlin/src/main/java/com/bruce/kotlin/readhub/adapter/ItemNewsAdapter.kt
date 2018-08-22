package com.bruce.kotlin.readhub.adapter

import com.bruce.kotlin.readhub.bean.HotTopic
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder

/**
 * Created by yangjianan on 2018/8/22.
 */
class ItemNewsAdapter(data: List<HotTopic>?) : BaseQuickAdapter<HotTopic, BaseViewHolder>(data) {
    init {
        mLayoutResId = android.R.layout.activity_list_item
    }

    override fun convert(helper: BaseViewHolder, item: HotTopic) {
        helper.setText(android.R.id.text1, item.summary)
    }
}
