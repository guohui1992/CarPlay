package com.example.neon.bluetoothdemo;

import android.os.Handler;
import android.os.Message;

/**
 * 对应实体小车
 * Created by Neon on 2017/2/24.
 */

public class Car {

    private static final int STATE_STOP = 0;//停止
    private static final int STATE_FORWARD = 1;//前进
    private static final int STATE_LEFT = 2;//左转
    private static final int STATE_RIGHT = 3;//右转
    private static final int STATE_BACKWARD = 4;//后退

    //控制小车
    private static final char CONTROL_FORWARD = 'A';  //前进
    private static final char CONTROL_BACKWRAD = 'B'; //后退
    private static final char CONTROL_TURNLEFT = 'C';  //左转
    private static final char CONTROL_TURNRIGHT = 'D'; //右转
    private static final char CONTROL_PAUSE = 'E';  //暂停

    private static final char CONTROL_SPEED1 = 'F';  //1挡
    private static final char CONTROL_SPEED2 = 'G';  //2挡
    private static final char CONTROL_SPEED3 = 'H';  //3挡
    private static final char CONTROL_SPEED4 = 'I';  //4挡
    private static final char CONTROL_SPEED5 = 'J';  //5挡

    private static final char CONTROL_FORWARD8S = 'K';  //前进8s后停止
    private static final char CONTROL_LEFT45 = 'L';  //左转弯45度前行
    private static final char CONTROL_RIGHT45 = 'M';  //右转弯45度前行
    private static final char CONTROL_CONNECT = 'N';  //蓝牙连接成功
    private static final char CONTROL_WHISTLE = 'O';  //鸣笛
    private static final char CONTROL_VOICE_ON = 'P';  //开启声控
    private static final char CONTROL_VOICE_OFF = 'Q';  //关闭声控


    private int mState;//小车当前的状态
    private int mSpeed;//小车当前的速度
    private Handler mHandlerThread;//连接线程的Handler，用于向子线程发送消息

    //停止
    public void stop() {
        send(CONTROL_PAUSE);
    }

    //左转
    public void turnLeft() {
        send(CONTROL_TURNLEFT);

    }

    //右转
    public void turnRight() {
        send(CONTROL_TURNRIGHT);

    }

    //后退
    public void backward() {
        send(CONTROL_BACKWRAD);
    }

    //左转45度后直行
    public void left45() {
        send(CONTROL_LEFT45);
    }

    //右转45度后直行
    public void right45() {
        send(CONTROL_RIGHT45);
    }

    //前行8s后停止
    public void forward8s() {
        send(CONTROL_FORWARD8S);
    }


    //鸣笛
    public void whistle() {
        send(CONTROL_WHISTLE);
    }

    public void voiceOn() {
        send(CONTROL_VOICE_ON);
    }

    public void voiceOff() {
        send(CONTROL_VOICE_OFF);
    }

    public void controlSuccess() {
        send(CONTROL_CONNECT);
    }


    /**
     * 通过子线程向小车发送控制信息
     *
     * @param control 控制信息
     */
    private void send(char control) {
        Message msg = mHandlerThread.obtainMessage(Constant.MESSAGE_SEND);
        msg.obj = Character.toString(control);
        mHandlerThread.sendMessage(msg);
    }


    public void setState(int state) {
        mState = state;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
        switch (speed) {
            case 1:
                send(CONTROL_SPEED1);
                break;
            case 2:
                send(CONTROL_SPEED2);
                break;
            case 3:
                send(CONTROL_SPEED3);
                break;
            case 4:
                send(CONTROL_SPEED4);
                break;
            case 5:
                send(CONTROL_SPEED5);
                break;

        }
    }


    public int getSpeed() {
        return mSpeed;
    }


    public int getState() {
        return mState;
    }

    public Handler getHandlerThread() {
        return mHandlerThread;
    }

    public void setHandlerThread(Handler handlerThread) {
        mHandlerThread = handlerThread;
    }
}
