package com.htc.autostresstest.Reboot;

import com.htc.autostresstest.GeneralValue;
import com.htc.autostresstest.utils.Configuration.ConfigurationSet;

public class RebootValue {
	//every 30 sec, 10 trials
	public static final String Reboot30SEC_TAG         = "Reboot30secTest";
	public static final String Reboot30SECCYCLE         = "10";
	public static final String Reboot30SECSLEEP         = "30";
	
	//every 2 mins, 10 trials
	public static final String Reboot2MIN_TAG          = "Reboot2minTest";
	public static final String Reboot2MINCCYCLE         = "10";
	public static final String Reboot2MINSLEEP         = "120";
	
	//every 5 mins, 5 trials
	public static final String Reboot5MIN_TAG          = "Reboot5minTest";
	public static final String Reboot5MINCCYCLE         = "5";
	public static final String Reboot5MINSLEEP         = "300";
	
	//every 10 min, 4 trials
	public static final String Reboot10MIN_TAG         = "Reboot10minTest";
	public static final String Reboot10MINCCYCLE         = "4";
	public static final String Reboot10MINSLEEP         = "600";
	
	//general
	public static final String SLEEP         = "Sleep";
	
	public static void setupConfiguration1(ConfigurationSet cfg)
	{
		cfg.addSubSet(SLEEP, Reboot30SECSLEEP);
		cfg.addSubSet(GeneralValue.CYCLE, Reboot30SECCYCLE);
	}
 
	public static void setupConfiguration2(ConfigurationSet cfg)
	{
		cfg.addSubSet(SLEEP, Reboot2MINSLEEP);
		cfg.addSubSet(GeneralValue.CYCLE, Reboot2MINCCYCLE);
	}
	
	public static void setupConfiguration3(ConfigurationSet cfg)
	{
		cfg.addSubSet(SLEEP, Reboot5MINSLEEP);
		cfg.addSubSet(GeneralValue.CYCLE, Reboot5MINCCYCLE);
	}
	
	public static void setupConfiguration4(ConfigurationSet cfg)
	{
		cfg.addSubSet(SLEEP, Reboot10MINSLEEP);
		cfg.addSubSet(GeneralValue.CYCLE, Reboot10MINCCYCLE);
	}

}
