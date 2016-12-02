/**
 * 
 */
package com.ybg.ga.ymga.ga.tz;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import com.ybg.ga.ymga.ui.ChartView;
import com.ybg.ga.ymga.util.TimeUtil;

/**
 * @author 杨拔纲
 * 
 */
public class TZHistoryActivity extends SubActivity {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
			Locale.getDefault());
	private TZDataService tzDataService = null;
	private Intent bindIntent = null;
	private Cursor cursor = null;

	// 截止日期
	private TextView tongJiEndDate = null;
	// 日期选择标志位，避免重复执行监听
	private boolean isDateClicked = false;
	// 时间周期选择项
	private LinearLayout periodWeek = null;
	private LinearLayout periodMonth = null;
	private LinearLayout periodYear = null;
	private int periodIndex = 0;
	// 画图
	private LinearLayout chartLinearLayout = null;

	// 图形内容选择项
	private LinearLayout chart_tz_layout = null;
	private LinearLayout chart_bmi_layout = null;
	private LinearLayout chart_zf_layout = null;
	private LinearLayout chart_nz_layout = null;
	private LinearLayout chart_jr_layout = null;
	private LinearLayout chart_sf_layout = null;
	private LinearLayout chart_qz_layout = null;
	private LinearLayout chart_gg_layout = null;
	private LinearLayout chart_jc_layout = null;
	private TextView chart_tz_label = null;
	private TextView chart_bmi_label = null;
	private TextView chart_zf_label = null;
	private TextView chart_nz_label = null;
	private TextView chart_jr_label = null;
	private TextView chart_sf_label = null;
	private TextView chart_qz_label = null;
	private TextView chart_gg_label = null;
	private TextView chart_jc_label = null;
	private int chartIndex = 0;

	private int period = 7;
	private String format = "E";
	private String columnName = "tzValue";

	private ProgressDialog drawProgressBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.tz_history);

		bindIntent = new Intent(TZHistoryActivity.this, TZDataService.class);
		getApplicationContext().bindService(bindIntent, mConnection,
				Context.BIND_AUTO_CREATE);
		initView();

		Toolbar toolbar = (Toolbar) findViewById(R.id.tzHistoryToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	private void initView() {
		chartLinearLayout = (LinearLayout) findViewById(R.id.chartLinearLayout);

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
				new DatePickerDialog(TZHistoryActivity.this,
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
				setPeriodItem(0);
				drawChart();
			}

		});
		periodMonth.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPeriodItem(1);
				drawChart();
			}

		});
		periodYear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPeriodItem(2);
				drawChart();
			}

		});

		chart_tz_label = (TextView) findViewById(R.id.chart_tz_label);
		chart_bmi_label = (TextView) findViewById(R.id.chart_bmi_label);
		chart_zf_label = (TextView) findViewById(R.id.chart_zf_label);
		chart_nz_label = (TextView) findViewById(R.id.chart_nz_label);
		chart_jr_label = (TextView) findViewById(R.id.chart_jr_label);
		chart_sf_label = (TextView) findViewById(R.id.chart_sf_label);
		chart_qz_label = (TextView) findViewById(R.id.chart_qz_label);
		chart_gg_label = (TextView) findViewById(R.id.chart_gg_label);
		chart_jc_label = (TextView) findViewById(R.id.chart_jc_label);

		chart_tz_layout = (LinearLayout) findViewById(R.id.chart_tz_layout);
		chart_bmi_layout = (LinearLayout) findViewById(R.id.chart_bmi_layout);
		chart_zf_layout = (LinearLayout) findViewById(R.id.chart_zf_layout);
		chart_nz_layout = (LinearLayout) findViewById(R.id.chart_nz_layout);
		chart_jr_layout = (LinearLayout) findViewById(R.id.chart_jr_layout);
		chart_sf_layout = (LinearLayout) findViewById(R.id.chart_sf_layout);
		chart_qz_layout = (LinearLayout) findViewById(R.id.chart_qz_layout);
		chart_gg_layout = (LinearLayout) findViewById(R.id.chart_gg_layout);
		chart_jc_layout = (LinearLayout) findViewById(R.id.chart_jc_layout);
		chart_tz_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(0);
				drawChart();
			}

		});
		chart_bmi_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(1);
				drawChart();
			}

		});
		chart_zf_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(2);
				drawChart();
			}

		});
		chart_nz_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(3);
				drawChart();
			}

		});
		chart_jr_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(4);
				drawChart();
			}

		});
		chart_sf_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(5);
				drawChart();
			}

		});
		chart_qz_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(6);
				drawChart();
			}

		});
		chart_gg_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(7);
				drawChart();
			}

		});
		chart_jc_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(8);
				drawChart();
			}

		});
	}

	private void setPeriodItem(int index) {
		if (index == 0) {
			periodWeek.setBackgroundResource(R.drawable.selected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodYear.setBackgroundResource(R.drawable.unselected_sharp_bg);
			period = 7;
			format = "E";
		} else if (index == 1) {
			periodWeek.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.selected_sharp_bg);
			periodYear.setBackgroundResource(R.drawable.unselected_sharp_bg);
			period = 30;
			format = "dd";
		} else if (index == 2) {
			periodWeek.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodYear.setBackgroundResource(R.drawable.selected_sharp_bg);
			period = 365;
			format = "MM";
		}
		periodIndex = index;
	}

	private void setChartItem(int index) {
		if (index == 0) {
			chart_tz_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xffffffff);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzValue";
		} else if (index == 1) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xffffffff);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzBMIValue";
		} else if (index == 2) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xffffffff);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzZFValue";
		} else if (index == 3) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xffffffff);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzNZValue";
		} else if (index == 4) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xffffffff);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzJRValue";
		} else if (index == 5) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xffffffff);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzSFValue";
		} else if (index == 6) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xffffffff);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzQZValue";
		} else if (index == 7) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_jc_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xffffffff);
			chart_jc_label.setTextColor(0xff7f7f7f);

			columnName = "tzGGValue";
		} else if (index == 8) {
			chart_tz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_bmi_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_zf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_nz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jr_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sf_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_qz_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_gg_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_jc_layout.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_tz_label.setTextColor(0xff7f7f7f);
			chart_bmi_label.setTextColor(0xff7f7f7f);
			chart_zf_label.setTextColor(0xff7f7f7f);
			chart_nz_label.setTextColor(0xff7f7f7f);
			chart_jr_label.setTextColor(0xff7f7f7f);
			chart_sf_label.setTextColor(0xff7f7f7f);
			chart_qz_label.setTextColor(0xff7f7f7f);
			chart_gg_label.setTextColor(0xff7f7f7f);
			chart_jc_label.setTextColor(0xffffffff);

			columnName = "tzJCValue";
		}
	}

	private void drawChart() {
		new TZHistoryChartTask().execute(periodIndex, chartIndex);
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (cursor != null && !cursor.isClosed()) {
			cursor.close();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		getApplicationContext().unbindService(mConnection);
		super.onDestroy();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			tzDataService = ((TZDataService.TZDataBinder) service).getService();
			setPeriodItem(0);
			setChartItem(0);
			drawChart();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			tzDataService = null;
		}

	};

	private class TZHistoryChartTask extends
			AsyncTask<Integer, Integer, ChartView> {

		private List<String> xValue = new ArrayList<String>();
		private List<String> yValue = new ArrayList<String>();

		@Override
		protected ChartView doInBackground(Integer... params) {
			// 准备参数
			String endDate = tongJiEndDate.getText().toString();
			String startDate = TimeUtil.getDateByOffset(endDate, period);
			// 填充日期数据
			if ("MM".equals(format)) {
				List<String> dates = TimeUtil.get12Months(endDate);
				xValue.addAll(dates);
			} else {
				List<String> dates = TimeUtil.getDates(endDate, period, format);
				xValue.addAll(dates);
			}
			// 初始化数据集合
			String[] strValues = new String[xValue.size()];
			for (int i = 0; i < strValues.length; i++) {
				strValues[i] = "0";
			}
			// 查询数据库
			Cursor cursor = tzDataService.list(startDate + " 00:00:00", endDate
					+ " 23:59:59");

			float value = 0f;
			float avgValue = 0f;
			float maxValue = 0f;
			String oldTime = null;
			int count = 0;
			if (cursor.moveToFirst()) {
				do {
					float value_t = cursor.getFloat(cursor
							.getColumnIndex(columnName));
					String createTime = cursor.getString(cursor
							.getColumnIndex("createTime"));
					String timeString = TimeUtil.getPartTimeFromDate(
							createTime, format);
					if (oldTime == null) {
						// 初始值
						oldTime = timeString;
						value = value_t;
						count = 1;
					} else if (oldTime.equals(timeString)) {
						// 累加值
						oldTime = timeString;
						value += value_t;
						count += 1;
					} else {
						// 平均值
						avgValue = value / count;
						strValues[getIndex(oldTime)] = new DecimalFormat("#.#").format(avgValue);
						if (avgValue > maxValue)
							maxValue = avgValue;
						// 记录下一个值
						oldTime = timeString;
						value = value_t;
						count = 1;
					}
					if (cursor.isLast()) {
						avgValue = value / count;
						strValues[getIndex(oldTime)] = new DecimalFormat("#.#").format(avgValue);
						if (avgValue > maxValue)
							maxValue = avgValue;
					}
				} while (cursor.moveToNext());
			}
			cursor.close();

			// 生成Y轴数据
			generalYScale(yValue, maxValue);
			// 修正空集合
			if (xValue.size() == 0) {
				xValue.add("0");
			}
			if (yValue.size() == 0) {
				yValue.add("0");
			}
			// 画图
			int height = chartLinearLayout.getMeasuredHeight();
			int width = chartLinearLayout.getMeasuredWidth();
			ChartView chart = new ChartView(TZHistoryActivity.this, width,
					height, xValue.size(), yValue.size());
			chart.setInfo((String[]) xValue.toArray(new String[xValue.size()]),
					(String[]) yValue.toArray(new String[yValue.size()]),
					strValues);

			// 返回结果
			return chart;
		}

		@Override
		protected void onPreExecute() {
			// 开始画图前调用，显示出进度条。
			super.onPreExecute();
			drawProgressBar = ProgressDialog.show(TZHistoryActivity.this,
					"正在统计数据...", "请稍等片刻...", true, true);
		}

		@Override
		protected void onPostExecute(ChartView result) {
			// 画图完成，关闭进度条。
			super.onPostExecute(result);
			chartLinearLayout.removeAllViews();
			chartLinearLayout.addView(result);
			if (drawProgressBar != null) {
				drawProgressBar.dismiss();
			}
		}

		private void generalYScale(List<String> list, float maxValue) {
			float yScale = (maxValue * 1.0f) / 4;
			String format = "#.#";
			for (int i = 0; i < 5; i++) {
				list.add(new DecimalFormat(format).format(i * yScale));
			}
		}

		private int getIndex(String date) {
			if (xValue == null || xValue.size() == 0)
				return 0;
			if (xValue.contains(date)) {
				for (int i = 0; i < xValue.size(); i++) {
					if (xValue.get(i).equals(date))
						return i;
				}
			}
			return 0;
		}

	}

}
