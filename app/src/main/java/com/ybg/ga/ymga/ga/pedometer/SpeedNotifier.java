/**
 * 
 */
package com.ybg.ga.ymga.ga.pedometer;

import com.ybg.ga.ymga.ga.preference.YdPreference;

/**
 * @author 杨拔纲
 * 
 */
public class SpeedNotifier implements PaceNotifier.Listener {

	private YdPreference ydPreference = YdPreference.getInstance();

	public interface Listener {

		public void valueChanged(float value);

		public void passValue();

	}

	private Listener listener;

	int counter = 0;
	float speed = 0;

	boolean isMetric;
	float stepLength;

	public SpeedNotifier(Listener listener) {
		this.listener = listener;
		reloadSettings();
	}

	public void setSpeed(float speed) {
		this.speed = speed;
		notifyListener();
	}

	public void reloadSettings() {
		stepLength = ydPreference.getStepLength();
		notifyListener();
	}

	private void notifyListener() {
		listener.valueChanged(speed);
	}

	@Override
	public void paceChanged(int value) {
		speed = value * stepLength / 1000 * 60;
		notifyListener();
	}

	public void passValue() {
		// Not used
	}

}
