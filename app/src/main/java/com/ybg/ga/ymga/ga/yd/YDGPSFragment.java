/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import java.text.DecimalFormat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.ga.pedometer.PedometerActivity;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ui.CustomDialog;
import com.ybg.ga.ymga.user.UserPreferences;

/**
 * @author 杨拔纲
 * 
 */
public class YDGPSFragment extends Fragment {

	private YdPreference ydPreference = YdPreference.getInstance();
	private UserPreferences userPreference = UserPreferences.getInstance();
	private YbgApp ybgApp = YbgApp.getInstance();

	private EditText bodyWeightView = null;
	private EditText setpLengthView = null;
	private TextView sportTypeView = null;
	private LinearLayout pedometerStartButton = null;
	private ImageView subWeightImg = null;
	private ImageView addWeightImg = null;
	private ImageView subLengthImg = null;
	private ImageView addLengthImg = null;
	private ImageView changeTypeImg1 = null;
	private ImageView changeTypeImg2 = null;

	private MapView mapView = null;
	private LocationClient locationClient = null;
	private MyLocationListener myLocationListener = null;
	private BaiduMap baiduMap;
	private TextView gpsLevel = null;
	private int locatetype;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		SDKInitializer.initialize(getActivity().getApplicationContext());
		return inflater.inflate(R.layout.yd_gps_main, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// 初始化设置
		bodyWeightView = (EditText) getActivity().findViewById(
				R.id.ydGPSBodyWeight);
		setpLengthView = (EditText) getActivity().findViewById(
				R.id.ydGPSStepLength);
		sportTypeView = (TextView) getActivity().findViewById(
				R.id.ydGPSSportType);
		bodyWeightView.setText("" + userPreference.getBodyWeight());
		setpLengthView.setText("" + ydPreference.getStepLength());

		// 百度地图定位初始化
		mapView = (MapView) getActivity().findViewById(R.id.bmapView);
		gpsLevel = (TextView) getActivity().findViewById(
				R.id.locationMeGPSLevel);
		locationClient = new LocationClient(getActivity()
				.getApplicationContext());
		myLocationListener = new MyLocationListener();

		mapView.showScaleControl(false);
		mapView.showZoomControls(false);
		baiduMap = mapView.getMap();
		// 开启定位图层
		baiduMap.setMyLocationEnabled(true);
		initLocation();
		locationClient.registerLocationListener(myLocationListener);
		if (!locationClient.isStarted()) {
			locationClient.requestLocation();
			locationClient.start();
		}

		// 图片事件
		initImgEvent();
	}

	private void initImgEvent() {
		// 开始计步
		pedometerStartButton = (LinearLayout) getActivity().findViewById(
				R.id.pedometerStartButton);
		pedometerStartButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				beginPedomete();
			}

		});
		// 设置
		subWeightImg = (ImageView) getActivity()
				.findViewById(R.id.subWeightImg);
		addWeightImg = (ImageView) getActivity()
				.findViewById(R.id.addWeightImg);
		subLengthImg = (ImageView) getActivity()
				.findViewById(R.id.subLengthImg);
		addLengthImg = (ImageView) getActivity()
				.findViewById(R.id.addLengthImg);
		changeTypeImg1 = (ImageView) getActivity().findViewById(
				R.id.changeTypeImg1);
		changeTypeImg2 = (ImageView) getActivity().findViewById(
				R.id.changeTypeImg2);

		subWeightImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bodyWeightView.setText(""
						+ new DecimalFormat("#.#").format((Float
								.valueOf(bodyWeightView.getText().toString()) - 0.1f)));
			}

		});
		addWeightImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bodyWeightView.setText(""
						+ new DecimalFormat("#.#").format((Float
								.valueOf(bodyWeightView.getText().toString()) + 0.1f)));
			}

		});
		subLengthImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setpLengthView.setText(""
						+ new DecimalFormat("#.##").format((Float
								.valueOf(setpLengthView.getText().toString()) - 0.01f)));
			}

		});
		addLengthImg.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setpLengthView.setText(""
						+ new DecimalFormat("#.##").format((Float
								.valueOf(setpLengthView.getText().toString()) + 0.01f)));
			}

		});
		changeTypeImg1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switchSportType();
			}

		});
		changeTypeImg2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				switchSportType();
			}

		});
	}

	private void switchSportType() {
		String walk = getResources().getString(R.string.pedometerSportTypeWalk);
		String run = getResources().getString(R.string.pedometerSportTypeRun);
		String now = sportTypeView.getText().toString();
		if (walk.equals(now)) {
			sportTypeView.setText(run);
		} else {
			sportTypeView.setText(walk);
		}
	}

	private void beginPedomete() {
		// 保存参数
		userPreference.setBodyWeight(Float.valueOf(bodyWeightView.getText()
				.toString()));
		float stepLength = Float.valueOf(setpLengthView.getText().toString());
		if (stepLength < 0.3 || stepLength > 2) {
			ybgApp.showMessage(getActivity().getApplicationContext(), "步长数据有误。");
			return;
		}

		ydPreference.setStepLength(stepLength);
		String walk = getResources().getString(R.string.pedometerSportTypeWalk);
		String now = sportTypeView.getText().toString();
		if (walk.equals(now)) {
			ydPreference.setWalkSportType();
		} else {
			ydPreference.setRunSportType();
		}

		CustomDialog.Builder builder = null;
		if (locatetype != 61) {
			builder = new CustomDialog.Builder(getActivity());
			builder.setMessage("当前GPS信号弱，继续运动将无法较准确记录您的运动轨迹，是否继续？");
			builder.setTitle("GPS信号弱");
			builder.setPositiveButton("确定",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							// 设置你的操作事项
							// 开始计步，进入实时计步界面
							Intent intent = new Intent(getActivity(),
									PedometerActivity.class);
							startActivity(intent);
						}
					});
			builder.setNegativeButton("取消",
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		} else {
			// 开始计步，进入实时计步界面
			Intent intent = new Intent(getActivity(), PedometerActivity.class);
			startActivity(intent);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		baiduMap.setMyLocationEnabled(false);
		locationClient.unRegisterLocationListener(myLocationListener);
		locationClient.stop();
		mapView.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mapView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mapView.onPause();
		baiduMap.setMyLocationEnabled(false);
	}

	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);
		option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(false);
		locationClient.setLocOption(option);
	}

	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			locatetype = location.getLocType();
			if (locatetype == 61) {
				gpsLevel.setText("强");
				gpsLevel.setTextColor(0xff23ac38);
			} else if (locatetype == 65 || locatetype == 161) {
				gpsLevel.setText("弱");
				gpsLevel.setTextColor(0xffff0000);
			} else {
				gpsLevel.setText("无");
				gpsLevel.setTextColor(0xffff0000);
			}
			// 构造定位数据
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			// 设置定位数据
			baiduMap.setMyLocationData(locData);
			// 定义定位坐标点
			LatLng point = new LatLng(location.getLatitude(),
					location.getLongitude());
			// 移动到地图中间并设置缩放级别
			MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
					.newLatLngZoom(point, 15);
			baiduMap.animateMapStatus(mapStatusUpdate);
			//locationClient.unRegisterLocationListener(myLocationListener);
		}

	}
}
