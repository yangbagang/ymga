/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ui.ChartView;
import com.ybg.ga.ymga.ui.RoundProgressBar;
import com.ybg.ga.ymga.util.TimeUtil;

/**
 * @author 杨拔纲
 * 
 */
public class YdHistoryActivity extends SubActivity {

	@SuppressLint("SimpleDateFormat")
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	private YdPreference ydPreference = YdPreference.getInstance();

	private YDDataService ydDataService = null;
	private Intent bindIntent = null;

	// 截止日期
	private TextView tongJiEndDate = null;
	// 日期选择标志位，避免重复执行监听
	private boolean isDateClicked = false;
	// 时间周期选择项
	private LinearLayout periodDay = null;
	private LinearLayout periodWeek = null;
	private LinearLayout periodMonth = null;
	private int periodIndex = 0;
	// 画图
	private LinearLayout chartLinearLayout = null;
	// 圆型进度条
	private RoundProgressBar completeProgressBar = null;
	// 图形内容选择项
	private LinearLayout chart_steps_layout = null;
	private LinearLayout chart_distance_layout = null;
	private LinearLayout chart_calories_layout = null;
	private LinearLayout chart_sleep_layout = null;
	private LinearLayout chart_finish_layout = null;
	private TextView chart_steps_label = null;
	private TextView chart_distance_label = null;
	private TextView chart_calories_label = null;
	private TextView chart_sleep_label = null;
	private TextView chart_finish_label = null;
	private int chartIndex = 0;

	private ProgressDialog drawProgressBar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.yd_history);

		Toolbar toolbar = (Toolbar) findViewById(R.id.ydHistoryToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		chartLinearLayout = (LinearLayout) findViewById(R.id.chartLinearLayout);

		bindIntent = new Intent(YdHistoryActivity.this, YDDataService.class);
		getApplication().bindService(bindIntent, mConnection,
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
					if (periodIndex != 0 || chartIndex != 4) {
						drawChart();
					} else {
						drawRoundProgress();
					}
				}
			}

		};
		tongJiEndDate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isDateClicked = true;
				new DatePickerDialog(YdHistoryActivity.this,
						tongJiEndDateListener, calendar.get(Calendar.YEAR),
						calendar.get(Calendar.MONTH), calendar
								.get(Calendar.DAY_OF_MONTH)).show();
			}

		});

		periodDay = (LinearLayout) findViewById(R.id.period_day_layout);
		periodWeek = (LinearLayout) findViewById(R.id.period_week_layout);
		periodMonth = (LinearLayout) findViewById(R.id.period_month_layout);
		periodDay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setPeriodItem(0);
				if (chartIndex != 4) {
					drawChart();
				} else {
					drawRoundProgress();
				}
			}

		});
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

		chart_steps_label = (TextView) findViewById(R.id.chart_steps_label);
		chart_distance_label = (TextView) findViewById(R.id.chart_distance_label);
		chart_calories_label = (TextView) findViewById(R.id.chart_calories_label);
		chart_sleep_label = (TextView) findViewById(R.id.chart_sleep_label);
		chart_finish_label = (TextView) findViewById(R.id.chart_finish_label);
		chart_steps_layout = (LinearLayout) findViewById(R.id.chart_steps_layout);
		chart_distance_layout = (LinearLayout) findViewById(R.id.chart_distance_layout);
		chart_calories_layout = (LinearLayout) findViewById(R.id.chart_calories_layout);
		chart_sleep_layout = (LinearLayout) findViewById(R.id.chart_sleep_layout);
		chart_finish_layout = (LinearLayout) findViewById(R.id.chart_finish_layout);
		chart_steps_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(0);
				drawChart();
			}

		});
		chart_distance_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(1);
				drawChart();
			}

		});
		chart_calories_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(2);
				drawChart();
			}

		});
		chart_sleep_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(3);
				drawChart();
			}

		});
		chart_finish_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setChartItem(4);
				if (periodIndex != 0) {
					drawChart();
				} else {
					drawRoundProgress();
				}
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
			ydDataService = ((YDDataService.YDDataBinder) service).getService();
			setPeriodItem(0);
			setChartItem(0);
			drawChart();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			ydDataService = null;
		}

	};

	private void setPeriodItem(int index) {
		if (index == 0) {
			periodDay.setBackgroundResource(R.drawable.selected_sharp_bg);
			periodWeek.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.unselected_sharp_bg);
		} else if (index == 1) {
			periodDay.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodWeek.setBackgroundResource(R.drawable.selected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.unselected_sharp_bg);
		} else if (index == 2) {
			periodDay.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodWeek.setBackgroundResource(R.drawable.unselected_sharp_bg);
			periodMonth.setBackgroundResource(R.drawable.selected_sharp_bg);
		}
		periodIndex = index;
	}

	private void setChartItem(int index) {
		if (index == 0) {
			chart_steps_layout
					.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_distance_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_calories_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sleep_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_finish_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_steps_label.setTextColor(0xffffffff);
			chart_distance_label.setTextColor(0xff7f7f7f);
			chart_calories_label.setTextColor(0xff7f7f7f);
			chart_sleep_label.setTextColor(0xff7f7f7f);
			chart_finish_label.setTextColor(0xff7f7f7f);
		} else if (index == 1) {
			chart_steps_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_distance_layout
					.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_calories_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sleep_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_finish_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_steps_label.setTextColor(0xff7f7f7f);
			chart_distance_label.setTextColor(0xffffffff);
			chart_calories_label.setTextColor(0xff7f7f7f);
			chart_sleep_label.setTextColor(0xff7f7f7f);
			chart_finish_label.setTextColor(0xff7f7f7f);
		} else if (index == 2) {
			chart_steps_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_distance_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_calories_layout
					.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_sleep_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_finish_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_steps_label.setTextColor(0xff7f7f7f);
			chart_distance_label.setTextColor(0xff7f7f7f);
			chart_calories_label.setTextColor(0xffffffff);
			chart_sleep_label.setTextColor(0xff7f7f7f);
			chart_finish_label.setTextColor(0xff7f7f7f);
		} else if (index == 3) {
			chart_steps_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_distance_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_calories_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sleep_layout
					.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_finish_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_steps_label.setTextColor(0xff7f7f7f);
			chart_distance_label.setTextColor(0xff7f7f7f);
			chart_calories_label.setTextColor(0xff7f7f7f);
			chart_sleep_label.setTextColor(0xffffffff);
			chart_finish_label.setTextColor(0xff7f7f7f);
		} else if (index == 4) {
			chart_steps_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_distance_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_calories_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_sleep_layout
					.setBackgroundResource(R.drawable.unselected_sharp_bg2);
			chart_finish_layout
					.setBackgroundResource(R.drawable.selected_sharp_bg);
			chart_steps_label.setTextColor(0xff7f7f7f);
			chart_distance_label.setTextColor(0xff7f7f7f);
			chart_calories_label.setTextColor(0xff7f7f7f);
			chart_sleep_label.setTextColor(0xff7f7f7f);
			chart_finish_label.setTextColor(0xffffffff);
		}
		chartIndex = index;
	}

	private void drawChart() {
		DrawLineChartTask task = new DrawLineChartTask();
		task.execute(periodIndex, chartIndex);
	}

	private void drawRoundProgress() {
		// 实例化圆形进度条
		completeProgressBar = new RoundProgressBar(getApplicationContext());
		// 设置颜色
		completeProgressBar.setCricleColor(0xffbcbdbb);
		completeProgressBar.setCricleProgressColor(0xff48cc06);
		completeProgressBar.setTextColor(0xff23ac38);
		// 设置圆环的宽度
		int maxWidth = Math.min(chartLinearLayout.getMeasuredHeight(),
				chartLinearLayout.getMeasuredWidth());
		completeProgressBar.setRoundWidth((float) (maxWidth * 0.1));
		// 设置最大进度
		int aimSteps = ydPreference.getAimSteps();
		completeProgressBar.setMax(aimSteps);
		// 设置显示中间的进度
		completeProgressBar.setTextIsDisplayable(true);
		// 设置空心进度的风格
		completeProgressBar.setStyle(0);

		chartLinearLayout.removeAllViews();
		chartLinearLayout.addView(completeProgressBar);
		// 开始后台查询更新操作
		DrawRoundChartTask task = new DrawRoundChartTask();
		task.execute();
	}

	/**
	 * 异步更新，画折线图
	 * 
	 * @author 杨拔纲
	 * 
	 */
	private class DrawLineChartTask extends
			AsyncTask<Integer, Integer, ChartView> {

		private int periodIndex = 0;
		private int chartIndex = 0;
		String format = "";
		private List<String> xValue = new ArrayList<String>();
		private List<String> yValue = new ArrayList<String>();
		private List<String> values = new ArrayList<String>();

		@Override
		protected void onPreExecute() {
			// 开始画图前调用，显示出进度条。
			super.onPreExecute();
			drawProgressBar = ProgressDialog.show(YdHistoryActivity.this,
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

		@Override
		protected ChartView doInBackground(Integer... params) {
			// 获得参数
			periodIndex = params[0];
			chartIndex = params[1];
			String endDate = tongJiEndDate.getText().toString();
			if (periodIndex == 0) {
				if (chartIndex < 3) {
					// 运动日统计图，按小时计算各项数据，不显示实际数据值。
					searchActivityDetailData(endDate);
				} else if (chartIndex == 3) {
					// 查询当天的睡眠详情
					searchSleepDetailData(endDate);
				}
			} else if (periodIndex == 1) {
				format = "E";
				String beginDate = TimeUtil.getDateByOffset(endDate, 7);
				List<String> dates = TimeUtil.getDates(endDate, 7, format);
				xValue.addAll(dates);
				if (chartIndex != 3) {
					// 周统计图，按天显示一周的运动数据，显示实际值。
					searchHistoryActivityData(beginDate, endDate, 1);
				} else {
					// 周统计图，按天显示一周的睡眠数据，显示实际值。
					searchHistorySleepData(beginDate, endDate, 1);
				}
			} else if (periodIndex == 2) {
				format = "dd";
				String beginDate = TimeUtil.getDateByOffset(endDate, 30);
				List<String> dates = TimeUtil.getDates(endDate, 30, format);
				xValue.addAll(dates);
				if (chartIndex != 3) {
					// 月统计图，按天显示各项运动数据，不显示实际值。
					searchHistoryActivityData(beginDate, endDate, 0);
				} else {
					// 月统计图，按天显示各项睡眠数据，不显示实际值。
					searchHistorySleepData(beginDate, endDate, 0);
				}
			}

			// 修正空集合
			if (xValue.size() == 0) {
				xValue.add("0");
			}
			if (yValue.size() == 0) {
				yValue.add("0");
			}
			if (values.size() == 0) {
				values.add("0");
			}
			// 画图
			int height = chartLinearLayout.getMeasuredHeight();
			int width = chartLinearLayout.getMeasuredWidth();
			ChartView chart = new ChartView(YdHistoryActivity.this, width,
					height, xValue.size(), yValue.size());
			chart.setInfo((String[]) xValue.toArray(new String[xValue.size()]),
					(String[]) yValue.toArray(new String[yValue.size()]),
					(String[]) values.toArray(new String[values.size()]));

			// 返回结果
			return chart;
		}

		private void searchHistoryActivityData(String startDate,
				String endDate, int flag) {
			// 初始化数据值
			String[] strValues = new String[xValue.size()];
			for (int i = 0; i < strValues.length; i++) {
				strValues[i] = "0";
			}
			// 查询数据
			Cursor cursor = ydDataService.listActivitySum(startDate, endDate);
			if (cursor.moveToFirst()) {
				String day = null;
				String timeString = null;
				float maxValue = 0f;
				int aimSteps = ydPreference.getAimSteps();
				do {
					day = cursor.getString(0) + " 00:00:00";
					int s = cursor.getInt(1);
					float d = cursor.getFloat(2);
					float c = cursor.getFloat(3);
					timeString = TimeUtil.getPartTimeFromDate(day, format);
					if (chartIndex == 0) {
						if (s > maxValue) {
							maxValue = s;
						}
						strValues[getIndex(timeString)] = "" + s;
					} else if (chartIndex == 1) {
						if (d > maxValue) {
							maxValue = d;
						}
						strValues[getIndex(timeString)] = new DecimalFormat(
								"#.##").format(d);
					} else if (chartIndex == 2) {
						if (c > maxValue) {
							maxValue = c;
						}
						strValues[getIndex(timeString)] = new DecimalFormat(
								"#.#").format(c);
					} else if (chartIndex == 4) {
						int f = s * 100 / aimSteps;
						if (f > maxValue) {
							maxValue = f;
						}
						strValues[getIndex(timeString)] = "" + f;
					}
				} while (cursor.moveToNext());
				if (chartIndex == 0 || chartIndex == 4) {
					generalYScale(yValue, maxValue, 0);
				} else if (chartIndex == 1) {
					generalYScale(yValue, maxValue, 2);
				} else if (chartIndex == 2) {
					generalYScale(yValue, maxValue, 1);
				}
				cursor.close();
				values.clear();
				values.addAll(Arrays.asList(strValues));
			}
		}

		private void searchHistorySleepData(String startDate, String endDate,
				int flag) {
			// 初始化数据值
			String[] strValues = new String[xValue.size()];
			for (int i = 0; i < strValues.length; i++) {
				strValues[i] = "0";
			}
			// 查询数据
			Cursor cursor = ydDataService.listSleepSum(startDate, endDate);
			if (cursor.moveToFirst()) {
				String day = null;
				String timeString = null;
				float maxValue = 0f;
				do {
					day = cursor.getString(0) + " 00:00:00";
					timeString = TimeUtil.getPartTimeFromDate(day, format);
					int minute = cursor.getInt(1);
					if (minute > maxValue) {
						maxValue = minute;
					}
					strValues[getIndex(timeString)] = "" + minute;
				} while (cursor.moveToNext());
				generalYScale(yValue, maxValue, 0);
				cursor.close();
				values.clear();
				values.addAll(Arrays.asList(strValues));
			}
		}

		private void searchActivityDetailData(String endDate) {
			// 构造数据结构
			for (int i = 0; i < 24; i++) {
				xValue.add(i + 1 + "");
			}
			int[] steps = new int[24];
			float[] distances = new float[24];
			float[] calories = new float[24];
			// 查询出配件运动记录
			Cursor cursor = ydDataService.listSyncActivityDetail(endDate);
			// 打开游标
			if (cursor.moveToFirst()) {
				do {
					// 查出数据
					int timeIndex = cursor.getInt(0);
					int step = cursor.getInt(1);
					float distance = cursor.getFloat(2);
					float calorie = cursor.getFloat(3);
					// 累加
					int index = timeIndex / 4;
					steps[index] += step;
					distances[index] += distance;
					calories[index] += calorie;
				} while (cursor.moveToNext());
			}
			cursor.close();
			// 查询出GPS运动
			Cursor cursor2 = ydDataService.listGPSDetail(endDate);
			if (cursor2.moveToFirst()) {
				do {
					String createTime = cursor2.getString(0);
					int step = cursor2.getInt(1);
					float distance = cursor2.getFloat(2);
					float calorie = cursor2.getFloat(3);
					int index = getHourFromTime(createTime);
					// 累加
					steps[index] += step;
					distances[index] += distance;
					calories[index] += calorie;
				} while (cursor2.moveToNext());
			}
			cursor2.close();
			// 填充数据
			if (chartIndex == 0) {
				// 填充步数
				int maxStep = 0;
				for (int step : steps) {
					values.add(String.valueOf(step));
					if (step > maxStep) {
						maxStep = step;
					}
				}
				generalYScale(yValue, maxStep, 0);
			} else if (chartIndex == 1) {
				// 填充里程
				float maxDistance = 0f;
				for (float distance : distances) {
					values.add(new DecimalFormat("#.##").format(distance));
					if (distance > maxDistance) {
						maxDistance = distance;
					}
				}
				generalYScale(yValue, maxDistance, 2);
			} else if (chartIndex == 2) {
				// 填充卡路里
				float maxCalorie = 0f;
				for (float calorie : calories) {
					values.add(new DecimalFormat("#.#").format(calorie));
					if (calorie > maxCalorie) {
						maxCalorie = calorie;
					}
				}
				generalYScale(yValue, maxCalorie, 1);
			}
		}

		private void searchSleepDetailData(String endDate) {
			// 构造数据结构
			for (int i = 0; i < 24; i++) {
				xValue.add(i + 1 + "");
			}
			int[] sleeps = new int[24];
			// 查询出配件运动记录
			Cursor cursor = ydDataService.listSyncSleepDetail(endDate);
			// 打开游标
			if (cursor.moveToFirst()) {
				do {
					// 查出数据
					int timeIndex = cursor.getInt(0);
					int sm1 = cursor.getInt(1);
					int sm2 = cursor.getInt(2);
					int sm3 = cursor.getInt(3);
					int sm4 = cursor.getInt(4);
					int sm5 = cursor.getInt(5);
					int sm6 = cursor.getInt(6);
					int sm7 = cursor.getInt(7);
					int sm8 = cursor.getInt(8);
					// 累加
					int index = timeIndex / 4;
					if (sm1 > 0) {
						sleeps[index] += 2;
					}
					if (sm2 > 0) {
						sleeps[index] += 2;
					}
					if (sm3 > 0) {
						sleeps[index] += 2;
					}
					if (sm4 > 0) {
						sleeps[index] += 2;
					}
					if (sm5 > 0) {
						sleeps[index] += 2;
					}
					if (sm6 > 0) {
						sleeps[index] += 2;
					}
					if (sm7 > 0) {
						sleeps[index] += 2;
					}
					if (sm8 > 0) {
						sleeps[index] += 1;
					}
				} while (cursor.moveToNext());
			}
			cursor.close();

			// 填充数据
			for (int sleep : sleeps) {
				values.add(String.valueOf(sleep));
			}
			yValue.add("0");
			yValue.add("15");
			yValue.add("30");
			yValue.add("45");
			yValue.add("60");
		}

		/**
		 * 生成Y轴刻度
		 * 
		 * @param list
		 * @param maxValue
		 * @param digital
		 *            小数点位数
		 */
		private void generalYScale(List<String> list, float maxValue,
				int digital) {
			float yScale = (maxValue * 1.0f) / 4;
			String format = "#";
			if (digital == 1) {
				format = "#.#";
			} else if (digital == 2) {
				format = "#.##";
			} else if (digital == 3) {
				format = "#.###";
			}
			for (int i = 0; i < 5; i++) {
				list.add(new DecimalFormat(format).format(i * yScale));
			}
		}

		private int getHourFromTime(String time) {
			return Integer.valueOf(time.substring(time.indexOf(" ") + 1,
					time.indexOf(":")));
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

	private class DrawRoundChartTask extends
			AsyncTask<Integer, Integer, Integer> {

		@Override
		protected Integer doInBackground(Integer... params) {
			// 后台查询出当天总运动步数
			String date = tongJiEndDate.getText().toString();
			return ydDataService.getStepsByDate(date);
		}

		@Override
		protected void onPreExecute() {
			// 开始画图前调用，显示出进度条。
			super.onPreExecute();
			drawProgressBar = ProgressDialog.show(YdHistoryActivity.this,
					"正在统计数据...", "请稍等片刻...", true, true);
		}

		@Override
		protected void onPostExecute(Integer result) {
			// 画图完成，关闭进度条。
			super.onPostExecute(result);
			completeProgressBar.setProgress(result);
			if (drawProgressBar != null) {
				drawProgressBar.dismiss();
			}
		}

	}
}
