package com.example.neon.bluetoothdemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 用于与蓝牙设备建立连接，并发送和接收数据
 * <p>
 * Created by Neon on 2017/2/21.
 */

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;
    private Handler mHandlerMain;
    //这个线程的Handler
    private Handler mHandlerSelf;

    public ConnectThread(BluetoothDevice device, Handler handler) {
        mDevice = device;
        mHandlerMain = handler;
        mHandlerSelf = new MyHandler();
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();

        BluetoothSocket temp = null;
        try {
            //建立蓝牙连接
            temp = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            Message msgFailed = mHandlerMain.obtainMessage(Constant.MESSAGE_CONNECT_FAILED);
            mHandlerMain.sendMessage(msgFailed);
        }
        mSocket = temp;

        try {
            mSocket.connect();
            //成功后发送消息
            Message msg = mHandlerMain.obtainMessage(Constant.MESSAGE_CONNECT_SUCCESS);
            mHandlerMain.sendMessage(msg);

        } catch (IOException e) {
            //失败后发送消息
            Message msgFailed = mHandlerMain.obtainMessage(Constant.MESSAGE_CONNECT_FAILED);
            mHandlerMain.sendMessage(msgFailed);
            try {
                mSocket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

        Looper.loop();


    }


    /**
     * 向蓝牙模块发送字符
     *
     * @param s
     */
    private void send(final String s) {
        OutputStream os = null;
        try {
            os = mSocket.getOutputStream();
            byte[] data = s.getBytes();
            os.write(data);
            mHandlerMain.sendEmptyMessage(Constant.MESSAGE_WRITE_SUCCESS);
        } catch (Exception e) {
            mHandlerMain.sendEmptyMessage(Constant.MESSAGE_WRITE_FAILED);
        }

    }

    public Handler getHandlerSelf() {
        return mHandlerSelf;
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MESSAGE_SEND:
                    //调用send方法发送数据
                    send((String) msg.obj);
                    break;
            }
        }
    }
}
