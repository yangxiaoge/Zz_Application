package com.bruce.kotlin.readhub.ui

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bruce.kotlin.readhub.R
import com.bruce.kotlin.readhub.R.layout.activity_main
import com.bruce.kotlin.readhub.ui.fragment.HotFragment
import com.bruce.kotlin.readhub.ui.fragment.MimeFragment
import com.bruce.kotlin.readhub.ui.fragment.NewsFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var findFragment: NewsFragment? = null
    var hotFragment: HotFragment? = null
    var mimeFragment: MimeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        // 隐藏 虚拟导航栏
        /*val window = window;
        val param = window.attributes
        param.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.attributes = param*/

        initToolBar()
        initFragments(savedInstanceState)
        initNavBtn()
    }

    private fun initFragments(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            //异常情况
            val fragments = supportFragmentManager.fragments
            for (fragment in fragments) {
                if (fragment is NewsFragment) {
                    findFragment = fragment
                }
                if (fragment is HotFragment) {
                    hotFragment = fragment
                }
                if (fragment is MimeFragment) {
                    mimeFragment = fragment
                }
            }
        } else {
            findFragment = NewsFragment()
            hotFragment = HotFragment()
            mimeFragment = MimeFragment()

            val fragmentTrans = supportFragmentManager.beginTransaction()
            fragmentTrans.add(R.id.fl_content, hotFragment!!)
            fragmentTrans.add(R.id.fl_content, findFragment!!)
            fragmentTrans.add(R.id.fl_content, mimeFragment!!)
            fragmentTrans.commit()
        }

        supportFragmentManager.beginTransaction()
                .hide(findFragment!!)
                .hide(hotFragment!!)
                .hide(mimeFragment!!)
                .commit()
    }

    private fun initToolBar() {
        tv_bar_title.text = getToday()
        tv_bar_title.typeface = Typeface.createFromAsset(this.assets, "fonts/Lobster-1.4.otf")
    }

    /**
     * 获取当前属于星期几
     */
    private fun getToday(): String {
        val dayList = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val data = Date()
        val calendar: Calendar = Calendar.getInstance()
        calendar.time = data
        var index = calendar.get(Calendar.DAY_OF_WEEK) - 1
        if (index < 0) {
            index = 0
        }
        return dayList[index]
    }

    /**
     * 初始化底部导航
     */
    private fun initNavBtn() {
        rb_hot.setOnClickListener(this)
        rb_find.setOnClickListener(this)
        rb_mine.setOnClickListener(this)

        //默认选中首页
        rb_hot.performClick()
    }

    /**
     * 点击事件
     */
    override fun onClick(v: View?) {
        clearState();
        when (v?.id) {
            R.id.rb_hot -> {
                rb_hot.isChecked = true
                rb_hot.setTextColor(resources.getColor(android.R.color.black))

                tv_bar_title.visibility = View.VISIBLE
                tv_bar_title.text = getToday()

                supportFragmentManager.beginTransaction().show(hotFragment!!)
                        .hide(findFragment!!)
                        .hide(mimeFragment!!)
                        .commit()
            }

            R.id.rb_find -> {
                rb_find.isChecked = true
                rb_find.setTextColor(resources.getColor(android.R.color.black))

                tv_bar_title.visibility = View.VISIBLE
                tv_bar_title.text = getString(R.string.tab_news)

                supportFragmentManager.beginTransaction().show(findFragment!!)
                        .hide(hotFragment!!)
                        .hide(mimeFragment!!)
                        .commit()
            }

            R.id.rb_mine -> {
                rb_mine.isChecked = true
                rb_mine.setTextColor(resources.getColor(android.R.color.black))

                tv_bar_title.visibility = View.VISIBLE
                tv_bar_title.text = getString(R.string.tab_mine)

                supportFragmentManager.beginTransaction().show(mimeFragment!!)
                        .hide(findFragment!!)
                        .hide(hotFragment!!)
                        .commit()
            }
        }
    }

    /**
     * 清除所有 nav 的状态
     */
    private fun clearState() {
        rg_nav.clearCheck()
        rb_find.setTextColor(resources.getColor(android.R.color.darker_gray))
        rb_hot.setTextColor(resources.getColor(android.R.color.darker_gray))
        rb_mine.setTextColor(resources.getColor(android.R.color.darker_gray))
    }
}
