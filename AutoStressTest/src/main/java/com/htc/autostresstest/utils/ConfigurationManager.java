package com.htc.autostresstest.utils;

public class ConfigurationManager
{
    private static Configuration mInstance = null;

    public static Configuration getMainConfiguration()
    {
        if (mInstance == null)
        {
            synchronized (Configuration.class)
            {
                if (mInstance == null)
                {
                    mInstance = new Configuration();
                }
            }
        }
        return mInstance;
    }
}
