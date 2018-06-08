package com.seuic.gaopaiyisk;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
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
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompleteCallback, View.OnClickListener {
    private SurfaceView mSurfaceView;
    private HSIScanner hsiScanner;
    private Socket mClientSocket;
    private PrintWriter mPrintWriter;
    private ImageView mIvPlay; //启动开关
    private boolean isOpened; //是否已经开启
    private List<CodeItem> data;
    private RecyclerView codeListRV;
    private CodeListAdapter codeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();

        startScanner();
    }



    private void initData() {
        initRV();
    }

    private void initRV() {
        data = new ArrayList<>();
        codeListAdapter = new CodeListAdapter(data);
        codeListRV.setLayoutManager(new LinearLayoutManager(this));
        codeListRV.setAdapter(codeListAdapter);
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceview);
        codeListRV = findViewById(R.id.rv_list);
        mIvPlay = findViewById(R.id.iv_play);
        mIvPlay.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("MainActivity","onResume");
      /*  //打开和设置参数
        hsiScanner.open();
        //hsiScanner.setParams(SymbologyID.CODE128, 1); // 1，表示打开该条码，0 表示关闭
        hsiScanner.setParams(SymbologyID.QR, 0); // 1，表示打开该条码，0 表示关闭*/

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("MainActivity","onPause");
        //onDestory中关闭释放对象
        closeScanner();
    }

    /**
     * 初始化HSIScanner 对象
     */
    private void startScanner() {
        hsiScanner = HSIScanner.getInstance(this, mSurfaceView);
        hsiScanner.setCompleteCallback(this);
    }

    /**
     * 关闭scanner释放对象
     */
    private void closeScanner() {
        if (hsiScanner != null) {
            hsiScanner.close();
            HSIScanner.destroyInstance();
        }
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

                if (!isOpened) {
                    mIvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_64dp);
                    isOpened = true;
                    //打开和设置参数
                    hsiScanner.open();
                    hsiScanner.setParams(SymbologyID.CODE128, 1); // 1，表示打开该条码，0 表示关闭
                    hsiScanner.setParams(SymbologyID.QR, 1); // 1，表示打开该条码，0 表示关闭
                } else {
                    mIvPlay.setImageResource(R.drawable.ic_play_circle_outline_black_64dp);
                    isOpened = false;
                    closeScanner();
                }
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

                /*if (mClientSocket != null) {
                    if (isServerClose(mClientSocket)) {
                        sendCommodityInfo2Bankend(strings[i]);
                    } else {
                        mPrintWriter.println(strings[i]);
                    }
                } else {
                    sendCommodityInfo2Bankend(strings[i]);
                }*/
                if (mClientSocket == null || (mClientSocket != null && isServerClose(mClientSocket))) {
                    // TODO: 2018/6/8 先注释掉
                    //sendCommodityInfo2Bankend(strings[i]);
                } else {
                    sendPrint2Server(strings[i]);
                }
            }
        }
    }

    private void sendCommodityInfo2Bankend(String barcode) {
        int retryCount = 3; //重连尝试
        Socket socket = null;
        while (socket == null && retryCount > 0) {
            try {
                //socket = new Socket("192.168.80.64", 7777);
                socket = new Socket("192.168.1.117", 8688);
                mClientSocket = socket;
                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                System.out.println("连接服务器成功");
                //发送数据给服务器
                sendPrint2Server(barcode);
            } catch (IOException e) {
                retryCount--;
                e.printStackTrace();
                releaseSocket();
            }

        }
    }

    /**
     * 发送数据给服务器
     *
     * @param barcode barcode
     */
    private void sendPrint2Server(String barcode) {
        //更新code list
        data.add(new CodeItem(barcode));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                codeListAdapter.notifyDataSetChanged();
            }
        });

        //PrintWriter发给服务器
        if (mPrintWriter == null) return;
        mPrintWriter.println(barcode);
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
            se.printStackTrace();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                if (!isOpened) {
                    mIvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_64dp);
                    isOpened = true;
                    //打开和设置参数
                    hsiScanner.open();
                    //可以设置多个，具体看SymbologyID中
                    hsiScanner.setParams(SymbologyID.CODE128, 1); // 1，表示打开该条码，0 表示关闭
                    hsiScanner.setParams(SymbologyID.QR, 1); // 1，表示打开该条码，0 表示关闭
                } else {
                    mIvPlay.setImageResource(R.drawable.ic_play_circle_outline_black_64dp);
                    isOpened = false;
                    closeScanner();
                }
                break;

            default:
                break;
        }
    }

    class CodeListAdapter extends BaseQuickAdapter<CodeItem, BaseViewHolder> {

        public CodeListAdapter(@Nullable List<CodeItem> data) {
            super(data);
            mLayoutResId = android.R.layout.simple_list_item_1;
        }

        @Override
        protected void convert(BaseViewHolder helper, CodeItem item) {
            helper.setText(android.R.id.text1, item.barcode);
        }
    }
}
