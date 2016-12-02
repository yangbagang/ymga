/**
 * 
 */
package com.ybg.ga.ymga.ga.pedometer;

import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.user.UserPreferences;

/**
 * @author 杨拔纲
 * 
 */
public class CaloriesNotifier implements StepListener {

	private YdPreference ydPreference = YdPreference.getInstance();
	private UserPreferences userPreference = UserPreferences.getInstance();

	public interface Listener {

		public void valueChanged(float value);

		public void passValue();

	}

	private Listener listener;

	private static double METRIC_RUNNING_FACTOR = 1.02784823;
	private static double METRIC_WALKING_FACTOR = 0.708;

	private double calories = 0;

	boolean mIsMetric;
	boolean mIsRunning;
	float stepLength;
	float bodyWeight;

	public CaloriesNotifier(Listener listener) {
		this.listener = listener;
		reloadSettings();
	}

	public void setCalories(float calories) {
		this.calories = calories;
		notifyListener();
	}

	public void reloadSettings() {
		mIsRunning = ydPreference.isRunSportType();
		stepLength = ydPreference.getStepLength();
		bodyWeight = userPreference.getBodyWeight();
		notifyListener();
	}

	public void resetValues() {
		calories = 0;
	}

	public void isMetric(boolean isMetric) {
		mIsMetric = isMetric;
	}

	public void setStepLength(float stepLength) {
		this.stepLength = stepLength;
	}

	public void onStep() {

		calories += (bodyWeight * (mIsRunning ? METRIC_RUNNING_FACTOR
				: METRIC_WALKING_FACTOR))

		* stepLength / 1000;

		notifyListener();
	}

	private void notifyListener() {
		listener.valueChanged((float) calories);
	}

	public void passValue() {

	}

}
