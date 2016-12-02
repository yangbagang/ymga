/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * @author 杨拔纲
 *
 */
public class YdDeviceListActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.yd_device_list);

		Toolbar toolbar = (Toolbar) findViewById(R.id.ydDeviceListToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	public void selectYdDevice(View view) {
		Intent intent = new Intent();
		if(view.getId() == R.id.ydApLabel1) {
			intent.putExtra("ydDeviceName", "AP1708");
			intent.putExtra("ydDeviceModel", "ydap");
		} else if (view.getId() == R.id.ydApLabel2) {
			intent.putExtra("ydDeviceName", "AP2000");
			intent.putExtra("ydDeviceModel", "ydap");
		}
		setResult(AppConstat.YD_DEVICE_RESULT_CODE, intent);
		finish();
	}
	
}
