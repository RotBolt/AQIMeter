package com.sage.thelimitbreaker.aqimeter;

import android.app.Application;

import com.yashish.library.solvewithstack.main.SolveWithStack;

public class AQIApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SolveWithStack.apply(getApplicationContext());
    }
}
