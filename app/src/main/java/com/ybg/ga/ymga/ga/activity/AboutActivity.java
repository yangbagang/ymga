/**
 *
 */
package com.ybg.ga.ymga.ga.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;
import com.ybg.ga.ymga.util.UpdateManager;

/**
 * 关于页面，显示版权信息。
 *
 * @author 杨拔纲
 */
public class AboutActivity extends AppCompatActivity {

    private YbgApp ybgApp = YbgApp.getInstance();
    private TextView versionView = null;
    private Button checkVersionButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.aboutToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 设置版本号
        versionView = (TextView) findViewById(R.id.aboutVersion);
        versionView.setText("(V" + ybgApp.getAppVersion(getApplicationContext()) + ")");
    }

}
