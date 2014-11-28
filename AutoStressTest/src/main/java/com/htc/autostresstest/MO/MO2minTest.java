package com.htc.autostresstest.MO;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

import com.htc.autostresstest.GeneralValue;
import com.htc.autostresstest.MainActivity;
import com.htc.autostresstest.PhoneService;
import com.htc.autostresstest.TestService;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;
import com.htc.autostresstest.utils.AutoStressLogger;
import com.htc.autostresstest.utils.ConfigurationManager;
import com.htc.autostresstest.utils.EventHandler;
import com.htc.autostresstest.utils.Test;
import com.htc.autostresstest.utils.WakelockUtils;

/**
 *  - 25 calls - Call duration 2 mins, Idle 20 sec, around 33.3 mins
 *
 */
public class MO2minTest extends Test{
	private int mTotalCycle = 0;
    private int mCycle = 0;
    private int mIdle;
    private int mDuration;
    private String mPhoneNumber;

    private final int EVENT_IDLE = 1;
	
	public MO2minTest(Context context, EventHandler handler, String tag) {
		super(context, handler, tag);
		// TODO Auto-generated constructor stub
		mTotalCycle = mConfig.getSubSetIntValue(GeneralValue.CYCLE);
		mIdle = mConfig.getSubSetIntValue(MOValue.IDLE);
		mDuration = mConfig.getSubSetIntValue(MOValue.DURATION);
		ConfigurationSet general = ConfigurationManager.getMainConfiguration().getSubSet(GeneralValue.GENERAL_TAG);
		mPhoneNumber = general.getSubSetValue(GeneralValue.PHONE_NUMBER);
	}

	@Override
	public void onReceiveEvent(Message msg) {
		// TODO Auto-generated method stub
		 switch(msg.what)
	     {
	        case EVENT_START_TEST:
	            {
	            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Start MO2min test Cycle: " + (mCycle+1));
	            	AutoStressLogger.writeLog(mContext,"Start MO2min test Cycle: " + (mCycle+1));
	            	//start PhoneSerice
	            	Intent it = new Intent();
	            	it.setClass(mContext, PhoneService.class);
	            	it.putExtra("duration", mDuration);
	            	it.putExtra("phonenum", mPhoneNumber);
	            	mContext.startService(it);
	            }
	            break;
	        case EVENT_IDLE:
	        {
	        	mCycle++;
                if( mCycle >= mTotalCycle )
                {
                	ConfigurationSet general = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.GENERAL_TAG);
                	//get current test item index
                	int testItemIdx = general.getSubSetIntValue(GeneralValue.CURRENT_ITEM_IDX_TAG);
                	//set next test item index
            		general.setSubSetValue(GeneralValue.CURRENT_ITEM_IDX_TAG, String.valueOf(testItemIdx+1));
            		ConfigurationManager.getMainConfiguration().save(
	            			mContext.getFilesDir().getAbsolutePath()  + "/default.xml");
                    finish();
                }
                else
                    mHandler.sendEmptyMessage(EVENT_START_TEST);
	        }
	        break;
	     }
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		IntentFilter intentFilter = new IntentFilter(PhoneService.IDLE_BROADCAST);
		intentFilter.addAction(PhoneService.FAIL_BROADCAST);
		mContext.registerReceiver(mReceiver, intentFilter);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		mHandler.removeMessages(EVENT_START_TEST);
		stopTimer(EVENT_IDLE);
		mContext.unregisterReceiver(mReceiver);
	}
	
	//set idle time between cycle and cycle
	private void setIdleTime()
	{
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Set idle time: " + mIdle);
        startTimer(EVENT_IDLE, mIdle*1000);
	}
	
	//Receive broadcast from PhoneService
	private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context arg0, Intent intent)
        {
        	if (intent.getAction().equals(PhoneService.IDLE_BROADCAST))
            {
        		setIdleTime();
            }
        	else if(intent.getAction().equals(PhoneService.FAIL_BROADCAST))
        	{
        		//[Fail] stopService -> pop up dialog
        		AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"<MO2minTest> [Fail] Drop Call");
        		AutoStressLogger.writeLog(mContext, "<MO2minTest> [Fail] Drop Call");
        		((TestService)mContext).stopSelf();
        		alertDialog(mContext,"<MO2minTest>\r\n [Fail] Drop Call");
        	}
        }
    };

	@Override
	protected void onAlarm(int alarm_type) {
		// TODO Auto-generated method stub
		
	}

}
