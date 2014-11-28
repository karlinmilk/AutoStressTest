package com.htc.autostresstest;


import java.util.LinkedList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;

import com.htc.autostresstest.utils.AutoStressLogger;
import com.htc.autostresstest.utils.Configuration;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;
import com.htc.autostresstest.utils.ConfigurationManager;
import com.htc.autostresstest.utils.EventCallback;
import com.htc.autostresstest.utils.EventHandler;
import com.htc.autostresstest.utils.Test;
import com.htc.autostresstest.utils.WakelockUtils;

public class TestService extends Service implements EventCallback
{
    protected int NOTIFICATION_ID = 1000;
    
    protected HandlerThread mThread;
    protected EventHandler mThreadHandler;
    private NotificationManager mNotificationManager;
    private Notification mNotification;
    private Configuration mConfig;
    private TestManager mTestManager;
    
    private boolean mRun = false;
    private int mTotalCycle = 1;
    private int mCurrentCycle = 0;
    
    private final int EVENT_NEXT_CYCLE = 0;
    
    public void onStart(Intent intent, int startId) 
    {
    	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "TestService start");
        super.onStart(intent, startId);
        	
        if( mRun )
            return;
        mRun = true;
        
        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        setNotification(R.drawable.ic_launcher, "Running Tests", true, null);

        mThread = new HandlerThread("AutoTestService");
        mThread.start();
        mThreadHandler = new EventHandler(mThread.getLooper(), this);
        
        if (ConfigurationManager.getMainConfiguration().load(
        		getFilesDir().getAbsolutePath()  + "/default.xml"))
        {
        	mConfig = ConfigurationManager.getMainConfiguration();
        
        	ConfigurationSet general = mConfig.getRoot().getSubSet(GeneralValue.GENERAL_TAG);
        	general.setSubSetValue(GeneralValue.RUNNING_TAG, true);
        
        	//ConfigurationSet run = mConfig.getRoot().getSubSet(GeneralValue.RUN_TAG);
        }
        
        ConfigurationManager.getMainConfiguration().save(
        		getFilesDir().getAbsolutePath()  + "/default.xml");
        
        mTestManager = new TestManager(this);

        // start test
        mThreadHandler.sendEmptyMessage(EVENT_NEXT_CYCLE);
        WakelockUtils.disableKeyguard(getApplicationContext());
        if(WakelockUtils.mPartialWakeLock==null)
        	WakelockUtils.partialWakeLockAcquire(this);
        
    }
    
    public void onDestroy()
    {
    	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "TestService destroy");
        mRun = false;
        
        if(WakelockUtils.mPartialWakeLock!=null)
        	WakelockUtils.partialtWakeLockRelease();
        //WakelockUtils.relockKeyguard();
        setNotification(R.drawable.ic_launcher, "Stopped", false, null);
        if( mThreadHandler != null )
        {
            mThreadHandler.quit();
            mThreadHandler = null;
        }
        mTestManager.release();
        AutoStressLogger.stopLogging();
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }
    
    private synchronized void setNotification(int icon, String msg, boolean visible, PendingIntent pi)
    {
        if (!visible && mNotification == null)
            return;
        
        if( mNotificationManager == null )
            return;
        
        if( visible )
        {
            if( mNotification == null )
            {
                mNotification = new Notification();
                mNotification.when = 0;
            }
            
            if( icon != -1 )
                mNotification.icon = icon;
            mNotification.defaults &= ~Notification.DEFAULT_SOUND;
            mNotification.flags = Notification.FLAG_ONGOING_EVENT;
            mNotification.tickerText = "AutoTest";
            
            if (pi == null)
            {
                //Intent intent = new Intent();
                //pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            }

            mNotification.setLatestEventInfo(getApplicationContext(), "AutoStressTest", msg, pi);
            
            startForeground(NOTIFICATION_ID, mNotification);
            mNotificationManager.notify(NOTIFICATION_ID, mNotification);
        }
        else
        {
            stopForeground(true);
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    public void onReceiveEvent(Message msg)
    {
        switch(msg.what)
        {
        case EVENT_NEXT_CYCLE:
            {
                if( mCurrentCycle < mTotalCycle )
                    mCurrentCycle++;
                else
                {
                	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "End of test");
                    //need to pop up Pass dialog
                    ConfigurationSet general = mConfig.getRoot().getSubSet(GeneralValue.GENERAL_TAG);
                	general.setSubSetValue(GeneralValue.RUNNING_TAG, false);
                	ConfigurationManager.getMainConfiguration().save(
                    		getFilesDir().getAbsolutePath()  + "/default.xml");
                	String testcase = getTestCaseName();
                	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "<"+testcase+"> [Pass] Test Completed");
                	AutoStressLogger.writeLog(this, "<"+testcase+"> [Pass] Test Completed");
                    stopSelf();
                    Test.alertDialog(this, "<"+testcase+">\r\n"+"[Pass] Test Completed");
                    return;
                }
                mTestManager.start(mThreadHandler.obtainMessage(EVENT_NEXT_CYCLE));
            }
            break;
        }
    }
    
    private String getTestCaseName()
    {
    	String name="";
    	LinkedList<ConfigurationSet> sets = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.RUN_TAG).getSubSets();
    	int size = sets.size();
    	switch (size)
    	{
    		case 4:
    			name = "Auto Reboot Test";
    			break;
    		case 2:
    			name = "MO Test";
    			break;
    		case 6:
    			name = "All Test";
    			break;
    			
    	}
    	return name;
    }
    
}
