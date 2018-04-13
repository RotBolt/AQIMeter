package com.sage.thelimitbreaker.aqimeter.services;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.sage.thelimitbreaker.aqimeter.Utils.AQIFetchUtil;

public class OneShotJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        AQIFetchUtil.scheduleJob(getApplicationContext());
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
