/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.background.sync.ReminderTasks;
import com.example.android.background.sync.WaterReminderIntentService;
import com.example.android.background.utilities.NotificationUtils;
import com.example.android.background.utilities.PreferenceUtilities;
import com.example.android.background.utilities.RemiderUtilities;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mWaterCountDisplay;
    private TextView mChargingCountDisplay;
    private ImageView mChargingImageView;

    private Toast mToast;
    IntentFilter mChargingIntentFilter;
     ChargingBroadcastReceiver mChargReciever;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /** Get the views **/
        mWaterCountDisplay = (TextView) findViewById(R.id.tv_water_count);
        mChargingCountDisplay = (TextView) findViewById(R.id.tv_charging_reminder_count);
        mChargingImageView = (ImageView) findViewById(R.id.iv_power_increment);

        /** Set the original values in the UI **/
        updateWaterCount();
        updateChargingReminderCount();

        /** Setup the shared preference listener **/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        RemiderUtilities.scheduleChargingReminder(this);
        //هناك طريقتين للتوصل لحالة الشحم اما عن طريق فحص الشحن بواسطة broadcast او عن طريق فحص البطارية بدون broadcast لكن نستخدمSTATIC INTENT ACTION BATTERY CHANGED
        mChargingIntentFilter  = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);//عن طريقو رح نعين ال رسايل النظام اللي بدنا نخلي receiver  يستمع الها
        Intent infilter = registerReceiver(null,mChargingIntentFilter);
        int batteryStatus = infilter.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING || batteryStatus == BatteryManager.BATTERY_STATUS_FULL;
        showCharging(isCharging);
        /*mChargingIntentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        mChargingIntentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);*/
       // mChargReciever = new ChargingBroadcastReceiver();//نقوم بتسجيل ال broadcast receiver

    }

  /*  @Override
    //هي الداله يعني اذا انا جوات لتطبيق وفاتحو بهي حالة رح يتم تنفيذ broadcast receiver
    protected void onResume() {
        super.onResume();
        registerReceiver(mChargReciever,mChargingIntentFilter);
    }

    @Override
    //اذا تم ايقاف التطبيق سوف لن يتم الاستماع لرسايل النظام
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mChargReciever);
    }*/

    /**
     * Updates the TextView to display the new water count from SharedPreferences
     */
    private void updateWaterCount() {
        int waterCount = PreferenceUtilities.getWaterCount(this);
        mWaterCountDisplay.setText(waterCount+"");
    }

    /**
     * Updates the TextView to display the new charging reminder count from SharedPreferences
     */
    private void updateChargingReminderCount() {
        int chargingReminders = PreferenceUtilities.getChargingReminderCount(this);
        String formattedChargingReminders = getResources().getQuantityString(
                R.plurals.charge_notification_count, chargingReminders, chargingReminders);
        mChargingCountDisplay.setText(formattedChargingReminders);

    }

    /**
     * Adds one to the water count and shows a toast
     */
    public void incrementWater(View view) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, R.string.water_chug_toast, Toast.LENGTH_SHORT);
        mToast.show();

        Intent incrementWaterCountIntent = new Intent(this, WaterReminderIntentService.class);
        incrementWaterCountIntent.setAction(ReminderTasks.ACTION_INCREMENT_WATER_COUNT);
        startService(incrementWaterCountIntent);
    }

    // COMPLETED (15) Create a method called testNotification that triggers NotificationUtils' remindUserBecauseCharging
   /* public void testNotification(View view) {
        NotificationUtils.remindUserBecauseCharging(this);
    }*/

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** Cleanup the shared preference listener **/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This is a listener that will update the UI when the water count or charging reminder counts
     * change
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceUtilities.KEY_WATER_COUNT.equals(key)) {
            updateWaterCount();
        } else if (PreferenceUtilities.KEY_CHARGING_REMINDER_COUNT.equals(key)) {
            updateChargingReminderCount();
        }
    }
    public void showCharging(boolean isCharging){// تقوم هذه الدالة بتغيير لون ايقونة الشحن في لشاشة بناء علا  ما اذا الجهاز عبشحن الا لأ
        if (isCharging){
            mChargingImageView.setImageResource(R.drawable.ic_power_pink_80px);
        }else{
            mChargingImageView.setImageResource(R.drawable.ic_power_grey_80px);
        }
    }
    //inner class for broadcast  receiver
    private class ChargingBroadcastReceiver extends BroadcastReceiver{
        // this type of receiver called dynamically broadcast receiver because we declare it in main activity
        //هاد مسوول عن استلام رسايل النظام(مثال : تم توصيل الشاحن) وبناء عليها بشتغل
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();// الاكشن رح يجي من  intent filter
            boolean isCharging = (action.equals(Intent.ACTION_POWER_CONNECTED));
       //مابين الاقواس اذا الاكشن وهو انو الجهاز موصل بالشحن اذا يسااوي action رح يرجع true  والا اذا ماهو بالشحن رح يرجع false
            showCharging(isCharging);
        }
    }
}