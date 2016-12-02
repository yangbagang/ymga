/**
 *
 */
package com.ybg.ga.ymga;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ybg.ga.ymga.ga.MainActivity;

/**
 * @author 杨拔纲
 */
public class AppIntroActivity extends Activity {

    private TextView tv_second_num;

    private int time = 5;//倒计时

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置无标题窗口
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.intro_page);
        tv_second_num = (TextView) findViewById(R.id.tv_second_num);

        mHandler.postDelayed(runnable, 1000);
    }

    public void enterMainActivity(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(null);
            mHandler = null;
        }
        time = 0;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            time--;
            if (time == 0) {
                enterMainActivity(null);
            } else {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(158);
                }
            }
            if (mHandler != null) {
                mHandler.postDelayed(this, 1000);
            }
        }
    };

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 158:
                    tv_second_num.setText(time + "秒后关闭");
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
