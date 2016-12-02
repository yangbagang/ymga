package com.ybg.ga.ymga.ga.tw;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ga.yd.YDDataService;
import com.ybg.ga.ymga.ui.ChartView;
import com.ybg.ga.ymga.ui.NoZeroLineChartView;
import com.ybg.ga.ymga.ui.RoundProgressBar;
import com.ybg.ga.ymga.util.TimeUtil;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yangbagang on 2015/5/27.
 */
public class TWQuShiActivity extends SubActivity {

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private TWPreference twPreference = TWPreference.getInstance();

    private TWDataService twDataService = null;
    private Intent bindIntent = null;

    // 截止日期
    private TextView tongJiBeginDate = null;
    private TextView tongJiEndDate = null;
    // 日期选择标志位，避免重复执行监听
    private boolean isDateClicked = false;
    // 画图
    private LinearLayout chartLinearLayout = null;

    private ProgressDialog drawProgressBar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tw_qushi);

        Toolbar toolbar = (Toolbar) findViewById(R.id.twQushiToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        chartLinearLayout = (LinearLayout) findViewById(R.id.chartLinearLayout);

        bindIntent = new Intent(TWQuShiActivity.this, TWDataService.class);
        getApplication().bindService(bindIntent, mConnection,
                Context.BIND_AUTO_CREATE);

        tongJiBeginDate = (TextView) findViewById(R.id.tongJiBeginDate);
        tongJiEndDate = (TextView) findViewById(R.id.tongJiEndDate);
        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Date now = calendar.getTime();
        tongJiEndDate.setText(sdf.format(now));
        tongJiBeginDate.setText(sdf.format(now));
        final DatePickerDialog.OnDateSetListener tongJiBeginDateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                if (isDateClicked) {
                    isDateClicked = false;
                    tongJiBeginDate.setText(String.format("%d-%02d-%02d", year,
                            monthOfYear + 1, dayOfMonth));
                    drawChart();
                }
            }

        };
        tongJiBeginDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isDateClicked = true;
                new DatePickerDialog(TWQuShiActivity.this,
                        tongJiBeginDateListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }

        });
        final DatePickerDialog.OnDateSetListener tongJiEndDateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                if (isDateClicked) {
                    isDateClicked = false;
                    tongJiEndDate.setText(String.format("%d-%02d-%02d", year,
                            monthOfYear + 1, dayOfMonth));
                    drawChart();
                }
            }

        };
        tongJiEndDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                isDateClicked = true;
                new DatePickerDialog(TWQuShiActivity.this,
                        tongJiEndDateListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        getApplication().unbindService(mConnection);
        super.onDestroy();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            twDataService = ((TWDataService.TWDataBinder) service).getService();
            drawChart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            twDataService = null;
        }

    };

    private void drawChart() {
        DrawLineChartTask task = new DrawLineChartTask();
        task.execute();
    }

    /**
     * 异步更新，画折线图
     *
     * @author 杨拔纲
     *
     */
    private class DrawLineChartTask extends
            AsyncTask<Integer, Integer, NoZeroLineChartView> {

        @Override
        protected void onPreExecute() {
            // 开始画图前调用，显示出进度条。
            super.onPreExecute();
            drawProgressBar = ProgressDialog.show(TWQuShiActivity.this,
                    "正在统计数据...", "请稍等片刻...", true, true);
        }

        @Override
        protected void onPostExecute(NoZeroLineChartView result) {
            // 画图完成，关闭进度条。
            super.onPostExecute(result);
            chartLinearLayout.removeAllViews();
            chartLinearLayout.addView(result);
            if (drawProgressBar != null) {
                drawProgressBar.dismiss();
            }
        }

        @Override
        protected NoZeroLineChartView doInBackground(Integer... params) {
            // 获得参数
            String beginDate = tongJiBeginDate.getText().toString() + " 00:00:00";
            String endDate = tongJiEndDate.getText().toString() + " 23:59:59";

            List<Float> data = new ArrayList<Float>();
            Cursor cursor = twDataService.list(beginDate, endDate);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                do {
                    data.add(cursor.getFloat(cursor.getColumnIndex("tw")));
                } while (cursor.moveToNext());
            }
            float[] floatData = new float[data.size()];
            for (int i = 0; i < floatData.length; i++) {
                int a = (int) (data.get(i) * 10);
                floatData[i] = a / 10f;
            }

            // 画图
            return new NoZeroLineChartView(TWQuShiActivity.this, floatData);
        }

    }


}
