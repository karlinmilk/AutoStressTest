package com.htc.autostresstest;

import java.util.LinkedList;

import com.htc.autostresstest.utils.AutoStressLogger;
import com.htc.autostresstest.utils.ConfigurationManager;
import com.htc.autostresstest.utils.MyAlert;
import com.htc.autostresstest.utils.Test;
import com.htc.autostresstest.utils.WakelockUtils;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class AutoStressReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub

		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			AutoStressLogger.startLogging(context.getFilesDir().getAbsolutePath()+"/"+MainActivity.DetailLogFile);
			if(WakelockUtils.mPartialWakeLock==null)
	        	WakelockUtils.partialWakeLockAcquire(context);
			AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "reboot completed");
			
			if (ConfigurationManager.getMainConfiguration().load(
					context.getFilesDir().getAbsolutePath()  + "/default.xml"))
	        {
	        
	        	ConfigurationSet general = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.GENERAL_TAG);
	        	boolean run = general.getSubSetBoolValue(GeneralValue.RUNNING_TAG);
	        	boolean reboot = general.getSubSetBoolValue(GeneralValue.REBOOT_TAG);
	        	long rebootStartTime = general.getSubSetLongValue(GeneralValue.REBOOT_TIME_TAG);
	        	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "test is running:"+run);
	        	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "reboot by tool:"+reboot);
	        	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "rebootStartTime:"+rebootStartTime);
	        	long bootCompletedTime = System.currentTimeMillis();
	        	AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "current_time:"+bootCompletedTime);
	        	
	        	String item = getTestItemName();
	        	//test is running and no reboot by tool-->Fail
				if(run && !reboot)
				{			
					//pop up fail dialog
					AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "<"+item+"> [Fail] Auto reboot by device self");
					AutoStressLogger.writeLog(context, "<"+item+"> [Fail] Auto reboot by device self");
					
					WakelockUtils.disableKeyguard(context);
					Test.alertDialog(context, "<"+item+">\r\n"+"[Fail] Auto reboot by device self");
					if(WakelockUtils.mPartialWakeLock!=null)
						WakelockUtils.partialtWakeLockRelease();
				}
				//test is running and reboot by tool-->Pass
				if(run && reboot)
				{
					long spendTime = (bootCompletedTime-rebootStartTime);
					AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "reboot spend time:"+spendTime);
					if(spendTime<600000)
					{
						AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "[Pass] Auto reboot by tool");
						Intent it = new Intent();
						it.setClass(context, TestService.class);	
						context.startService(it);
					}
                    //If boot time exceeds 10 mins, it is possible that device hang when booting then user reboot manually
					else
					{
						//pop up fail dialog
						AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "<"+item+"> [Fail] Device maybe hang when reboot");
						AutoStressLogger.writeLog(context, "<"+item+"> [Fail] Device maybe hang when reboot");
						
						WakelockUtils.disableKeyguard(context);
						Test.alertDialog(context, "<"+item+">\r\n"+"[Fail] Device maybe hang when reboot");
						if(WakelockUtils.mPartialWakeLock!=null)
							WakelockUtils.partialtWakeLockRelease();
					}
				}
				//need to set REBOOT_TAG to false
				general.setSubSetValue(GeneralValue.REBOOT_TAG, false);
				ConfigurationManager.getMainConfiguration().save(
						context.getFilesDir().getAbsolutePath()  + "/default.xml");
	        }
		}
	}
	
	//get current test item name
	private String getTestItemName()
	{
		String item="null";
		int idx;
		ConfigurationSet general = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.GENERAL_TAG);
		idx = general.getSubSetIntValue(GeneralValue.CURRENT_ITEM_IDX_TAG);
		LinkedList<ConfigurationSet> sets = ConfigurationManager.getMainConfiguration().getRoot().getSubSet(GeneralValue.RUN_TAG).getSubSets();
		if(sets.size()==0)
			return item;
		item = sets.get(idx).getValue();
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "item:"+item);
		return item;
	}

}
