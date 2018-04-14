package com.sage.thelimitbreaker.aqimeter.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.sage.thelimitbreaker.aqimeter.R;
import com.sage.thelimitbreaker.aqimeter.Utils.AQIFetchUtil;
import com.sage.thelimitbreaker.aqimeter.Utils.Constants;
import com.sage.thelimitbreaker.aqimeter.Utils.MySharedPref;

public class SettingsDialog extends DialogFragment
        implements DialogInterface.OnClickListener,
        CompoundButton.OnCheckedChangeListener {
    private static final String TAG = SettingsDialog.class.getSimpleName();
    private SeekBar seekBar;
    private TextView seekBarValue;
    private SharedPreferences preferences;
    private Switch sportSwitch;
    private Switch healthSwitch;
    private Switch aqi200Switch;
    private int updatedInterval = 0;
    private boolean sportNotify = false;
    private boolean healthNotify = false;
    private boolean isAQILess200=false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View settingsView = getActivity().getLayoutInflater().inflate(R.layout.layout_settings, null);
        builder.setPositiveButton(R.string.OK, this)
                .setNegativeButton(R.string.Cancel, this)
                .setView(settingsView);
        initFields(settingsView);
        return builder.create();
    }

    private void initFields(View view) {
        seekBar = view.findViewById(R.id.seekBar);
        seekBarValue = view.findViewById(R.id.seekBarValue);
        sportSwitch = view.findViewById(R.id.sportSwitch);
        healthSwitch = view.findViewById(R.id.healthSwitch);
        aqi200Switch=view.findViewById(R.id.aqi200Switch);

        aqi200Switch.setOnCheckedChangeListener(this);
        sportSwitch.setOnCheckedChangeListener(this);
        healthSwitch.setOnCheckedChangeListener(this);

        preferences = MySharedPref.getMySharedPrefInstance(getActivity()).getSharedPrefInstance();
        int prefValue = preferences.getInt(Constants.NOTIFY_INTERVAL, 0);
        seekBar.setProgress(prefValue);
        setSeekBarValue(prefValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.d(TAG, "onProgressChanged: " + progress);
                updatedInterval = progress;
                setSeekBarValue(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        healthNotify=preferences.getBoolean(Constants.NOTIFY_HEALTH,false);
        sportNotify=preferences.getBoolean(Constants.NOTIFY_SPORT,false);
        isAQILess200=preferences.getBoolean(Constants.AQI_LESS_200,false);

        aqi200Switch.setChecked(isAQILess200);
        sportSwitch.setChecked(sportNotify);
        healthSwitch.setChecked(healthNotify);

    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -1:
                Log.d(TAG, "onClick: ok");
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(Constants.NOTIFY_INTERVAL, updatedInterval);
                editor.putBoolean(Constants.NOTIFY_SPORT, sportNotify);
                editor.putBoolean(Constants.NOTIFY_HEALTH, healthNotify);
                editor.putBoolean(Constants.AQI_LESS_200,isAQILess200);
                editor.apply();

                JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                scheduler.cancelAll();
                if (doGenerateNotification(getActivity())) {
                    Log.d(TAG, "onClick: reSchedule");
                    AQIFetchUtil.scheduleOneShotJob(getActivity());
                }
                break;
            case -2:
                Log.d(TAG, "onClick: cancel");
                break;
        }
    }

    private boolean doGenerateNotification(Context context) {
        SharedPreferences pref = MySharedPref.getMySharedPrefInstance(context).getSharedPrefInstance();
        boolean healthNotify = pref.getBoolean(Constants.NOTIFY_HEALTH, false);
        boolean sportNotify = pref.getBoolean(Constants.NOTIFY_SPORT, false);
        if (healthNotify || sportNotify) {
            return true;
        }
        return false;
    }

    private void setSeekBarValue(int prefValue) {
        switch (prefValue) {
            case 0:
                seekBarValue.setText("30 min");
                break;
            case 1:
                seekBarValue.setText("1 hr");
                break;
            case 2:
                seekBarValue.setText("2 hr");
                break;
            case 3:
                seekBarValue.setText("3 hr");
                break;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        switch (buttonView.getId()) {
            case R.id.sportSwitch:
                Log.d(TAG, "onCheckedChanged: sport");
                sportNotify = isChecked;
                break;
            case R.id.healthSwitch:
                Log.d(TAG, "onCheckedChanged: health");
                healthNotify = isChecked;
                break;
            case R.id.aqi200Switch:
                isAQILess200=isChecked;
                break;
        }
    }
}
