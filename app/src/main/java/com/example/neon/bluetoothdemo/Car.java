package com.example.neon.bluetoothdemo;

import android.os.Handler;
import android.os.Message;

/**
 * 对应实体小车
 * Created by Neon on 2017/2/24.
 */

class Car {

    //控制小车
//    private static final char CONTROL_FORWARD = 'A';  //前进
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


    private int mState;//小车当前的状态
    private int mSpeed;//小车当前的速度
    private Handler mHandlerThread;//连接线程的Handler，用于向子线程发送消息

    /**
     * 停止
     */

    void stop() {
        send(CONTROL_PAUSE);
    }

    /**
     * 左转
     */
    void turnLeft() {
        send(CONTROL_TURNLEFT);

    }

    /**
     * 右转
     */
    void turnRight() {
        send(CONTROL_TURNRIGHT);

    }

    /**
     * 后退
     */
    void backward() {
        send(CONTROL_BACKWRAD);
    }

    /**
     * 左转45度后直行
     */
    void left45() {
        send(CONTROL_LEFT45);
    }

    /**
     * 右转45度后直行
     */
    void right45() {
        send(CONTROL_RIGHT45);
    }

    /**
     * 前行8s后停止
     */

    void forward8s() {
        send(CONTROL_FORWARD8S);
    }


    /**
     * 鸣笛
     */
    void whistle() {
        send(CONTROL_WHISTLE);
    }

    /**
     * 建立连接成功
     */
    void controlSuccess() {
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


    /**
     * 设置小车当前运行状态，并发送给主线程
     * @param state 小车状态
     */
    public void setState(int state) {
        mState = state;
    }

    /**
     * 设置当前小车速度
     * @param speed  小车速度
     */
    void setSpeed(int speed) {
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

    void setHandlerThread(Handler handlerThread) {
        mHandlerThread = handlerThread;
    }
}
