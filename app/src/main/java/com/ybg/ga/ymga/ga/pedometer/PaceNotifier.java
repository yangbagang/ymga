/**
 * 
 */
package com.ybg.ga.ymga.ga.pedometer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 杨拔纲
 *
 */
public class PaceNotifier implements StepListener {
	
	public interface Listener {

		public void paceChanged(int value);

		public void passValue();

	}

	private List<Listener> listeners = new ArrayList<Listener>();

	int counter = 0;

	private long lastStepTime = 0;
	private long[] lastStepDeltas = { -1, -1, -1, -1 };
	private int lastStepDeltasIndex = 0;
	private int pace = 0;

	public PaceNotifier() {
		reloadSettings();
	}

	public void setPace(int pace) {
		this.pace = pace;
		int avg = (int) (60 * 1000.0 / pace);
		for (int i = 0; i < lastStepDeltas.length; i++) {
			lastStepDeltas[i] = avg;
		}
		notifyListener();
	}

	public void reloadSettings() {
		notifyListener();
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void onStep() {
		counter++;

		// Calculate pace based on last x steps
		if (lastStepTime > 0) {
			long now = System.currentTimeMillis();
			long delta = now - lastStepTime;

			lastStepDeltas[lastStepDeltasIndex] = delta;
			lastStepDeltasIndex = (lastStepDeltasIndex + 1)
					% lastStepDeltas.length;

			long sum = 0;
			boolean isMeaningfull = true;
			for (int i = 0; i < lastStepDeltas.length; i++) {
				if (lastStepDeltas[i] < 0) {
					isMeaningfull = false;
					break;
				}
				sum += lastStepDeltas[i];
			}
			if (isMeaningfull) {
				long avg = sum / lastStepDeltas.length;
				pace = (int) (60 * 1000 / avg);
			} else {
				pace = -1;
			}
		}
		lastStepTime = System.currentTimeMillis();
		notifyListener();
	}

	private void notifyListener() {
		for (Listener listener : listeners) {
			listener.paceChanged(pace);
		}
	}

	public void passValue() {
		// Not used
	}

}
