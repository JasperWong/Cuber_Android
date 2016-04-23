package com.jasper.cuber;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import communication.BluetoothRfcommClient;


public class Control_vertical extends Activity {

    public AudioManager audiomanage;
    public SeekBar soundBar;
    private int maxVolume, currentVolume;

    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private String song_title;
    private int position;
    private SeekBar music_seekbar;
    private String song_path;
    private String artist;
    private String album_id;

    private Button mBLE;
    private Button mPauseButton;
    private GetSong mGetSong;
    private String[] mPreviousSongData = new String[5];
    // private ArrayList<String> mNextSongData;
    private String[] mNextSongData = new String[5];
    private Button mlist;
    public static ImageView mUp;
    public static ImageView mDown;
    public static ImageView mLeft;
    public static ImageView mRight;
    public final static int joy_up=1;
    public final static int joy_down=2;
    public final static int joy_left=3;
    public final static int joy_right=4;
    public final static int Control_Portrait=1;
    public final static int Control_land=2;
    JoystickView joystick=null;

    private ProgressDialog mBluetoothDialog;
    private ProgressDialog connDevice;
    private AlertDialog detectDialog;
    private ArrayList<BluetoothDevice> devices=new ArrayList<BluetoothDevice>();
    private final static String ON = "ON";
    private List<Map<String, Object>> datalist;
    private BluetoothAdapter mBluetoothAdapter;
    public static BluetoothRfcommClient mRfcommClient =null;
    public static int Mark=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_control_vertical);


        final SeekBar soundBar = (SeekBar) findViewById(R.id.seekBar1); // 音量设置
        audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
        soundBar.setMax(maxVolume); // 拖动条最高值与系统最大声匹配
        currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
        soundBar.setProgress(currentVolume);

        mBLE=(Button)findViewById(R.id.btn_bluetooth);
        mGetSong = new GetSong(this);
        mlist=(Button)findViewById(R.id.btn_list);
        mUp=(ImageView)findViewById(R.id.up);
        mDown=(ImageView)findViewById(R.id.down);
        mLeft=(ImageView)findViewById(R.id.left);
        mRight=(ImageView)findViewById(R.id.right);
        mPauseButton = (Button) findViewById(R.id.pause);
        joystick =(JoystickView)findViewById(R.id.joystick_view);
        datalist = new ArrayList<Map<String, Object>>();

        connDevice = new ProgressDialog(Control_vertical.this);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothDialog = new ProgressDialog(Control_vertical.this);
        datalist = new ArrayList<Map<String, Object>>();
        final AlertDialog.Builder builder = new AlertDialog.Builder(Control_vertical.this);



//        detectDevice();

        mBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v1) {
                Log.d("1","1");
                detectDevice();
                builder.show();
            }
        });

        mlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1=new Intent(Control_vertical.this,MusicActivity.class);
                intent1.putExtra("Mark",Control_Portrait);
                startActivity(intent1);
                Mark=Control_Portrait;
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);

            }
        });

        joystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ArrowChange();
                return false;

            }


        });

        soundBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() // 调音监听器
        {
            public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
                soundBar.setProgress(currentVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            // Show the Up button in the action bar.
//            getActionBar().setDisplayHomeAsUpEnabled(true);
//        }
        Intent it = getIntent();
//        mAlbumCover = (ImageView) findViewById(R.id.album_cover);
        song_path = it.getStringExtra("song_path");
        song_title = it.getStringExtra("song_title");
        artist = it.getStringExtra("artist");
        if(song_path==null)
        {
            mNextSongData = mGetSong.getPreviousSongData(song_title); // 取回下首歌的title和path,下标分别为0,
                // 1
//                    }
            song_title = mNextSongData[0];
            song_path = mNextSongData[1];
            }

//        titleText = (TextView) findViewById(R.id.song_title);
//        titleText.setText(song_title);

//        artistText = (TextView) findViewById(R.id.artist);
//        artistText.setText(artist);

//        progressText = (TextView) findViewById(R.id.song_progress);
//        durationText = (TextView) findViewById(R.id.song_duration);
        if (song_path != null) {
//            setAlbumCover();
        }
        play(0);

        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);
        music_seekbar = (SeekBar) this.findViewById(R.id.music_seekbar);
        // mProgressbar = (ProgressBar) findViewById(R.id.progressBar1);
        music_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress, boolean fromUser) {
                        if (fromUser == true) {
                            mMediaPlayer.seekTo(progress);
                            handler.post(updateThread);
                        }
                    }
                });
        mMediaPlayer
                .setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        handler.removeCallbacks(updateThread);
                        music_seekbar.setProgress(0);
//                        progressText.setText("0:00");
                        mPauseButton.setBackgroundResource(R.drawable.pause);
                    }
                });



    }

    Handler handler = new Handler();

    Runnable updateThread = new Runnable() {

        @Override
        public void run() {
            position = mMediaPlayer.getCurrentPosition();
            music_seekbar.setProgress(position); // 获得歌曲当前的播放位置并设置成播放进度条的值
//            progressText.setText(getTime(position));
            handler.postDelayed(updateThread, 100);
        }

    };

    private void ArrowChange(){

        if(joystick.GetSituationY()==joy_up){
            Control_vertical.mUp.setImageResource(R.drawable.up_1);
            Control_vertical.mDown.setImageResource(R.drawable.down_0);
        }
        else if(joystick.GetSituationY()==joy_down){
            Control_vertical.mUp.setImageResource(R.drawable.up_0);
            Control_vertical.mDown.setImageResource(R.drawable.down_1);
        }

        else{
            Control_vertical.mUp.setImageResource(R.drawable.up_0);
            Control_vertical.mDown.setImageResource(R.drawable.down_0);
        }
        if(joystick.GetSituationX()==joy_right){
            Control_vertical.mRight.setImageResource(R.drawable.right_1);
            Control_vertical.mLeft.setImageResource(R.drawable.left_0);
        }
        else if (joystick.GetSituationX()==joy_left){
            Control_vertical.mRight.setImageResource(R.drawable.right_0);
            Control_vertical.mLeft.setImageResource(R.drawable.left_1);
        }
        else{
            Control_vertical.mRight.setImageResource(R.drawable.right_0);
            Control_vertical.mLeft.setImageResource(R.drawable.left_0);
        }
    }

    private final class MyPhoneListener extends PhoneStateListener {

        /*
         * (non-Javadoc)
         *
         * @see android.telephony.PhoneStateListener#onCallStateChanged(int,
         * java.lang.String)
         */
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING: // 来电
                    if (mMediaPlayer.isPlaying()) {
                        position = mMediaPlayer.getCurrentPosition();
                        mMediaPlayer.stop();
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    if (position > 0 && song_path != null) {
                        play(position);
                        position = 0;
                    }
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_control_vertical, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void mediaplay(View v) {
        switch (v.getId()) {
//             case R.id.playbutton:
//             play(0);
//             break;
            case R.id.pause:
                handler.post(updateThread);
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause(); // 暂停
                    // pause = true;
                    mPauseButton.setBackgroundResource(R.drawable.play);
                } else {
                    mMediaPlayer.start(); // 继续播放
                    // pause = false;
                    mPauseButton.setBackgroundResource(R.drawable.pause);
                }
                break;
            case R.id.previous:
                if (!mMediaPlayer.isPlaying()) {
//                    mPauseButton = (ImageButton) (this.findViewById(R.id.pause));
//                    mPauseButton
//                            .setImageResource(android.R.drawable.ic_media_pause);
                    mPauseButton.setBackgroundResource(R.drawable.pause);
                    // pause = false;
                } // 如果不处于播放状态，并将暂停按钮状态设置为点击暂停
                music_seekbar.setProgress(0); // 不管怎样，进度条始终回到0
                if (mMediaPlayer.getCurrentPosition() > 5000) {
                    mMediaPlayer.seekTo(0); // 歌曲从头播放
                } else {

                }
//                    if (album_id != null) {
//                        mPreviousSongData = mGetSong.getPreviousAlbumSongData(
//                                song_title, album_id);
//                    }
//                    else {
                mPreviousSongData = mGetSong
                        .getPreviousSongData(song_title); // 取回下首歌的title和path,下标分别为0,
                // 1
//                    }
                song_title = mPreviousSongData[0];
                song_path = mPreviousSongData[1];
//                    artist = mPreviousSongData[2];
//                    titleText.setText(song_title);
//                    artistText.setText(artist);
                mMediaPlayer.stop();
//                }
//                if (song_path != null) {
//                    setAlbumCover();
//                }
                play(0);
			/*
			 * if (music_seekbar.getProgress() > 5000) { mMediaPlayer.seekTo(0);
			 * // 歌曲从头播放 if (!mMediaPlayer.isPlaying()) { mPauseButton =
			 * (ImageButton) (this.findViewById(R.id.pause));
			 * mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
			 * play(0); // pause = false; } //如果不处于播放状态，启用播放，并将暂停按钮状态设置为点击暂停 }
			 * else { if (!mMediaPlayer.isPlaying()) { mPauseButton =
			 * (ImageButton) (this.findViewById(R.id.pause));
			 * mPauseButton.setImageResource(android.R.drawable.ic_media_pause);
			 * // pause = false; } //如果不处于播放状态，将暂停按钮状态设置为点击暂停，停止播放后切换到上一首
			 * mPreviousSongData = mGetSong.getPreviousSongData(SONG_TITLE);
			 * //取回下首歌的title和path,下标分别为0, 1 SONG_TITLE = mPreviousSongData[0];
			 * SONG_PATH = mPreviousSongData[1]; mTextView.setText(SONG_TITLE);
			 * mMediaPlayer.stop(); // 停止播放 play(0); }
			 */
                break;
            case R.id.next:
//                if (album_id != null) {
//                    mNextSongData = mGetSong.getNextAlbumSongData(song_title,
//                            album_id);
//                } else {
                mNextSongData = mGetSong.getNextSongData(song_title); // 取回下首歌的title和path,下标分别为0,
                // 1
//                }
                song_title = mNextSongData[0];
                song_path = mNextSongData[1];
                song_title = mNextSongData[0];
                song_path = mNextSongData[1];
                if (!mMediaPlayer.isPlaying()) {
//                    mPauseButton = (ImageButton) (this.findViewById(R.id.pause));
                    mPauseButton.setBackgroundResource(R.drawable.play);
                }
//                if (song_path != null) {
//                    setAlbumCover();
//                }
                play(0);
			/*
			 * Toast.makeText(getApplicationContext(), "即将播放:" + SONG_TITLE ,
			 * Toast.LENGTH_LONG) .show();
			 */
                break;
        }
    }


    private String getTime(int t) {
        String mTime;
        int time[] = new int[2];
        time[0] = (int) (t / 60000); // 分
        time[1] = (int) (t / 1000 - time[0] * 60); // 秒
        if (time[1] < 10) {
        mTime = Integer.toString(time[0]) + ":0"
        + Integer.toString(time[1]);
        } else {
        mTime = Integer.toString(time[0]) + ":" + Integer.toString(time[1]);
        }
        return mTime;
        }

    private void play(int position) {
        try {
            mMediaPlayer.reset(); // 把各项参数恢复初始状态
            mMediaPlayer.setDataSource(song_path);
            mMediaPlayer.prepare(); // 进行缓冲
            mMediaPlayer.setOnPreparedListener(new PrepareListener(position));
//            progressText.setText(getTime(position));
//            durationText.setText(getTime(mMediaPlayer.getDuration()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



   private final class PrepareListener implements MediaPlayer.OnPreparedListener {

        private int position;

        public PrepareListener(int position) {
            this.position = position;
        }

        @Override
        public void onPrepared(MediaPlayer arg0) {
            mMediaPlayer.start(); // 开始播放
            music_seekbar.setMax(mMediaPlayer.getDuration()); // 获取歌曲的长度并设置成播放进度条的最大值
            // mProgressbar.setMax(mMediaPlayer.getDuration());
            handler.post(updateThread);
            if (position > 0) {
                mMediaPlayer.seekTo(position);
            }
        }
    }

    private void detectDevice() {

        if (mBluetoothAdapter.isEnabled()) {

            IntentFilter filter = new IntentFilter();
            mBluetoothAdapter.startDiscovery();

            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            registerReceiver(mReceiver, filter);
            mRfcommClient = new BluetoothRfcommClient(Control_vertical.this, null);
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Map<String, Object> map = new HashMap<String, Object>();
            String action = intent.getAction();
            final AlertDialog.Builder detectBuilder = new AlertDialog.Builder(Control_vertical.this);

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                mBluetoothDialog.setTitle("Bluetooth");
                mBluetoothDialog.setMessage("Scaning..");
                mBluetoothDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                });
                mBluetoothDialog.show();
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("FG_Bluetooth", "Found:" + device.getName());
                Toast.makeText(Control_vertical.this, "FOUND:" + device.getName(), Toast.LENGTH_SHORT).show();
                try {
                    map.clear();
                    map.put("Name", device.getName().toString());
                    map.put("Address", device.getAddress().toString());
                    datalist.add(map);
                    devices.add(device);
                } catch (NullPointerException e) {
                    map.clear();
                    map.put("Name", "Unknown device");
                    map.put("Address", device.getAddress().toString());
                    datalist.add(map);
                    devices.add(device);
                }
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                unregisterReceiver(mReceiver);
                mBluetoothDialog.dismiss();
                mBluetoothAdapter.cancelDiscovery();
                if (datalist.size() != 0) {
                    detectBuilder.setTitle("please choose");
                    LayoutInflater inflater = LayoutInflater.from(Control_vertical.this);
                    View view = inflater.inflate(R.layout.bluetooth_listview, null);
                    ListView listview = (ListView) view.findViewById(R.id.bluetooth);
                    detectBuilder.setView(view);
                    SimpleAdapter sim_arr = new SimpleAdapter(Control_vertical.this,
                            datalist,
                            R.layout.bluetooth_result,
                            new String[]{"Name", "Address"},
                            new int[]{R.id.DeviceName, R.id.DeviceAddress});
                    listview.setAdapter(sim_arr);
                    detectBuilder.setCancelable(false);
                    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            pairDevice(position);
                        }
                    });
                    detectBuilder.setPositiveButton("重新搜索", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            datalist.clear();
                            devices.clear();
                            detectDevice();
                        }
                    });
                    detectBuilder.setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            System.exit(0);
                        }
                    });
                    detectDialog = detectBuilder.show();

                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(Control_vertical.this);
                    mBluetoothAdapter.cancelDiscovery();
                    builder.setTitle("蓝牙");
                    builder.setMessage("找不到蓝牙设备，请重试！");
                    builder.setPositiveButton("重新搜索", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            datalist.clear();
                            devices.clear();
                            detectDevice();
                        }
                    });
////                    builder.setNegativeButton("退出程序", new DialogInterface.OnClickListener() {
////                        @Override
////                        public void onClick(DialogInterface dialog, int which) {
////                            finish();
////                            System.exit(0);
////                        }
////                    });
//                    builder.setCancelable(false);
                    builder.show();
                }
            }
        }
    };

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
        Control_vertical.mRfcommClient.connect(device);
        connDevice.dismiss();
        Intent intent = new Intent(Control_vertical.this, MusicActivity.class);
        Toast.makeText(Control_vertical.this, "配对成功，初始化完成", Toast.LENGTH_SHORT).show();
        startActivity(intent);
        finish();
    }
}