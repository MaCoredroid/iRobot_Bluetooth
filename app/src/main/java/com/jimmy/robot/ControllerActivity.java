package com.jimmy.robot;

import android.support.v7.app.AppCompatActivity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class ControllerActivity extends AppCompatActivity {

    public static int speed=0;
    public static int speedl=0;
    public static int speedr=0;
    public static int step=50;
    public static int steprot=60;

    private TextView textl,textr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Button BtnMode=(Button)findViewById(R.id.BtnMode);
        Button BtnFor=(Button)findViewById(R.id.BtnFor);
        Button BtnBack=(Button)findViewById(R.id.BtnBack);
        Button BtnRight=(Button)findViewById(R.id.BtnRight);
        Button BtnLeft=(Button)findViewById(R.id.BtnLeft);
        Button BtnStop=(Button)findViewById(R.id.BtnStop);
        Button BtnClean=(Button)findViewById(R.id.BtnClean );
        Button BtnStopClean=(Button)findViewById(R.id.BtnStopClean);
        Button BtnFull=findViewById(R.id.BtnFul);
        Button Btnbrush=findViewById(R.id.Btnbrush);
        textl=(TextView)findViewById(R.id.textl);
        textr=(TextView)findViewById(R.id.textr);

        BtnMode.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                send("84");
            }
        });
        BtnFull.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                send("9200ff00ff");
                speedl=speedr=255;
                updateSpeed();
            }
        });
        BtnFor.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(speedl!=speedr){
                    speed=(speedl+speedr)/2;
                    speedr=speed;
                    speedl=speed;
                }else if(speedl>=255){
                    speedl=255;
                    speedr=255;
                }else{
                    speedl+=step;
                    speedr+=step;
                }
                send("92"+ToHex(speedr)+ToHex(speedl));
                updateSpeed();

            }
        });
        BtnClean.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                send("9059007f");

            }
        });
        BtnStopClean.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                send("90000000");

            }
        });
        Btnbrush.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                send("907f7f00");

            }
        });

        BtnBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(speedl!=speedr){
                    speed=(speedl+speedr)/2;
                    speedr=speed;
                    speedl=speed;
                }else if(speedl<=-200){
                    speedl=-200;
                    speedr=-200;
                }else{
                    speedl-=step;
                    speedr-=step;
                }
                send("92"+ToHex(speedr)+ToHex(speedl));
                updateSpeed();
            }
        });
        BtnRight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                speedl+=steprot;
                speedr-=steprot;
                if(speedl>=250){
                    speedl-=steprot;
                    speedr-=steprot;
                }else if(speedr<=-200){
                    speedr+=steprot;
                    speedl+=steprot;
                }
                send("92"+ToHex(speedr)+ToHex(speedl));
                updateSpeed();
            }
        });
        BtnLeft.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                speedl-=steprot;
                speedr+=steprot;
                if(speedr>=250){
                    speedl-=steprot;
                    speedr-=steprot;
                }else if(speedl<=-200){
                    speedr+=steprot;
                    speedl+=steprot;
                }
                send("92"+ToHex(speedr)+ToHex(speedl));
                updateSpeed();
            }
        });
        BtnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                speedl=0;speedr=0;
                send("9200000000");
                updateSpeed();
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
    public void updateSpeed(){
        textl.setText("Left "+speedl);
        textr.setText("Right "+speedr);
    }
    public void send(String s){
        if(BleServe.getState()!=BluetoothService.STATE_CONNECTED)
        {
            msg("Cannot send unconnected");
            return;
        }
        BleServe.write(StringtoBytes(s));
        //msg("SH:"+s);
    }
    public String ToHex(int n){
        String s;
        if(n>=0){
            s=Integer.toString(n, 16);
        }else {
            s=Integer.toString(-256-n, 16);
        }

        switch (s.length()){
            case 4:break;
            case 3:s="0"+s;break;
            case 2:s="00"+s;break;
            case 1:s="000"+s;break;
            default:s="0000";break;}



        return s;
    }

    public void msg(String s){
        Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
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
    public BluetoothService BleServe=null;
    private static class MyHandler extends Handler {

        //对Activity的弱引用
        private final WeakReference<ControllerActivity> mActivity;

        public MyHandler(ControllerActivity activity){
            mActivity = new WeakReference<ControllerActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            ControllerActivity activity=(ControllerActivity)mActivity.get();
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
                    readMessage = ControllerActivity.bytes2String(readBuf, msg.arg1);
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
