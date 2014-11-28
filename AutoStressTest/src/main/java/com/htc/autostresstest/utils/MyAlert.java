package com.htc.autostresstest.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import com.htc.autostresstest.MainActivity;
import com.htc.autostresstest.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

public class MyAlert extends Activity {
	private Intent intent;
	private Bundle bundle;
	private String message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    setContentView(R.layout.myalert);
	    
	    intent = new Intent();
	    bundle = new Bundle();
	    intent = this.getIntent();
	    bundle = intent.getExtras();
	    message = bundle.getString("message");

	    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

	    // set title
	    alertDialogBuilder.setTitle("Test Result");

	    // set dialog message
	    alertDialogBuilder
	            .setMessage(message)
	            .setCancelable(false)
	            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    // if this button is clicked, just close
	                    // the dialog box and do nothing
	                    //stopService(getIntent());
	                    dialog.cancel();
	                    finish();
	                    WakelockUtils.relockKeyguard();
	                    WakelockUtils.fulllWakeLockRelease();
                        //If test pass then need to uninstall apk
	                    if(message.contains("Pass"))
	                    {
		                    try {
		    					final ApplicationManager am = new ApplicationManager(MyAlert.this);
		    					try {
		    						Log.i(MainActivity.LOG_TAG, "uninstall apk");
		    						am.uninstallPackage("com.htc.autostresstest");
		    					} catch (IllegalArgumentException e) {
		    						// TODO Auto-generated catch block
		    						e.printStackTrace();
		    					} catch (IllegalAccessException e) {
		    						// TODO Auto-generated catch block
		    						e.printStackTrace();
		    					} catch (InvocationTargetException e) {
		    						// TODO Auto-generated catch block
		    						e.printStackTrace();
		    					}
		    				} catch (SecurityException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				} catch (NoSuchMethodException e) {
		    					// TODO Auto-generated catch block
		    					e.printStackTrace();
		    				}
	                    }
	                }
	            });

	    // create alert dialog
	    AlertDialog alertDialog = alertDialogBuilder.create();

	    // show it
	    alertDialog.show();
	}
	
	 public boolean dispatchKeyEvent(KeyEvent event)
	 {
        final int keycode = event.getKeyCode();

        // block back key
        if (keycode != KeyEvent.KEYCODE_BACK)
        	return super.dispatchKeyEvent(event);
        return true;
	 }
	 
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	 // TODO Auto-generated method stub
	      if (keyCode == KeyEvent.KEYCODE_HOME) {
	           Log.i(MainActivity.LOG_TAG, "press home");
	      }
	 return super.onKeyDown(keyCode, event);
	 }

    //Ensure Alert dialog be exited by user tap home or other reasons,it still uninstall apk
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.i(MainActivity.LOG_TAG, "MyAlert-On Pause");
		
		//if pass then uninstall apk
		/*
		if(message.contains("Pass"))
        {
            try {
				final ApplicationManager am = new ApplicationManager(MyAlert.this);
				try {
					Log.i(MainActivity.LOG_TAG, "uninstall apk");
					am.uninstallPackage("com.htc.autostresstest");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }*/
    
		super.onPause();
	}

    //Ensure Alert dialog be exited by user tap home or other reasons,it still uninstall apk
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.i(MainActivity.LOG_TAG, "MyAlert-onStop");
		//if pass then uninstall apk
		
		if(message.contains("Pass"))
        {
            try {
				final ApplicationManager am = new ApplicationManager(MyAlert.this);
				try {
					Log.i(MainActivity.LOG_TAG, "uninstall apk");
					am.uninstallPackage("com.htc.autostresstest");
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}


}
