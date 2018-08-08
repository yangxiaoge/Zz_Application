package com.bruce.kotlin.readhub.ui.fragment

import com.bruce.kotlin.readhub.R
import kotlinx.android.synthetic.main.home_fragment.*

/**
 * Created by yang.jianan on 2017/09/01 17:07.
 */

class MimeFragment : BaseFragment() {
    override fun initViews() {
        txt_content.text = getString(R.string.tab_mine)
    }

    override fun getLayoutResources(): Int {
        return R.layout.home_fragment
    }

}
