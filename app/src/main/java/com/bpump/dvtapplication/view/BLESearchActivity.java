package com.bpump.dvtapplication.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bpump.dvtapplication.MainActivity;
import com.bpump.dvtapplication.R;

import java.util.ArrayList;

public class BLESearchActivity extends AppCompatActivity implements OnItemClickListener,
		OnClickListener {
	private BleDeviceListAdapter mLeDeviceListAdapter;
	private BluetoothAdapter mBluetoothAdapter;
	@SuppressWarnings("unused")
	private boolean mScanning = true;
	private Handler mHandler;
	private TextView search;
	private ListView listview;
	// 10秒后停止查找搜索.
	private static final long SCAN_PERIOD = 20000;

	private static final int REQUEST_FINE_LOCATION=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_main);

		setTitle("蓝牙搜索");
		mayRequestLocation();
		mHandler = new Handler();

		// 检查当前手机是否支持ble 蓝牙,如果不支持退出程序
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "蓝牙开启异常", Toast.LENGTH_SHORT).show();
			finish();
		}
		// 初始化 Bluetooth adapter, 通过蓝牙管理器得到一个参考蓝牙适配器(API必须在以上android4.3或以上和版本)
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 检查设备上是否支持蓝牙
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "蓝牙错误，请检查蓝牙", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		initView();
	}

	private void initView() {
		listview = (ListView) findViewById(R.id.listview);
		search = (TextView) findViewById(R.id.search);
		listview.setOnItemClickListener(this);
		search.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 为了确保设备上蓝牙能使用, 如果当前蓝牙设备没启用,弹出对话框向用户要求授予权限来启用
		if (!mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.enable();
		}
		mLeDeviceListAdapter = new BleDeviceListAdapter();
		listview.setAdapter(mLeDeviceListAdapter);
		scanLeDevice(true);
	}

	/**
	 * 搜索蓝牙
	 * 
	 * @param enable
	 *            是否开始搜索
	 */
	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	/**
	 * 设置蓝牙权限
	 */
	private void mayRequestLocation() {
		if (Build.VERSION.SDK_INT >= 23) {
			int checkCallPhonePermission = ContextCompat.checkSelfPermission(BLESearchActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
			if(checkCallPhonePermission != PackageManager.PERMISSION_GRANTED){
				//判断是否需要 向用户解释，为什么要申请该权限
				if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION))
					Toast.makeText(BLESearchActivity.this, R.string.action_settings, Toast.LENGTH_SHORT).show();

				ActivityCompat.requestPermissions(this ,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_FINE_LOCATION);
				return;
			}else{

			}
		} else {
			Toast.makeText(BLESearchActivity.this, R.string.action_settings, Toast.LENGTH_SHORT).show();
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults) {
		switch (requestCode) {
			case REQUEST_FINE_LOCATION:
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// The requested permission is granted.
					if (mScanning == false) {
						scanLeDevice(true);
					}
				} else{
					// The user disallowed the requested permission.
				}
				break;

		}

	}

	/**
	 * 蓝牙搜索回调，更新适配器
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mLeDeviceListAdapter.addDevice(device);
					mLeDeviceListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	/**
	 * 蓝牙列表适配器
	 * 
	 * @author qinli 于2016年8月17
	 * 
	 */
	private class BleDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;
		private LayoutInflater mInflator;

		public BleDeviceListAdapter() {
			super();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = BLESearchActivity.this.getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device) {
			if (!mLeDevices.contains(device)) {
				mLeDevices.add(device);
			}
		}

		public BluetoothDevice getDevice(int position) {
			return mLeDevices.get(position);
		}

		public void clear() {
			mLeDevices.clear();
		}

		@Override
		public int getCount() {
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i) {
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			if (view == null) {
				view = mInflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view
						.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view
						.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}
			BluetoothDevice device = mLeDevices.get(i);
			final String deviceName = device.getName();
			if (deviceName != null && deviceName.length() > 0)
				viewHolder.deviceName.setText(deviceName);
			else
				viewHolder.deviceName.setText("未知设备");
			viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.search:// 搜索
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			mLeDeviceListAdapter.clear();
			mLeDeviceListAdapter.notifyDataSetChanged();
			scanLeDevice(true);
			break;

		default:
			break;
		}
	}

	/**
	 * 点击蓝牙设备列表获取蓝牙地址信息并跳转到数据控制view
	 */
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		final BluetoothDevice device = mLeDeviceListAdapter.getDevice(arg2);
		if (device == null) {
			return;
		}
		Intent in = new Intent(BLESearchActivity.this, MainActivity.class);
		in.putExtra("adress",device.getAddress());
		startActivity(in);
	}
}