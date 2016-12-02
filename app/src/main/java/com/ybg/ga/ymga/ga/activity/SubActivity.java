/**
 *
 */
package com.ybg.ga.ymga.ga.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * @author 杨拔纲
 */
public class SubActivity extends AppCompatActivity implements OnResultAvailableListener {

    public void setActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

}
