package com.bpump.dvtapplication;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.bpump.dvtapplication.adapter.GridViewAdapter;
import com.bpump.dvtapplication.bleService.BluetoothLeService;
import com.bpump.dvtapplication.utils.OrderUtils;
import com.bpump.dvtapplication.view.BLESearchActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private GridView grid;
    private TextView result;
    private GridViewAdapter adapter;
    private List<String> list = new ArrayList<String>();


    private String connectText = "未连接 ";

    /**
     * 蓝牙相关定义
     */
    private String mDeviceAddress = "A4:D5:78:6C:BE:20";// 蓝牙mac地址
    private BluetoothLeService mBluetoothLeService;// 蓝牙service
    private boolean mConnected = false;// 蓝牙是否连接
    private static final int DATA_REQUEST_SUCCESS = 0x01;// 数据接收成功的handler flag
    StringBuffer sb = null;

    // 创建服务管理来连接蓝牙
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
                    .getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // 创建广播实时监听蓝牙状态
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                connectText = "已连接 ";
                setTitle(connectText + mDeviceAddress);
                Toast.makeText(MainActivity.this, "连接成功！", Toast.LENGTH_SHORT).show();
                // updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED
                    .equals(action)) {
                mConnected = false;
                connectText = "未连接 ";
                setTitle(connectText + mDeviceAddress);
                Toast.makeText(MainActivity.this, "蓝牙断开！", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                // clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED
                    .equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // 发送message上传数据到Pc端
                Message m = handler.obtainMessage();
                m.what = DATA_REQUEST_SUCCESS;
                m.obj = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                m.sendToTarget();
            }
        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case DATA_REQUEST_SUCCESS:
                    ControlByteArray((byte[]) msg.obj);
                    result.setText(sb + "/n");
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 将接收到的byte字节数组转换为可见的字符串
     *
     * @param bytes
     */
    private String ControlByteArray(byte[] bytes) {
        sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(bytes[i] + "");
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initData();
        initListener();

        if (getIntent().getStringExtra("adress") != null) {
            mDeviceAddress = getIntent().getStringExtra("adress");
        }

        setTitle("正在连接 " + mDeviceAddress);

        // 绑定服务
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void init() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        grid = (GridView) findViewById(R.id.grid_btn);
        result = (TextView) findViewById(R.id.result);
        setSupportActionBar(toolbar);
    }

    private void initData() {
        getData();
        adapter = new GridViewAdapter(getApplication(), list);
        grid.setAdapter(adapter);
    }

    private List<String> getData() {
        list.add("标定气压");
        list.add("某个腔加压到指定气压");
        list.add("某个腔泄气到指定气压");
        list.add("平均压测量");
        list.add("静脉回盈时间测量");
        list.add("读取当前气压");
        list.add("暂停当前操作");
        list.add("结束当前操作");
        list.add("读取治疗记录");
        list.add("选择下载的治疗记录");
        list.add("读取当前日志信息");
        list.add("读取下一条日志信息");
        list.add("读取用户信息");
        list.add("注册用户信息");
        return list;
    }

    private void initListener() {
        grid.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
           startActivity(new Intent(MainActivity.this, BLESearchActivity.class));
            MainActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {// 注册广播
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onDestroy() {// 界面销毁时解除服务与广播的绑定
        // TODO Auto-generated method stub
        super.onDestroy();
        unbindService(mServiceConnection);
        unregisterReceiver(mGattUpdateReceiver);
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter
                .addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {

        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        switch (i) {
            case 0:
                mBluetoothLeService.writeValue(OrderUtils.DEMARCATE_PRESSURE);//标定气压
                break;
            case 1:
                mBluetoothLeService.writeValue(OrderUtils.PRELUM_TO_APPOINT_PRESSURE);//某个腔加压到指定气压(发送长度为3 )
                break;
            case 2:
                mBluetoothLeService.writeValue(OrderUtils.STALENESS_TO_APPOINT_PRESSURE);//某个腔泄气到指定气压(发送长度为3 )
                break;
            case 3:
                mBluetoothLeService.writeValue(OrderUtils.AVG_PRESSURE_MESSURE);//平均压测量
                break;
            case 4:
                mBluetoothLeService.writeValue(OrderUtils.VENOUS_RETURN_TIME_MESSURE);//静脉回盈时间测量
                break;
            case 5:
                mBluetoothLeService.writeValue(OrderUtils.READ_NOW_PRESSURE);//读取当前气压
                break;
            case 6:
                mBluetoothLeService.writeValue(OrderUtils.PAUSE_NOW_OPERATION);//暂停当前操作
                break;
            case 7:
                mBluetoothLeService.writeValue(OrderUtils.FINISH_NOW_OPERATION);//结束当前操作
                break;
            case 8:
                mBluetoothLeService.writeValue(OrderUtils.READ_TREAMENT_RECORD);// 读取治疗记录（示例是最后一条日志记录的情况）(发送长度为2，记录编号(2bytes))
                break;
            case 9:
                mBluetoothLeService.writeValue(OrderUtils.DOWNLOAD_TREAMENT_RECORD);//选择下载的治疗记录(发送长度为2，记录编号(2bytes))
                break;
            case 10:
                mBluetoothLeService.writeValue(OrderUtils.READ_NOW_RECORD);//读取当前日志信息
                break;
            case 11:
                mBluetoothLeService.writeValue(OrderUtils.READ_NEXT_RECORD);//读取下一条日志信息
                break;
            case 12:
                mBluetoothLeService.writeValue(OrderUtils.READ_USERINFORMATION);//读取用户信息
                break;
            case 13:
                mBluetoothLeService.writeValue(OrderUtils.REGISTER_OR_UPDATE_USERINFORMATION);//注册用户信息/修改用户信息(发送长度为11，信息包含：用户ID(1byte)、血压信息(2bytes)、每次的最大治疗分钟数(2bytes) 、病历编号(6bytes))
                break;
        }
    }
}
