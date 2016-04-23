package com.jasper.cuber;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import communication.BluetoothRfcommClient;


public class StartAcitivty extends Activity {

    private BluetoothAdapter mBluetoothAdapter;
    private final static String OFF = "OFF";
    private final static String ON = "ON";
    public static BluetoothRfcommClient mRfcommClient =null;
    private ProgressDialog mBluetoothDialog;
    private ProgressDialog connDevice;
    private AlertDialog detectDialog;
    private List<Map<String, Object>> datalist;
    private ArrayList<BluetoothDevice> devices=new ArrayList<BluetoothDevice>();
    Timer timer = new Timer();
//    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_start);

        mBluetoothDialog = new ProgressDialog(StartAcitivty.this);
        connDevice = new ProgressDialog(StartAcitivty.this);
        datalist = new ArrayList<Map<String, Object>>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initBluetooth();
    }


    private void initBluetooth() {
        if (mBluetoothAdapter == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(StartAcitivty.this);
            builder.setTitle("抱歉！");
            builder.setMessage("本系统需要手机拥有蓝牙模块，系统检测到本机没有蓝牙模块，因此无法继续，程序将退出！");
            builder.setNegativeButton("好的", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    System.exit(0);
                }
            });
            builder.setCancelable(false);
            builder.show();
        }
        else if (!mBluetoothAdapter.isEnabled()) {
            setHardWareAlertDialog("Bluetooth");
        }
        else {
//            detectDevice();
                timer.schedule(task_turnToContorl, 1500);
        }
    }



    private void setHardWareAlertDialog(String HardWare) {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartAcitivty.this);
        builder.setCancelable(false);
        builder.setTitle("温馨提示");

        if (HardWare.equals("Bluetooth")) {
            builder.setMessage("蓝牙尚未打开，是否打开？");
            builder.setPositiveButton("打开蓝牙", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mSwitch(ON, null, null);
                }
            });
        }
        builder.show();
    }

    //    ---------------------------------------蓝牙调控------------------------------------------------
//    private void detectDevice() {
//
//        if (mBluetoothAdapter.isEnabled()) {
//
//            IntentFilter filter = new IntentFilter();
//            mBluetoothAdapter.startDiscovery();
//
//            filter.addAction(BluetoothDevice.ACTION_FOUND);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
//            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//            registerReceiver(mReceiver, filter);
//            mRfcommClient = new BluetoothRfcommClient(StartAcitivty.this, null);
//        }
//    }
//
//    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            Map<String, Object> map = new HashMap<String, Object>();
//            String action = intent.getAction();
//            final AlertDialog.Builder detectBuilder = new AlertDialog.Builder(StartAcitivty.this);
//
//            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
//                mBluetoothDialog.setTitle("蓝牙");
//                mBluetoothDialog.setMessage("正在搜寻蓝牙设备...");
//                mBluetoothDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                    @Override
//                    public void onCancel(DialogInterface dialog) {
//                        mBluetoothAdapter.cancelDiscovery();
//                    }
//                });
//                mBluetoothDialog.show();
//            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                Log.i("FG_Bluetooth", "Found:" + device.getName());
//                Toast.makeText(StartAcitivty.this, "FOUND:" + device.getName(), Toast.LENGTH_SHORT).show();
//                try {
//                    map.clear();
//                    map.put("Name", device.getName().toString());
//                    map.put("Address", device.getAddress().toString());
//                    datalist.add(map);
//                    devices.add(device);
//                } catch (NullPointerException e) {
//                    map.clear();
//                    map.put("Name", "未知设备");
//                    map.put("Address", device.getAddress().toString());
//                    datalist.add(map);
//                    devices.add(device);
//                }
//            }
//            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                unregisterReceiver(mReceiver);
//                mBluetoothDialog.dismiss();
//                mBluetoothAdapter.cancelDiscovery();
//                if (datalist.size() != 0) {
//                    detectBuilder.setTitle("选择蓝牙设备");
//                    LayoutInflater inflater = LayoutInflater.from(StartAcitivty.this);
//                    View view = inflater.inflate(R.layout.bluetooth_listview, null);
//                    ListView listview = (ListView) view.findViewById(R.id.bluetooth);
//                    detectBuilder.setView(view);
//                    SimpleAdapter sim_arr = new SimpleAdapter(StartAcitivty.this,
//                            datalist,
//                            R.layout.bluetooth_result,
//                            new String[]{"Name", "Address"},
//                            new int[]{R.id.DeviceName, R.id.DeviceAddress});
//                    listview.setAdapter(sim_arr);
//                    detectBuilder.setCancelable(false);
//                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                        @Override
//                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                            pairDevice(position);
//                        }
//                    });
//                    detectBuilder.setPositiveButton("重新搜索", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            datalist.clear();
//                            devices.clear();
//                            detectDevice();
//                        }
//                    });
//                    detectBuilder.setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                            System.exit(0);
//                        }
//                    });
//                    detectDialog = detectBuilder.show();
//
//                } else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(StartAcitivty.this);
//                    mBluetoothAdapter.cancelDiscovery();
//                    builder.setTitle("蓝牙");
//                    builder.setMessage("找不到蓝牙设备，请重试！");
//                    builder.setPositiveButton("重新搜索", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            datalist.clear();
//                            devices.clear();
//                            detectDevice();
//                        }
//                    });
//                    builder.setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            finish();
//                            System.exit(0);
//                        }
//                    });
//                    builder.setCancelable(false);
//                    builder.show();
//                }
//            }
//        }
//    };

    private void pairDevice(int position) {
        detectDialog.dismiss();

        connDevice.setTitle("蓝牙");
        BluetoothDevice device = devices.get(position);
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            connDevice.setMessage("正在尝试配对 " + datalist.get(position).get("Name"));
            connDevice.setCancelable(false);
            connDevice.show();
            connectDevice(device);
        }
        else {
            connDevice.setMessage("正在尝试配对 " + datalist.get(position).get("Name"));
            connDevice.setCancelable(false);
            connDevice.show();
            try {
                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                method.invoke(device, (Object[]) null);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void connectDevice(BluetoothDevice device) {
       StartAcitivty.mRfcommClient.connect(device);
        connDevice.dismiss();
        Intent intent = new Intent(StartAcitivty.this, MusicActivity.class);
        Toast.makeText(StartAcitivty.this, "配对成功，初始化完成", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    ----------------------------------------总开关-------------------------------------------------

    private void mSwitch(String Bluetooth, String GPS, String Internet) {
        //蓝牙
        if (Bluetooth == "ON") {
            mBluetoothAdapter.enable();
            while (!mBluetoothAdapter.isEnabled()) {
            }
            Log.i("FG_System", "蓝牙打开");
            timer.schedule(task_turnToContorl, 1500);
//            detectDevice();
        }

        else if (Bluetooth == "OFF") {
            mBluetoothAdapter.disable();
            Log.i("FG_System", "蓝牙关闭");
        }

    }

    TimerTask task_turnToContorl = new TimerTask(){
        public void run(){
            Intent it2=new Intent(StartAcitivty.this,Control_vertical.class);
            startActivity(it2);
            finish();
        }
    };
//    -------------------------------------文件读写系统-----------------------------------------------

    //小型数据库写文件
    private void record (String FileName, String Key, String FileContent){
        SharedPreferences preferences = getSharedPreferences(FileName, MODE_APPEND);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Key, FileContent);
        editor.commit();
    }

    //小型数据库读文件

    private String read(String FileName, String Key) {
        String content = null;
        SharedPreferences preferences = getSharedPreferences(FileName, 0);
        content = preferences.getString(Key, "无");
        return content;
    }

//    --------------------------------------生命周期-------------------------------------------------

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
}

