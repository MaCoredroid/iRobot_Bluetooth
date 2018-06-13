package com.jimmy.robot;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ContactActivity extends Activity {
    private TextView TextMsg;
    private Button BtnSend;
    private CheckBox ChkHex;
    private EditText EditSend;
    private List<String> mData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        TextMsg=(TextView)findViewById(R.id.TextContact);
        BtnSend=(Button)findViewById(R.id.SendButton);
        ChkHex=(CheckBox)findViewById(R.id.HexCheck);
        EditSend=(EditText)findViewById(R.id.SendInput);
        Button BtnCtrl=(Button)findViewById(R.id.BtnCtrl);

        BtnCtrl.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

            }
        });
        BtnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String s=EditSend.getText().toString();
                if(BleServe.getState()!=BluetoothService.STATE_CONNECTED)
                {
                    msg("Cannot send unconnected");
                    return;
                }
                if(ChkHex.isChecked())
                {
                    BleServe.write(StringtoBytes(s));
                    msg("SH:"+s);
                }
                else{
                    BleServe.write(s.getBytes());
                    msg("S:"+s);
                }
            }
        });
        BleServe=new BluetoothService(this,mHandler);
        BluetoothDevice dev=(BluetoothDevice) getIntent().getExtras().get("device");
        if(dev!=null) {
            //Toast.makeText(this, "device in contact:" + dev.getName() + ":" + dev.getAddress(), Toast.LENGTH_SHORT).show();
            BleServe.connect(dev, true);
        }
        else
        {
            Toast.makeText(this,"Invailed device!",Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    public void msg(String s) {
        Date dt = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("HH:mm:ss");
        TextMsg.append("[" + ft.format(dt) + "]" + s + "\n");
    }

    public static String bytes2String(byte[] data,int len){
        StringBuilder ss=new StringBuilder();
        for(int i = 0; i < len; i++){
            ss.append(String.format("%02X", data[i]));
        }
        return ss.toString();

    }


    public static byte[] StringtoBytes(String data){

        if(data == null || data.equals("")){

            return null;

        }

        data = data.toUpperCase();

        char[] datachar = data.toCharArray();

        byte[] getbytes = new byte[data.length() / 2];

        for( int i = 0; i < data.length()/2 ; i++){
            int pos = i * 2;

            getbytes[i] = (byte) (charToByte(datachar[pos]) << 4 | charToByte(datachar[pos + 1]));

        }

        return getbytes;

    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }



    //Bluetooth service
    public static BluetoothService BleServe=null;
    private static class MyHandler extends Handler {

        //对Activity的弱引用
        private final WeakReference<ContactActivity> mActivity;

        public MyHandler(ContactActivity activity){
            mActivity = new WeakReference<ContactActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ContactActivity activity=(ContactActivity)mActivity.get();
            switch (msg.what) {
                case Constants.MSG_STATE_CHANGED:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            activity.msg("B:Connected");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            activity.msg("B:connecting");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            activity.finish();
                            break;
                    }
                    break;
                case Constants.MSG_WRITE:
                    break;
                case Constants.MSG_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage;
                    if(activity.ChkHex.isChecked()) {
                        readMessage = ContactActivity.bytes2String(readBuf, msg.arg1);
                    }else {
                        readMessage= new String(readBuf, 0, msg.arg1);
                    }
                    activity.msg("R:" + readMessage);
                    break;
                case Constants.MSG_DEVICE_NAME:
                    // save the connected device's name
                    activity.msg("Connected to "+  msg.getData().getString("divece_name"));
                    break;
                case Constants.MSG_TOAST:
                    Toast.makeText(activity, msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
    private final MyHandler mHandler = new MyHandler(this);

}
