package com.htc.autostresstest;


import java.lang.reflect.InvocationTargetException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.os.UserHandle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.htc.autostresstest.MO.MOValue;
import com.htc.autostresstest.Reboot.RebootValue;
import com.htc.autostresstest.utils.ApplicationManager;
import com.htc.autostresstest.utils.AutoStressLogger;
import com.htc.autostresstest.utils.Configuration;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;
import com.htc.autostresstest.utils.ConfigurationManager;

import android.content.pm.*;

public class MainActivity extends PreferenceActivity implements OnPreferenceClickListener, OnPreferenceChangeListener{
	public static String       LOG_TAG               = "AutoStressTest";
	// add test option, need modify config version number.
    public static final String CONFIGURATION_VERSION = "2";
	private EditTextPreference mPhoneNumber;
	
	//test result log file for user
	public static String LogFile = "AutoStressLog.txt";
	//detail log file for developer
	public static String DetailLogFile = "AutoStressDetailLog.txt";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.activity_main);

		/*
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		*/
		mPhoneNumber = (EditTextPreference)findPreference("phone_number");
        mPhoneNumber.setOnPreferenceChangeListener(this);
        findPreference("start_reboot").setOnPreferenceClickListener(this);
        findPreference("start_mo").setOnPreferenceClickListener(this);
        findPreference("start_all").setOnPreferenceClickListener(this);
        
        getFilesDir().mkdir();
        if (!ConfigurationManager.getMainConfiguration().load(
        		getFilesDir().getAbsolutePath() + "/default.xml"))
        {
            Toast.makeText(this, getString(R.string.cfg_reset), Toast.LENGTH_LONG).show();
            restoreToDefault();
            ConfigurationManager.getMainConfiguration()
                    .save(getFilesDir().getAbsolutePath() + "/default.xml");
        }
        else
        {
            // get cfg version and if it doesn't match, still do the restore to
            // default
            if (!CONFIGURATION_VERSION.equals(ConfigurationManager.getMainConfiguration().getRoot()
                    .getStringAttr(GeneralValue.VERSION, "0")))
            {
                Toast.makeText(this, getString(R.string.cfg_ver_reset), Toast.LENGTH_LONG).show();
                restoreToDefault();
                ConfigurationManager.getMainConfiguration().save(
                		getFilesDir().getAbsolutePath() + "/default.xml");
            }
        }

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		try
        {
            menu.findItem(R.id.menu_version).setTitle(
                    getText(R.string.tool_version)
                            + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        }
        catch (Exception e)
        {
            Log.w(MainActivity.LOG_TAG, e);
        }
        return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
       super.onOptionsItemSelected(item);
       switch(item.getItemId())
       {
       		case R.id.menu_stop:          			
       			Intent it = new Intent();
       		    it.setClass(this, TestService.class);
       		    stopService(it);
       			break;
       }
       return true;
	}

	@Override
	public boolean onPreferenceChange(Preference p, Object value) {
		// TODO Auto-generated method stub
		p.setSummary(value.toString());
		if(p==mPhoneNumber)
		{
			mPhoneNumber.setText(value.toString());
			if(value.toString()==null || value.toString().length()==0 )
			{
				WarningDialog("Please input phone number!");
				return false;
			}
		}
		saveGeneralSetting();
        return true;
	}

	@Override
	public boolean onPreferenceClick(Preference p) {
		// TODO Auto-generated method stub
		ConfigurationSet run = ConfigurationManager.getMainConfiguration().getSubSet(GeneralValue.RUN_TAG);
		ConfigurationSet general = ConfigurationManager.getMainConfiguration().getSubSet(GeneralValue.GENERAL_TAG);
		general.setSubSetValue(GeneralValue.RUNNING_TAG, false);
		general.setSubSetValue(GeneralValue.REBOOT_TAG, false);
		general.setSubSetValue(GeneralValue.CURRENT_CYCLE_TAG, GeneralValue.DEFAULT_CURRENT_CYCLE);
		general.setSubSetValue(GeneralValue.CURRENT_ITEM_IDX_TAG, GeneralValue.DEFAULT_ITEM_IDX);
		general.setSubSetValue(GeneralValue.REBOOT_TIME_TAG, GeneralValue.DEFAULT_REBOOT_TIME);
		String[] tests = getResources().getStringArray(R.array.test_items);
		
		Intent it = new Intent();
	    it.setClass(this, TestService.class);	
		Log.i(LOG_TAG, "key:"+p.getKey());
		if(p.getKey().equals("start_reboot"))
		{
			run.clearSubSets();
			for(int n=0;n<4;n++)
				run.addSubSet(GeneralValue.TEST_TAG,tests[n]);
			 
			 
		}
		else if(p.getKey().equals("start_mo"))
		{
			run.clearSubSets();
			for(int n=4;n<6;n++)
				run.addSubSet(GeneralValue.TEST_TAG,tests[n]);
			if(mPhoneNumber.getText()==null || mPhoneNumber.getText().length()==0 )
			{
				WarningDialog("Please input phone number!");
				return false;
			}
			 
			 
		}
		else if(p.getKey().equals("start_all"))
		{
			run.clearSubSets();
			for(int n=0;n<6;n++)
				run.addSubSet(GeneralValue.TEST_TAG,tests[n]);
			if(mPhoneNumber.getText()==null || mPhoneNumber.getText().length()==0 )
			{
				WarningDialog("Please input phone number!");
				return false;
			}
						 
		}
		
		AutoStressLogger.startLogging(getFilesDir().getAbsolutePath() +"/"+DetailLogFile);	
		ConfigurationManager.getMainConfiguration().save(getFilesDir().getAbsolutePath() + "/default.xml");
		AutoStressLogger.detailLogging(MainActivity.LOG_TAG, "Version:1.0.0.1");
		startService(it);
		return true;
	}
	
	private void WarningDialog(String msg)
    {
        // TODO Auto-generated method stub
        new AlertDialog.Builder(this).setTitle("Warning").setMessage(msg)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int btn)
                    {
                    }
                }).setCancelable(false).show();
    }
	
	@Override
    protected void onPause()
    {
        super.onPause();
        saveGeneralSetting();
    }
	
	@Override
    protected void onResume()
    {
        super.onResume();

        ConfigurationSet general = ConfigurationManager.getMainConfiguration().getSubSet(GeneralValue.GENERAL_TAG);
        mPhoneNumber.setText(general.getSubSetValue(GeneralValue.PHONE_NUMBER));
        mPhoneNumber.setSummary(general.getSubSetValue(GeneralValue.PHONE_NUMBER));
	}
	
	private void restoreToDefault()
    {
        Configuration cfg = ConfigurationManager.getMainConfiguration();
        cfg.clear();

        ConfigurationSet root = cfg.getRoot();
        root.setTag(getString(R.string.app_name));
        root.addAttribute(GeneralValue.VERSION, CONFIGURATION_VERSION);
        ConfigurationSet def = root.addSubSet(GeneralValue.DEFAULT_TAG);
        
        //MO1min
        ConfigurationSet mo1 = def.addSubSet(MOValue.MO1MIN_TAG);
        MOValue.setupConfiguration1(mo1);
        
        //MO2min
        ConfigurationSet mo2 = def.addSubSet(MOValue.MO2MIN_TAG);
        MOValue.setupConfiguration2(mo2);
        
        //Reboot30sec
        ConfigurationSet sr1 = def.addSubSet(RebootValue.Reboot30SEC_TAG);
        RebootValue.setupConfiguration1(sr1);
        
        //Reboot2min
        ConfigurationSet sr2 = def.addSubSet(RebootValue.Reboot2MIN_TAG);
        RebootValue.setupConfiguration2(sr2);
        
        //Reboot5min
        ConfigurationSet sr3 = def.addSubSet(RebootValue.Reboot5MIN_TAG);
        RebootValue.setupConfiguration3(sr3);
        
        //Reboot10min
        ConfigurationSet sr4 = def.addSubSet(RebootValue.Reboot10MIN_TAG);
        RebootValue.setupConfiguration4(sr4);
        
        //general
        ConfigurationSet general = root.addSubSet(GeneralValue.GENERAL_TAG);
        general.addSubSet(GeneralValue.PHONE_NUMBER, GeneralValue.DEFAULT_NUMBER);
        general.addSubSet(GeneralValue.REBOOT_TAG, GeneralValue.DEFAULT_REBOOT); //Record if reboot by tool
        general.addSubSet(GeneralValue.RUNNING_TAG, GeneralValue.DEFAULT_RUN);   //Record if test is running
        general.addSubSet(GeneralValue.CURRENT_CYCLE_TAG, GeneralValue.DEFAULT_CURRENT_CYCLE);//Record which cycle is running in every test item
        general.addSubSet(GeneralValue.CURRENT_ITEM_IDX_TAG, GeneralValue.DEFAULT_ITEM_IDX);//Record which test item is running
        general.addSubSet(GeneralValue.REBOOT_TIME_TAG, GeneralValue.DEFAULT_REBOOT_TIME);//Record reboot start time
        //Run
        ConfigurationSet run = root.addSubSet(GeneralValue.RUN_TAG);
        
    }
        

	
	private void saveGeneralSetting()
    {
        ConfigurationSet general = ConfigurationManager.getMainConfiguration().getSubSet(GeneralValue.GENERAL_TAG);
        general.setSubSetValue(GeneralValue.PHONE_NUMBER, mPhoneNumber.getText());
        ConfigurationManager.getMainConfiguration().save(getFilesDir().getAbsolutePath() + "/default.xml");
    }


	/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	*/

	/**
	 * A placeholder fragment containing a simple view.
	 */
	/*
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	*/

}
