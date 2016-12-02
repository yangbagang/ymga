/**
 * 
 */
package com.ybg.ga.ymga.ga.tz;

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
public class TZDeviceListActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tz_device_list);

		Toolbar toolbar = (Toolbar) findViewById(R.id.tzDeviceListToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});
	}

	public void selectTZDevice(View view) {
		Intent intent = new Intent();
		if (view.getId() == R.id.tzLefu1) {
			intent.putExtra("tzDeviceName", "AS-CF/CW");
			intent.putExtra("tzDeviceModel", TzUtil.TZ_DEVICE_LEFU);
		} else if (view.getId() == R.id.tzFuruik1){
			intent.putExtra("tzDeviceName", "AS-F16");
			intent.putExtra("tzDeviceModel", TzUtil.TZ_DEVICE_FURUIK);
		}
		setResult(AppConstat.TZ_DEVICE_RESULT_CODE, intent);
		finish();
	}

}
