/**
 *
 */
package com.ybg.ga.ymga.user;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

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
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.util.AppConstat;

/**
 * @author 杨拔纲
 */
public class LocateMeActivity extends AppCompatActivity {

    private YbgApp ybgApp = YbgApp.getInstance();

    private MapView mapView = null;
    private LocationClient locationClient = null;
    private MyLocationListener myLocationListener = null;
    private BaiduMap baiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 此句需要在setContentView之前
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.location_me);

        Toolbar toolbar = (Toolbar) findViewById(R.id.locationMeToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 百度地图定位初始化
        mapView = (MapView) findViewById(R.id.bmapView);
        locationClient = new LocationClient(getApplicationContext());
        myLocationListener = new MyLocationListener();

        // 微信
        wxApi = WXAPIFactory.createWXAPI(this, AppConstat.WX_APP_ID);
        wxApi.registerApp(AppConstat.WX_APP_ID);

        //api 23 permission check
        String message = getString(R.string.permission_request_notice, getString(R.string
                .app_name), getString(R.string.permission_read_phone_state));
        ybgApp.checkPermission(this, Manifest.permission.READ_PHONE_STATE, message, AppConstat
                .PERMISSION_REQUEST_CODE_READ_PHONE_STATE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == AppConstat.PERMISSION_REQUEST_CODE_READ_PHONE_STATE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //do nothing
            } else {
                String message = getString(R.string.permission_request_notice, getString(R.string
                        .app_name), getString(R.string.permission_read_phone_state));
                String notice = getString(R.string.permission_setting_notice, getString(R.string
                        .app_name));
                ybgApp.showMessage(getApplicationContext(), message + notice);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        baiduMap.setMyLocationEnabled(false);
        locationClient.unRegisterLocationListener(myLocationListener);
        locationClient.stop();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
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
    }

    @Override
    protected void onPause() {
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
            y = location.getLatitude();
            x = location.getLongitude();
            //System.out.println("x=" + x);
            //System.out.println("y=" + y);
            // 移动到地图中间并设置缩放级别
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                    .newLatLngZoom(point, 18);
            baiduMap.animateMapStatus(mapStatusUpdate);
            locationClient.unRegisterLocationListener(myLocationListener);
        }

    }

    /**
     * 微信分享
     *
     * @param view
     */
    public void shareWeixin(View view) {
        wechatShare(0);
    }

    /**
     * 分享到朋友圈
     *
     * @param view
     */
    public void shareFriend(View view) {
        wechatShare(1);
    }

    private void wechatShare(int flag) {
        // int flag = 0;//0分享到微信好友,1分享到微信朋友圈
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = AppConstat.APP_HOST + "/user/location?x=" + x + "&y=" + y;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "雍美关爱，我在这里！";
        // 研究实现增加“这一刻的想法”
        msg.description = "您的好友给您分享了他(她)的位置。";
        // 这里替换一张自己工程里的图片资源
        Bitmap thumb = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ga);
        msg.setThumbImage(thumb);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = String.valueOf(System.currentTimeMillis());
        req.message = msg;
        req.scene = flag == 0 ? SendMessageToWX.Req.WXSceneSession
                : SendMessageToWX.Req.WXSceneTimeline;
        wxApi.sendReq(req);
    }

    private double x = 0;
    private double y = 0;
    // 微信分享
    private IWXAPI wxApi;
}
