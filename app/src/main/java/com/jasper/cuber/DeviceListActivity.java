package com.jasper.cuber;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class DeviceListActivity extends Activity {
	private ListView mListView;
	private DeviceListAdapter mAdapter;
	private ArrayList<BluetoothDevice> mDeviceList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//����ģʽ
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//��ֹ��Ļ�䰵
		setContentView(R.layout.activity_paired_devices);
		
		mDeviceList		= getIntent().getExtras().getParcelableArrayList("device.list");
		
		mListView		= (ListView) findViewById(R.id.lv_paired);
		
		mAdapter		= new DeviceListAdapter(this);
		
		mAdapter.setData(mDeviceList);

		mAdapter.setListener(new DeviceListAdapter.OnPairButtonClickListener() {
			@Override
			public void onPairButtonClick(int position) {

				BluetoothDevice device = mDeviceList.get(position);

				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    showToast("Connecting...");
                    connectDevice(device);
//                    Intent startintent=new Intent(DeviceListActivity.this, ActivityMode.class);
//                    startActivity(startintent);
                    finish();


				}

                else
                {

					showToast("Pairing...");

					pairDevice(device);
				}
			}
		});
		
		mListView.setAdapter(mAdapter);
		
		registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
	}
	

	public void onDestroy() {
        super.onDestroy();
		unregisterReceiver(mPairReceiver);
	}
	
	
	private void showToast(String message) {
		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
	}
	
    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectDevice(BluetoothDevice device){
//        MainActivity.mRfcommClient.connect(device);
    }
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        
	        if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {

	        	 final int state 		= intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
	        	 final int prevState	= intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
	        	 
	        	 if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
	        		 showToast("Paired");
	        	 } else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
	        		 showToast("Unpaired");
	        	 }
	        	 
	        	 mAdapter.notifyDataSetChanged();
	        }
	    }
	};
}