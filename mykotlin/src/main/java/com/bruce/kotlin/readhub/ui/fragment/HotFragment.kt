package com.bruce.kotlin.readhub.ui.fragment

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.bruce.kotlin.readhub.R
import com.bruce.kotlin.readhub.adapter.ItemNewsAdapter
import com.bruce.kotlin.readhub.adapter.SpaceItemDecoration
import com.bruce.kotlin.readhub.api.ApiConstants
import com.bruce.kotlin.readhub.bean.HotTopic
import com.bruce.kotlin.readhub.contract.HotContract
import com.bruce.kotlin.readhub.model.HotModel
import com.bruce.kotlin.readhub.presenter.HotPresenter
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yjn.common.base.BaseFragment
import com.yjn.common.util.ToastUtil
import java.util.*
import kotlin.collections.ArrayList


/**
 * Created by yang.jianan on 2017/09/01 17:07.
 */

class HotFragment : BaseFragment<HotPresenter, HotModel>(), HotContract.View {
    private lateinit var refreshLayout: SmartRefreshLayout
    private var lastCursor: String = "" //上次查询的游标

    private lateinit var itemNewsAdapter: ItemNewsAdapter
    private lateinit var newsDatas: MutableList<HotTopic>
    private lateinit var recyclerView: RecyclerView

    override fun stopLoading() {

    }

    override fun showErrorTip(msg: String?) {
        ToastUtil.showShort(mContext, msg)

        dialogDismiss()
        finishLoading()
    }

    override fun getDataSuccess(data: List<HotTopic>) {
        dialogDismiss()
        finishLoading()

        newsDatas.addAll(data)
        // 延时刷新数据
        recyclerView.postDelayed({ itemNewsAdapter.notifyDataSetChanged() }, 1000)

        lastCursor = data[data.size - 1].order.toString()
    }

    private fun finishLoading() {
        // 延时2秒关闭
        refreshLayout.finishRefresh(1500)
        refreshLayout.finishLoadMore(1500)
    }

    override fun showLoading(title: String?) {
        // 已经有上拉下拉刷新， 所以这个dialog也可以不用
        //dialog.show()
    }

    override fun getLayoutResource(): Int {
        return R.layout.hot_fragment
    }

    override fun initPresenter() {
        mPresenter.setVM(this, mModel)
    }

    override fun initView() {
        initRefreshLayout()
        initRV()

        getHotTopics(true)
    }

    private fun initRV() {
        newsDatas = ArrayList()
        recyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        recyclerView.addItemDecoration(SpaceItemDecoration(30))
        itemNewsAdapter = ItemNewsAdapter(newsDatas)
        recyclerView.adapter = itemNewsAdapter
    }

    /**
     * @param refresh 是否是下拉刷新
     */
    private fun getHotTopics(refresh: Boolean) {
        if (refresh) {
            lastCursor = ""
            newsDatas.clear()
            itemNewsAdapter.notifyDataSetChanged()
        }
        val qryMaps = HashMap<String, String>()
        qryMaps["lastCursor"] = lastCursor
        qryMaps["pageSize"] = ApiConstants.TOPIC_PAGE_SIZE.toString()

        mPresenter.getTopics(qryMaps)
    }

    /**
     * 初始化刷新控件
     */
    private fun initRefreshLayout() {
        refreshLayout = rootView.findViewById(R.id.refreshLayout)

        refreshLayout.setOnRefreshListener { refreshLayout ->
            getHotTopics(true)

            //refreshLayout.finishRefresh(3000)
 /*           refreshLayout.layout.postDelayed({
                val refreshHeader = refreshLayout.refreshHeader
                *//*if (refreshHeader is RefreshHeaderWrapper) {
                    refreshLayout.setRefreshHeader(PhoenixHeader(context!!))
                } else if (refreshHeader is PhoenixHeader) {
                    refreshLayout.setRefreshHeader(DropBoxHeader(context!!))
                } else if (refreshHeader is DropBoxHeader) {
                    refreshLayout.setRefreshHeader(FunGameHitBlockHeader(context!!))
                } else if (refreshHeader is FunGameHitBlockHeader) {
                    refreshLayout.setRefreshHeader(ClassicsHeader(context!!))
                } else {
                    refreshLayout.setRefreshHeader(RefreshHeaderWrapper(BallPulseFooter(context!!)))
                }*//*
//                refreshLayout.setRefreshHeader(ClassicsHeader(context!!))
                //refreshLayout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white)
                refreshLayout.setRefreshHeader(FunGameHitBlockHeader(context!!))
            }, 3000)*/
        }

        refreshLayout.setOnLoadMoreListener { refreshlayout ->
            //refreshlayout.finishLoadMore(2000/*,false*/)//传入false表示加载失败
            getHotTopics(false)
        }
    }

}
