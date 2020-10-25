package com.example.powerpointcontrol;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Process;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private Button disconnect;
    private Button remote;
    private Button voice;
    private Button edit;
    private Button info;
    private Button qr;
    private Button connect;
    private Button exit;
    private EditText ipinput;
    private TextView status;
    private String command = "";

    public static final String EXTRA_TEXT = "com.example..application.example.EXTRA_TEXT";  //dữ liệu ip chuyển qua cho service
    //Intent in = new Intent(this, serverService.class);   //khai báo Intent service
    Intent intent = new Intent("sendCommand");  //khai bao intent để gửi lệnh qua service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        //Phần chính của khởi tạo
        disconnect = (Button) findViewById(R.id.button6);
        remote = (Button) findViewById(R.id.remote_button);
        voice = (Button) findViewById(R.id.hand_free_button);
        info = (Button) findViewById(R.id.info_button);
        edit = (Button) findViewById(R.id.Live_edit);
        connect = (Button)findViewById(R.id.connect);
        qr = (Button)findViewById(R.id.qr);
        exit = (Button)findViewById(R.id.dummy_button);
        status = (TextView) findViewById(R.id.status);
        ipinput = (EditText)findViewById(R.id.ipinput);


        //final String ip = "";   //chuỗi ip lấy từ người dùng nhập trực tiếp qua edittext
        disconnect.setVisibility(View.GONE);

        //Phần code cho các button:
        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(FullscreenActivity.this, ScanQR.class);
                startActivity(in);
                //String is_connected = in2.getStringExtra(serverService.EXTRA_TEXT);
                disconnect.setVisibility(View.VISIBLE);
                connect.setVisibility(View.GONE);
                qr.setVisibility(View.GONE);
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect.setVisibility(View.VISIBLE);
                connect.setVisibility(View.GONE);
                qr.setVisibility(View.GONE);
                thread.start();
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_command("disconnect");
                Toast.makeText(getApplication().getBaseContext(), "Đã ngắt kết nối", Toast.LENGTH_SHORT).show();
                status.setText("Chưa được kết nối với PC");
                disconnect.setVisibility(View.GONE);
                connect.setVisibility(View.VISIBLE);
                qr.setVisibility(View.VISIBLE);
                stopService(new Intent(FullscreenActivity.this, serverService.class));
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplication().getBaseContext(), "Đã tắt service và thoát ứng dụng", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        remote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(FullscreenActivity.this, Remote.class);
                startActivity(in);
            }
        });

        voice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(FullscreenActivity.this, hand_free.class);
                startActivity(in);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(FullscreenActivity.this, realTime.class);
                startActivity(in);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(FullscreenActivity.this, Info.class);
                startActivity(in);
            }
        });

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);

    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    protected void onDestroy() {
        //stopService(in);
        Process.killProcess(Process.myPid());
        super.onDestroy();
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Intent in = new Intent(FullscreenActivity.this, serverService.class);
            String ip = ipinput.getText().toString();
            in.putExtra(EXTRA_TEXT, ip);
            //Toast.makeText(FullscreenActivity.this, ip, Toast.LENGTH_SHORT).show();
            startService(in);
            status.setText("Đã kết nối với PC");
            //connect
        }
    });

    //Phần gửi data qua cho service thông qua localBroardcastManager:
    public void send_command(String command){
        intent.putExtra("message", command);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}







