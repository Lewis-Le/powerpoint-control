package com.example.powerpointcontrol;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQR extends AppCompatActivity {

    public static final String EXTRA_TEXT = "com.example..application.example.EXTRA_TEXT";  //dữ liệu ip chuyển qua cho service
    String ipScan = "";

    Button scan, back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_q_r);

        scan = (Button)findViewById(R.id.scan);
        back = (Button)findViewById(R.id.back);

        ScanCode();

        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScanCode();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void ScanCode(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureAct.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(integrator.ALL_CODE_TYPES);
        integrator.setPrompt("Đang quét mã QR");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents() != null){
                ipScan = result.getContents();
                thread.start();
                Toast.makeText(getApplication().getBaseContext(), "Đã kết nối với địa chỉ: "+result.getContents(), Toast.LENGTH_LONG).show();
                Intent in = new Intent(ScanQR.this, FullscreenActivity.class);
                in.putExtra(EXTRA_TEXT, "Đã kết nối với PC");
                finish();
            }
        }
    }

    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            Intent in = new Intent(ScanQR.this, serverService.class);
            in.putExtra(EXTRA_TEXT, ipScan);
            Intent in2 = new Intent(ScanQR.this, FullscreenActivity.class);
            in2.putExtra(EXTRA_TEXT, "connected");
            //Toast.makeText(FullscreenActivity.this, ip, Toast.LENGTH_SHORT).show();
            startService(in);
        }
    });
}