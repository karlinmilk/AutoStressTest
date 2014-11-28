package com.htc.autostresstest.utils;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.htc.autostresstest.MainActivity;
import com.htc.autostresstest.utils.Logger;

/**
 * write detail log to /data/data/com.htc.autostress/files/AutoStressDetailLog.txt
 * @author jolin_ke
 *
 */
public class AutoStressLogger {
	//logger for detail log
	private static Logger mDetailLogger;
		
	public static void startLogging(String detailLogPath)
	{
		try
        {			
			File detail_file = new File(detailLogPath);
			if (!detail_file.exists())
				detail_file.createNewFile();     
    
    		mDetailLogger = new Logger(detail_file, "rw");
    		if(mDetailLogger != null)
	        {
    			Log.i(MainActivity.LOG_TAG,"detail log file was opened!");
    			mDetailLogger.seek(mDetailLogger.length());        	
    			mDetailLogger.println();       	
	        }

        }
        catch(Exception e)
        {
        	Log.w(MainActivity.LOG_TAG, "(startLogging) Exception: " + e.getMessage(), e);
        }
	}
	
	public static void stopLogging()
	{		
		if (mDetailLogger != null)
		{
			mDetailLogger.flush();
			mDetailLogger.close();
		}
	}
	
	public static  void detailLogging(String LOG_TAG,String msg,Throwable e)
	{
		Log.e(LOG_TAG, msg, e);
		
		if (mDetailLogger == null)
			return;
		
		mDetailLogger.full_date_println(msg+e.getMessage());
	}
	
	public static  void detailLogging(String LOG_TAG,String msg)
	{
		Log.i(LOG_TAG, msg);
		
		if (mDetailLogger == null)
			return;
		
		mDetailLogger.full_date_println(msg);
	}
	
	
	//Start AutoStressLogService(on phone process) to write log to SD card
	public static void writeLog(Context context, String msg)
	{
		Intent i = new Intent();
        i.setClassName(context, AutoStressLogService.class.getName());
        i.putExtra("log", msg);
        context.startService(i);
	}

}
