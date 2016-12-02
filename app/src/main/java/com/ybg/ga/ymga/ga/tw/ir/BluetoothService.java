package com.ybg.ga.ymga.ga.tw.ir;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.ybg.ga.ymga.ga.tw.TWMainActivity;
import com.ybg.ga.ymga.util.AppConstat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by yangbagang on 15/6/15.
 */
public class BluetoothService {

    // 调试
    private static final String TAG = "BluetoothService";
    private static final boolean D = false;

    // 记录当创建服务器套接字
    private static final String NAME = "TWMainActivity";

    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static StringBuffer hexString = new StringBuffer();
    // 适配器成员
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState, count;
    // 常数，指示当前的连接状态
    public static final int STATE_NONE = 0; // 当前没有可用的连接
    public static final int STATE_LISTEN = 1; // 现在侦听传入的连接
    public static final int STATE_CONNECTING = 2; // 现在开始传出联系
    public static final int STATE_CONNECTED = 3; // 现在连接到远程设备
    public static boolean bRun = true;
    int[] com = {0, 0, 0, 0, 0, 0, 0};
    private String asd;

    /**
     * 构造函数。准备一个新的bluetoothchat会话。
     *
     * @param context 用户界面活动的背景
     * @param handler 一个处理程序发送邮件到用户界面活性
     */
    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;

    }

    /**
     * 设置当前状态的聊天连接
     *
     * @param state 整数定义当前连接状态
     */
    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        // 给新状态的处理程序，界面活性可以更新
        mHandler.obtainMessage(IRConstants.MESSAGE_STATE_CHANGE, state, -1)
                .sendToTarget();
    }

    /**
     * 返回当前的连接状态。
     */
    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        if (D)
            Log.d(TAG, "start");

        // 取消任何线程试图建立连接
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 启动线程来听一个bluetoothserversocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    // 连接按键响应函数

    /**
     * 开始connectthread启动连接到远程设备。
     *
     * @param device 装置连接的蓝牙设备
     */
    public synchronized void connect(BluetoothDevice device) {
        stop();
        if (D)
            Log.d(TAG, "connect to: " + device);
        stop();
        // 取消任何线程试图建立连接
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 启动线程连接的设备
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * 开始connectedthread开始管理一个蓝牙连接
     *
     * @param socket bluetoothsocket插座上连接了
     * @param device 设备已连接的蓝牙设备
     */
    @SuppressWarnings("unused")
    public synchronized void connected(BluetoothSocket socket,
                                       BluetoothDevice device) {
        if (D)
            Log.d(TAG, "connected");

        // 取消线程完成连接
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // 取消任何线程正在运行的连接
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // 取消接受线程只因为我们要连接到一个设备
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }

        // 启动线程管理连接和传输
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // 把名字的连接设备到Activity
        Message msg = mHandler.obtainMessage(IRConstants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(IRConstants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    /**
     * 停止所有的线程
     */
    public synchronized void stop() {
        if (D)
            Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // 创建临时对象
        ConnectedThread r;
        // 同步副本的connectedthread
        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        // 执行写同步
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        setState(STATE_LISTEN);
        // 发送失败的信息带回活动
        Message msg = mHandler.obtainMessage(IRConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(IRConstants.TOAST, "无法连接装置");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        setState(STATE_LISTEN);
        // 发送失败的信息带回Activity
        Message msg = mHandler.obtainMessage(IRConstants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(IRConstants.TOAST, "装置连接丢失");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * 本线同时侦听传入的连接。它的行为 喜欢一个服务器端的客户端。它运行直到连接被接受 （或取消）。
     */
    private class AcceptThread extends Thread {
        // 本地服务器套接字
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {

            BluetoothServerSocket tmp = null;

            // 创建一个新的侦听服务器套接字
            try {
                tmp = mAdapter
                        .listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run() {
            if (D)
                Log.d(TAG, "BEGIN mAcceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // 听服务器套接字如果我们没有连接
            while (mState != STATE_CONNECTED) {
                try {
                    // 这是一个阻塞调用和将只返回一个
                    // 成功的连接或例外
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // 如果连接被接受
                if (socket != null) {
                    synchronized (BluetoothService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // 正常情况。启动连接螺纹。
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // 没有准备或已连接。新插座终止。
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (D)
                Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if (D)
                Log.d(TAG, "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    /**
     * 本线在试图使传出联系 与设备。它径直穿过连接；或者 成功或失败。
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // 得到一个bluetoothsocket为与连接
            // 由于蓝牙设备
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            // 总是取消的发现，因为它会减缓连接
            mAdapter.cancelDiscovery();
            // 使一个连接到bluetoothsocket
            try {
                // 这是一个阻塞调用和将只返回一个
                // 成功的连接或例外
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                // 关闭这个socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG,
                            "unable to close() socket during connection failure",
                            e2);
                }
                // 启动服务在重新启动聆听模式
                BluetoothService.this.start();
                return;
            }
            // 因为我们所做的connectthread复位
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            // 启动连接线程
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * 本线在连接与远程设备。 它处理所有传入和传出的传输。
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // 获得bluetoothsocket输入输出流
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "没有创建临时sockets", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[16];
            int bytes;
            Data dt = new Data();
            // 继续听InputStream同时连接
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    int[] f = CodeFormat.bytesToHexStringTwo(buffer, 8);
                    dt.analysis(f);
                    int a = dt.getHead();
                    int two = dt.getDatatwo();
                    if (a == 253) {
                        com[0] = dt.getHead();
                        com[1] = dt.getDataone();
                        com[2] = dt.getDatatwo();
                        com[3] = dt.getThree();
                        com[4] = dt.getFour();
                        com[5] = dt.getFive();
                        com[6] = dt.getSex();
                        if (com[0] == 253 && com[5] == 13 && com[6] == 10) {
                            if ((com[1] == 26 | com[1] == 21) && com[2] == 170) {
                                mHandler.obtainMessage(IRConstants.YYOK,
                                        bytes, -1, asd).sendToTarget();
                            }
                            if (com[1] == 26 | com[1] == 21) {
                                if (com[2] == 129 && com[4] == 1) {
                                    mHandler.obtainMessage(IRConstants.EEONE,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 130 && com[4] == 2) {
                                    mHandler.obtainMessage(IRConstants.EETWO,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 131 && com[4] == 3) {
                                    mHandler.obtainMessage(IRConstants.EETHR,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 132 && com[4] == 4) {
                                    mHandler.obtainMessage(IRConstants.EEFOU,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 133 && com[4] == 5) {
                                    mHandler.obtainMessage(IRConstants.EEFIV,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 134 && com[4] == 6) {
                                    mHandler.obtainMessage(IRConstants.EESIX,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 135 && com[4] == 7) {
                                    mHandler.obtainMessage(IRConstants.EESEV,
                                            bytes, -1, buffer).sendToTarget();
                                }
                                if (com[2] == 136 && com[4] == 8) {
                                    mHandler.obtainMessage(IRConstants.EEEIG,
                                            bytes, -1, buffer).sendToTarget();
                                }
                            }
                            // 摄氏度
                            if (com[1] == 26) {
                                //人体温度
                                if (com[2] == 1) {
                                    asd = com[3] + "" + "," + com[4] + "";
                                    mHandler.obtainMessage(
                                            IRConstants.YYRESULT, bytes, -1,
                                            asd).sendToTarget();
                                }
                                //目标 摄氏度
                                if (com[2] == 0) {
                                    asd = com[3] + "" + "," + com[4] + "";
                                    mHandler.obtainMessage(IRConstants.YYMU,
                                            bytes, -1, asd).sendToTarget();
                                }
                            }
                            // F
                            if (com[1] == 21) {
                                //人体F
                                if (com[2] == 1) {
                                    asd = com[3] + "" + "," + com[4] + "";
                                    mHandler.obtainMessage(
                                            IRConstants.FYRESULT, bytes, -1,
                                            asd).sendToTarget();
                                }
                                //目标 F
                                if (com[2] == 0) {
                                    asd = com[3] + "" + "," + com[4] + "";
                                    mHandler.obtainMessage(IRConstants.FYMU,
                                            bytes, -1, asd).sendToTarget();
                                }
                            }


                        }


                    }

                    try {
                        ConnectedThread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * 写输出的连接。 @param buffer 这是一个字节流
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // 分享发送的信息到Activity
                mHandler.obtainMessage(IRConstants.MESSAGE_WRITE, -1, -1,
                        buffer).sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

}
