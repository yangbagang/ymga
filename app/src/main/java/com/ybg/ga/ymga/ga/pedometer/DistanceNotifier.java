/**
 * 
 */
package com.ybg.ga.ymga.ga.pedometer;

import com.ybg.ga.ymga.ga.preference.YdPreference;

/**
 * @author 杨拔纲
 * 
 */
public class DistanceNotifier implements StepListener {

	private YdPreference ydPreference = YdPreference.getInstance();

	public interface Listener {

		public void valueChanged(float value);

		public void passValue();

	}

	private Listener listener;

	float distance = 0;

	float stepLength;

	public DistanceNotifier(Listener listener) {
		this.listener = listener;
		reloadSettings();
	}

	public void setDistance(float distance) {
		this.distance = distance;
		notifyListener();
	}

	public void reloadSettings() {
		stepLength = ydPreference.getStepLength();
		notifyListener();
	}

	public void onStep() {
		distance += stepLength / 1000.0;
		notifyListener();
	}

	private void notifyListener() {
		listener.valueChanged(distance);
	}

	public void passValue() {
		// Callback of StepListener - Not implemented
	}

}
