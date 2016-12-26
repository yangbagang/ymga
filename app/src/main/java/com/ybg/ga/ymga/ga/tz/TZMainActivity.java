package com.ybg.ga.ymga.ga.tz;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.bt.BTMessage;
import com.ybg.ga.ymga.bt.BTStatus;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.TZPreference;
import com.ybg.ga.ymga.ga.tz.furuik.TZFurikBLEActivity;
import com.ybg.ga.ymga.ga.tz.lefu.TZLefuBLEActivity;
import com.ybg.ga.ymga.ga.tz.lefu.TZLefuBTActivity;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 杨拔纲
 */
@SuppressLint("NewApi")
public class TZMainActivity extends SubActivity {

    private TZPreference tzPreference = TZPreference.getInstance();
    private UserPreferences userPreference = UserPreferences.getInstance();
    private YbgApp ybgApp = YbgApp.getInstance();
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private TextView tzPJName = null;
    private Button tzPJOperator = null;

    private TextView tzMeasureTime = null;

    private TextView tzValue = null;
    private TextView tzZFValue = null;
    private TextView tzJRValue = null;
    private TextView tzSFValue = null;
    private TextView tzBMIValue = null;
    private TextView tzQZValue = null;
    private TextView tzGGValue = null;
    private TextView tzNZValue = null;
    private TextView tzJCValue = null;
    private TextView tzSTValue = null;

    private ImageView tzZFImage = null;
    private ImageView tzJRImage = null;
    private ImageView tzBMIImage = null;
    private ImageView tzQZImage = null;
    private ImageView tzSFImage = null;
    private ImageView tzGGImage = null;
    private ImageView tzNZImage = null;
    private ImageView tzJCImage = null;
    private ImageView tzSTImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tz_main);

        initView();
        initEvent();

        Toolbar toolbar = (Toolbar) findViewById(R.id.tzMainToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void initView() {
        // 标签及按钮
        tzPJName = (TextView) findViewById(R.id.tzPJName);
        tzPJOperator = (Button) findViewById(R.id.tzPJOperator);
        tzMeasureTime = (TextView) findViewById(R.id.tzMeasureTimeLabel);
        // 各项数据
        tzValue = (TextView) findViewById(R.id.tzValue);
        tzZFValue = (TextView) findViewById(R.id.tzZFValue);
        tzJRValue = (TextView) findViewById(R.id.tzJRValue);
        tzSFValue = (TextView) findViewById(R.id.tzSFValue);
        tzBMIValue = (TextView) findViewById(R.id.tzBMIValue);
        tzQZValue = (TextView) findViewById(R.id.tzQZValue);
        tzGGValue = (TextView) findViewById(R.id.tzGGValue);
        tzNZValue = (TextView) findViewById(R.id.tzNZValue);
        tzJCValue = (TextView) findViewById(R.id.tzJCValue);
        tzSTValue = (TextView) findViewById(R.id.tzSTValue);
        // 各项数据是否正常的图
        tzZFImage = (ImageView) findViewById(R.id.tzZFImage);
        tzJRImage = (ImageView) findViewById(R.id.tzJRImage);
        tzBMIImage = (ImageView) findViewById(R.id.tzBMIImage);
        tzQZImage = (ImageView) findViewById(R.id.tzQZImage);
        tzSFImage = (ImageView) findViewById(R.id.tzSFImage);
        tzGGImage = (ImageView) findViewById(R.id.tzGGImage);
        tzNZImage = (ImageView) findViewById(R.id.tzNZImage);
        tzJCImage = (ImageView) findViewById(R.id.tzJCImage);
        tzSTImage = (ImageView) findViewById(R.id.tzSTImage);
    }

    private void initEvent() {
        if (tzPreference.hasAssign()) {
            // 己绑定，显示设备名称。准备连接操作
            tzPJName.setText(tzPreference.getTzDeviceName());
            tzPJOperator
                    .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
        } else {
            // 未绑定，提示需要绑定。准备进行设备绑定操作。
            tzPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_ASSIGN]);
            tzPJOperator
                    .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN]);
        }
    }

    public void tzOperation(View view) {
        String operator = ((Button) view).getText().toString();
        if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_ASSIGN].equals(operator)) {
            // 还未绑定，开始绑定过程
            Intent intent = new Intent(this, TZDeviceListActivity.class);
            getParent().startActivityForResult(intent,
                    AppConstat.TZ_DEVICE_REQUEST_CODE);
        } else if (BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START].equals(operator)) {
            startMeasure();
        }
    }

    private void startMeasure() {
        String model = tzPreference.getTzDeviceModel();
        if (TzUtil.TZ_DEVICE_FURUIK.equalsIgnoreCase(model)) {
            Intent intent = new Intent(this, TZFurikBLEActivity.class);
            getParent().startActivityForResult(intent, AppConstat.TZ_MEASURE_REQUEST_CODE);
        } else if (TzUtil.TZ_DEVICE_LEFU_BLE.equalsIgnoreCase(model)) {
            Intent intent = new Intent(this, TZLefuBLEActivity.class);
            getParent().startActivityForResult(intent, AppConstat.TZ_MEASURE_REQUEST_CODE);
        } else if (TzUtil.TZ_DEVICE_LEFU_BT.equalsIgnoreCase(model)) {
            Intent intent = new Intent(this, TZLefuBTActivity.class);
            getParent().startActivityForResult(intent, AppConstat.TZ_MEASURE_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppConstat.TZ_DEVICE_REQUEST_CODE
                && resultCode == AppConstat.TZ_DEVICE_RESULT_CODE) {
            // 体重设备列表
            String tzDeviceName = data.getExtras().getString("tzDeviceName");
            String tzDeviceModel = data.getExtras().getString("tzDeviceModel");
            if (!"".equals(tzDeviceName) && !"".equals(tzDeviceModel)) {
                // 记录下设备名称及代号
                tzPreference.setTzDeviceName(tzDeviceName);
                tzPreference.setTzDeviceModel(tzDeviceModel);
                // 修改状态，准备连接
                tzPJName.setText(BTStatus.BT_LABELS[BTStatus.BT_STATU_NOT_START]);
                tzPJOperator
                        .setText(BTStatus.BT_BUTTONS[BTStatus.BT_STATU_NOT_START]);
            }
            startMeasure();
        } else if (requestCode == AppConstat.TZ_MEASURE_REQUEST_CODE
                && resultCode == AppConstat.TZ_MEASURE_RESULT_CODE) {
            TzBean tzBean = (TzBean) data.getSerializableExtra("tzBean");
            processTzData(tzBean);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开启蓝牙
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();
        if (bluetoothAdapter == null) {
            ybgApp.showMessage(getApplicationContext(),
                    BTMessage.BLUETOOTH_ADAPTER_NOTFOUND);
        } else {
            // 如未开启，则先开启
            if (!bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.enable();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, TZSettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void processTzData(TzBean tzBean) {
        tzMeasureTime.setVisibility(View.VISIBLE);
        tzMeasureTime.setText("测量时间：" + sdf.format(new Date()));
        if (tzBean != null) {
            tzValue.setText(new DecimalFormat("#.##").format(tzBean.getTzValue()));
            // 保存体重至preference
            userPreference.setBodyWeight(tzBean.getTzValue());
            // 瘦 　　　标准　　　　 轻度　　　　肥胖
            // 男性 不足10％ 　10～20％ 　　20～25％　　 25％以上
            // 女性 不足20％　 20～30％ 　　30～35％ 　　35％以上
            int sex = userPreference.getUserSex();
            float bodyFat = tzBean.getTzZFValue();
            if (sex == AppConstat.SEX_MALE) {
                if (bodyFat < 10) {
                    tzZFImage.setImageResource(R.mipmap.low);
                    tzQZImage.setImageResource(R.mipmap.normal);
                } else if (bodyFat > 20) {
                    tzZFImage.setImageResource(R.mipmap.high);
                    tzQZImage.setImageResource(R.mipmap.low);
                } else {
                    tzZFImage.setImageResource(R.mipmap.normal);
                    tzQZImage.setImageResource(R.mipmap.normal);
                }
            } else {
                if (bodyFat < 20) {
                    tzZFImage.setImageResource(R.mipmap.low);
                    tzQZImage.setImageResource(R.mipmap.normal);
                } else if (bodyFat > 30) {
                    tzZFImage.setImageResource(R.mipmap.high);
                    tzQZImage.setImageResource(R.mipmap.low);
                } else {
                    tzZFImage.setImageResource(R.mipmap.normal);
                    tzQZImage.setImageResource(R.mipmap.normal);
                }
            }
            tzZFValue.setText(new DecimalFormat("#.##").format(bodyFat));
            tzJRValue.setText(new DecimalFormat("#.##").format(tzBean.getTzJRValue()));
            tzJRImage.setImageResource(R.mipmap.normal);
            tzSFValue.setText(new DecimalFormat("#.##").format(tzBean.getTzSFValue()));
            tzSFImage.setImageResource(R.mipmap.normal);
            // 偏瘦： BMI指数 < 18 正常体重： BMI指数 = 18 - 25 超重： BMI指数 = 25 -
            // 30轻度肥胖： BMI指数 > 30中度肥胖： BMI指数 > 35重度肥胖： BMI指数 > 40
            float bodyHigh = userPreference.getBodyHigh();
            float bmi = 0f;
            if (bodyHigh != 0) {
                bmi = tzBean.getTzBMIValue();
                tzBMIValue.setText(new DecimalFormat("#.##").format(bmi));
                if (bmi < 18) {
                    tzBMIImage.setImageResource(R.mipmap.low);
                } else if (bmi > 25) {
                    tzBMIImage.setImageResource(R.mipmap.high);
                } else {
                    tzBMIImage.setImageResource(R.mipmap.normal);
                }
            } else {
                tzBMIValue.setText("--");
                tzBMIImage.setImageResource(R.mipmap.none);
            }
            tzQZValue.setText(new DecimalFormat("#.##").format(tzBean.getTzQZValue()));
            tzGGValue.setText(new DecimalFormat("#.##").format(tzBean.getTzGGValue()));
            tzGGImage.setImageResource(R.mipmap.normal);
            tzNZValue.setText(new DecimalFormat("#.##").format(tzBean.getTzNZValue()));
            if (tzBean.getTzNZValue() < 10) {
                tzNZImage.setImageResource(R.mipmap.normal);
            } else {
                tzNZImage.setImageResource(R.mipmap.high);
            }
            tzJCValue.setText("" + tzBean.getTzJCValue());
            tzJCImage.setImageResource(R.mipmap.normal);
            int age = tzBean.getTzSTValue();
            if (age > 0 && age < 100) {
                tzSTValue.setText("" + tzBean.getTzSTValue());
            } else {
                tzSTValue.setText("无");
                tzBean.setTzSTValue(0);
            }
            tzSTImage.setImageResource(R.mipmap.none);
        }
    }

}
