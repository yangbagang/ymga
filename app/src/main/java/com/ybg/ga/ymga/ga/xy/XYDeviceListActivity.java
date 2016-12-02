/**
 * 
 */
package com.ybg.ga.ymga.ga.xy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 *
 */
public class XYDeviceListActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.xy_device_list);
	}

	public void selectXYDevice(View view) {
		Intent intent = new Intent();
		if(view.getId() == R.id.xyUrionLabel1) {
			intent.putExtra("xyDeviceName", "ABP-U80/60");
			intent.putExtra("xyDeviceModel", "urion");
		}
		setResult(AppConstat.XY_DEVICE_RESULT_CODE, intent);
		finish();

		Toolbar toolbar = (Toolbar) findViewById(R.id.xyDeviceListToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}
}
