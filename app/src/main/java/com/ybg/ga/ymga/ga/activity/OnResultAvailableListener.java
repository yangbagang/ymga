/**
 * 
 */
package com.ybg.ga.ymga.ga.activity;

import android.content.Intent;

/**
 * @author 杨拔纲
 *
 */
public interface OnResultAvailableListener {

	public void setActivityResult(int requestCode, int resultCode, Intent data);
	
}
