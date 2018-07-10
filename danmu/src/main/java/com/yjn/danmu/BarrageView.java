package com.yjn.danmu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by yangjianan on 2018/7/10.
 * Desc: 自定义弹幕View，继承自TextView
 * https://github.com/Xieyupeng520/AZBarrage
 */
@SuppressLint("AppCompatCustomView")
public class BarrageView extends TextView {
    private static final String TAG = BarrageView.class.getSimpleName();
    //画布
    private Paint mPaint = new Paint();
    //滚动线程
    private RollThread rollThread;
    //字体大小
    private int textSize = 30;
    public static final int TEXT_MIN = 10;
    public static final int TEXT_MAX = 60;
    //x坐标
    private int posX;
    //y坐标
    private int posY = textSize;
    //屏幕宽
    private int windowWidth;
    //屏幕高
    private int windowHeight;
    //字体颜色
    private int color = 0xffffffff;

    //滚动结束监听器
    private OnRollEndListener mOnRollEndListener;

    public BarrageView(Context context) {
        super(context);
        init();
    }

    public BarrageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    /**
     * 初始化
     */
    private void init() {
        Random random = new Random();
        //1.设置文字大小
        textSize = TEXT_MIN + random.nextInt(TEXT_MAX - TEXT_MIN);
        mPaint.setTextSize(textSize);
        //2.设置字体颜色
        color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        mPaint.setColor(color);
        //3.得到屏幕宽高
        Rect rect = new Rect();
        getWindowVisibleDisplayFrame(rect);
        windowWidth = rect.width();
        windowHeight = rect.height();
        //4.设置x为屏幕宽
        posX = windowWidth;
        //5.设置y为屏幕高度内内随机，需要注意的是，文字是以左下角为起始点计算坐标的，所以要加上TextSize的大小
        posY = textSize + random.nextInt(windowHeight - textSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //super.onDraw(canvas);
        //白色
        //mPaint.setColor(getResources().getColor(color));
        //字体
        //mPaint.setTextSize(textSize);
        canvas.drawText(getShowText(), posX, posY, mPaint);

        if (rollThread == null) {
            rollThread = new RollThread();
            rollThread.start();
        }
    }

    /**
     * @return 显示的文字
     */
    private String getShowText() {
        if (getText() != null && !getText().toString().isEmpty()) {
            return getText().toString();
        } else {
            return getResources().getString(R.string.default_text);
        }
    }

    /**
     * 动画逻辑处理
     */
    private void animLogic() {
        //每次减少8像素
        posX -= 8;
    }

    /**
     * 是否需要关闭滚动线程
     *
     * @return boolean
     */
    private boolean needStopRollThread() {
        return posX <= -mPaint.measureText(getShowText());
    }

    /**
     * 线程重绘
     * 每30毫秒 posX 左边往左移动 8 像素，然后重绘ondraw
     */
    class RollThread extends Thread {
        //线程锁
        private Object mPauseLock;
        //标签：是否暂停
        private boolean mPauseFlag;

        RollThread() {
            mPauseLock = new Object();
            mPauseFlag = false;
        }

        @Override
        public void run() {
            while (true) {
                //首先检查是否挂起, 解决按 Home 后一分钟以上回到程序会发生满屏线程阻塞
                checkPause();

                //1.动画逻辑
                animLogic();
                //2.绘制图像,该方法会自动调用onDraw()方法
                postInvalidate();
                //3.延迟，不然会造成执行太快动画一闪而过
                try {
                    //睡30毫秒
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //关闭线程逻辑判断
                if (needStopRollThread()) {

                    if (mOnRollEndListener != null) {
                        mOnRollEndListener.onRollEnd();
                    }
                    //从父类中移除本view
                    post(new Runnable() {
                        @Override
                        public void run() {
                            ((ViewGroup) BarrageView.this.getParent()).removeView(BarrageView.this);
                        }
                    });
                    break;
                }
            }
        }

        public void onPause() {
            synchronized (mPauseLock) {
                mPauseFlag = true;
            }
        }
        public void onResume() {
            synchronized (mPauseLock) {
                mPauseFlag = false;
                Log.i(TAG, "线程恢复-" + getShowText());
                mPauseLock.notify();
            }
        }
        private void checkPause() {
            synchronized (mPauseLock) {
                if (mPauseFlag) {
                    try {
                        Log.e(TAG, "线程挂起-" + getShowText());
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * @param onRollEndListener 设置滚动结束监听器
     */
    public void setOnRollEndListener(OnRollEndListener onRollEndListener) {
        this.mOnRollEndListener = onRollEndListener;
    }

    /**
     * 滚动结束接听器
     */
    interface OnRollEndListener {
        void onRollEnd();
    }

    /**
     * 15/11/01 测试按 Home 后一分钟以上回到程序会发生满屏线程阻塞
     */
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (rollThread == null) {
            return;
        }
        if (View.GONE == visibility) {
            rollThread.onPause();
        } else {
            rollThread.onResume();
        }

    }
}
