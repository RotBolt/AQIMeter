package com.sage.thelimitbreaker.aqimeter.services;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.sage.thelimitbreaker.aqimeter.MainActivity;
import com.sage.thelimitbreaker.aqimeter.R;
import com.sage.thelimitbreaker.aqimeter.Utils.AQIFetchUtil;
import com.sage.thelimitbreaker.aqimeter.Utils.Constants;
import com.sage.thelimitbreaker.aqimeter.Utils.MySharedPref;
import com.sage.thelimitbreaker.aqimeter.api.AirQualityApi;
import com.sage.thelimitbreaker.aqimeter.api.BreezoMeterApi;
import com.sage.thelimitbreaker.aqimeter.models.AirQualityObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.app.NotificationManagerCompat.IMPORTANCE_HIGH;

public class NotifyJobService extends JobService {
    private static final String TAG=NotifyJobService.class.getSimpleName();
    private static final String CHANNEL_ID="notify_channel";
    private static final int NOTIFY_ID=2;

    @Override
    public boolean onStartJob(JobParameters params) {

        Log.d(TAG, "onStartJob: ");
        if(AQIFetchUtil.isConnectedToNetwork(getApplicationContext())) {
           getAQIInfo();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void generateNotification(int aqi , String sportRecommendation){
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this,CHANNEL_ID)
                .setSmallIcon(R.drawable.wind)
                .setContentTitle("AQI : "+aqi)
                .setContentText(sportRecommendation)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(sportRecommendation))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            CharSequence name = "Name";
            String description = "Description";

            @SuppressLint("WrongConstant")
            NotificationChannel channel =
                    new NotificationChannel(CHANNEL_ID,
                                            name,
                                            IMPORTANCE_HIGH);
            channel.setDescription(description);

            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(NOTIFY_ID,builder.build());

    }

    private void getAQIInfo(){
        AirQualityApi api = BreezoMeterApi.getApiInstance().getAirQualityApi();
        api.getAQIDetail(
                getResources().getString(R.string.breezo_api_key),
                Constants.INDIAN_LAT,
                Constants.INDIAN_LONG
        ).enqueue(new Callback<AirQualityObject>() {
            @Override
            public void onResponse(Call<AirQualityObject> call, Response<AirQualityObject> response) {
                Log.d(TAG, "onResponse: ");
                if (response.body() != null) {
                    StringBuilder sb = new StringBuilder();
                    SharedPreferences pref= MySharedPref.getMySharedPrefInstance(getApplicationContext()).getSharedPrefInstance();
                    int aqi = response.body().getCountry_aqi();
                    String desc = response.body().getCountry_description();
                    String sports = response.body().getRandom_recommendations().getSport();
                    String health=response.body().getRandom_recommendations().getHealth();

                    SharedPreferences.Editor editor= pref.edit();
                    editor.putInt(Constants.AQI_VALUE,aqi);
                    editor.putString(Constants.AQI_STATUS,desc);
                    editor.putString(Constants.TIMESTAMP,AQIFetchUtil.getTimeStamp());
                    editor.apply();

                    if(pref.getBoolean(Constants.NOTIFY_SPORT,false))
                        sb.append("Sport : "+sports+"\n\n");
                    if(pref.getBoolean(Constants.NOTIFY_HEALTH,false)){
                        sb.append("Health : "+health);
                    }
                    if(pref.getBoolean(Constants.AQI_LESS_200,false)){
                        if(aqi<=200){
                            generateNotification(aqi, sb.toString());
                        }else{
                            Log.d(TAG, "onResponse: More than 200 aqi");
                        }
                    }else {
                        generateNotification(aqi, sb.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<AirQualityObject> call, Throwable t) {
                Log.d(TAG, "onFailure: "+t.toString());
            }
        });
    }


}