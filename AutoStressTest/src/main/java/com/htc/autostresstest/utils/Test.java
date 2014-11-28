package com.htc.autostresstest.utils;

import java.util.Random;

import com.htc.autostresstest.GeneralValue;
import com.htc.autostresstest.MainActivity;
import com.htc.autostresstest.TestService;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public abstract class Test implements EventCallback
{
    protected Context mContext;
    protected EventHandler mHandler;
    protected ConfigurationSet mConfig;
    private Message mCompleteMessage;
    
    protected static final int EVENT_START_TEST = 0;
  	
  	public static final String ALARM_PROCESS_ACTION = "com.htc.autostresstest.PROCESS_ALARM_ALERT";
    
    public Test(Context context, EventHandler handler, String tag)
    {
        mContext = context;
        mHandler = handler;
        mConfig = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.DEFAULT_TAG).getSubSet(tag); 
    }
    
    public abstract void init();
    public abstract void release();
    protected abstract void onAlarm(int alarm_type);
    
    public void start(Message onComplete)
    {
    	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Start Test");

        init();
        mCompleteMessage = onComplete;
        mHandler.sendEmptyMessage(EVENT_START_TEST);
    }
    
    protected void finish()
    {
        release();
        mCompleteMessage.sendToTarget();
    }
    
    protected void startTimer(int id, long time)
    {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(id), time);
    }
    
    protected boolean hasTimer(int id)
    {
        return mHandler.hasMessages(id);
    }
    
    protected void stopTimer(int id)
    {
        mHandler.removeMessages(id);
    }
    
    protected int getRandomValue(int min, int max)
    {
        int randomNum;
        Random random = new Random(System.currentTimeMillis());
        if (min == max) 
            return min;
        
        randomNum = min + random.nextInt(max - min + 1);
        return randomNum;
    }
    
    public final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            int alarm_type = intent.getIntExtra("alarm_type", -1);
            onAlarm(alarm_type);
        }
    };
    
    public static void setAlarm(Context context, String alarm, long duration, int type) {
		Log.i(MainActivity.LOG_TAG,"setAlarm: after-"+duration+"ms");
		long currentTime = System.currentTimeMillis();
		long alarmTime = currentTime + duration;
		try {
			Object service = context.getSystemService(Context.ALARM_SERVICE);
			AlarmManager alarmManager = (AlarmManager) service;

			Intent intent = new Intent(ALARM_PROCESS_ACTION);
			intent.putExtra("alarm_type", type);

			PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);
			alarmManager.cancel(sender);
			alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTime, sender);
		} catch (Exception e1) {
			e1.printStackTrace();
			//Log.e(MainActivity.LOG_TAG, "setAlarm Exception: " + e1.getMessage());
			AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"setAlarm Exception: " , e1);
		}
	}
	
	public static void cancelAlarm(Context context, String alarm, int type) {
		Object service = context.getSystemService(Context.ALARM_SERVICE);
		AlarmManager alarmManager = (AlarmManager) service;
		try {
			Intent intent = new Intent(ALARM_PROCESS_ACTION);
			intent.putExtra("alarm_type", type);

			PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			alarmManager.cancel(sender);
		} catch (Exception e1) {
			e1.printStackTrace();
			//Log.e(MainActivity.LOG_TAG, "CancelAlarm Exception: " + e1.getMessage());
			AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"CancelAlarm Exception: " , e1);
		}
	}
	
	public static void alertDialog(Context context, String message)
	{
		WakelockUtils.fullWakeLockAcquire(context);
		Intent intent = new Intent(context, MyAlert.class);
	    Bundle bundle=new Bundle();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    bundle.putString("message", message);
	    intent.putExtras(bundle);
	    context.startActivity(intent);
	}
    
}