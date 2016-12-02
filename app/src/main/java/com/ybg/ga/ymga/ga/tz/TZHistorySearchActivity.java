/**
 * 
 */
package com.ybg.ga.ymga.ga.tz;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 * 
 */
public class TZHistorySearchActivity extends Activity {

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private EditText beginDateView;
	private EditText endDateView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tz_history_search);

		beginDateView = (EditText) findViewById(R.id.tzHistorySearchBegin);
		endDateView = (EditText) findViewById(R.id.tzHistorySearchEnd);

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
		beginDateView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new DatePickerDialog(TZHistorySearchActivity.this,
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
		endDateView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new DatePickerDialog(TZHistorySearchActivity.this,
						endDateListener, calendar.get(Calendar.YEAR), calendar
								.get(Calendar.MONTH), calendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}

		});
	}

	public void beginSearch(View view) {
		Intent intent = new Intent();
		intent.putExtra("beginDate", beginDateView.getText().toString());
		intent.putExtra("endDate", endDateView.getText().toString());
		setResult(AppConstat.TZ_SEARCH_RESULT_CODE, intent);
		finish();
	}

	public void cancelSearch(View view) {
		Intent intent = new Intent();
		setResult(AppConstat.TZ_SEARCH_CANCEL_CODE, intent);
		finish();
	}

}
