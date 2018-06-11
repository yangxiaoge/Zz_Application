package com.seuic.gaopaiyisk;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.fsa.decoder.SymbologyID;
import com.seuic.bean.CommodityInfo;
import com.seuic.callback.CompleteCallback;
import com.seuic.gaopaiyisk.server.GaopaiyiServer;
import com.seuic.hsiscanner.HSIScanner;
import com.seuic.utils.SeuicLog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CompleteCallback, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private HSIScanner hsiScanner;
    private ImageView mIvPlay; //启动开关
    private TextView tvCount;
    private TextView tvWeight;
    private boolean isOpened; //是否已经开启
    private List<CodeItem> data;
    private RecyclerView codeListRV;
    private CodeListAdapter codeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initServer();
        initView();
        initData();
        initScanner();
    }

    GaopaiyiServer server;

    /**
     * 初始化server服务器
     */
    private void initServer() {
        server = new GaopaiyiServer(this);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        initRV();
    }

    private void initRV() {
        data = new ArrayList<>();
        codeListAdapter = new CodeListAdapter(data);
        codeListRV.setLayoutManager(new LinearLayoutManager(this));
        codeListRV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        codeListRV.setAdapter(codeListAdapter);
    }

    private void initView() {
        mSurfaceView = findViewById(R.id.surfaceview);
        codeListRV = findViewById(R.id.rv_list);
        tvCount = findViewById(R.id.tv_count);
        tvWeight = findViewById(R.id.tv_weight);
        mIvPlay = findViewById(R.id.iv_play);
        mIvPlay.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        hsiScanner.open();
        hsiScanner.setParams(SymbologyID.QR, 0); // 1，表示打开该条码，0 表示关闭

        isOpened = true;
        mIvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_64dp);

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        //onPause中关闭
        closeScanner();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        //onDestory中释放对象
        HSIScanner.destroyInstance();

        //server服务关闭
        server.onDestroy();
    }

    /**
     * 初始化HSIScanner 对象
     */
    private void initScanner() {
        hsiScanner = HSIScanner.getInstance(this, mSurfaceView);
        hsiScanner.setCompleteCallback(this);
    }

    /**
     * 关闭scanner释放对象
     */
    private void closeScanner() {
        if (hsiScanner != null) {
            hsiScanner.close();
            isOpened = false;
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
                //版本信息
                showInfoDialog();
                break;
            case R.id.menu_setting:
                //弹窗设置页面
                showSettingDialog();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 版本信息
     */
    private void showInfoDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("版本信息");
        try {
            builder.setMessage("当前版本为：" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName
                    + "\n\n" + "本机ip为：" + server.getIpAddress() + "\n" + "本机port为：" + server.getPort());
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
     * 设置界面
     */
    private void showSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置");

        final View dialogView = getLayoutInflater().inflate(R.layout.setting_layout, null);
        builder.setView(dialogView);

        final CheckBox mCbCodabar = dialogView.findViewById(R.id.cb_codabar);
        final CheckBox mCbCode11 = dialogView.findViewById(R.id.cb_code11);
        final CheckBox mCbCode128 = dialogView.findViewById(R.id.cb_code128);
        final CheckBox mCbCode39 = dialogView.findViewById(R.id.cb_code39);
        mCbCodabar.setChecked(hsiScanner.getParams(SymbologyID.CODABAR) == 1);
        mCbCode11.setChecked(hsiScanner.getParams(SymbologyID.CODE11) == 1);
        mCbCode128.setChecked(hsiScanner.getParams(SymbologyID.CODE128) == 1);
        mCbCode39.setChecked(hsiScanner.getParams(SymbologyID.CODE39) == 1);

        final CheckBox mCbCode93 = dialogView.findViewById(R.id.cb_code93);
        final CheckBox mCbDatamatrix = dialogView.findViewById(R.id.cb_datamatrix);
        final CheckBox mCbEan8 = dialogView.findViewById(R.id.cb_ean8);
        final CheckBox mCbEan13 = dialogView.findViewById(R.id.cb_ean13);
        mCbCode93.setChecked(hsiScanner.getParams(SymbologyID.CODE93) == 1);
        mCbDatamatrix.setChecked(hsiScanner.getParams(SymbologyID.DATAMATRIX) == 1);
        mCbEan8.setChecked(hsiScanner.getParams(SymbologyID.EAN8) == 1);
        mCbEan13.setChecked(hsiScanner.getParams(SymbologyID.EAN13) == 1);

        final CheckBox mCbInt125 = dialogView.findViewById(R.id.cb_int125);
        final CheckBox mCbMaxicode = dialogView.findViewById(R.id.cb_maxicode);
        final CheckBox mCbMicropdf = dialogView.findViewById(R.id.cb_micropdf);
        final CheckBox mCbPdf417 = dialogView.findViewById(R.id.cb_pdf417);
        mCbInt125.setChecked(hsiScanner.getParams(SymbologyID.INT25) == 1);
        mCbMaxicode.setChecked(hsiScanner.getParams(SymbologyID.MAXICODE) == 1);
        mCbMicropdf.setChecked(hsiScanner.getParams(SymbologyID.MICROPDF) == 1);
        mCbPdf417.setChecked(hsiScanner.getParams(SymbologyID.PDF417) == 1);

        final CheckBox mCbQr = dialogView.findViewById(R.id.cb_qr);
        final CheckBox mCbUpca = dialogView.findViewById(R.id.cb_upca);
        final CheckBox mCbUpce0 = dialogView.findViewById(R.id.cb_upce0);
        final CheckBox mCbGsi128 = dialogView.findViewById(R.id.cb_gsi128);
        mCbQr.setChecked(hsiScanner.getParams(SymbologyID.QR) == 1);
        mCbUpca.setChecked(hsiScanner.getParams(SymbologyID.UPCA) == 1);
        mCbUpce0.setChecked(hsiScanner.getParams(SymbologyID.UPCE0) == 1);
        mCbGsi128.setChecked(hsiScanner.getParams(SymbologyID.GS1_128) == 1);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                hsiScanner.setParams(SymbologyID.CODABAR, mCbCodabar.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.CODE11, mCbCode11.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.CODE128, mCbCode128.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.CODE39, mCbCode39.isChecked() ? 1 : 0);

                hsiScanner.setParams(SymbologyID.CODE93, mCbCode93.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.DATAMATRIX, mCbDatamatrix.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.EAN8, mCbEan8.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.EAN13, mCbEan13.isChecked() ? 1 : 0);

                hsiScanner.setParams(SymbologyID.INT25, mCbInt125.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.MAXICODE, mCbMaxicode.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.MICROPDF, mCbMicropdf.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.PDF417, mCbPdf417.isChecked() ? 1 : 0);

                hsiScanner.setParams(SymbologyID.QR, mCbQr.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.UPCA, mCbUpca.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.UPCE0, mCbUpce0.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.GS1_128, mCbGsi128.isChecked() ? 1 : 0);
            }
        });
        builder.setPositiveButtonIcon(getResources().getDrawable(R.drawable.ic_ok));
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
            //条码集合
            String[] codeList = commodityInfo.getbarcodeArr();
            SeuicLog.d("codeList长度:" + codeList.length);
            StringBuilder sb = new StringBuilder();
            for (String code : codeList) {

                sb.append(code);
                sb.append("\n");
            }
            String barcode = sb.toString().trim();
            //重量
            String weight = TextUtils.isEmpty(commodityInfo.getWeight()) ? "" : commodityInfo.getWeight();
            SeuicLog.d("barcode:" + barcode + " weight:" + weight);

            //更新UI数据
            refreshData(barcode, weight);
            //发给条码给客户端
            if (server.printStream != null) {
                try {
                    server.printStream.println(sb.toString().trim());
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "发送数据给客户端: " + sb.toString().trim());
                }
            }

        }
    }

    /**
     * 更新UI数据
     *
     * @param barcode barcode
     * @param weight  weight
     */
    private void refreshData(String barcode, final String weight) {
        data.add(new CodeItem(barcode, weight));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvCount.setText(String.valueOf(data.size())); //数量
                tvWeight.setText(weight); //数量
                codeListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play:
                if (!isOpened) {
                    //打开和设置参数
                    boolean startSuccess = hsiScanner.open();
                    if (startSuccess){
                        Toast.makeText(this, "开始扫描", Toast.LENGTH_SHORT).show();
                        mIvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_64dp);
                        isOpened = true;
                    }else {
                        isOpened = false;
                        closeScanner();
                        Toast.makeText(this, "开启失败", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mIvPlay.setImageResource(R.drawable.ic_play_circle_outline_black_64dp);
                    isOpened = false;
                    closeScanner();
                    Toast.makeText(this, "暂停扫描", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    class CodeListAdapter extends BaseQuickAdapter<CodeItem, BaseViewHolder> {

        public CodeListAdapter(@Nullable List<CodeItem> data) {
            super(data);
            mLayoutResId = R.layout.codelist_item;
        }

        @Override
        protected void convert(BaseViewHolder helper, CodeItem item) {
            helper.setText(R.id.barcode_text, item.barcode.trim());
            helper.setText(R.id.barcode_weight, item.weight);
        }
    }
}
