package com.yjn.common.base;


import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.yjn.common.baserx.RxManager;
import com.yjn.common.util.TUtil;
import com.yjn.common.util.ToastUtil;
import com.zhy.autolayout.AutoLayoutActivity;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;


/**
 * 基类
 */

/***************使用例子*********************/
//1.mvp模式
//public class SampleActivity extends BaseActivity<NewsChanelPresenter, NewsChannelModel>implements NewsChannelContract.View {
//    @Override
//    public int getLayoutId() {
//        return R.layout.activity_news_channel;
//    }
//
//    @Override
//    public void initPresenter() {
//        mPresenter.setVM(this, mModel);
//    }
//
//    @Override
//    public void initView() {
//    }
//}
//2.普通模式
//public class SampleActivity extends BaseActivity {
//    @Override
//    public int getLayoutId() {
//        return R.layout.activity_news_channel;
//    }
//
//    @Override
//    public void initPresenter() {
//    }
//
//    @Override
//    public void initView() {
//    }
//}
public abstract class BaseActivity<T extends BasePresenter, E extends BaseModel> extends AutoLayoutActivity {
    public T mPresenter;
    public E mModel;
    public Context mContext;
    public RxManager mRxManager;
    private boolean isConfigChange = false;
    protected final String TAG = getClass().getSimpleName();
    protected boolean isFinished = false;
    //    private Unbinder unbinder;
    private ImmersionBar mImmersionBar;
    public KProgressHUD dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // 无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        isFinished = false;
        isConfigChange = false;
        mRxManager = new RxManager();
        doBeforeSetcontentView();
        setContentView(getLayoutId());
        mContext = this;
        mPresenter = TUtil.getT(this, 0);

        mModel = TUtil.getT(this, 1);
        if (mPresenter != null) {
            mPresenter.mContext = this;
        }

        ButterKnife.bind(this);
        this.initPresenter();
        this.initView();

        initLoading();
    }

    /**
     * 设置layout前配置
     */
    protected void doBeforeSetcontentView() {
        // 把actvity放到application栈中管理
        AppManager.getAppManager().addActivity(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        // 无状态栏
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 初始化 Loading
     */
    private void initLoading() {
        dialog = KProgressHUD.create(mContext)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                //.setLabel("Please wait")
                .setCancellable(false);
    }

    /**
     * 隐藏 Loading
     */
    public void dialogDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }, 2000);
    }

    /*********************子类实现*****************************/
    //获取布局文件
    public abstract int getLayoutId();

    //简单页面无需mvp就不用管此方法即可,完美兼容各种实际场景的变通
    public abstract void initPresenter();

    //初始化view
    public abstract void initView();


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isConfigChange = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbinder.unbind();//解除绑定，官方文档只对fragment做了解绑
        if (mPresenter != null) mPresenter.onDestroy();
        if (mRxManager != null) {
            mRxManager.clear();
        }
        if (!isConfigChange) {
            AppManager.getAppManager().finishActivity(this);
        }

        if (mImmersionBar != null)
            mImmersionBar.destroy();  //必须调用该方法，防止内存泄漏，不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
    }

    /*--------------------------------Handler---------------------------------*/
    public Handler mHandler = new MyHandler(this);

    /**
     * 避免Handler引起的内存泄露
     * 使用显性的引用，1.静态内部类。 2. 外部类
     * 使用弱引用 2. WeakReference
     */
    public static class MyHandler extends Handler {
        private final WeakReference<BaseActivity> mActivity;

        public MyHandler(BaseActivity activity) {

            mActivity = new WeakReference<BaseActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity baseActy = mActivity.get();
            if (baseActy == null) {
                return;
            }
//			if (!BaseActy.this.isFinishing()){
//			}
            if (!baseActy.isFinished()) {
                baseActy.handleMessaged(msg);
            } else {
                removeMessages(msg.what);
            }
        }
    }

    public void handleMessaged(Message msg) {
    }


    protected void closeBoard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        View focus = getCurrentFocus();
        if (null != focus) {
            imm.hideSoftInputFromWindow(focus.getApplicationWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    @Override
    public void finish() {
        super.finish();
    }

    private long preTime = 0;

    protected void closeApp() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - preTime > 2000) {
            ToastUtil.showShort(mContext, "双击退出程序");
            preTime = currentTime;
        } else {
            System.exit(0);
        }
    }

    protected void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
