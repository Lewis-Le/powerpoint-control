package com.example.powerpointcontrol;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class serverService extends Service {

    String ip = "";
    String command = "";
    String receive_data = "";
    Boolean is_receive = false;

    Socket socket;
    PrintWriter out = null;
    public static final String EXTRA_TEXT = "com.example..application.example.EXTRA_TEXT";  //dữ liệu ip chuyển qua cho FulllScreenActivity

    public serverService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //Toast.makeText(getApplication().getBaseContext(), "starting service...", Toast.LENGTH_SHORT).show();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, new IntentFilter("sendCommand"));
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ip = intent.getStringExtra(FullscreenActivity.EXTRA_TEXT);
        //Toast.makeText(getApplication().getBaseContext(), ip, Toast.LENGTH_SHORT).show();
        thread.start();
        thread2.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        //disconnect();
        thread.stop();
        thread2.stop();
        super.onDestroy();
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            client(ip);
        }
    });
    Thread thread2 = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true){
                try {
                    if(is_receive){
                        out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
                        out.println(command);  //gui lenh den PC
                        is_receive = false;
                        if(command.equals("disconnect")){
                            socket.close();
                            //stopService(new Intent(serverService.this, serverService.class));
                        }
                    }
                } catch (IOException e) {
                    out.close();
                    e.printStackTrace();
                }
            }
        }
    });

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            command = intent.getStringExtra("message");
            is_receive = true;
        }
    };

    public void client(String ip){
        try {
            InetAddress serverAddr = InetAddress.getByName(ip);  //địa chỉ của server
            //Log.d("TCP", "C: Connecting...");
            socket = new Socket(serverAddr, 9999);
            //Toast.makeText(serverService.this, "Đã kết nối với PC", Toast.LENGTH_SHORT).show();

            String message = "data from Android";  //data dẽ gửi đến PC

            PrintWriter out = null;
            BufferedReader in = null;

            try {
                //Log.d("TCP", "C: Sending: '" + message + "'");
                out = new PrintWriter( new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //out.println(message);  //gửi data đến PC

                while ((in.readLine()) != null) {
                    receive_data = receive_data+(in.readLine());
                }
                //Toast.makeText(serverService.this, receive_data, Toast.LENGTH_SHORT).show();
            } catch(Exception e) {
                socket.close();
                //Toast.makeText(getApplication().getBaseContext(), "TCP Error " + e, Toast.LENGTH_SHORT).show();
            } //finally {
                //socket.close();
                //Toast.makeText(getApplication().getBaseContext(), "Close socket: " , Toast.LENGTH_SHORT).show();
            //}
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            //Log.e("TCP", "C: UnknownHostException", e);
            //Toast.makeText(getApplication().getBaseContext(), "Unknow host exception: " + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //Log.e("TCP", "C: IOException", e);
            //Toast.makeText(getApplication().getBaseContext(), "IOE Exception: " + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}
