package com.jimmy.robot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.jimmy.robot.BluetoothService;

public class MainActivity extends AppCompatActivity {
    //ui
    private TextView TextLog;
    private Button BtnSend;
    private EditText EditSend;
    private CheckBox ChkHex;
    //Bluetooth
    private BluetoothAdapter adapter;
    private List<BluetoothDevice> mDevices=new ArrayList<>();
    private Boolean LocPermisOK=false;
    final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION=0x201;


    //蓝牙发现广播
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                mDevices.add(device);
                log(device.getName() + "|Addr:" + device.getAddress());


            }
            else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
            {
                log("Discovery started.");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                log("Discovery finished.");
            }
        }
    };

    public void log(String s) {
        Date dt = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");
        TextLog.append("[" + ft.format(dt) + "]" + s + "\n");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ui
        setContentView(R.layout.activity_main);
        TextLog = (TextView) findViewById(R.id.textView);
        log("start");

        //获取位置权限，以允许蓝牙发现设备
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }else{
                LocPermisOK=true;
            }
        }

        //配置广播
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        IntentFilter filterStart = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(mReceiver, filterStart);

        IntentFilter filterFinish = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filterFinish);

        //初始化&启动蓝牙
        initBlue();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    private void QuitQuestion() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("退出")//设置对话框的标题
                .setMessage("是否退出")//设置对话框的内容
                //设置对话框的按钮
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "取消", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "退出", Toast.LENGTH_SHORT).show();
                        finish();
                        System.exit(0);
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }
    public void showDeviceChooseDialog()
    {
        List<String> devNames=new ArrayList<>();
        for(BluetoothDevice dev : mDevices)
        {
            devNames.add(dev.getName()+"\n"+dev.getAddress());
        }
        new  AlertDialog.Builder(this)
                .setTitle("请选择" )
                .setSingleChoiceItems(devNames.toArray(new String[devNames.size()]),  0 ,
                        new  DialogInterface.OnClickListener() {
                            public   void  onClick(DialogInterface dialog,  int  which) {
                                Intent ContactIntent=new Intent(getApplicationContext(),ContactActivity.class);
                                ContactIntent.putExtra("device",mDevices.get(which));
                                startActivity(ContactIntent);
                            }
                        }
                )
                .setNegativeButton("取消" ,  null )
                .show();
    }
    public void showDeviceChooseDialog1()
    {
        List<String> devNames=new ArrayList<>();
        for(BluetoothDevice dev : mDevices)
        {
            devNames.add(dev.getName()+"\n"+dev.getAddress());
        }
        new  AlertDialog.Builder(this)
                .setTitle("请选择" )
                .setSingleChoiceItems(devNames.toArray(new String[devNames.size()]),  0 ,
                        new  DialogInterface.OnClickListener() {
                            public   void  onClick(DialogInterface dialog,  int  which) {
                                Intent CtrlIntent=new Intent(getApplicationContext(),ControllerActivity.class);
                                CtrlIntent.putExtra("device",mDevices.get(which));
                                startActivity(CtrlIntent);
                            }
                        }
                )
                .setNegativeButton("取消" ,  null )
                .show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_settings:
                Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.main_menu_contact:
                //Toast.makeText(this, "Contact", Toast.LENGTH_SHORT).show();
                showDeviceChooseDialog1();
                return true;
            case R.id.main_menu_quit:
                Toast.makeText(this, "Quit", Toast.LENGTH_SHORT).show();
                QuitQuestion();
                return true;
            case R.id.main_menu_scan:
                findBot();
                return true;
            case R.id.main_menu_connect:
                showDeviceChooseDialog();
                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==0x101)
        {
            if(resultCode==RESULT_OK)
            {
                log("Bluetooth OK.");
            }
            else{
                log("Bluetooth failed.");
            }
        }
    }

    public void initBlue() {
        //获取默认蓝牙
        adapter = BluetoothAdapter.getDefaultAdapter();

        //无法找到蓝牙
        if (adapter == null) {
            Toast.makeText(this, "无法找到蓝牙设备", Toast.LENGTH_SHORT).show();
            log("E:No bluetooth adapter!");
            return;
        }
        //启动蓝牙，如果没有启动
        if (!adapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0x101);
            log("Starting Bluetooth...");
        }else{
            log("Bluetooth OK!");
        }

        Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            log("Paired devices.");
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                log( "-"+device.getName() + ":" + device.getAddress());
            }
        }
    }
    public void findBot()
    {
        if(null==adapter)
        {
            Toast.makeText(this,"无可用蓝牙设备",Toast.LENGTH_SHORT).show();
            return;
        }
        if(true!=LocPermisOK)
        {

            Toast.makeText(this,"没有位置权限，无法搜索蓝牙设备",Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.startDiscovery();
        log("Start discover");
    }
    public BluetoothServerSocket OpenServerSocket(String name,UUID uuid)
    {
        try {
            return adapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
        }
        catch (IOException e)
        {
            log("E:Listen as server."+e.getMessage());
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    log("Coarse Location Permission granted.");
                    LocPermisOK=true;
                }else{
                    log("CoarseLocation Permission failed.");
                }

                break;
        }
    }
}
