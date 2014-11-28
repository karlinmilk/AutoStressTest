package com.htc.autostresstest.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.htc.autostresstest.MainActivity;

public class EventHandler extends Handler
{
    public static final int QUIT_TEST_PROCESS = 9999999;
    private EventCallback mCallback = null;
    
    public EventHandler(Looper looper)
    {
        super(looper);
    }
    
    public EventHandler(Looper looper, EventCallback cb)
    {
        super(looper);
        setEventCallback(cb);
    }
    
    public void setEventCallback(EventCallback cb)
    {
        mCallback = cb;
    }
    
    public void handleMessage(Message msg)
    {
        switch(msg.what)
        {
        case QUIT_TEST_PROCESS:
            {
                Log.i(MainActivity.LOG_TAG, "EventHandler:quit()");
                Looper.myLooper().quit();
            }
            break;
            
        default:
            {
                if( mCallback != null )
                    mCallback.onReceiveEvent(msg);
            }
            break;
        }
    }
    
    public void quit()
    {
        Message msg;
        msg = obtainMessage(QUIT_TEST_PROCESS);
        msg.sendToTarget();
    }
}
