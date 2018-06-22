package com.seuic.gaopaiyisk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.fsa.decoder.SymbologyID;
import com.jakewharton.rxbinding2.view.RxView;
import com.seuic.bean.CommodityInfo;
import com.seuic.callback.CompleteCallback;
import com.seuic.gaopaiyisk.server.GaopaiyiServer;
import com.seuic.gaopaiyisk.util.SoundUtils;
import com.seuic.gaopaiyisk.util.WakeLockCtrl;
import com.seuic.hsiscanner.HSIScanner;
import com.seuic.utils.SeuicLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity implements CompleteCallback {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private HSIScanner hsiScanner;
    private ImageView mIvPlay; //启动开关
    private TextView tvCount;
    private TextView tvWeight;
    private boolean isOpened; //是否已经开启
    private List<CodeItem> data;
    private List<String> barcodeList;
    private RecyclerView codeListRV;
    private CodeListAdapter codeListAdapter;
    private SharedPreferences sharedPreferences;
    private String SPNAME = "gaopaiyisp"; //SP名称
    private static int EXPOSURE = 1500; //默认的曝光时间
    //相机分辨率
    private static int WIDTH = 2112;
    private static int HEIGHT = 1568;
//    private static int WIDTH = 1280;
//    private static int HEIGHT = 720;
    private SoundUtils soundUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(SPNAME, Context.MODE_PRIVATE);
        initServer();
        initView();
        initData();
        initScanner();

        soundUtils = new SoundUtils(this);
        soundUtils.init();
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
        barcodeList = new ArrayList<>();
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

        //防止多次点击
        RxView.clicks(mIvPlay)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        handlePlayClick();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");

        //不默认打开扫描解码
        /*if (hsiScanner != null) {
            isOpened = hsiScanner.open(WIDTH, HEIGHT);
            if (isOpened) {
                hsiScanner.setCompleteCallback(this);

                //设置参数
                setScanParams();
                mIvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_64dp);
            }
        }*/

        //禁止自动休眠
        WakeLockCtrl.lock(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        //onPause中关闭
        closeScanner();
        //释放自动休眠控制
        WakeLockCtrl.release();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");
        //onDestory中释放对象
        HSIScanner.destroyInstance();
        //释放声音播放
        soundUtils.release();
        server.onDestroy();
    }

    /**
     * 初始化HSIScanner 对象
     */
    private void initScanner() {
        hsiScanner = HSIScanner.getInstance(this, mSurfaceView);
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
                //弹窗设置页面,相机不开启不允许设置
                if (isOpened) {
                    showSettingDialog();
                } else {
                    showToast("请先开启扫描");
                }
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置");

        final View dialogView = getLayoutInflater().inflate(R.layout.setting_layout, null);
        builder.setView(dialogView);
        final EditText etValue = dialogView.findViewById(R.id.et_value1);
        etValue.setText(String.valueOf(hsiScanner.getParams(SymbologyID.EXPOSURE))); //曝光时间
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
                SharedPreferences.Editor editor = sharedPreferences.edit();
                //曝光时间
                hsiScanner.setParams(SymbologyID.EXPOSURE, TextUtils.isEmpty(etValue.getText().toString().trim()) ? EXPOSURE : Integer.parseInt(etValue.getText().toString().trim()));
                editor.putInt(SymbologyID.EXPOSURE + "", TextUtils.isEmpty(etValue.getText().toString().trim()) ? EXPOSURE : Integer.parseInt(etValue.getText().toString().trim())).apply();

                //参数设置
                hsiScanner.setParams(SymbologyID.CODABAR, mCbCodabar.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.CODE11, mCbCode11.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.CODE128, mCbCode128.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.CODE39, mCbCode39.isChecked() ? 1 : 0);
                editor.putInt(SymbologyID.CODABAR + "", mCbCodabar.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.CODE11 + "", mCbCode11.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.CODE128 + "", mCbCode128.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.CODE39 + "", mCbCode39.isChecked() ? 1 : 0).apply();

                hsiScanner.setParams(SymbologyID.CODE93, mCbCode93.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.DATAMATRIX, mCbDatamatrix.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.EAN8, mCbEan8.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.EAN13, mCbEan13.isChecked() ? 1 : 0);
                editor.putInt(SymbologyID.CODE93 + "", mCbCode93.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.DATAMATRIX + "", mCbDatamatrix.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.EAN8 + "", mCbEan8.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.EAN13 + "", mCbEan13.isChecked() ? 1 : 0).apply();

                hsiScanner.setParams(SymbologyID.INT25, mCbInt125.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.MAXICODE, mCbMaxicode.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.MICROPDF, mCbMicropdf.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.PDF417, mCbPdf417.isChecked() ? 1 : 0);
                editor.putInt(SymbologyID.INT25 + "", mCbInt125.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.MAXICODE + "", mCbMaxicode.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.MICROPDF + "", mCbMicropdf.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.PDF417 + "", mCbPdf417.isChecked() ? 1 : 0).apply();

                hsiScanner.setParams(SymbologyID.QR, mCbQr.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.UPCA, mCbUpca.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.UPCE0, mCbUpce0.isChecked() ? 1 : 0);
                hsiScanner.setParams(SymbologyID.GS1_128, mCbGsi128.isChecked() ? 1 : 0);
                editor.putInt(SymbologyID.QR + "", mCbQr.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.UPCA + "", mCbUpca.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.UPCE0 + "", mCbUpce0.isChecked() ? 1 : 0).apply();
                editor.putInt(SymbologyID.GS1_128 + "", mCbGsi128.isChecked() ? 1 : 0).apply();
            }
        });
        builder.setPositiveButtonIcon(getResources().getDrawable(R.drawable.ic_ok));
        builder.show();
    }

    /**
     * 处理开关逻辑
     */
    private void handlePlayClick() {
        if (!isOpened) {
            //打开和设置参数
            isOpened = hsiScanner.open(WIDTH, HEIGHT);
            if (isOpened) {
                Toast.makeText(this, "开始扫描", Toast.LENGTH_SHORT).show();
                //解码回调
                hsiScanner.setCompleteCallback(this);
                //设置参数
                setScanParams();
                mIvPlay.setImageResource(R.drawable.ic_pause_circle_outline_black_64dp);
            } else {
                closeScanner();
                Toast.makeText(this, "开启失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            mIvPlay.setImageResource(R.drawable.ic_play_circle_outline_black_64dp);
            isOpened = false;
            closeScanner();
            Toast.makeText(this, "暂停扫描", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 读取sp，设置参数
     */
    private void setScanParams() {
        hsiScanner.setParams(SymbologyID.CODABAR, sharedPreferences.getInt(SymbologyID.CODABAR + "", 0));
        hsiScanner.setParams(SymbologyID.CODE11, sharedPreferences.getInt(SymbologyID.CODE11 + "", 0));
        hsiScanner.setParams(SymbologyID.CODE128, sharedPreferences.getInt(SymbologyID.CODE128 + "", 0));
        hsiScanner.setParams(SymbologyID.CODE39, sharedPreferences.getInt(SymbologyID.CODE39 + "", 0));

        hsiScanner.setParams(SymbologyID.CODE93, sharedPreferences.getInt(SymbologyID.CODE93 + "", 0));
        hsiScanner.setParams(SymbologyID.DATAMATRIX, sharedPreferences.getInt(SymbologyID.DATAMATRIX + "", 0));
        hsiScanner.setParams(SymbologyID.EAN8, sharedPreferences.getInt(SymbologyID.EAN8 + "", 0));
        hsiScanner.setParams(SymbologyID.EAN13, sharedPreferences.getInt(SymbologyID.EAN13 + "", 0));

        hsiScanner.setParams(SymbologyID.INT25, sharedPreferences.getInt(SymbologyID.INT25 + "", 0));
        hsiScanner.setParams(SymbologyID.MAXICODE, sharedPreferences.getInt(SymbologyID.MAXICODE + "", 0));
        hsiScanner.setParams(SymbologyID.MICROPDF, sharedPreferences.getInt(SymbologyID.MICROPDF + "", 0));
        hsiScanner.setParams(SymbologyID.PDF417, sharedPreferences.getInt(SymbologyID.PDF417 + "", 0));

        hsiScanner.setParams(SymbologyID.QR, sharedPreferences.getInt(SymbologyID.QR + "", 0));
        hsiScanner.setParams(SymbologyID.UPCA, sharedPreferences.getInt(SymbologyID.UPCA + "", 0));
        hsiScanner.setParams(SymbologyID.UPCE0, sharedPreferences.getInt(SymbologyID.UPCE0 + "", 0));
        hsiScanner.setParams(SymbologyID.GS1_128, sharedPreferences.getInt(SymbologyID.GS1_128 + "", 0));

        hsiScanner.setParams(SymbologyID.SAVE_IMAGE, 1);//设置保存图像,设置成0，如果是1没次扫码都会保存图片，会很慢
        hsiScanner.setParams(SymbologyID.EXPOSURE, sharedPreferences.getInt(SymbologyID.EXPOSURE + "", EXPOSURE));//设置曝光值
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
            String[] codeList = commodityInfo.getBarcodeArr();
            SeuicLog.d("codeList长度:" + codeList.length);
            StringBuilder sb = new StringBuilder();
            for (String code : codeList) {
                sb.append(code);
                sb.append("\n");
            }
            String barcode = sb.toString().trim();
            //重量
            String weight = TextUtils.isEmpty(commodityInfo.getWeight()) ? "无重量" : commodityInfo.getWeight();

            //条码重复存在,重复条码不处理(release版本开启)
            if (!BuildConfig.DEBUG) {
                if (barcodeList.contains(barcode)) return;
                barcodeList.add(barcode);
            }
            SeuicLog.d("barcode:" + barcode + " weight:" + weight);

            //解码成功后，播放雨滴声音
            soundUtils.playSound(SoundUtils.START_SOUND_ID_RAINBOW,1);
            //更新UI数据
            refreshData(barcode, weight);

            //发给条码给客户端w
            String s = sb.toString().trim() + "$" + weight;
            SeuicLog.d("barcode & weight = " + s);
            if (server.printStream != null) {
                try {
                    server.printStream.print(s); //末尾传“$”给客户端识别
                } catch (Exception e) {
                    Log.e(TAG, "发送数据给客户端异常: " + e.toString());
                    e.printStackTrace();
                }
            }

        }
    }

    private void showToast(final String msg) {
        try {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
                tvCount.setText(String.format("数量:%s", data.size())); //数量
                tvWeight.setText(weight); //重量
                codeListAdapter.notifyDataSetChanged();
                codeListRV.smoothScrollToPosition(data.size() - 1);
            }
        });
    }

    class CodeListAdapter extends BaseQuickAdapter<CodeItem, BaseViewHolder> {

        CodeListAdapter(@Nullable List<CodeItem> data) {
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
