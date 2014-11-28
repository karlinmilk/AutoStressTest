package com.htc.autostresstest.utils;

import com.htc.autostresstest.MainActivity;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class WakelockUtils
{
    public static WakeLock mPartialWakeLock = null;
    public static WakeLock mFullWakeLock = null;
    public static KeyguardManager.KeyguardLock mKeyguardLock=null;
    public static void partialWakeLockAcquire(Context context)
    {
        if (mPartialWakeLock != null)
            mPartialWakeLock.release();
        
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        /*
        mWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, MainActivity.LOG_TAG);
                */
        mPartialWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, MainActivity.LOG_TAG);
        mPartialWakeLock.acquire();
        AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "partial wakelock acquire");
    }
    

    public static void partialtWakeLockRelease()
    {
        if (mPartialWakeLock == null)
            Log.w(MainActivity.LOG_TAG, "AlarmAlertWakeLock attempting to release null wakelock");
        else
        {
            mPartialWakeLock.release();
            mPartialWakeLock = null;
        }
    }
    
    public static void fullWakeLockAcquire(Context context)
    {
    	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "full wakelock acquire");
    	if(mFullWakeLock!=null)
    		mFullWakeLock.release();
    	PowerManager pm1 = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    	mFullWakeLock = pm1.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "full");
    	mFullWakeLock.acquire();
    	
    	
    }
    
    public static void fulllWakeLockRelease()
    {
        if (mFullWakeLock == null)
            Log.w(MainActivity.LOG_TAG, "AlarmAlertWakeLock attempting to release null wakelock");
        else
        {
        	mFullWakeLock.release();
        	mFullWakeLock = null;
        }
        
    }
    
    public static void disableKeyguard(Context context)
    {
		
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        mKeyguardLock = km.newKeyguardLock("MyKeyguardLock"); 
        if(mKeyguardLock!=null)
        {
        	Log.i(MainActivity.LOG_TAG, "disableKeyguard");
        	mKeyguardLock.disableKeyguard();
        }
    }
    
    public static void relockKeyguard() {
    	if(mKeyguardLock!=null)
        {
    		Log.i(MainActivity.LOG_TAG, "reenableKeyguard");
        	mKeyguardLock.reenableKeyguard();
        	mKeyguardLock =null;
        }			//unlock menu screen lock
    }
    
    public static void alarmGoToSleep(Context context) 
    {
    	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    	//pm.goToSleep(SystemClock.uptimeMillis() + 1);
    	pm.goToSleep(SystemClock.uptimeMillis());//441 modify by Flora
    }
    
    public static boolean isScreenOn(Context context)
    {
    	PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    	boolean isOn = pm.isScreenOn();
    	Log.i(MainActivity.LOG_TAG, "screen is on:"+isOn);
    	return isOn;
    }
}
