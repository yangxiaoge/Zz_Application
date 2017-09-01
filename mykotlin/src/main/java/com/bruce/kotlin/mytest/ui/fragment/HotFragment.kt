package com.bruce.kotlin.mytest.ui.fragment

import com.bruce.kotlin.mytest.R
import kotlinx.android.synthetic.main.home_fragment.*

/**
 * Created by yang.jianan on 2017/09/01 17:07.
 */

class HotFragment : BaseFragment() {
    override fun initViews() {
        txt_content.text = "Ranking"
    }

    override fun getLayoutResources(): Int {
        return R.layout.home_fragment;
    }

}
