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
 * Created by Neon on 2017/2/21.
 */

public class ConnectThread extends Thread {
    private static final String TAG = "ConnectThread";
    private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";

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

//            read();//从小车读取数据

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
    public void send(final String s) {
        new Thread() {
            @Override
            public void run() {
                super.run();
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
        }.start();

    }

    //读取小车发过来的数据
//    public void read() {
//        new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    InputStream is = mSocket.getInputStream();
//                    byte data[] = new byte[8];
//                    int hasRead;
//                    while ((hasRead = is.read(data)) != -1) {
//                        //发送过来的数据第一个字符是字母,后面有乱码,取字母
//                        char c = new String(data, "ISO-8859-1").charAt(0);
//                        String obj = "";
//                        switch (c) {
//                            case 'A':
//                                obj = "前进";
//                                break;
//                            case 'B':
//                                obj = "后退";
//                                break;
//                            case 'C':
//                                obj = "左转";
//                                break;
//                            case 'D':
//                                obj = "右转";
//                                break;
//                            case 'E':
//                                obj = "0";
//                                break;
//                            case 'F':
//                                obj = "1";
//                                break;
//                            case 'G':
//                                obj = "2";
//                                break;
//                            case 'H':
//                                obj = "3";
//                                break;
//                            case 'I':
//                                obj = "4";
//                                break;
//                            case 'J':
//                                obj = "5";
//                                break;
//                        }
//                        Message msg = mHandlerMain.obtainMessage(Constant.MESSAGE_READ_SUCCESS);
//                        msg.obj = obj;
//                        mHandlerMain.sendMessage(msg);
//
////                        Log.d(TAG, "run: 读取"+hasRead+"位数据"+",字符为"+s);
//                    }
//                } catch (IOException e) {
//                    //发送读取失败的消息
//                    mHandlerMain.sendEmptyMessage(Constant.MESSAGE_READ_FAILED);
//                }
//            }
//        }.start();
//
//
//    }

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
