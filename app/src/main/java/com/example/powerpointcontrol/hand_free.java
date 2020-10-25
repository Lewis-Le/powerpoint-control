package com.example.powerpointcontrol;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class hand_free extends AppCompatActivity {
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
            /*mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);  */
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

    TextView result;
    TextView handle_voice;
    Button exit;
    SpeechRecognizer speechRecognizer;
    Intent speechIntent;
    private static final int RECORD_AUDIO_CODE = 101;
    Intent intent = new Intent("sendCommand");
    String data = ""; //chuoi data luu trữ kết quả nghe dc liên tiếp

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_hand_free);

        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        //mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        /*mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        }); */

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        result = (TextView)findViewById(R.id.voiceresult);
        handle_voice = (TextView)findViewById(R.id.textView2);
        exit = (Button)findViewById(R.id.dummy_button);

        //Check permission record audio
        checkPermission(Manifest.permission.RECORD_AUDIO, RECORD_AUDIO_CODE);

        //khởi tạo nhận dạng giọng nói
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {
                Toast.makeText(getApplication().getBaseContext(), "Beginning of speech", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> txt_result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                data = "";
                String handle_result = "";
                if(txt_result != null)
                    for (String result : txt_result)
                        data += result + "\t";
                result.setText(data);
                data = data.toLowerCase();
                //if(data.toLowerCase().contains("ok")) {
                    //handle_result += "ok ";
                    if (data.contains("kế") | data.contains("tiếp") | data.contains("tới"))
                        handle_result += "Đến slide tiếp theo ";
                    if (data.contains("trước") | data.contains("trở lại") | data.contains("quay lại") | data.contains("về") | data.contains("lui") | data.contains("quay"))
                        handle_result += "Trở về slide trước đó ";
                    if (data.contains("bắt đầu") | data.contains("hiện") | data.contains("start") | data.contains("mở") | data.contains("vào") | data.contains("bắc") | data.contains("bắt"))
                        handle_result += "Bắt đầu trình chiếu ";
                    if (data.contains("tắt") | data.contains("dừng") | data.contains("stop") | data.contains("đóng") | data.contains("ngưng") | data.contains("thoát") | data.contains("ngừng"))
                        handle_result += "Thoát trình chiếu ";
                //}
                handle_voice.setText(handle_result);
                if (data.contains("kế") | data.contains("tiếp") | data.contains("tới"))
                    send_command("next");
                if (data.contains("trước") | data.contains("trở lại") | data.contains("quay lại") | data.contains("về") | data.contains("lui") | data.contains("quay"))
                    send_command("pre");
                if (data.contains("bắt đầu") | data.contains("hiện") | data.contains("start") | data.contains("mở") | data.contains("vào") | data.contains("bắc") | data.contains("bắt") | data.contains("bác"))
                    send_command("start");
                if (data.contains("tắt") | data.contains("dừng") | data.contains("stop") | data.contains("đóng") | data.contains("ngưng") | data.contains("thoát") | data.contains("ngừng"))
                    send_command("end");
            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        });

        exit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        result.setText("");
                        speechRecognizer.stopListening();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        result.setText("");
                        result.setText("Đang nghe . . .");
                        speechRecognizer.startListening(speechIntent);
                        break;
                }
                return false;
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
        //mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        //mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
             //   | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
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


    //Hàm để send data qua cho Service
    public void send_command(String command){
        intent.putExtra("message", command);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    //vòng lặp để luôn luôn lắng nghe
    public void listen() throws InterruptedException {
        while(true){
            speechRecognizer.startListening(speechIntent);
            TimeUnit.SECONDS.sleep(4);
            speechRecognizer.startListening(speechIntent);
            TimeUnit.MILLISECONDS.sleep(50);
        }
    }


    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(hand_free.this, permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(hand_free.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            Toast.makeText(hand_free.this,
                    "Permission already granted",
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }
    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == RECORD_AUDIO_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(hand_free.this,
                        "Record audio Permission Granted",
                        Toast.LENGTH_SHORT)
                        .show();
            }
            else {
                Toast.makeText(hand_free.this,
                        "Record audio Permission Denied",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


}