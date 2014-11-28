package com.htc.autostresstest.MO;

import com.htc.autostresstest.GeneralValue;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;

public class MOValue {
	//25 calls - Call duration 1 mins, Idle 20 sec, around 33.3 mins
	public static final String MO1MIN_TAG          = "MO1minTest";
	public static final String DURATION_TIME1      = "60";
	
	//25 calls - Call duration 2 mins, Idle 20 sec, around 58.3 mins
	public static final String MO2MIN_TAG          = "MO2minTest";
	public static final String DURATION_TIME2      = "120";
	
	//general
	public static final String DURATION			   = "duration";
	public static final String IDLE			   	   = "idle";
	public static final String IDLE_TIME		   = "20";
	public static final String CYCLE_VALUE         = "25";
	
	 
	 public static void setupConfiguration1(ConfigurationSet cfg)
	 {
		 cfg.addSubSet(DURATION, DURATION_TIME1);
		 cfg.addSubSet(IDLE, IDLE_TIME);
		 cfg.addSubSet(GeneralValue.CYCLE, CYCLE_VALUE);
	 }
	 
	 public static void setupConfiguration2(ConfigurationSet cfg)
	 {
		 cfg.addSubSet(DURATION, DURATION_TIME2);
		 cfg.addSubSet(IDLE, IDLE_TIME);
		 cfg.addSubSet(GeneralValue.CYCLE, CYCLE_VALUE);
	 }

}
