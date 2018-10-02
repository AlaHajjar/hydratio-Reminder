package com.example.android.background.utilities;

import android.content.Context;

import com.example.android.background.sync.com.example.android.background.sync.WaterReminderFairBaseJobService;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;
// يتم استتدعائها في main activity
public class RemiderUtilities {
    public  final static int REMINDER_INTERVAL_MINUTES= 30;
    public  final static int REMINDER_INTERVAL_SECONDS = (int) TimeUnit.MINUTES.toSeconds(REMINDER_INTERVAL_MINUTES);
    public  final static int SYNC_FLEXTIME_SECONDS = REMINDER_INTERVAL_SECONDS;
    public final static String REMINDER_JOB_TAG ="hydration-reminder-tag";
    public static boolean isInitialized;// وظيفتو  فحص ال job اذا شغال وتمت جدولته
    synchronized public static void scheduleChargingReminder(final Context context){
        if (isInitialized) return; // اذا كان المتغير محقق اي ترو فلا داعي لانشاء job  جديد وبالتالي افصل
        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);
        Job constraintREminderJob =dispatcher.newJobBuilder().
                setService(WaterReminderFairBaseJobService.class).//نتعيين سرفز التي تحتوي عالمهمة التي ستقوم بها
                setTag(REMINDER_JOB_TAG).// تاغ مميز لكل job لانه ممكن يكون في اكترمن جوب
                //تعيين كل ايمت يجيني اشعار
                setTrigger(Trigger.executionWindow(REMINDER_INTERVAL_SECONDS,REMINDER_INTERVAL_SECONDS+SYNC_FLEXTIME_SECONDS)).
                setRecurring(true)//سوف تقو م الوظسفة بتكرار نفسها
                .setReplaceCurrent(true)// اذا في وظيفة قديمة امحيا وحط محلا الجديدة
                .setConstraints(Constraint.DEVICE_CHARGING) //تعيين قيود للوظيفة وهاد احد القيود وهو تشغيل الوظيفة بس اذا الجهاز بالشحن
                .setLifetime(Lifetime.FOREVER)//نعين عمر الوظيفة وهو دائما يعني اذا طفيت الجهاز نهائيا ورجعت شغلتو بترجع لحالا بتشتغل
                .build();// مع هذه الدالة يتم  انهاء انشاء ال job
        dispatcher.schedule(constraintREminderJob);// خنا قمنا بجدولة العمل اي job اللي عملناه
        isInitialized = true; // بما اننا انشانا ال  job  نعطي قيمة ترو للمتغير
    }

}
