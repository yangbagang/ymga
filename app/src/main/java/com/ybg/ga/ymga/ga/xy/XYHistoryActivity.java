/**
 * 
 */
package com.ybg.ga.ymga.ga.xy;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ui.XYHistoryMultiLineChart;
import com.ybg.ga.ymga.ui.XYHistoryScatterChart;
import com.ybg.ga.ymga.util.TimeUtil;

/**
 * @author 杨拔纲
 * 
 */
public class XYHistoryActivity extends SubActivity {

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private XYDataService xyDataService = null;
	private Intent bindIntent = null;

	private LinearLayout scatterChartLinear = null;
	private LinearLayout multiLineChartLinear = null;

	// 截止日期
	private TextView tongJiEndDate = null;
	// 日期选择标志位，避免重复执行监听
	private boolean isDateClicked = false;
	// 时间周期选择项
	private LinearLayout periodWeek = null;
	private LinearLayout periodMonth = null;
	private LinearLayout periodYear = null;
	private int periodIndex = 1;
	private ProgressDialog drawProgressBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.xy_history);

		Toolbar toolbar = (Toolbar) findViewById(R.id.xyHistoryToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		scatterChartLinear = (LinearLayout) findViewById(R.id.xyHistoryScatterChart);
		multiLineChartLinear = (LinearLayout) findViewById(R.id.xyHistoryMultiLineChart);

		bindIntent = new Intent(XYHistoryActivity.this, XYDataService.class);
		getApplicationContext().bindService(bindIntent, mConnection,
				Context.BIND_AUTO_CREATE);
		
		tongJiEndDate = (TextView) findViewById(R.id.tongJiEndDate);
		final Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		Date now = calendar.getTime();
		tongJiEndDate.setText(sdf.format(now));
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
		tongJiEndDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isDateClicked = true;
				new DatePickerDialog(XYHistoryActivity.this,
						tongJiEndDateListener, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}

		});

		periodWeek = (LinearLayout) findViewById(R.id.period_week_layout);
		periodMonth = (LinearLayout) findViewById(R.id.period_month_layout);
		periodYear = (LinearLayout) findViewById(R.id.period_year_layout);
		periodWeek.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPeriodItem(1);
				drawChart();
			}

		});
		periodMonth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPeriodItem(2);
				drawChart();
			}

		});
		periodYear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPeriodItem(3);
				drawChart();
			}

		});
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		getApplicationContext().unbindService(mConnection);
		super.onDestroy();
	}
	
	private void setPeriodItem(int index) {
		if (index == 1) {
			periodWeek.setBackgroundResource(R.drawable.selected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodYear.setBackgroundResource(R.drawable.unselected_sharp_bg);
		} else if (index == 2) {
			periodWeek.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.selected_sharp_bg);
			periodYear.setBackgroundResource(R.drawable.unselected_sharp_bg);
		} else if (index == 3) {
			periodWeek.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodYear.setBackgroundResource(R.drawable.selected_sharp_bg);
		}
		periodIndex = index;
	}

	private void drawChart() {
		new XYHistoryChartTask().execute();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			xyDataService = ((XYDataService.XYDataBinder) service).getService();
			setPeriodItem(1);
			drawChart();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			xyDataService = null;
		}

	};

	private class XYHistoryChartTask extends
			AsyncTask<Integer, Integer, List<XYBean>> {

		private List<String> xValues = new ArrayList<String>();

		@Override
		protected List<XYBean> doInBackground(Integer... params) {
			// 准备参数
			int period = 7;
			String format = "E";
			if (periodIndex == 1) {
				period = 7;
				format = "E";
			} else if (periodIndex == 2) {
				period = 30;
				format = "dd";
			} else if (periodIndex == 3) {
				period = 365;
				format = "MM";
			}
			String endDate = tongJiEndDate.getText().toString();
			String startDate = TimeUtil.getDateByOffset(endDate, period);
			List<XYBean> list = new ArrayList<XYBean>();
			// 查询数据库
			Cursor cursor = xyDataService.list(startDate + " 00:00:00", endDate + " 23:59:59");
			int sys = 0;
			int dia = 0;
			int pul = 0;
			String oldTime = null;
			int count = 0;
			if (cursor.moveToFirst()) {
				do {
					int sys_t = cursor.getInt(cursor.getColumnIndex("sys"));
					int dia_t = cursor.getInt(cursor.getColumnIndex("dia"));
					int pul_t = cursor.getInt(cursor.getColumnIndex("pul"));
					String createTime = cursor.getString(cursor.getColumnIndex("createTime"));
					String timeString = TimeUtil.getPartTimeFromDate(createTime, format);
					if (oldTime == null) {
						// 初始值
						oldTime = timeString;
						sys = sys_t;
						dia = dia_t;
						pul = pul_t;
						count = 1;
					} else if (oldTime.equals(timeString)) {
						// 累加值
						oldTime = timeString;
						sys += sys_t;
						dia += dia_t;
						pul += pul_t;
						count += 1;
					} else {
						// 平均值
						list.add(new XYBean(oldTime, sys/count, dia/count, pul/count));
						// 记录下一个值
						oldTime = timeString;
						sys = sys_t;
						dia = dia_t;
						pul = pul_t;
						count = 1;
					}
					if (cursor.isLast()) {
						list.add(new XYBean(oldTime, sys/count, dia/count, pul/count));
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
			// 填充日期数据
			if ("MM".equals(format)) {
				List<String> dates = TimeUtil.get12Months(endDate);
				xValues.clear();
				xValues.addAll(dates);
			} else {
				List<String> dates = TimeUtil.getDates(endDate, period, format);
				xValues.clear();
				xValues.addAll(dates);
			}
			return list;
		}

		@Override
		protected void onPostExecute(List<XYBean> data) {
			super.onPostExecute(data);

			scatterChartLinear.removeAllViews();
			XYHistoryScatterChart scatterChart = new XYHistoryScatterChart(
					getApplicationContext());
			scatterChart.setXYData(data);
			scatterChartLinear.addView(scatterChart);

			multiLineChartLinear.removeAllViews();
			XYHistoryMultiLineChart lineChart = new XYHistoryMultiLineChart(
					getApplicationContext());
			lineChart.setXYData(data);
			lineChart.setXValues(xValues);
			multiLineChartLinear.addView(lineChart);
			// 关闭进度条
			if(drawProgressBar != null) {
				drawProgressBar.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			drawProgressBar = ProgressDialog.show(XYHistoryActivity.this,
					"正在查询数据...", "请稍等片刻...", true, true);
		}

	}
	
}
