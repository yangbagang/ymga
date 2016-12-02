package com.ybg.ga.ymga.ga.tw;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by yangbagang on 2015/6/1.
 */
public class TWHistoryDataSearchActivity extends Activity {

    @SuppressLint("SimpleDateFormat")
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private EditText beginDateView;
    private EditText endDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tw_history_search);

        beginDateView = (EditText) findViewById(R.id.twHistorySearchBegin);
        endDateView = (EditText) findViewById(R.id.twHistorySearchEnd);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        Date now = calendar.getTime();
        beginDateView.setText(sdf.format(now));
        endDateView.setText(sdf.format(now));

        final DatePickerDialog.OnDateSetListener beginDateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                beginDateView.setText(String.format("%d-%02d-%02d", year,
                        monthOfYear + 1, dayOfMonth));
            }

        };
        beginDateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(TWHistoryDataSearchActivity.this,
                        beginDateListener, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }

        });

        final DatePickerDialog.OnDateSetListener endDateListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                endDateView.setText(String.format("%d-%02d-%02d", year,
                        monthOfYear + 1, dayOfMonth));
            }

        };
        endDateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(TWHistoryDataSearchActivity.this,
                        endDateListener, calendar.get(Calendar.YEAR), calendar
                        .get(Calendar.MONTH), calendar
                        .get(Calendar.DAY_OF_MONTH)).show();
            }

        });
    }

    public void beginSearch(View view) {
        Intent intent = new Intent();
        intent.putExtra("beginDate", beginDateView.getText().toString() + " 00:00:00");
        intent.putExtra("endDate", endDateView.getText().toString() + " 23:59:59");
        setResult(AppConstat.TW_SEARCH_RESULT_CODE, intent);
        finish();
    }

    public void cancelSearch(View view) {
        Intent intent = new Intent();
        setResult(AppConstat.TW_SEARCH_CANCEL_CODE, intent);
        finish();
    }
    
}
