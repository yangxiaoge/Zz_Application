package com.example.hencoder1_1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yang.jianan on 2017/09/07 14:01.
 */

public class First_Ondraw extends View {
    Paint paint = new Paint();

    public First_Ondraw(Context context) {
        super(context);
    }

    public First_Ondraw(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public First_Ondraw(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        canvas.drawCircle(300,300,200,paint); //画圆
//        canvas.drawColor(Color.BLACK); //纯黑色
//        canvas.drawColor(Color.parseColor("#88880000")); //半透明红色

        /**圆心坐标和半径，这些都是圆的基本信息，也是它的独有信息。什么叫独有信息？就是只有它有，别人没有的信息。你画圆有圆心坐标和半径，
         * 画方有吗？画椭圆有吗？这就叫独有信息。独有信息都是直接作为参数写进 drawXXX() 方法里的（比如 drawCircle(centerX, centerY, radius, paint) 的前三个参数）。
         *而除此之外，其他的都是公有信息。比如图形的颜色、空心实心这些，你不管是画圆还是画方都有可能用到的，这些信息则是统一放在 paint 参数里的。**/

        //插播一： Paint.setColor(int color)
        //例如，你要画一个红色的圆，并不是写成 canvas.drawCircle(300, 300, 200, RED, paint) 这样，而是像下面这样：
        /*paint.setColor(Color.RED);
        canvas.drawCircle(300, 300, 200, paint);*/

        //插播二： Paint.setStyle(Paint.Style style)
        /*paint.setStyle(Paint.Style.STROKE); //Style 修改为画线模式
        canvas.drawCircle(300, 300, 200, paint);*/

        //插播三： Paint.setStrokeWidth(float width)
        paint.setStyle(Paint.Style.STROKE); //Style 修改为画线模式
        paint.setStrokeWidth(20); //线条宽度为 20 像素
        canvas.drawCircle(300, 300, 200, paint);

        //插播四： 抗锯齿
    }
}