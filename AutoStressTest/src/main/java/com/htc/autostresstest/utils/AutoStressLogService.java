package com.htc.autostresstest.utils;

import java.io.File;

import com.htc.autostresstest.MainActivity;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

/**
 * write log to SD card (phone process)
 * @author jolin_ke
 *
 */

public class AutoStressLogService extends IntentService{

	private static String LogFile = "AutoStressLog.txt";
	
	//logger for test result
	private static Logger mLogger;
	
	public AutoStressLogService() {
		super("AutoStressLogService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(MainActivity.LOG_TAG, "onHandleIntent(), start work.");
		String log = intent.getExtras().getString("log");
		String logPath = Environment.getExternalStorageDirectory()+"/"+LogFile;
		try
        {
			File file = new File(logPath);
			if (!file.exists())
				file.createNewFile();
			
			mLogger = new Logger(file, "rw");
	        if(mLogger != null)
	        {
	        	Log.i(MainActivity.LOG_TAG,"log file was opened!");
	        	mLogger.seek(mLogger.length());        	
	        	//mLogger.println();	
	        }
	     }
		catch(Exception e)
        {
        	Log.w(MainActivity.LOG_TAG, "(AutoStressLogService) Exception: " + e.getMessage(), e);
        }
		logging(log);
	        
	}
	
	private static  void logging(String msg)
	{
		if (mLogger == null)
			return;
		
		mLogger.full_date_println(msg);
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		Log.d(MainActivity.LOG_TAG, "IntentService on destroy");
		if (mLogger != null)
		{
			mLogger.flush();
			mLogger.close();
		}
		super.onDestroy();
	}

}
