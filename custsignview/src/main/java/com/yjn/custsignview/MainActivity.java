package com.yjn.custsignview;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {
    private SignView singView;
    private ImageView img_from_generated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        singView = findViewById(R.id.signview);
        img_from_generated = findViewById(R.id.img_from_generated);
        findViewById(R.id.generate_signview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap signBp = loadBitmapFromView(singView);
                saveImage(signBp);

                img_from_generated.setImageBitmap(signBp);
            }
        });

        findViewById(R.id.reset_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                singView.reset();
            }
        });
    }

    /**
     * 把一个view转化成bitmap对象
     */
    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        /** 如果不设置canvas画布为白色，则生成透明 */
        v.layout(0, 0, w, h);
        v.draw(c);
        return bmp;
    }

    private void saveImage(Bitmap bmp) {
        String fileName = "0001.jpg";
        File file = new File(Environment.getExternalStorageDirectory(), fileName);
//        File path = Environment.getExternalStorageDirectory();
//        File file = new File(path, System.currentTimeMillis() + ".jpg");
        if (!file.exists()) {
            file.mkdir();
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("rxjava", "IOException e = " + e.getMessage());
        }
    }
}
