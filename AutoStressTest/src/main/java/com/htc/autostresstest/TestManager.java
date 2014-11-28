package com.htc.autostresstest;

import java.util.LinkedList;


import com.htc.autostresstest.MO.MO1minTest;
import com.htc.autostresstest.MO.MO2minTest;
import com.htc.autostresstest.MO.MOValue;
import com.htc.autostresstest.Reboot.Reboot10minTest;
import com.htc.autostresstest.Reboot.Reboot2minTest;
import com.htc.autostresstest.Reboot.Reboot30secTest;
import com.htc.autostresstest.Reboot.Reboot5minTest;
import com.htc.autostresstest.Reboot.RebootValue;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;
import com.htc.autostresstest.utils.AutoStressLogger;
import com.htc.autostresstest.utils.ConfigurationManager;
import com.htc.autostresstest.utils.EventCallback;
import com.htc.autostresstest.utils.EventHandler;
import com.htc.autostresstest.utils.Test;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class TestManager implements EventCallback
{
    private HandlerThread mTestThread;
    private EventHandler mTestHandler;
    
    private HandlerThread mThread;
    private EventHandler mHandler;
    
    private LinkedList<Test> mTests = new LinkedList<Test>();
    private int mIndex = 0;
    private Message mCompleteMessage;
    private Context mContext;
    
    private final int TEST_COMPLETED = 0;

    public TestManager(Context context)
    {
        mContext = context;
        // for test item use
        mTestThread = new HandlerThread("AutoTestThread");
        mTestThread.start();
        mTestHandler = new EventHandler(mTestThread.getLooper());
        
        // for test manager use
        mThread = new HandlerThread("AutoTestManager");
        mThread.start();
        mHandler = new EventHandler(mThread.getLooper(), this);
        
        // create test items
        LinkedList<ConfigurationSet> sets = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.RUN_TAG).getSubSets();
        for(ConfigurationSet set : sets)
            mTests.add(createTest(set.getValue()));
    }
    
    public void start(Message onComplete)
    {
        mIndex =  ConfigurationManager.getMainConfiguration().getRoot().
        		getSubSet(GeneralValue.GENERAL_TAG).getSubSetIntValue(GeneralValue.CURRENT_ITEM_IDX_TAG);
        AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "mIndex:"+mIndex);
        mCompleteMessage = onComplete;
        mTestHandler.setEventCallback(mTests.get(mIndex));
        mTests.get(mIndex).start(mHandler.obtainMessage(TEST_COMPLETED));
    }
    
    public void release()
    {
        for(Test test : mTests)
        {
            try {
                test.release();
            } catch(Exception e) {
                Log.w(MainActivity.LOG_TAG, e);
            }
        }
        
        if( mTestHandler != null )
        {
            mTestHandler.quit();
            mTestHandler = null;
        }
        
        if( mHandler != null )
        {
            mHandler.quit();
            mHandler = null;
        }
    }
    
    private Test createTest(String name)
    {
    	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Create test: " + name);
        if( name.equals(MOValue.MO1MIN_TAG))
        	return new MO1minTest(mContext, mTestHandler, MOValue.MO1MIN_TAG);
        else if( name.equals(MOValue.MO2MIN_TAG))
        	return new MO2minTest(mContext, mTestHandler, MOValue.MO2MIN_TAG);
        else if( name.equals(RebootValue.Reboot30SEC_TAG))
        	return new Reboot30secTest(mContext, mTestHandler, RebootValue.Reboot30SEC_TAG);
        else if( name.equals(RebootValue.Reboot2MIN_TAG))
        	return new Reboot2minTest(mContext, mTestHandler, RebootValue.Reboot2MIN_TAG);
        else if( name.equals(RebootValue.Reboot5MIN_TAG))
        	return new Reboot5minTest(mContext, mTestHandler, RebootValue.Reboot5MIN_TAG);
        else if( name.equals(RebootValue.Reboot10MIN_TAG))
        	return new Reboot10minTest(mContext, mTestHandler, RebootValue.Reboot10MIN_TAG);
        
        return null;
    }

    @Override
    public void onReceiveEvent(Message msg)
    {
        switch(msg.what)
        {
        case TEST_COMPLETED:
            {
            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Test Completed");              
                
                mIndex = ConfigurationManager.getMainConfiguration().getRoot().
        			getSubSet(GeneralValue.GENERAL_TAG).getSubSetIntValue(GeneralValue.CURRENT_ITEM_IDX_TAG);
                AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "--mIndex:"+mIndex);
                if( mIndex >= mTests.size() )
                    mCompleteMessage.sendToTarget();
                else
                {
                    mTestHandler.setEventCallback(mTests.get(mIndex));
                    mTests.get(mIndex).start(mHandler.obtainMessage(TEST_COMPLETED));
                }
            }
            break;
        }
    }
     
    
}
