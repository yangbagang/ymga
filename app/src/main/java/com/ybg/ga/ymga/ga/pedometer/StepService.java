/**
 *
 */
package com.ybg.ga.ymga.ga.pedometer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.ga.pedometer.bean.BaiduGPS;

/**
 * @author 杨拔纲
 */
public class StepService extends Service {

    private SharedPreferences settings;
    private SharedPreferences state;
    private SensorManager sensorManager;
    private StepDetector stepDetector;
    private StepDisplayer stepDisplayer;
    private PaceNotifier paceNotifier;
    private DistanceNotifier distanceNotifier;
    private SpeedNotifier speedNotifier;
    private CaloriesNotifier caloriesNotifier;

    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;

    private int steps;
    private int pace;
    private float distance;
    private float speed;
    private float calories;
    private int timerValue;
    private Timer timer = null;
    private TimerTask task = null;

    // 定位
    private LocationClient locationClient = null;
    private TraceLocationListener myLocationListener = null;
    private List<BaiduGPS> list = null;

    public class StepBinder extends Binder {
        StepService getService() {
            return StepService.this;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm
                .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepService");
        wakeLock.acquire();

        // Load settings
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        state = getSharedPreferences("state", 0);

        // Start detecting
        stepDetector = new StepDetector();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(stepDetector,
                SensorManager.SENSOR_ACCELEROMETER
                        | SensorManager.SENSOR_MAGNETIC_FIELD
                        | SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
        reloadSettings();

        // 开始定位
        locationClient = new LocationClient(this);
        myLocationListener = new TraceLocationListener();
        list = new ArrayList<BaiduGPS>();
        initLocation();
    }

    public void startPedometer() {
        stepDisplayer = new StepDisplayer();
        stepDisplayer.setSteps(steps = state.getInt("steps", 0));
        stepDisplayer.addListener(stepListener);
        stepDetector.addStepListener(stepDisplayer);

        paceNotifier = new PaceNotifier();
        paceNotifier.setPace(pace = state.getInt("pace", 0));
        paceNotifier.addListener(paceListener);
        stepDetector.addStepListener(paceNotifier);

        distanceNotifier = new DistanceNotifier(distanceListener);
        distanceNotifier.setDistance(distance = state.getFloat("distance", 0));
        stepDetector.addStepListener(distanceNotifier);

        speedNotifier = new SpeedNotifier(speedListener);
        speedNotifier.setSpeed(speed = state.getFloat("speed", 0));
        paceNotifier.addListener(speedNotifier);

        caloriesNotifier = new CaloriesNotifier(caloriesListener);
        caloriesNotifier.setCalories(calories = state.getFloat("calories", 0));
        stepDetector.addStepListener(caloriesNotifier);

        timerValue = state.getInt("timerValue", 0);
        // 启动计时器
        task = new TimerTask() {
            public void run() {
                timerValue++;
                if (mCallback != null) {
                    mCallback.timerChanged(timerValue);
                }
            }
        };
        timer = new Timer(true);
        timer.schedule(task, 1000, 1000);

        locationClient.registerLocationListener(myLocationListener);
        if (!locationClient.isStarted()) {
            locationClient.requestLocation();
            locationClient.start();
        }
    }

    public void stopPedometer() {
        stepDetector.removeStepListener(stepDisplayer);
        stepDisplayer = null;

        stepDetector.removeStepListener(paceNotifier);
        paceNotifier = null;

        stepDetector.removeStepListener(distanceNotifier);
        distanceNotifier = null;

        speedNotifier = null;

        stepDetector.removeStepListener(caloriesNotifier);
        caloriesNotifier = null;

        if (timer != null) {
            timer.cancel();
        }
        timer = null;
        task = null;

        locationClient.unRegisterLocationListener(myLocationListener);
        if (locationClient.isStarted()) {
            locationClient.stop();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        // 关闭定位
        locationClient.unRegisterLocationListener(myLocationListener);
        locationClient.stop();

        notificationManager.cancel(R.string.app_name);

        wakeLock.release();

        super.onDestroy();

        // Stop detecting
        sensorManager.unregisterListener(stepDetector);

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new StepBinder();

    public interface ICallback {
        public void stepsChanged(int value);

        public void paceChanged(int value);

        public void distanceChanged(float value);

        public void speedChanged(float value);

        public void caloriesChanged(float value);

        public void timerChanged(int timerValue);

        public void locationChanged(List<BaiduGPS> list);
    }

    private ICallback mCallback;

    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    public void reloadSettings() {
        settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (stepDetector != null) {
            stepDetector.setSensitivity(Integer.valueOf(settings.getString(
                    "sensitivity", "30")));
        }

        if (stepDisplayer != null)
            stepDisplayer.reloadSettings();
        if (paceNotifier != null)
            paceNotifier.reloadSettings();
        if (distanceNotifier != null)
            distanceNotifier.reloadSettings();
        if (speedNotifier != null)
            speedNotifier.reloadSettings();
        if (caloriesNotifier != null)
            caloriesNotifier.reloadSettings();

    }

    public void resetValues() {
        state.edit().putInt("steps", 0);
        state.edit().putInt("pace", 0);
        state.edit().putInt("distance", 0);
        state.edit().putInt("speed", 0);
        state.edit().putInt("calories", 0);
        state.edit().commit();
        timerValue = 0;
        list.clear();
    }

    /**
     * Forwards pace values from PaceNotifier to the activity.
     */
    private StepDisplayer.Listener stepListener = new StepDisplayer.Listener() {
        public void stepsChanged(int value) {
            steps = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.stepsChanged(steps);
            }
        }
    };
    /**
     * Forwards pace values from PaceNotifier to the activity.
     */
    private PaceNotifier.Listener paceListener = new PaceNotifier.Listener() {
        public void paceChanged(int value) {
            pace = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.paceChanged(pace);
            }
        }
    };
    /**
     * Forwards distance values from DistanceNotifier to the activity.
     */
    private DistanceNotifier.Listener distanceListener = new DistanceNotifier.Listener() {
        public void valueChanged(float value) {
            distance = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.distanceChanged(distance);
            }
        }
    };
    /**
     * Forwards speed values from SpeedNotifier to the activity.
     */
    private SpeedNotifier.Listener speedListener = new SpeedNotifier.Listener() {
        public void valueChanged(float value) {
            speed = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.speedChanged(speed);
            }
        }
    };
    /**
     * Forwards calories values from CaloriesNotifier to the activity.
     */
    private CaloriesNotifier.Listener caloriesListener = new CaloriesNotifier.Listener() {
        public void valueChanged(float value) {
            calories = value;
            passValue();
        }

        public void passValue() {
            if (mCallback != null) {
                mCallback.caloriesChanged(calories);
            }
        }
    };

    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
    private void showNotification() {
//        CharSequence text = getText(R.string.app_name);
//        Notification notification = new Notification(R.mipmap.yd_notify,
//                null, System.currentTimeMillis());
//        notification.flags = Notification.FLAG_NO_CLEAR
//                | Notification.FLAG_ONGOING_EVENT;
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, PedometerActivity.class), 0);
//        notification.setLatestEventInfo(this, text,
//                getText(R.string.notification_subtitle), contentIntent);
//        notification.
//
//        notificationManager.notify(R.string.app_name, notification);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, PedometerActivity.class), 0);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setAutoCancel(true)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.notification_subtitle))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.yd_notify)
                .setWhen(System.currentTimeMillis())
                .build();
        this.notificationManager.notify(R.string.app_name, notification);
    }

    // 定位回调
    private class TraceLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            list.add(new BaiduGPS(location.getLocType(),
                    location.getLatitude(), location.getLongitude()));
            mCallback.locationChanged(list);
        }

    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setLocationMode(LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度
        option.setScanSpan(1000 * 5);// 设置发起定位请求的间隔时间为5000ms
        option.setIsNeedAddress(false);
        locationClient.setLocOption(option);
    }
}
