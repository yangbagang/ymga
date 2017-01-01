package com.ybg.ga.ymga.ga;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.ga.activity.AboutActivity;
import com.ybg.ga.ymga.ga.tw.TWWelcomeActivity;
import com.ybg.ga.ymga.ga.tz.TZWelcomeActivity;
import com.ybg.ga.ymga.ga.xy.XYWelcomeActivity;
import com.ybg.ga.ymga.ga.yd.YDWelcomeActivity;
import com.ybg.ga.ymga.user.LocateMeActivity;
import com.ybg.ga.ymga.user.RegisterActivity;
import com.ybg.ga.ymga.user.UserAction;
import com.ybg.ga.ymga.user.UserCenterActivity;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.user.UserSettingActivity;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.UpdateManager;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private View headView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        headView = navigationView.getHeaderView(0);
        headView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (userPreferences.hasLogin()) {
                    intent = new Intent(MainActivity.this, UserCenterActivity.class);
                } else {
                    intent = new Intent(MainActivity.this, RegisterActivity.class);
                }
                startActivity(intent);
            }
        });
        initUser();
        // 检查更新
        UpdateManager update = new UpdateManager(this);
        update.checkUpdateInfo();
        // 动态注册广播接收器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UserAction.USER_REGISTER);
        intentFilter.addAction(UserAction.USER_LOGIN);
        registerReceiver(userActionReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(userActionReceiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, UserSettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_about) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.nav_sport) {
            ydControl(null);
        } else if (id == R.id.nav_history) {
            ydHistory(null);
        } else if (id == R.id.nav_location) {
            locateMe(null);
        } else if (id == R.id.nav_device) {
            startActivity(new Intent(this, DeviceManagerActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //-菜单事件开始

    /**
     * 血压控制。连接电子血压计相关操作。
     *
     * @param view
     */
    public void xyControl(View view) {
        Intent intent = new Intent(this, XYWelcomeActivity.class);
        startActivity(intent);
    }

    /**
     * 运动控制。读取计步器数据或者进行GPS运动。
     *
     * @param view
     */
    public void ydControl(View view) {
        Intent intent = new Intent(this, YDWelcomeActivity.class);
        startActivity(intent);
    }

    /**
     * 体重控制。读取体重计数据。
     *
     * @param view
     */
    public void tzControl(View view) {
        Intent intent = new Intent(this, TZWelcomeActivity.class);
        startActivity(intent);
    }

    /**
     * 进入消息管理界面
     *
     * @param view
     */
    public void enterMessage(View view) {
        coming(view);
    }

    public void locateMe(View view) {
        Intent intent = new Intent(this, LocateMeActivity.class);
        startActivity(intent);
    }

    public void ydHistory(View view) {
        Intent intent = new Intent(this, YDWelcomeActivity.class);
        intent.putExtra("index", 1);
        startActivity(intent);
    }

    public void ydDiscover(View view) {
        coming(view);
    }

    public void coming(View view) {
        YbgApp ybgApp = YbgApp.getInstance();
        ybgApp.showMessage(getApplicationContext(), "本功能暂未开放，更多精彩，即将推出！");
    }

    public void ydLeiTai(View view) {
        coming(view);
    }

    public void myHonor(View view) {
        coming(view);
    }

    public void twControl(View view) {
        Intent intent = new Intent(this, TWWelcomeActivity.class);
        startActivity(intent);
    }

    /**
     * 打开浏览器访问微店
     *
     * @param view
     */
    public void goWeiDian(View view) {
        String url = "https://weidian.com/s/404088691?wfr=c"; // 微店地址
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
    //-菜单事件结束

    private void initUser() {
        TextView userTV = (TextView) headView.findViewById(R.id.userNameLabel);
        ImageView userImg = (ImageView) headView.findViewById(R.id.userImageView);
        if (userPreferences.hasLogin()) {
            userTV.setText(userPreferences.getName());
            String img = userPreferences.getUserHeadImg();
            if (!"0".equals(img)) {
                ImageLoader.getInstance().displayImage(AppConstat.APP_HOST + img, userImg);
            }
        } else {
            userTV.setText(getResources().getString(R.string.user_register_label));
        }
    }

    private UserPreferences userPreferences = UserPreferences.getInstance();

    private BroadcastReceiver userActionReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UserAction.USER_LOGIN.equals(action)) {
                String loginResult = intent.getExtras().getString("loginResult");
                if ("ok".equals(loginResult)) {
                    initUser();
                }
            } else if (UserAction.USER_REGISTER.equals(action)) {
                String registerResult = intent.getExtras().getString("registerResult");
                if ("ok".equals(registerResult)) {
                    initUser();
                }
            }
        }

    };

}
