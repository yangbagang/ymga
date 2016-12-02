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
public class StepDisplayer implements StepListener {

	private int count = 0;

	public StepDisplayer() {
		notifyListener();
	}

	public void setSteps(int steps) {
		count = steps;
		notifyListener();
	}

	public void onStep() {
		count++;
		notifyListener();
	}

	public void reloadSettings() {
		notifyListener();
	}

	public void passValue() {

	}

	public interface Listener {

		public void stepsChanged(int value);

		public void passValue();

	}

	private List<Listener> listeners = new ArrayList<Listener>();

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.remove(l);
	}

	public void notifyListener() {
		for (Listener listener : listeners) {
			listener.stepsChanged(count);
		}
	}

}
