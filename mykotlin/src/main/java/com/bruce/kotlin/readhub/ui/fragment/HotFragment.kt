package com.bruce.kotlin.readhub.ui.fragment

import android.util.Log
import com.bruce.kotlin.readhub.R
import com.bruce.kotlin.readhub.api.ApiConstants
import com.bruce.kotlin.readhub.bean.HotTopic
import com.bruce.kotlin.readhub.contract.HotContract
import com.bruce.kotlin.readhub.model.HotModel
import com.bruce.kotlin.readhub.presenter.HotPresenter
import com.scwang.smartrefresh.header.DropBoxHeader
import com.scwang.smartrefresh.header.FunGameHitBlockHeader
import com.scwang.smartrefresh.header.PhoenixHeader
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.BallPulseFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader
import com.scwang.smartrefresh.layout.impl.RefreshHeaderWrapper
import com.yjn.common.base.BaseFragment
import com.yjn.common.util.ToastUtil
import kotlinx.android.synthetic.main.hot_fragment.*
import java.util.*


/**
 * Created by yang.jianan on 2017/09/01 17:07.
 */

class HotFragment : BaseFragment<HotPresenter, HotModel>(), HotContract.View {
    private lateinit var refreshLayout: SmartRefreshLayout
    private var lastCursor: String = ""

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

        data.forEach {
            Log.e("HotFragment", it.summary)
            txt_content.append(it.title + "\n")
        }

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

        getHotTopics(true)
    }

    /**
     * @param refresh 是否是下拉刷新
     */
    private fun getHotTopics(refresh: Boolean) {
        if (refresh) {
            lastCursor = ""
        }
        var qryMaps = HashMap<String, String>()
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
            refreshLayout.layout.postDelayed({
                val refreshHeader = refreshLayout.refreshHeader
                if (refreshHeader is RefreshHeaderWrapper) {
                    refreshLayout.setRefreshHeader(PhoenixHeader(context!!))
                } else if (refreshHeader is PhoenixHeader) {
                    refreshLayout.setRefreshHeader(DropBoxHeader(context!!))
                } else if (refreshHeader is DropBoxHeader) {
                    refreshLayout.setRefreshHeader(FunGameHitBlockHeader(context!!))
                } else if (refreshHeader is FunGameHitBlockHeader) {
                    refreshLayout.setRefreshHeader(ClassicsHeader(context!!))
                } else {
                    refreshLayout.setRefreshHeader(RefreshHeaderWrapper(BallPulseFooter(context!!)))
                }
                refreshLayout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white)
            }, 4000)
        }

        refreshLayout.setOnLoadMoreListener { refreshlayout ->
            //refreshlayout.finishLoadMore(2000/*,false*/)//传入false表示加载失败
            getHotTopics(false)
        }
    }

}
