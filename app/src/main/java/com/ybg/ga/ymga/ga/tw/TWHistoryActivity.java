package com.ybg.ga.ymga.ga.tw;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.bt.BTAction;
import com.ybg.ga.ymga.bt.BTPrefix;
import com.ybg.ga.ymga.ga.activity.SubActivity;
import com.ybg.ga.ymga.ga.preference.TWPreference;
import com.ybg.ga.ymga.user.UserPreferences;
import com.ybg.ga.ymga.util.AppConstat;
import com.ybg.ga.ymga.util.TimeUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by yangbagang on 2015/5/27.
 */
public class TWHistoryActivity extends SubActivity {

    private UserPreferences userPreferences = UserPreferences.getInstance();
    private TWPreference twPreference = TWPreference.getInstance();
    private TWDataService twDataService = null;
    private Intent bindIntent = null;
    private Cursor cursor = null;
    private ListView historyDataList = null;
    private SimpleCursorAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.tw_history_data);

        Toolbar toolbar = (Toolbar) findViewById(R.id.twHistoryDataToolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        historyDataList = (ListView) findViewById(R.id.twHistoryDataList);

        bindIntent = new Intent(TWHistoryActivity.this, TWDataService.class);
        getApplicationContext().bindService(bindIntent, mConnection,
                Context.BIND_AUTO_CREATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BTAction.getUpdateAction(BTPrefix.TW));
        registerReceiver(twSyncBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void loadingData() {
        cursor = twDataService.list();

        String[] columns = {"_id", "tw", "createTime"};
        int[] layoutIds = {0, 0, 0, 0};

        adapter = new TWHistoryAdapter(TWHistoryActivity.this,
                R.layout.tw_history_item, cursor, columns, layoutIds, 0);
        historyDataList.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(twSyncBroadcastReceiver);
        getApplicationContext().unbindService(mConnection);
        super.onDestroy();
    }

    private void loadHistoryDate(String beginDate, String endDate) {
        // 查询历史
        cursor = twDataService.list(beginDate, endDate);
        String[] columns = {"_id", "tw", "createTime"};
        int[] layoutIds = {0, 0, 0, 0};

        adapter = new TWHistoryAdapter(TWHistoryActivity.this,
                R.layout.tw_history_item, cursor, columns, layoutIds, 0);
        historyDataList.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
            Intent intent = new Intent(this, TWHistoryDataSearchActivity.class);
            getParent().startActivityForResult(intent, AppConstat.TW_SEARCH_REQUEST_CODE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("twtwwtwtw");
        if (requestCode == AppConstat.TW_SEARCH_REQUEST_CODE
                && resultCode == AppConstat.TW_SEARCH_RESULT_CODE) {
            String beginDate = data.getExtras().getString("beginDate");
            String endDate = data.getExtras().getString("endDate");
            System.out.println("beginDate=" + beginDate);
            System.out.println("endDate=" + endDate);
            loadHistoryDate(beginDate, endDate);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            twDataService = ((TWDataService.TWDataBinder) service).getService();
            loadingData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            twDataService = null;
        }

    };

    private class TWHistoryAdapter extends SimpleCursorAdapter {

        private LayoutInflater miInflater;

        public TWHistoryAdapter(Context context, int layout, Cursor c,
                                String[] from, int[] to, int flags) {
            super(context, layout, c, from, to, flags);
            miInflater = LayoutInflater.from(context);
        }

        @SuppressLint("InflateParams")
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            View convertView = null;
            if (view == null) {
                convertView = miInflater.inflate(R.layout.tw_history_item, null);
            } else {
                convertView = view;
            }

            TextView twHistoryUserName = (TextView) convertView
                    .findViewById(R.id.twHistoryUserName);
            TextView twHistoryData = (TextView) convertView
                    .findViewById(R.id.twHistoryData);
            TextView twHistoryTime = (TextView) convertView
                    .findViewById(R.id.twHistoryTime);
            ImageView twUnit = (ImageView) convertView.findViewById(R.id.twUnit);

            String username = userPreferences.getNickName();
            twHistoryUserName.setText(username);
            float tw = cursor.getFloat(cursor.getColumnIndex("tw"));
            String createTime = cursor.getString(cursor
                    .getColumnIndex("createTime"));

            if (twPreference.isCAsDefaultUnit()) {
                twHistoryData.setText(String.format("温度：%s", new DecimalFormat("#.#").format(tw)));
                twUnit.setImageResource(R.mipmap.cc);
            } else {
                twHistoryData.setText(String.format("温度：%s", new DecimalFormat("#.#").format(WenduTool.c2f(tw))));
                twUnit.setImageResource(R.mipmap.ff);
            }
            twHistoryTime.setText(TimeUtil.getButtyTime(createTime));
        }

    }

    private BroadcastReceiver twSyncBroadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BTAction.getUpdateAction(BTPrefix.TW).equals(action)) {
                loadingData();
            }
        }

    };

}
