package com.sage.thelimitbreaker.aqimeter.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPref {
    static MySharedPref mySharedPref;
    SharedPreferences preferences;

    public SharedPreferences getSharedPrefInstance(){
        return preferences;
    }
    public static MySharedPref getMySharedPrefInstance(Context context){
        if(mySharedPref==null){
            mySharedPref=new MySharedPref(context);
        }
        return mySharedPref;
    }

    private MySharedPref(Context context){
        preferences=context.getSharedPreferences(Constants.SHARED_PREF_KEY,Context.MODE_PRIVATE);
    }
}
