/**
 *
 */
package com.ybg.ga.ymga.ga.pedometer;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.ga.pedometer.bean.BaiduGPS;
import com.ybg.ga.ymga.ga.preference.YdPreference;
import com.ybg.ga.ymga.ga.yd.YDDataService;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.TimeUtil;

/**
 * @author 杨拔纲
 */
public class PedometerActivity extends AppCompatActivity {

    private YdPreference ydPreference = YdPreference.getInstance();

    private YbgApp ybgApp = YbgApp.getInstance();

    // 总步数
    private TextView stepValueView;
    // 每分钟步数
    private TextView paceValueView;
    // 总路程
    private TextView distanceValueView;
    // 时速
    private TextView speedValueView;
    // 总卡路里数
    private TextView caloriesValueView;
    // 目标达成率
    private TextView finishValueView;

    // 计数数据值
    private int stepValue;
    private int paceValue;
    private float distanceValue;
    private float speedValue;
    private int caloriesValue;
    private float aimStep;

    private boolean isRunning;

    // 百度地图相关组件
    private MapView mapView = null;
    private BaiduMap baiduMap;
    private int ptsIndex = 0;

    private Intent bindIntent = null;
    private YDDataService ydDataService = null;
    private LinearLayout yd_gps_button = null;

    // 计时
    private TextView timerView = null;
    private int timerValue = 0;
    private int ydLocaleId;

    // 微信分享
    private IWXAPI wxApi;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始数据为0
        stepValue = 0;
        paceValue = 0;
        distanceValue = 0;
        speedValue = 0;
        caloriesValue = 0;
        aimStep = ydPreference.getAimSteps();

        // 百度地图初始化
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.yd_gps_pedometer);

        // 获取地图控件引用
        mapView = (MapView) findViewById(R.id.ydGPSRouter);

        // 启动计步服务
        bindStepService();

        // bind service
        bindIntent = new Intent(PedometerActivity.this, YDDataService.class);
        bindService(bindIntent, dataConnection, Context.BIND_AUTO_CREATE);

        // 微信
        wxApi = WXAPIFactory.createWXAPI(this, AppConstat.WX_APP_ID);
        wxApi.registerApp(AppConstat.WX_APP_ID);

        Toolbar toolbar = (Toolbar) findViewById(R.id.ydGPSPedometerToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        stepValueView = (TextView) findViewById(R.id.pedometerStepValue);
        paceValueView = (TextView) findViewById(R.id.pedometerPaceValue);
        distanceValueView = (TextView) findViewById(R.id.pedometerDistanceValue);
        speedValueView = (TextView) findViewById(R.id.pedometerSpeedValue);
        caloriesValueView = (TextView) findViewById(R.id.pedometerCaloriesValue);
        finishValueView = (TextView) findViewById(R.id.pedometerFinishValue);
        timerView = (TextView) findViewById(R.id.pedometerTimeValue);
        yd_gps_button = (LinearLayout) findViewById(R.id.yd_gps_button);

        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
        mapView.showScaleControl(false);
        mapView.showZoomControls(false);
        baiduMap = mapView.getMap();
        // 开启定位图层
        baiduMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        unbindService(dataConnection);
        super.onDestroy();

        // 关闭时停止计步
        stopStepService();
        unbindStepService();

        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.yd_gps_pedometer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_switch) {
            if (yd_gps_button.getVisibility() == View.VISIBLE) {
                yd_gps_button.setVisibility(View.GONE);
            } else {
                yd_gps_button.setVisibility(View.VISIBLE);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ServiceConnection dataConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ydDataService = ((YDDataService.YDDataBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ydDataService = null;
        }

    };

    private long currentTime = 0L;

    @Override
    public void onBackPressed() {
        if (isRunning) {
            if (currentTime == 0L) {
                // 第一次按
                ybgApp.showMessage(getApplicationContext(),
                        "退出此界面将无法计步，再按一次将退出并停止计步。");
                currentTime = System.currentTimeMillis();
            } else if ((System.currentTimeMillis() - currentTime) > 10 * 1000) {
                // 第二次按，但超过10秒，重新提示。
                ybgApp.showMessage(getApplicationContext(),
                        "退出此界面将无法计步，再按一次将退出并停止计步。");
                currentTime = System.currentTimeMillis();
            } else {
                // 第二次且在10少内
                super.onBackPressed();
            }
        } else {
            // 非计步状态，直接退出。
            super.onBackPressed();
        }
    }

    /**
     * 停止或启动计步。启动时将重新计数。
     *
     * @param view
     */
    public void gpsPedometerStop(View view) {
        TextView gpsStopButton = (TextView) findViewById(R.id.gpsStopButton);
        String stop = getResources().getString(R.string.button_stop);
        String start = getResources().getString(R.string.button_start);
        if (stop.equals(gpsStopButton.getText().toString())) {
            // 停止计步
            stopStepService();
            // 插入数据库
            ydLocaleId = ydDataService.save(stepValue, distanceValue,
                    caloriesValue, timerValue, 0);
            gpsStopButton.setText(start);
            isFinish = true;
        } else {
            // 启动计步
            resetValues();
            gpsStopButton.setText(stop);
            startStepService();
            isFinish = false;
        }
        yd_gps_button.setVisibility(View.GONE);
    }

    /**
     * 结束计数并返回到计步主界面。
     *
     * @param view
     */
    public void gpsPedometerFinish(View view) {
        // 未结束运动时，结速动运并保存数据。
        if (!isFinish) {
            TextView gpsFinishButton = (TextView) findViewById(R.id.gpsFinishButton);
            gpsFinishButton.setEnabled(false);
            // 停止计步
            stopStepService();
            // 插入数据库
            ydLocaleId = ydDataService.save(stepValue, distanceValue,
                    caloriesValue, timerValue, 0);
            yd_gps_button.setVisibility(View.GONE);
            // 清除当前记录
            isFinish = true;
            // 更改按钮文字
            TextView gpsStopButton = (TextView) findViewById(R.id.gpsStopButton);
            String start = getResources().getString(R.string.button_start);
            gpsStopButton.setText(start);
        }
    }

    /**
     * 暂停或继续计步。继纽计步数将累加。
     *
     * @param view
     */
    public void gpsPedometerPause(View view) {
        TextView gpsPauseButton = (TextView) findViewById(R.id.gpsPauseButton);
        String pause = getResources().getString(R.string.button_pause);
        String resume = getResources().getString(R.string.button_resume);
        if (pause.equals(gpsPauseButton.getText().toString())) {
            // 暂停计步
            gpsPauseButton.setText(resume);
            stopStepService();
        } else {
            // 继续计步
            if (isFinish) {
                resetValues();
            }
            startStepService();
            gpsPauseButton.setText(pause);
        }
        yd_gps_button.setVisibility(View.GONE);
    }

    private boolean isFinish = false;

    private StepService stepService;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            stepService = ((StepService.StepBinder) service).getService();

            stepService.registerCallback(mCallback);
            stepService.reloadSettings();
            startStepService();
        }

        public void onServiceDisconnected(ComponentName className) {
            stepService = null;
        }
    };

    private void startStepService() {
        isRunning = true;
        // 初始化数据
        SharedPreferences state = getSharedPreferences("state", 0);
        SharedPreferences.Editor stateEditor = state.edit();
        stateEditor.putInt("steps", stepValue);
        stateEditor.putInt("pace", paceValue);
        stateEditor.putFloat("distance", distanceValue);
        stateEditor.putFloat("speed", speedValue);
        stateEditor.putFloat("calories", caloriesValue);
        stateEditor.putInt("timerValue", timerValue);
        stateEditor.commit();
        if (stepService != null) {
            stepService.startPedometer();
        }
    }

    private void bindStepService() {
        bindService(new Intent(this, StepService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void unbindStepService() {
        unbindService(mConnection);
    }

    private void stopStepService() {
        isRunning = false;
        if (stepService != null) {
            stepService.stopPedometer();
        }
    }

    private void resetValues() {
        if (stepService != null) {
            stepService.resetValues();
            stepValueView.setText("0");
            paceValueView.setText("0");
            distanceValueView.setText("0");
            speedValueView.setText("0");
            caloriesValueView.setText("0");
            finishValueView.setText("0");
            timerView.setText("0:00:00");
            // 初始数据为0
            stepValue = 0;
            paceValue = 0;
            distanceValue = 0;
            speedValue = 0;
            caloriesValue = 0;
            timerValue = 0;
        }
        // 清除生成的路径
        baiduMap.clear();
    }

    private StepService.ICallback mCallback = new StepService.ICallback() {
        private OverlayOptions polylineOptions = null;
        private List<LatLng> points = null;
        private LatLng prePoint = null;
        private LatLng curPoint = null;

        public void stepsChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
        }

        public void paceChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
        }

        public void distanceChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG,
                    (int) (value * 1000), 0));
        }

        public void speedChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG,
                    (int) (value * 1000), 0));
        }

        public void caloriesChanged(float value) {
            mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG,
                    (int) (value), 0));
        }

        public void timerChanged(int timerValue) {
            mHandler.sendMessage(mHandler.obtainMessage(TIMER_MSG, timerValue,
                    0));
        }

        @Override
        public void locationChanged(List<BaiduGPS> list) {
            if (isRunning && list != null && list.size() > 0) {
                // 取得后台传来定位数据
                BaiduGPS bdGPS = null;
                // 遍历数姐
                while (ptsIndex < list.size()) {
                    bdGPS = list.get(ptsIndex);
                    // 如果是硬件GPS数据，则开始划线操作。
                    if (bdGPS.isGPSLocation()) {
                        if (prePoint == null) {
                            // 首次取得硬件GPS数据
                            prePoint = bdGPS.getLatLng();
                        } else if (curPoint == null) {
                            // 第二次取得硬件GPS数据，划一条线
                            curPoint = bdGPS.getLatLng();
                            drawLine();
                        } else {
                            // 后续数据，更新节点，再划线。
                            prePoint = curPoint;
                            curPoint = bdGPS.getLatLng();
                            drawLine();
                        }
                    }
                    if (ptsIndex % 5 == 0) {
                        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory
                                .newLatLngZoom(list.get(ptsIndex).getLatLng(), 16);
                        baiduMap.animateMapStatus(mapStatusUpdate);
                    }
                    ptsIndex++;
                }
            }
        }

        /**
         * 画线
         */
        private void drawLine() {
            points = new ArrayList<LatLng>();
            points.add(prePoint);
            points.add(curPoint);
            polylineOptions = new PolylineOptions().points(points).color(
                    0xffff0000);
            baiduMap.addOverlay(polylineOptions);
        }
    };

    private static final int STEPS_MSG = 1;
    private static final int PACE_MSG = 2;
    private static final int DISTANCE_MSG = 3;
    private static final int SPEED_MSG = 4;
    private static final int CALORIES_MSG = 5;
    private static final int TIMER_MSG = 0;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (isRunning) {
                switch (msg.what) {
                    case STEPS_MSG:
                        stepValue = (int) msg.arg1;
                        stepValueView.setText("" + stepValue);
                        finishValueView.setText((stepValue * 100) / aimStep + "%");
                        break;
                    case PACE_MSG:
                        paceValue = msg.arg1;
                        if (paceValue <= 0) {
                            paceValueView.setText("0");
                        } else {
                            paceValueView.setText("" + (int) paceValue);
                        }
                        break;
                    case DISTANCE_MSG:
                        distanceValue = ((int) msg.arg1) / 1000f;
                        if (distanceValue <= 0) {
                            distanceValueView.setText("0");
                        } else {
                            distanceValueView
                                    .setText(("" + (distanceValue + 0.000001f))
                                            .substring(0, 5));
                        }
                        break;
                    case SPEED_MSG:
                        speedValue = ((int) msg.arg1) / 1000f;
                        if (speedValue <= 0) {
                            speedValueView.setText("0");
                        } else {
                            speedValueView.setText(("" + (speedValue + 0.000001f))
                                    .substring(0, 4));
                        }
                        break;
                    case CALORIES_MSG:
                        caloriesValue = msg.arg1;
                        if (caloriesValue <= 0) {
                            caloriesValueView.setText("0");
                        } else {
                            caloriesValueView.setText("" + caloriesValue);
                        }
                        break;
                    case TIMER_MSG:
                        timerValue = msg.arg1;
                        timerView.setText(TimeUtil.getHourAndMinutes(timerValue));
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }
        }

    };

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
        if (isRunning) {
            ybgApp.showMessage(getApplicationContext(), "请先完成运动再点分享。");
            return;
        }
        long remoteId = ydDataService.getRemoteId(ydLocaleId);
        if (remoteId == 0) {
            ybgApp.showMessage(getApplicationContext(), "您当前的运动数据未上传，不能分享。");
            return;
        }
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = AppConstat.APP_HOST + "/yd/share2?id=" + remoteId;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = "雍美关爱，运动分享！";
        // 研究实现增加“这一刻的想法”
        msg.description = "您的好友给您分享了一次运动。";
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

}
