package com.sage.thelimitbreaker.aqimeter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sage.thelimitbreaker.aqimeter.Utils.AQIFetchUtil;
import com.sage.thelimitbreaker.aqimeter.Utils.Constants;
import com.sage.thelimitbreaker.aqimeter.Utils.MySharedPref;
import com.sage.thelimitbreaker.aqimeter.dialogs.SettingsDialog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG=MainActivity.class.getSimpleName();

    private FloatingActionButton fabLoading;
    private TextView tvAqiValue;
    private TextView tvAqiStatus;
    private TextView tvAqiHeader;
    private ImageView ivOptions;
    private TextView tvLastSyncHeader;
    private TextView tvLastSyncedTime;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFields();
        String[] permissions = {
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.RECEIVE_BOOT_COMPLETED,
                Manifest.permission.REBOOT
        };
        PermissionManager.askForPermission(
                this,
                permissions,
                new PermissionManager.OnPermissionResultListener() {
                    @Override
                    public void onGranted(String permission) {
                        Toast.makeText(MainActivity.this,"Great!!",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(String permission) {
                        Toast.makeText(MainActivity.this,"Please grant permissions!!",Toast.LENGTH_SHORT).show();
                    }
                }
        );
        AQIFetchUtil.scheduleOneShotJob(getApplicationContext());

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    private void initFields(){
        fabLoading=findViewById(R.id.fabLoading);
        tvAqiValue =findViewById(R.id.aqiValue);
        tvAqiHeader =findViewById(R.id.aqiHeader);
        tvAqiStatus =findViewById(R.id.aqiStatus);
        ivOptions=findViewById(R.id.ivOptions);
        tvLastSyncedTime=findViewById(R.id.tvLastSyncedTime);
        tvLastSyncHeader=findViewById(R.id.tvLastSyncHeader);

        preferences=MySharedPref.getMySharedPrefInstance(this).getSharedPrefInstance();
        int aqiVal=preferences.getInt(Constants.AQI_VALUE,-1);
        String desc=preferences.getString(Constants.AQI_STATUS,null);
        String timeStamp=preferences.getString(Constants.TIMESTAMP,null);
        if(aqiVal==-1){
            tvLastSyncedTime.setVisibility(View.GONE);
            tvAqiHeader.setVisibility(View.GONE);
            tvAqiValue.setVisibility(View.GONE);
            tvLastSyncHeader.setVisibility(View.GONE);
            tvAqiStatus.setText("No last time sync");
        }else{
            tvLastSyncHeader.setVisibility(View.VISIBLE);
            tvLastSyncedTime.setVisibility(View.VISIBLE);
            tvAqiValue.setVisibility(View.VISIBLE);
            tvAqiHeader.setVisibility(View.VISIBLE);
            tvAqiValue.setText(String.valueOf(aqiVal));
            tvAqiStatus.setText(desc);
            tvLastSyncedTime.setText(timeStamp);


        }
        fabLoading.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if(AQIFetchUtil.isConnectedToNetwork(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, AQIFetchingActivity.class);
                    Pair pair = new Pair<>(fabLoading, "fabTransition");
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            MainActivity.this,
                            pair
                    );
                    startActivityForResult(intent, Constants.REQ_CODE, options.toBundle());
                }else{
                    Toast.makeText(MainActivity.this,"No Connected to Internet",Toast.LENGTH_LONG).show();
                }
            }
        });
        ivOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsDialog dialog = new SettingsDialog();
                dialog.show(getFragmentManager(),"Settings Dialog");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Constants.REQ_CODE){
            if(resultCode==RESULT_OK){
                int aqi = data.getIntExtra("aqi",-1);
                String desc=data.getStringExtra("desc");
                if(aqi==-1){
                    tvAqiHeader.setVisibility(View.GONE);
                    tvAqiValue.setVisibility(View.GONE);
                    tvAqiStatus.setText("Some problem in fetching ;(");
                }else{
                    tvAqiHeader.setVisibility(View.VISIBLE);
                    tvAqiValue.setVisibility(View.VISIBLE);
                    tvLastSyncedTime.setVisibility(View.VISIBLE);
                    tvAqiValue.setText(String.valueOf(aqi));
                    tvAqiStatus.setText(desc);
                    String timeStamp = AQIFetchUtil.getTimeStamp();
                    tvLastSyncedTime.setText(timeStamp);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putInt(Constants.AQI_VALUE,aqi);
                    editor.putString(Constants.AQI_STATUS,desc);
                    editor.putString(Constants.TIMESTAMP,timeStamp);
                    editor.apply();
                }
            }
        }
    }

    public static class BootBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            Toast.makeText(context,"AQI Service Scheduling",Toast.LENGTH_SHORT).show();
            AQIFetchUtil.scheduleOneShotJob(context);
        }
    }



}
