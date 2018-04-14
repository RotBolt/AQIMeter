package com.sage.thelimitbreaker.aqimeter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.sage.thelimitbreaker.aqimeter.Utils.Constants
import com.sage.thelimitbreaker.aqimeter.api.BreezoMeterApi
import com.sage.thelimitbreaker.aqimeter.models.AirQualityObject
import kotlinx.android.synthetic.main.activity_aqifetching.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class AQIFetchingActivity : AppCompatActivity() {

    private val TAG:String="AQIFetchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aqifetching)
        pulsator.start()
        startFetching()
    }

    private fun startFetching(){
        val handler =Handler()
        val runnable= Runnable { getAQIInfo() }
        handler.postDelayed(runnable,3600)
    }

    private fun getAQIInfo(){
        val aqiApi = BreezoMeterApi.getApiInstance().airQualityApi
        aqiApi.getAQIDetail(
                resources.getString(R.string.breezo_api_key),
                Constants.INDIAN_LAT,
                Constants.INDIAN_LONG
        ).enqueue(object : Callback<AirQualityObject>{
            override fun onFailure(call: Call<AirQualityObject>?, t: Throwable?) {
                Log.d(TAG,"Err.......r"+t.toString())
                finishAfterTransition()
            }

            override fun onResponse(call: Call<AirQualityObject>?, response: Response<AirQualityObject>?) {
                Log.d(TAG,"Response")
                val aqi = response?.body()?.country_aqi
                val desc=response?.body()?.country_description
                val intent = Intent()
                intent.putExtra("aqi",aqi)
                intent.putExtra("desc",desc)
                setResult(Activity.RESULT_OK,intent)
                pulsator.stop()
                finishAfterTransition()
            }

        })
    }

}
