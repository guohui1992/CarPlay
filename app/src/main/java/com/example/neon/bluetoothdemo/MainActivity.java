package com.example.neon.bluetoothdemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {
    private static final String TAG = "MainActivity";


    private BluetoothAdapter mAdapter;
    private BluetoothDevice mSelectedDevice;//要连接的设备

    private List<BluetoothDevice> mFoundDevices = new ArrayList<>();//储存已找到的所有蓝牙设备
    private List<Map<String, String>> deviceData = new ArrayList<>();//储存所有已找到的设备名称

    private ConnectThread mConnectThread;//用于建立连接和发送数据的子线程

    private Car mCar;

    private SimpleAdapter mListAdapter;
    private ProgressDialog mProgress;//进度条对话框

    private SensorManager mSensorManager;//传感器管理器

    private boolean isMove = false;//当前是否移动(可用重力感应控制方向)
    private boolean isVoiceOn = false;
    private Button mVoice;

    //用于接收消息的主线程的Handler
    private Handler mHandler;

    //用于监听找到蓝牙设备广播的receiver
    private BroadcastReceiver mDiscoveryReceiver;
    //用于接收搜索结束的广播
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化布局
        initView();

        //检查蓝牙状态
        checkBluetooth();

        mHandler = new MainHandler();
        mDiscoveryReceiver = new DiscoveryReceiver();
        mReceiver = new EndReceiver();
        //动态注册receiver
        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mDiscoveryReceiver, filter1);
        IntentFilter filter2 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter2);
        //获取系统传感器服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }


    /**
     * 初始化布局
     */

    public void initView() {
        Button startDiscovery = (Button) findViewById(R.id.discoverDev); //搜索蓝牙设备按钮
        Button connect = (Button) findViewById(R.id.connect); //“连接”按钮
        startDiscovery.setOnClickListener(this);
        connect.setOnClickListener(this);

        //控制按钮设置监听
        Button backward = (Button) findViewById(R.id.backward);
        Button pause = (Button) findViewById(R.id.pause);
        Button turnLeft = (Button) findViewById(R.id.turnLeft);
        Button turnRight = (Button) findViewById(R.id.turnRight);

        Button speed1 = (Button) findViewById(R.id.speed1);
        Button speed2 = (Button) findViewById(R.id.speed2);
        Button speed3 = (Button) findViewById(R.id.speed3);
        Button speed4 = (Button) findViewById(R.id.speed4);
        Button speed5 = (Button) findViewById(R.id.speed5);

        mVoice = (Button) findViewById(R.id.voiceOn);
        Button whistle = (Button) findViewById(R.id.whistle);

        Button left45 = (Button) findViewById(R.id.left45);
        Button right45 = (Button) findViewById(R.id.right45);
        Button forward8s = (Button) findViewById(R.id.forward8s);

        ControlListener listener = new ControlListener();

        backward.setOnClickListener(listener);
        pause.setOnClickListener(listener);
        turnLeft.setOnClickListener(listener);
        turnRight.setOnClickListener(listener);

        mVoice.setOnClickListener(listener);
        whistle.setOnClickListener(listener);

        speed1.setOnClickListener(listener);
        speed2.setOnClickListener(listener);
        speed3.setOnClickListener(listener);
        speed4.setOnClickListener(listener);
        speed5.setOnClickListener(listener);
        left45.setOnClickListener(listener);
        right45.setOnClickListener(listener);
        forward8s.setOnClickListener(listener);


        //显示已找到蓝牙设备的spinner
        Spinner devices = (Spinner) findViewById(R.id.devices);

        mListAdapter = new SimpleAdapter(this, deviceData, R.layout.item_devicesdialog,
                new String[]{"name"},
                new int[]{R.id.deviceName});
        devices.setAdapter(mListAdapter);

        devices.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAdapter.cancelDiscovery();//取消搜索设备
                mSelectedDevice = mFoundDevices.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();
        //为陀螺仪添加监听器
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * 检查是否开启蓝牙，没开启发起开启请求
     */
    private void checkBluetooth() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();//获取系统默认蓝牙适配器
        if (mAdapter == null) {
            Toast.makeText(this, "设备不支持蓝牙", Toast.LENGTH_SHORT).show();
        } else {
            if (!mAdapter.isEnabled()) {
                Intent requestEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(requestEnable, 123);
            } else {
                Toast.makeText(this, "蓝牙已开启", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 123:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "蓝牙开启成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "蓝牙开启失败", Toast.LENGTH_SHORT).show();
                }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消注册的receiver
        unregisterReceiver(mDiscoveryReceiver);
        unregisterReceiver(mReceiver);

    }


    /**
     * 主界面按钮相应点击事件
     *
     * @param v 被点击的按钮
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            //执行发现周边蓝牙设备的操作
            case R.id.discoverDev:
                if (!mAdapter.isEnabled() || mAdapter == null) {
                    checkBluetooth();
                }
                deviceData.clear();
                mListAdapter.notifyDataSetChanged();
                //开始搜索
                boolean isDis = mAdapter.startDiscovery();
                if (isDis) {
                    mProgress = new ProgressDialog(this);
                    mProgress.setIndeterminate(true);
                    mProgress.setMessage("正在搜索附近蓝牙设备...");
                    mProgress.show();
                }
                break;
            case R.id.connect:
                //取消搜索
                mAdapter.cancelDiscovery();
                //连接选定设备
                if (mSelectedDevice != null) {
                    mConnectThread = new ConnectThread(mSelectedDevice, mHandler);
                    mConnectThread.start();
                    //显示进度对话框
                    mProgress = new ProgressDialog(this);
                    mProgress.setMessage("正在进行连接.....");
                    mProgress.setIndeterminate(true);
                    mProgress.show();
                } else {
                    Toast.makeText(this, "没有设备", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
        }

    }


    //传感器数据发生变化时回调
    @Override
    public void onSensorChanged(SensorEvent event) {
        //获取触发事件的传感器类型
        int sensorType = event.sensor.getType();
        switch (sensorType) {
//            case Sensor.TYPE_GRAVITY://陀螺仪
//                if (isMove) {//如果正在行进
//                    float y = event.values[1];
//
//                    if (y < -1) {//左转
////                        mCar.turnLeft();
//
//                    } else if (y > 1) {//右转
////                        mCar.turnRight();
//
//                    } else {//直行
////                        mCar.forward();
//                    }
//                }
//                break;

        }
//
    }
//
//    //传感器精度发生变化时回调
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    protected void onPause() {
        super.onPause();
        //取消为传感器注册监听器
//        mSensorManager.unregisterListener(this);

    }

    //控制按钮的监听器
    class ControlListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                switch (v.getId()) {//根据点击按钮设置消息参数

                    case R.id.backward://后退
                        mCar.backward();
                        break;
                    case R.id.pause://停止
                        mCar.stop();
                        break;
                    case R.id.turnLeft:
                        mCar.turnLeft();
                        break;

                    case R.id.turnRight:
                        mCar.turnRight();
                        break;

                    case R.id.speed1://1挡
                        mCar.setSpeed(1);
                        break;
                    case R.id.speed2://2挡
                        mCar.setSpeed(2);
                        break;
                    case R.id.speed3://3挡
                        mCar.setSpeed(3);
                        break;
                    case R.id.speed4://4挡
                        mCar.setSpeed(4);
                        break;
                    case R.id.speed5://5挡
                        mCar.setSpeed(5);
                        break;

                    case R.id.left45://左转45度后直行
                        mCar.left45();
                        break;
                    case R.id.right45://右转45度后直行
                        mCar.right45();
                        break;

                    case R.id.forward8s://直行8秒后停止
                        mCar.forward8s();
                        break;
                    case R.id.whistle:
                        mCar.whistle();
                        break;
                    case R.id.voiceOn:
                        if(isVoiceOn){//如果声控开启，则关闭
                            mCar.voiceOff();
                            isVoiceOn=false;
                            mVoice.setText("开启声控");
                        }else {//如果声控关闭，则开启
                            mCar.voiceOn();
                            isVoiceOn=true;
                            mVoice.setText("关闭声控");
                        }
                        break;
                }

            } catch (NullPointerException e) {
                Toast.makeText(MainActivity.this, "请先连接设备", Toast.LENGTH_SHORT).show();
            }

        }

    }

    //主线程的Handler
    class MainHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constant.MESSAGE_CONNECT_FAILED://连接失败
                    mProgress.dismiss();//取消进度条对话框
                    Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                    break;
                case Constant.MESSAGE_WRITE_FAILED://数据写入失败
                    Toast.makeText(MainActivity.this, "传输失败", Toast.LENGTH_SHORT).show();
                    break;
                case Constant.MESSAGE_CONNECT_SUCCESS://连接成功
                    mProgress.dismiss();//隐藏进度对话框
                    Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                    //连接成功后创建Car对象
                    mCar = new Car();
                    //获得子线程的Handler
                    mCar.setHandlerThread(mConnectThread.getHandlerSelf());
                    //向小车发送数据，使led闪烁
                    mCar.controlSuccess();
                    break;
                case Constant.MESSAGE_READ_SUCCESS:
//                    String s = (String) msg.obj;
//                    Toast.makeText(MainActivity.this,"handleMessage: 读取到数据"+s,Toast.LENGTH_SHORT).show();
                    break;
                case Constant.MESSAGE_READ_FAILED:
//                    Log.e(TAG, "handleMessage: 从小车读取数据失败" );
//                    Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();

            }
        }
    }

    //接收搜索搜索到设备广播的Receiver
    class DiscoveryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //找到设备后会发送广播
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //获得蓝牙设备对象
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!mFoundDevices.contains(device)) {
                    mFoundDevices.add(device);
                }

                Map<String, String> map = new HashMap<>();
                map.put("name", device.getName());
                deviceData.add(map);
                //通知view更新数据
                mListAdapter.notifyDataSetChanged();
                mProgress.dismiss();
                Log.i(TAG, "onReceive: 查找到设备" + device.getName() + ",Mac地址为：" + device.getAddress());

            }
        }
    }

    //接收搜索结束广播的Receiver
    class EndReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && mFoundDevices.isEmpty())
            //搜索结束且没有找到设备
            {
                mProgress.dismiss();
                Toast.makeText(context, "没有找到设备，请重试。", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


