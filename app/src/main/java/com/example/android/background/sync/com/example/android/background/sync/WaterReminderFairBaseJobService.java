package com.example.android.background.sync.com.example.android.background.sync;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.example.android.background.sync.ReminderTasks;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
// هذه عبارة عن سيرفز جديددة بالتالي يجب تسجيلها في manifest
// عند انشاء    job لتنفيذه سنقوم بتمرير ال سيرفز هي اليه ليقوم بتنفيذه
// لقد نم انشاء new job in Reminder Utilities class
public class WaterReminderFairBaseJobService extends JobService {
     private AsyncTask mbackgroundTask;
    @SuppressLint("StaticFieldLeak")
    @Override
    public boolean onStartJob(  final JobParameters jobParameters) {
        mbackgroundTask =new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                Context context = WaterReminderFairBaseJobService.this;
                ReminderTasks.executeTask(context,ReminderTasks.ACTION_CHANGING_REMINDER);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(jobParameters,false);
            }
        };
        mbackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        if (mbackgroundTask!= null){
            mbackgroundTask.cancel(true);
        }
        return true;
    }
}
