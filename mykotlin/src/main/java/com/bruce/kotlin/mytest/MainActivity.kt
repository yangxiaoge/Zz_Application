package com.bruce.kotlin.mytest

import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bruce.kotlin.mytest.R.layout.activity_main
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        // 隐藏 虚拟导航栏
        /*val window = window;
        val param = window.attributes
        param.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        window.attributes = param*/

        initNavBtn()
        initToolBar()
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
        val data: Date = Date()
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
        rb_home.isChecked = true //默认选中首页
        rb_home.setTextColor(resources.getColor(android.R.color.black))

        rb_home.setOnClickListener(this)
        rb_find.setOnClickListener(this)
        rb_hot.setOnClickListener(this)
        rb_mine.setOnClickListener(this)
    }

    /**
     * 点击事件
     */
    override fun onClick(v: View?) {
    }
}
