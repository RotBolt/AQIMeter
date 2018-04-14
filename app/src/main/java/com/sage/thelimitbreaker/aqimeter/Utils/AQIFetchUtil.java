package com.sage.thelimitbreaker.aqimeter.Utils;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.sage.thelimitbreaker.aqimeter.services.NotifyJobService;
import com.sage.thelimitbreaker.aqimeter.services.OneShotJobService;

import java.util.Calendar;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class AQIFetchUtil {
    private static final String TAG= AQIFetchUtil.class.getSimpleName();
    private static int JOB_ID=1;
    private static int ONE_SHOT_ID=2;
    public static long INTERVAL=15*60*1000;


    public static void scheduleOneShotJob(Context context){
        Log.d(TAG, "scheduleOneShotJob: ");
        ComponentName  componentName= new ComponentName(context, OneShotJobService.class);
        JobInfo.Builder builder = new JobInfo.Builder(ONE_SHOT_ID,componentName);
        builder.setMinimumLatency(1*60*1000);
        JobScheduler scheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        scheduler.schedule(builder.build());
    }

    public static void scheduleJob(Context context){

        Log.d(TAG, "scheduleJob: ");

        ComponentName serviceComponent= new ComponentName(context, NotifyJobService.class);

        final JobInfo.Builder builder= new JobInfo.Builder(JOB_ID,serviceComponent);
        long notifyInterval=getNotifyInterval(context);
        Log.d(TAG, "scheduleJob: "+notifyInterval);
        builder.setPeriodic(notifyInterval);

        final JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if(!hasServiceScheduled(scheduler)) {
            Log.d(TAG, "scheduleJob: not running . Reschedule");
            scheduler.schedule(builder.build());
        }

    }

    @SuppressLint("MissingPermission")
    public static boolean isConnectedToNetwork(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState()== NetworkInfo.State.CONNECTED
                ||connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState()== NetworkInfo.State.CONNECTED){
            return true;
        }
        return false;
    }


    private static long getNotifyInterval(Context context){
        SharedPreferences pref = MySharedPref.getMySharedPrefInstance(context).getSharedPrefInstance();
        int prefValue = pref.getInt(Constants.NOTIFY_INTERVAL,0);
        int retValue=30*60*1000;
        switch (prefValue){
            case 0:
                retValue=30*60*1000;
                break;
            case 1:
                retValue=60*60*1000;
                break;
            case 2:
                retValue=2*60*60*1000;
                break;
            case 3:
                retValue=3*60*60*1000;
                break;
        }
        return retValue;
    }

    private static boolean hasServiceScheduled(JobScheduler scheduler){
        for(JobInfo jobInfo:scheduler.getAllPendingJobs()){
            if(jobInfo.getId()==JOB_ID){
                return true;
            }
        }
        return false;
    }

    public static String getTimeStamp(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        int date = calendar.get(Calendar.DAY_OF_MONTH);
        int month=calendar.get(Calendar.MONTH);
        int year=calendar.get(Calendar.YEAR);

        int hour=calendar.get(Calendar.HOUR_OF_DAY);
        int min=calendar.get(Calendar.MINUTE);

        String timeStamp = String.format("%02d/%02d/%02d, %02d:%02d",date,month,year,hour,min);
        return timeStamp;
    }
}
