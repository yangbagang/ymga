/**
 *
 */
package com.ybg.ga.ymga.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.YbgApp;

/**
 * @author 杨拔纲
 */
public class UpdateManager {

    private Context mContext;

    // 提示语
    private String updateMsg = "新版本%s己经发布，功能更强、更健康哦，亲快下载吧~";

    // 返回的安装包url
    private String apkUrl = "";
    private String apkVersion = "";

    private Dialog noticeDialog;

    private Dialog downloadDialog;
    /* 下载包安装路径 */
    private static final String savePath = Environment
            .getExternalStorageDirectory().getPath() + "/oumupdate/";

    private static final String saveFileName = savePath + "oumUpdate.apk";

    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;

    private static final int DOWN_OVER = 2;

    private static final int HAS_NEW_VERSION = 3;

    private int progress;

    private Thread downLoadThread;

    private boolean interceptFlag = false;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    break;
                case DOWN_OVER:
                    installApk();
                    break;
                case HAS_NEW_VERSION:
                    showNoticeDialog();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    public UpdateManager(Context context) {
        this.mContext = context;
    }

    // 外部接口让主Activity调用
    public void checkUpdateInfo() {
        new CheckUpdateThread().start();
    }

    private void showNoticeDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(String.format(updateMsg, apkVersion));
        builder.setPositiveButton("立即下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    @SuppressLint("InflateParams")
    private void showDownloadDialog() {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");

        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);

        builder.setView(v);
        builder.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                interceptFlag = true;
            }
        });
        downloadDialog = builder.create();
        downloadDialog.show();

        downloadApk();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                URL url = new URL(AppConstat.APP_HOST + apkUrl);

                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdir();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消就停止下载.

                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * 下载apk
     *
     */

    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     *
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        mContext.startActivity(i);
    }

    private class CheckUpdateThread extends Thread {

        public void run() {
            YbgApp ybgApp = YbgApp.getInstance();
            URL url = null;
            try {
                url = new URL(AppConstat.APP_HOST + "/app/getUpdate");
                HttpURLConnection urlConn = (HttpURLConnection) url
                        .openConnection();
                urlConn.setDoInput(true);// 字节流
                urlConn.setDoOutput(true);// 字节流
                urlConn.setRequestMethod("POST");
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content_Type",
                        "application/x-www-form-urlencoded");
                urlConn.setRequestProperty("Charset", "UTF-8");

                urlConn.connect();

                DataOutputStream dos = new DataOutputStream(
                        urlConn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append("ver=" + ybgApp.getAppVersion(mContext));
                dos.writeBytes(sb.toString());
                dos.flush();
                dos.close();

                BufferedReader br = new BufferedReader(new InputStreamReader(
                        urlConn.getInputStream()));
                String readLine = null;
                String strResult = "";
                while ((readLine = br.readLine()) != null) {
                    strResult += readLine;
                }

                br.close();
                urlConn.disconnect();

                if (!"0".equals(strResult)) {
                    String[] result = strResult.split(",");
                    apkVersion = result[0];
                    apkUrl = result[1];
                    mHandler.sendEmptyMessage(HAS_NEW_VERSION);
                }
            } catch (MalformedURLException e) {
                // userIntent.putExtra(BTAction.INFO, "地址格式错误");
            } catch (IOException e) {
                // userIntent.putExtra(BTAction.INFO, "网络错误");
            } catch (Exception e) {
                //  未知错误
            }
        }

    }

}
