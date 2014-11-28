package com.htc.autostresstest.Reboot;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

import com.htc.autostresstest.GeneralValue;
import com.htc.autostresstest.MainActivity;
import com.htc.autostresstest.TestService;
import com.htc.autostresstest.utils.AutoStressLogger;
import com.htc.autostresstest.utils.ConfigurationManager;
import com.htc.autostresstest.utils.EventHandler;
import com.htc.autostresstest.utils.Test;
import com.htc.autostresstest.utils.WakelockUtils;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;

/**
 * Auto Reboot- every 5 min, 5 trials
 */
public class Reboot5minTest extends Test{
	private int mTotalCycle = 0;
    private int mCycle = 0;
    private int mSleepTime ;
    
    private static final int EVENT_IDLE=1;
    //alarm type
  	public static final int ALARM_REBOOT = 2;  	
  	//alarm name
  	public static final String REBOOT_ALARM = "reboot_alarm";
    
	public Reboot5minTest(Context context, EventHandler handler, String tag) {
		super(context, handler, tag);
		// TODO Auto-generated constructor stub
		mTotalCycle = mConfig.getSubSetIntValue(GeneralValue.CYCLE);
		mSleepTime  = mConfig.getSubSetIntValue(RebootValue.SLEEP);
	}

	@Override
	public void onReceiveEvent(Message msg) {
		// TODO Auto-generated method stub
		switch(msg.what)
        {
        case EVENT_START_TEST:
            {
            	ConfigurationSet general = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.GENERAL_TAG);
            	mCycle = general.getSubSetIntValue(GeneralValue.CURRENT_CYCLE_TAG);
            	
            	if(mCycle < mTotalCycle)
            	{	            	
            		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Start Reboot5minTest Cycle: " + (mCycle+1));
            		AutoStressLogger.writeLog(mContext,"Start Reboot5minTest Cycle: " + (mCycle+1));
            		mCycle++;
            		general.setSubSetValue(GeneralValue.CURRENT_CYCLE_TAG, String.valueOf(mCycle));

                    //The first cycle reboot directly,no need to delay
	            	if(mCycle==1)
	            	{
	            		//before reboot,need to set the REBOOT_TAG to true(reboot by tool)
		            	general.setSubSetValue(GeneralValue.REBOOT_TAG, true);
		            	general.setSubSetValue(GeneralValue.REBOOT_TIME_TAG, String.valueOf(System.currentTimeMillis()));
		            	ConfigurationManager.getMainConfiguration().save(
		            			mContext.getFilesDir().getAbsolutePath()  + "/default.xml");

		            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "reboot intent");
	                	Intent i= new Intent( Intent.ACTION_REBOOT);
	                	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	                	mContext.startActivity(i);
	            	}	
	            	else
	            	{
	            		//setAlarm(mContext,REBOOT_ALARM,mSleepTime*1000,ALARM_REBOOT);
	            		setIdle();
	            	}
	            	ConfigurationManager.getMainConfiguration().save(
	            			mContext.getFilesDir().getAbsolutePath()  + "/default.xml");
            	}
            	else
            	{
            		//get current test item index
            		int testItemIdx = general.getSubSetIntValue(GeneralValue.CURRENT_ITEM_IDX_TAG);
            		//set next test item index
            		general.setSubSetValue(GeneralValue.CURRENT_ITEM_IDX_TAG, String.valueOf(testItemIdx+1));
            		//test finish, need to set current cycle to default value(0)
            		general.setSubSetValue(GeneralValue.CURRENT_CYCLE_TAG, GeneralValue.DEFAULT_CURRENT_CYCLE);
            		ConfigurationManager.getMainConfiguration().save(
	            			mContext.getFilesDir().getAbsolutePath()  + "/default.xml");
            		finish();
            	}
            }
            break;
        case EVENT_IDLE:
	        {
	        	//before reboot,need to set the REBOOT_TAG to true(reboot by tool)
				ConfigurationSet general = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.GENERAL_TAG);
	        	general.setSubSetValue(GeneralValue.REBOOT_TAG, true);
	        	general.setSubSetValue(GeneralValue.REBOOT_TIME_TAG, String.valueOf(System.currentTimeMillis()));
	        	ConfigurationManager.getMainConfiguration().save(
	        			mContext.getFilesDir().getAbsolutePath()  + "/default.xml");
	        	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "reboot intent");
	        	Intent i= new Intent( Intent.ACTION_REBOOT);
	        	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        	mContext.startActivity(i);
	        }
	        break;
        }
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		mContext.registerReceiver(mAlarmReceiver, new IntentFilter(ALARM_PROCESS_ACTION));
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		mHandler.removeMessages(EVENT_START_TEST);
		cancelAlarm(mContext,REBOOT_ALARM,ALARM_REBOOT);
		mContext.unregisterReceiver(mAlarmReceiver);
	}

	@Override
	protected void onAlarm(int alarm_type) {
		// TODO Auto-generated method stub
		/*
		Log.i(MainActivity.LOG_TAG,"onAlarm");
		switch(alarm_type)
		{
			case ALARM_REBOOT:
			{
				//before reboot,need to set the REBOOT_TAG to true(表示reboot by tool)
				ConfigurationSet general = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.GENERAL_TAG);
            	general.setSubSetValue(GeneralValue.REBOOT_TAG, true);
            	ConfigurationManager.getMainConfiguration().save(
            			mContext.getFilesDir().getAbsolutePath()  + "/default.xml");
            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "reboot intent");
            	Intent i= new Intent( Intent.ACTION_REBOOT);
            	i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
            	mContext.startActivity(i);
			}
			break;
		}
		*/
	}
	
	private void setIdle()
	{
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "set idle time:"+mSleepTime);
		startTimer(EVENT_IDLE,mSleepTime*1000);
	}

}
