package com.example.faioo.socketdemo;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
//import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    //sensor
    private SensorManager sm;
    int count=0;
    String mfileName = "123.txt" ;
    public String path = "/storage/emulated/0/Download";
    private static final String TAG = "sensor";

    //端口号
    private static final int PORT = 30000;
    public ServerSocket ss;
    WifiManager wifiManager;

    private static final int asd = 1; //标记是服务器0还是客户端1

    //在主线程中定义Handler传入子线程用于更新TextView
    private Handler mHandler;

    private ClientThread mClientThread; //客户端线程
    private ServerThread mServerThread; //服务器线程

    // UI框架
    private Button btn;
    private TextView show;
    //定义messageb
    public static final int MSG_REV = 0;//接收
    public static final int MSG_SEND = 1;//发送
    public static final int MSG_START = 2;//开始跑数据

    //所有socket的Arraylist
    //public static ArrayList<Socket> socketArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_REV) {
                    show.append("\n" + msg.obj.toString());
                    start_sensor();
                }
            }
        };
        switch (asd)
        {
            case 0:
                //服务器端代码
                //WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
                //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                //String IPAddress = intToIp(wifiInfo.getIpAddress());
                //show.setText(IPAddress);
                mServerThread = new ServerThread(PORT, mHandler);
                new Thread(mServerThread).start();
//                new Thread()
//                {
//                    @Override
//                    public void run()
//                    {
//                        try {
//                            ss = new ServerSocket(PORT);
//                            while (true)
//                            {
//                                //阻塞
//                                Socket s = ss.accept();
//                                //socketArrayList.add(s);
//                                OutputStream os = s.getOutputStream();
//                                os.write("hi\n".getBytes());
//                                BufferedReader br2 = new BufferedReader(new InputStreamReader(s.getInputStream()));
//                                String line = br2.readLine();
//                                show.setText("来自客户端的数据："+line);
//                                br2.close();
//                                os.close();
//                                s.close();
//                            }
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
                break;
            case 1:
                //客户端代码
                DhcpInfo dhcpinfo = wifiManager.getDhcpInfo();
                final String serverAddress = intToIp(dhcpinfo.serverAddress);
                show.setText(serverAddress);
                mClientThread = new ClientThread(serverAddress, mHandler);
                new Thread(mClientThread).start();
//                new Thread()
//                {
//                    @Override
//                    public void run()
//                    {
//                        try {
//                            Socket socket = new Socket(serverAddress,PORT);
//                            //show.setText();
//                            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                            String line = br.readLine();
//                            show.setText("来自服务器的数据："+line);
//                            OutputStream os = socket.getOutputStream();
//                            os.write("ok\n".getBytes());
//                            br.close();
//                            os.close();
//                            socket.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }.start();
                break;
        }
    }

    public void init()
    {
        show = (TextView) findViewById(R.id.show);
        btn =(Button)findViewById(R.id.button);
        //点击button时，获取EditText中string并且调用子线程的Handler发送到服务器
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Message msg = new Message();
                    String sss = "start";
                    msg.what = MSG_SEND;
                    msg.obj = sss;

                    if( asd == 0 )
                    {
                        mServerThread.revHandler.sendMessage(msg);
                        start_sensor();
                    }
                    else if(asd == 1)
                    {
                        mClientThread.revHandler.sendMessage(msg);
                        start_sensor();
                    }
                    show.setText(sss);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    public void start_sensor()
    {
        //boolean re = false;
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //int sensorType = Sensor.TYPE_ACCELEROMETER;
        int sensorType = Sensor.TYPE_LINEAR_ACCELERATION;
        //20Hz=50000,50Hz=20000 100Hz=10000
        //创建新文件名
        fileNameBasedOnTime();
        sm.registerListener(myAccelerometerListener, sm.getDefaultSensor(sensorType), 10000);
        count =0;
        //return re;
    }

    final SensorEventListener myAccelerometerListener = new SensorEventListener(){

        //复写onSensorChanged方法
        public void onSensorChanged(SensorEvent sensorEvent){
            if(sensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION){
                Log.i(TAG,"onSensorChanged");

                float X_lateral = sensorEvent.values[0];
                float Y_longitudinal = sensorEvent.values[1];
                float Z_vertical = sensorEvent.values[2];
                Log.i(TAG,"\n heading "+X_lateral);
                Log.i(TAG,"\n pitch "+Y_longitudinal);
                Log.i(TAG,"\n roll "+Z_vertical);

                //x.setText("X: "+X_lateral);
                //y.setText("Y: "+Y_longitudinal);
                //z.setText("Z: "+(Z_vertical));

                //String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"Download"+File.separator+"123.txt";
                String path = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"Download"+File.separator+mfileName;
                File file = new File(path);

                float f[]={X_lateral,Y_longitudinal,Z_vertical};

                try {
                    //Toast.makeText(MainActivity.this,"文件写入中...",Toast.LENGTH_SHORT).show();
                    show.setText("");
                    show.setText("文件写入中...");
                    FileOutputStream out = new FileOutputStream(file,true);

                    //out.write(("\n heading "+X_lateral).getBytes());
                    //out.write(("\n pitch "+Y_longitudinal).getBytes());
                    //out.write(("\n roll "+Z_vertical).getBytes());
                    out.write((f[0]+"\t"+f[1]+"\t"+f[2]+"\n").getBytes());
                    //out.write((X_lateral+",").getBytes());
                    //out.write((Y_longitudinal+",").getBytes());
                    //out.write((Z_vertical+"\n").getBytes());
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.e("","path : "+path);

                //write(z.getText().toString());
                count++;
                if (count == 12800)
                {
                    sm.unregisterListener(myAccelerometerListener);
                    show.setText("");
                    show.setText("时间到，已保存文件！");
                    Toast.makeText(MainActivity.this,"时间到，已保存文件！.",Toast.LENGTH_SHORT).show();
                }

            }
        }
        //复写onAccuracyChanged方法
        public void onAccuracyChanged(Sensor sensor , int accuracy){
            Log.i(TAG, "onAccuracyChanged");
        }
    };

    public void onPause(){
        /*
         * 很关键的部分：注意，说明文档中提到，即使activity不可见的时候，感应器依然会继续的工作，测试的时候可以发现，没有正常的刷新频率
         * 也会非常高，所以一定要在onPause方法中关闭触发器，否则讲耗费用户大量电量，很不负责。
         * */
        super.onPause();
    }

    public void fileNameBasedOnTime()
    {
        //当前时间
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        String year;
        String month;
        String day;
        String hour;
        String minute;
        String second;
        String my_time_1;
        String my_time_2;
        year = String.valueOf(cal.get(Calendar.YEAR));
        month = String.valueOf(cal.get(Calendar.MONTH)+1);
        day = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        if (cal.get(Calendar.AM_PM) == 0)
            hour = String.valueOf(cal.get(Calendar.HOUR));
        else
            hour = String.valueOf(cal.get(Calendar.HOUR)+12);
        minute = String.valueOf(cal.get(Calendar.MINUTE));
        second = String.valueOf(cal.get(Calendar.SECOND));
        my_time_1 = year + "-" + month + "-" + day;
        my_time_2 = hour + "-" + minute + "-" + second;
        mfileName = "123 "+my_time_1+" "+my_time_2+".txt";
    }
}
