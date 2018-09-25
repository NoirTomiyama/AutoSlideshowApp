package jp.techacademy.tomiyama.ryota.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private Cursor cursor;
    private ImageView imageView;

    private Timer mTimer;
    private Handler mHandler = new Handler();

    private Button backButton;
    private Button playStopButton;
    private Button forwardButton;

    private TextView textView;
    private TextView textView2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backButton = findViewById(R.id.back);
        playStopButton = findViewById(R.id.playStop);
        forwardButton = findViewById(R.id.forward);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);

        backButton.setOnClickListener(this);
        playStopButton.setOnClickListener(this);
        forwardButton.setOnClickListener(this);


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View view) {

        if(cursor!=null){

            int v_id = view.getId();

            switch (v_id){
                case R.id.back:
                    // 戻るボタンの処理
                    if (cursor.moveToPrevious()) {
                        imageView.setImageURI(getImageUri());
                    } else {
                        // 戻ることができなかった場合
                        cursor.moveToLast();
                        imageView.setImageURI(getImageUri());
                    }

                    textView2.setText("");

                    break;
                case R.id.playStop:
                    // 再生の処理
                    // ①送りボタン，戻るボタンをタップ不可に
                    // ②文字の変更
                    if(mTimer == null){
                        textView2.setText("スライドショー再生中！");
                        mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {

                                if(!cursor.moveToNext()) cursor.moveToFirst();

                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageURI(getImageUri());
                                        Log.d("ANDROID_Timer内", "URI : " + getImageUri().toString());
                                        textView.setText(getImageUri().toString());

                                    }
                                });
                            }
                        }, 2000, 2000);

                        playStopButton.setText("停止");
                        backButton.setEnabled(false);
                        forwardButton.setEnabled(false);

                    }else{
                        // 停止の処理
                        // 停止ボタンを押すと，スライドショー停止になる．
                        // 送りボタンと戻るボタンをタップ可能に
                        mTimer.cancel();
                        mTimer = null;

                        playStopButton.setText("再生");
                        backButton.setEnabled(true);
                        forwardButton.setEnabled(true);

                        textView2.setText("");

                    }

                    break;

                case R.id.forward:
                    // 進むボタンの処理

                    // 進むことができなかったときの処理
                    if(!cursor.moveToNext()) cursor.moveToFirst();

                    imageView.setImageURI(getImageUri());

                    textView2.setText("");

                    break;
            }

            Log.d("ANDROID", "URI : " + getImageUri().toString()); // 表示する
            textView.setText(getImageUri().toString());
        }


    }

    private Uri getImageUri() {

        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);

        return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
    }

//    //クリックイベント管理
//    private View.OnClickListener onClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//
//
//        }
//    };

    private void getContentsInfo() {

        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if(cursor != null){
            if (cursor.moveToFirst()) {

                imageView.setImageURI(getImageUri());
                textView.setText(getImageUri().toString());
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if(cursor != null){
            cursor.close();
        }

    }
}
