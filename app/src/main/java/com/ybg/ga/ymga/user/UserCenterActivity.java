/**
 * 
 */
package com.ybg.ga.ymga.user;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.preference.XYPreference;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ui.ClipImageActivity;
import com.ybg.ga.ymga.util.AppConstat;

import java.io.FileNotFoundException;

/**
 * @author 杨拔纲
 *
 */
public class UserCenterActivity extends AppCompatActivity {

	private UserPreferences userPreferences = UserPreferences.getInstance();
	private TZPreference tzPreference = TZPreference.getInstance();
	private XYPreference xyPreference = XYPreference.getInstance();
	private YdPreference ydPreference = YdPreference.getInstance();
	private TWPreference twPreference = TWPreference.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();

	private UserSettingService userSettingService;
	private Intent bindIntent;

	private TextView userNameView = null;
	private ImageView userImgView = null;
	private TextView ydPjName = null;
	private TextView tzPjName = null;
	private TextView xyPjName = null;
	private TextView twPjName = null;

	private Button ydPjButton = null;
	private Button tzPjButton = null;
	private Button xyPjButton = null;
	private Button twPjButton = null;

	private final int START_ALBUM_REQUESTCODE = 1;
	private final int CAMERA_WITH_DATA = 2;
	private final int CROP_RESULT_CODE = 3;

	private ProgressDialog progressDialog = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_center);
		initView();

		Toolbar toolbar = (Toolbar) findViewById(R.id.userCenterToolbar);
		setSupportActionBar(toolbar);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		bindIntent = new Intent(UserCenterActivity.this, UserSettingService.class);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);

		// 注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("UPLOAD_USERIMG");
		registerReceiver(broadcastReceiver, intentFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		unregisterReceiver(broadcastReceiver);
		unbindService(mConnection);
	}

	private void initView() {
		// 设置用户名
		userImgView = (ImageView) findViewById(R.id.imageView1);
		userNameView = (TextView) findViewById(R.id.userCenterName);
		userNameView.setText(userPreferences.getName());
		String img = userPreferences.getUserHeadImg();
		if ("0".equals(img)) {
			userImgView.setImageResource(R.mipmap.default_uimg);
		} else {
			ImageLoader.getInstance().displayImage(AppConstat.APP_HOST + img, userImgView);
		}

		// 读取配件名称，激活解除绑定按钮
		ydPjName = (TextView) findViewById(R.id.ydPjName);
		ydPjButton = (Button) findViewById(R.id.ydPjButton);
		if (ydPreference.hasAssign()) {
			ydPjName.setText("手环：" + ydPreference.getYdDeviceName());
			ydPjButton.setEnabled(true);
		}

		tzPjName = (TextView) findViewById(R.id.tzPjName);
		tzPjButton = (Button) findViewById(R.id.tzPjButton);
		if (tzPreference.hasAssign()) {
			tzPjName.setText("电子秤：" + tzPreference.getTzDeviceName());
			tzPjButton.setEnabled(true);
		}

		xyPjName = (TextView) findViewById(R.id.xyPjName);
		xyPjButton = (Button) findViewById(R.id.xyPjButton);
		if (xyPreference.hasAssign()) {
			xyPjName.setText("血压计：" + xyPreference.getXyDeviceName());
			xyPjButton.setEnabled(true);
		}

		twPjName = (TextView) findViewById(R.id.twPjName);
		twPjButton = (Button) findViewById(R.id.twPjButton);
		if (twPreference.hasAssign()) {
			twPjName.setText("体温计：" + twPreference.getTwDeviceName());
			twPjButton.setEnabled(true);
		}
	}

	public void enterSetting(View view) {
		Intent intent = new Intent(this, UserSettingActivity.class);
		startActivity(intent);
	}

	public void removeXYDevice(View view) {
		xyPreference.setXyDeviceAddr("");
		xyPreference.setXyDeviceModel("");
		xyPreference.setXyDeviceName("");
		xyPreference.setHasAssign(false);
		xyPjButton.setEnabled(false);
	}

	public void removeTZDevice(View view) {
		tzPreference.setTzDeviceAddr("");
		tzPreference.setTzDeviceModel("");
		tzPreference.setTzDeviceName("");
		tzPreference.setHasAssign(false);
		tzPjButton.setEnabled(false);
	}

	public void removeYDDevice(View view) {
		ydPreference.setYdDeviceAddr("");
		ydPreference.setYdDeviceModel("");
		ydPreference.setYdDeviceName("");
		ydPreference.setHasAssign(false);
		ydPjButton.setEnabled(false);
	}

	public void removeTWDevice(View view) {
		twPreference.setTwDeviceAddr("");
		twPreference.setTwDeviceModel("");
		twPreference.setTwDeviceName("");
		twPreference.setHasAssign(false);
		twPjButton.setEnabled(false);
	}

	public void startAlbum(View view) {
		try {
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
			intent.setType("image/*");
			startActivityForResult(intent, START_ALBUM_REQUESTCODE);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
			try {
				Intent intent = new Intent(Intent.ACTION_PICK, null);
				intent.setDataAndType(
						MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
				startActivityForResult(intent, START_ALBUM_REQUESTCODE);
			} catch (Exception e2) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// String result = null;
		if (resultCode != RESULT_OK) {
			return;
		}

		switch (requestCode) {
			case CROP_RESULT_CODE:
				String path = data.getStringExtra(ClipImageActivity.RESULT_PATH);
				Bitmap photo = BitmapFactory.decodeFile(path);
				userImgView.setImageBitmap(photo);
				//upload userImg if haslogin
				if (userPreferences.hasLogin() && userSettingService != null) {
					progressDialog = ybgApp.getProgressDialog(UserCenterActivity.this,
							"正在上传头像，请稍候...");
					progressDialog.show();
					userSettingService.uploadUserImg(path);
				}
				break;
			case START_ALBUM_REQUESTCODE:
				startCropImageActivity(getFilePath(data.getData()));
				break;
			case CAMERA_WITH_DATA:
				// 照相机程序返回的,再次调用图片剪辑程序去修剪图片
				startCropImageActivity(Environment.getExternalStorageDirectory() + "/" + ClipImageActivity.TMP_PATH);
				break;
		}
	}

	// 裁剪图片的Activity
	private void startCropImageActivity(String path) {
		ClipImageActivity.startActivity(this, path, CROP_RESULT_CODE);
	}

	public String getFilePath(Uri mUri) {
		try {
			if (mUri.getScheme().equals("file")) {
				return mUri.getPath();
			} else {
				return getFilePathByUri(mUri);
			}
		} catch (FileNotFoundException ex) {
			return null;
		}
	}

	// 获取文件路径通过url
	private String getFilePathByUri(Uri mUri) throws FileNotFoundException {
		Cursor cursor = getContentResolver()
				.query(mUri, null, null, null, null);
		cursor.moveToFirst();
		return cursor.getString(1);
	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			userSettingService = ((UserSettingService.UserSettingBinder) service).getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			userSettingService = null;
		}
	};

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if ("UPLOAD_USERIMG".equals(action)) {
				String msg = intent.getExtras().getString("msg");
				ybgApp.showMessage(UserCenterActivity.this.getApplicationContext(), msg);
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			}
		}
	};
}
