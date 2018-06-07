package com.seuic.gaopaiyisk;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;

import com.fsa.decoder.SymbologyID;
import com.seuic.bean.CommodityInfo;
import com.seuic.callback.CompleteCallback;
import com.seuic.hsiscanner.HSIScanner;
import com.seuic.utils.SeuicLog;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements CompleteCallback {
    private SurfaceView mSurfaceView;
    private HSIScanner hsiScanner;
    private Socket mClientSocket;
    private PrintWriter mPrintWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        //初始化sdk
        hsiScanner = HSIScanner.getInstance(this, mSurfaceView);
        hsiScanner.setCompleteCallback(this);
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceview);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //打开和设置参数, 这个可以放在 按钮中执行
        hsiScanner.open();
        hsiScanner.setParams(SymbologyID.CODE128, 1); // 1，表示打开该条码，0 表示关闭
    }

    @Override
    protected void onPause() {
        super.onPause();
        //onDestory中关闭释放对象
        hsiScanner.close();
        HSIScanner.destroyInstance();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_about:
                showInfoDialog();
                break;
            case R.id.menu_setting:
                //弹窗设置页面
                //showSettingDialog();
                break;
            default:
                break;
        }
        return true;
    }

    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本信息");
        try {
            builder.setMessage("当前版本为：" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            builder.setMessage("");
        }
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    /**
     * 数据回调
     *
     * @param commodityInfo datas
     */
    @Override
    public void onComplete(CommodityInfo commodityInfo) {

        if (commodityInfo != null) {
            String[] strings = commodityInfo.getbarcodeArr();
            for (int i = 0; i < strings.length; i++) {
                SeuicLog.d("barcode:" + strings[i]);

                if (mClientSocket != null) {
                    if (isServerClose(mClientSocket)) {
                        sendCommodityInfo2Bankend(strings[i]);
                    } else {
                        mPrintWriter.println(strings[i]);
                    }
                } else {
                    sendCommodityInfo2Bankend(strings[i]);
                }
                /*if (mClientSocket == null || (mClientSocket != null && isServerClose(mClientSocket))) {
                    sendCommodityInfo2Bankend(strings[i]);
                }else {
                    mPrintWriter.println(strings[i]);
                }*/
            }
        }
    }

    private void sendCommodityInfo2Bankend(String barcode) {
        Socket socket = null;
        while (socket == null) {
            try {
                //socket = new Socket("192.168.80.64", 7777);
                socket=new Socket("192.168.1.117",8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("连接服务器成功");

                //发送数据给服务器
                mPrintWriter.println(barcode);
            } catch (IOException e) {
                e.printStackTrace();
                releaseSocket();
            }

        }
    }

    /**
     * 判断是否断开连接，断开返回 true, 没有返回 false
     *
     * @param socket
     * @return
     */
    private Boolean isServerClose(Socket socket) {
        try {
            socket.sendUrgentData(0);// 发送 1 个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            return false;
        } catch (Exception se) {
            return true;
        }
    }

    /*释放资源*/
    private void releaseSocket() {
        if (mClientSocket != null) {
            try {
                mClientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mClientSocket = null;
        }

        if (mPrintWriter != null) {
            try {
                mPrintWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mPrintWriter = null;
        }
    }
}
