package com.bruce.kotlin.readhub.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by yang.jianan on 2017/09/01 17:08.
 */

abstract class BaseFragment : Fragment() {
    private var rootView: View? = null
    private var isFragmentVisiable: Boolean = false
    private val isFirst: Boolean = false

    /**
     * 此方法 再 onCreateView 生命周期之前
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            isFragmentVisiable = true
        }
        if (rootView == null) {
            return
        }

        //可见，并且没有加载过
        if (!isFirst && isFragmentVisiable) {
            onFragmentVisiableChange(true)
            return
        }
        //由可见——>不可见 已经加载过
        if (isFragmentVisiable) {
            onFragmentVisiableChange(false)
            isFragmentVisiable = false
        }
    }

    private fun onFragmentVisiableChange(b: Boolean) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutResources(), container, false)
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    abstract fun initViews()

    abstract fun getLayoutResources(): Int
}
