package com.htc.autostresstest;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.htc.autostresstest.MO.MOValue;
import com.htc.autostresstest.utils.AutoStressLogger;
import com.internal.library.os.AsyncResult;
import com.internal.library.phone.Call;
import com.internal.library.phone.CallManager;
import com.internal.library.phone.Connection;
import com.internal.library.phone.ITelephony;
import com.internal.library.phone.PhoneConstants;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class PhoneService extends Service {
	
	private String mPhoneNumber;
	private int mDuration;	//call duration
    private long startTime=0;
    private long runTime;    
    protected HandlerThread              mPhoneThread;
    public static PhoneHandler         mPhoneHandler;
	
    private static final int QUIT_TEST_PROCESS = 0;
    private static final int EVENT_END_CALL = 1;
    private static final int PRECISE_CALL_STATE_CHANGED=2;
    
    public static final String IDLE_BROADCAST = "com.htc.autostresstest.idle_broadcast";
    public static final String FAIL_BROADCAST = "com.htc.autostresstest.fail_broadcast";
    
    protected CallManager mCallManager;
    protected Call mCurrentCall;
	protected Call.State mLastCallState= Call.State.IDLE;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		mPhoneThread = new HandlerThread("PhoneThread");
		mPhoneThread.start();
		mPhoneHandler = new PhoneHandler(mPhoneThread.getLooper());
		mCallManager = CallManager.getInstance();
		registerPhoneStatus();
		mPhoneNumber = intent.getStringExtra("phonenum");
		mDuration = intent.getIntExtra("duration", Integer.valueOf(MOValue.DURATION_TIME1));
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "mPhoneNumber:"+mPhoneNumber);
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "mDuration:"+mDuration);
		makeCall();
	}
	
	public class PhoneHandler extends Handler
    {
        public PhoneHandler(Looper looper)
		{
		    super(looper);
		}

		public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
	            case PRECISE_CALL_STATE_CHANGED:
				{
					Log.i(MainActivity.LOG_TAG, "In PRECISE_CALL_STATE_CHANGED section  ");
			    	
			    	Call.State callState;
					PhoneConstants.State phoneState;
			    	
					Connection connection = null;				               
	                
	                connection = new Connection((new AsyncResult (msg.obj)).result);
	                
	                phoneState = mCallManager.getState();
	
	                
	                if(phoneState == PhoneConstants.State.RINGING)
					{
	                	Log.i(MainActivity.LOG_TAG, "getFirstActiveRingingCall");
	                	mCurrentCall =mCallManager.getFirstActiveRingingCall();
	                	callState = mCurrentCall.getState();
					}
					else
					{
						Log.i(MainActivity.LOG_TAG, "getActiveFgCall");
						mCurrentCall =mCallManager.getActiveFgCall();
						callState = mCurrentCall.getState();	
					}
	                AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"call state:"+callState+", lastCallState:"+mLastCallState);
	                if(callState == Call.State.IDLE || callState == Call.State.DISCONNECTED)
					{
						if( mLastCallState != Call.State.IDLE && mLastCallState != Call.State.DISCONNECTED )
						{						
							long dropTime = System.currentTimeMillis();
							if(startTime==0)
							{
                                //Dialing or Alerting call be dropped
								Log.i(MainActivity.LOG_TAG, "drop call");
			            		//send broadcast to pop up fail dialog
			            		Intent intent = new Intent(FAIL_BROADCAST);
				            	sendBroadcast(intent);
				            	//stop PhoneService
				            	stopSelf();
							}
							else
							{
                                //Call be dropped when call active
				            	runTime = dropTime-startTime;
				            	Date dt = new Date(dropTime);
				            	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				            	String dT = sdf.format(dt);
				            	
				            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"dropTime:"+dT);
				            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"runTime:"+runTime);
                                //Call be dropped-> fail->finish test
				            	if(runTime<mDuration*1000)
				            	{
				            		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "drop call");
				            		//send broadcast to pop up fail dialog
				            		Intent intent = new Intent(FAIL_BROADCAST);
					            	sendBroadcast(intent);
					            	//stop PhoneService
					            	stopSelf();
				            		
				            	}
							}
						}
						
					}
					else if( callState == Call.State.DIALING )
					{
						
					}
					else if(callState == Call.State.ALERTING || callState == Call.State.INCOMING )
					{
						
					}
					else if( callState == Call.State.ACTIVE )
					{
                        //Start to calculate call duration when call active
						startTime = System.currentTimeMillis();
		            	Date dt = new Date(startTime);
		            	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		            	String time = sdf.format(dt);
		            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"start time:"+time);
		            	setRunTime();
					}
					
					mLastCallState = callState;
					
					AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Current Call State: " + callState+"Phone state: "+phoneState);
					
				}
				break;
	            case EVENT_END_CALL:
				{
	            	long endTime = System.currentTimeMillis();
	            	Date dt = new Date(endTime);
	            	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	            	String time = sdf.format(dt);
	            	AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"end time:"+time);
	            	endCall();
	            	//send broadcast to set idle time
	            	Intent intent = new Intent(IDLE_BROADCAST);
	            	sendBroadcast(intent);
	            	//stop PhoneService
	            	stopSelf();
				}
				break;
	            case QUIT_TEST_PROCESS:
                {
                    Looper.myLooper().quit();
                }
                break;
            }
        }
		 public void quit()
		 {
		 	Message msg;
            msg = mPhoneHandler.obtainMessage(QUIT_TEST_PROCESS);
            msg.sendToTarget();
		 }
    }
	
	private void makeCall()
	{
	    // dial phone number
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "sendBroadcast: ACTION_CALL");			
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + mPhoneNumber));
    	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(intent);
        Log.i(MainActivity.LOG_TAG, "dial number="+Uri.parse("tel:" + mPhoneNumber));
	}
	
	private boolean endCall()
    {
    	ITelephony iTelephony = ITelephony.getITelephony(this);
    	return iTelephony.endCall();
    } 
	
	private void registerPhoneStatus()
	{
    	try{
			mCallManager.registerForPreciseCallStateChanged(mPhoneHandler, PRECISE_CALL_STATE_CHANGED, null);
		}catch (Exception e){
			AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"register PreciseCallStateChanged fail.");
			Log.w(MainActivity.LOG_TAG,e);
		}
	}
		
    private void unregisterPhoneStatus()
	{
    	try{
			mCallManager.unregisterForPreciseCallStateChanged(mPhoneHandler);
		}catch (Exception e)
		{
			AutoStressLogger.detailLogging(MainActivity.LOG_TAG,"unregister ServiceStateChanged fail");
			Log.w(MainActivity.LOG_TAG,e);
		}		
	}
    
	//set every cycle run time
	private void setRunTime()
    {
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Set call duration: " + mDuration);
        mPhoneHandler.sendMessageDelayed(mPhoneHandler.obtainMessage(EVENT_END_CALL), mDuration*1000);
    }

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterPhoneStatus();
		mPhoneHandler.removeMessages(EVENT_END_CALL);
		if (mPhoneHandler != null)
			mPhoneHandler.quit();
	}

	
}
