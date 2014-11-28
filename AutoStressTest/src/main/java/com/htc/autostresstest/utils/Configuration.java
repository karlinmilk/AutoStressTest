package com.htc.autostresstest.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.os.Bundle;
import android.util.Log;
import android.util.Xml;

import com.htc.autostresstest.MainActivity;

/*
 * 
 * The configuration xml format
 * 
 * <tag1> # subset
 *      # subsets
 *      <tag2>value1</tag2> # subset
 *      <tag3>value2</tag3> # subset
 * </tag1>
 * 
 * example:
 * 
 * tag1 = config.getSubSet("tag1")
 * 
 * tag1.getSubSets()
 * 
 * value1 = tag1.getSubSetValue("tag2")
 * "tag2" = tag1.getSubSets()[0].getTag()
 * 
 */
public class Configuration
{
    private ConfigurationSet mRootSet;

    public static Bundle createStringBundle(String name, String value)
    {
        Bundle bundle = new Bundle();
        bundle.putString(name, value);
        return bundle;
    }

    public Configuration()
    {
        mRootSet = new ConfigurationSet("unset");
    }

    public Configuration(String roottag)
    {
        mRootSet = new ConfigurationSet(roottag);
    }

    public ConfigurationSet find(String tag)
    {
        return mRootSet.getSubSet(tag, true);
    }

    public ConfigurationSet getRoot()
    {
        return mRootSet;
    }

    public ConfigurationSet getSubSet(String tag)
    {
        return mRootSet.getSubSet(tag, false);
    }

    public void clear()
    {
        mRootSet = null;
        mRootSet = new ConfigurationSet("unset");
    }

    public boolean load(String file)
    {
        File f = new File(file);
        if (!f.exists() || !f.canRead())
            return false;

        try
        {
            DataInputStream is = new DataInputStream(new FileInputStream(f));
            ConfigurationHandler handler = new ConfigurationHandler();
            Xml.parse(is, Xml.Encoding.UTF_8, handler);
            is.close();

            mRootSet = handler.getRoot();
        }
        catch (Exception e)
        {
            Log.w(MainActivity.LOG_TAG, e);
            return false;
        }
        return true;
    }

    private String generateXML(ConfigurationSet set)
    {
        String xml = "";
        String attrs = "";
        for (String key : set.getAttrKeys())
            attrs += " " + key + "=\"" + set.getStringAttr(key, "") + "\"";

        xml += "<" + set.getTag() + (attrs.length() > 0 ? attrs + ">" : ">");

        if (!set.getValue().isEmpty())
            xml += set.getValue();
        else
        {
            xml += "\r\n";
            for (ConfigurationSet subset : set.getSubSets())
                xml += generateXML(subset);
        }

        xml += "</" + set.getTag() + ">\r\n";
        return xml;
    }

    public boolean save(ConfigurationSet set, String file)
    {
        String xml = generateXML(set);

        try
        {
            File f = new File(file);
            DataOutputStream os = new DataOutputStream(new FileOutputStream(f));
            os.write(xml.getBytes());
            os.close();
        }
        catch (Exception e)
        {
            Log.w(MainActivity.LOG_TAG, e);
            return false;
        }
        return true;
    }

    public boolean save(String file)
    {
        return save(mRootSet, file);
    }

    public class ConfigurationHandler implements ContentHandler
    {
        private StringBuffer     mBuf     = new StringBuffer();
        private ConfigurationSet mCurrent = null;

        public ConfigurationSet getRoot()
        {
            ConfigurationSet root = mCurrent;
            while (root.getParent() != null)
                root = root.getParent();
            return root;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            mBuf.append(ch, start, length);
        }

        @Override
        public void endDocument() throws SAXException
        {
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
        {
            // this should be root element.
            if (mCurrent == null)
                mCurrent = new ConfigurationSet(qName);
            else
            {
                // add child element
                ConfigurationSet newSet = new ConfigurationSet(qName);
                mCurrent.addSubSet(newSet);
                mCurrent = newSet;
            }

            mCurrent.addAttributes(atts);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            if (!mBuf.toString().trim().equals(""))
                mCurrent.setValue(mBuf.toString().trim());
            mBuf.setLength(0);

            if (mCurrent.getParent() != null)
                mCurrent = mCurrent.getParent();
        }

        @Override
        public void endPrefixMapping(String prefix) throws SAXException
        {
        }

        @Override
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
        {
        }

        @Override
        public void processingInstruction(String target, String data) throws SAXException
        {
        }

        @Override
        public void setDocumentLocator(Locator locator)
        {
        }

        @Override
        public void skippedEntity(String name) throws SAXException
        {
        }

        @Override
        public void startDocument() throws SAXException
        {
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException
        {
        }

    }

    public class ConfigurationSet
    {
        private String                       mTag        = "";
        private String                       mValue      = "";
        private Bundle                       mAttributes = new Bundle();
        private ConfigurationSet             mParent     = null;
        private LinkedList<ConfigurationSet> mSet        = new LinkedList<ConfigurationSet>();

        public ConfigurationSet(String tag, String value)
        {
            mTag = tag;
            mValue = value;
        }

        public ConfigurationSet(String tag)
        {
            mTag = tag;
        }

        public ConfigurationSet(ConfigurationSet parent, String tag)
        {
            mParent = parent;
            mTag = tag;
        }

        public void setParent(ConfigurationSet set)
        {
            mParent = set;
        }

        public ConfigurationSet getParent()
        {
            return mParent;
        }

        public String getValue()
        {
            return mValue;
        }
        
        public boolean getBooleanValue()
        {
            return Boolean.parseBoolean(mValue);
        }
        
        public int getIntegerValue()
        {
            int ret = 0;
            try {
                ret = Integer.parseInt(mValue);
            } catch(Exception e) {
            }
            return ret;
        }
        
        public long getLongValue()
        {
            long ret = 0;
            try {
                ret = Long.parseLong(mValue);
            } catch(Exception e) {
            }
            return ret;
        }
        
        public void setValue(String value)
        {
            mValue = value;
        }

        public String getTag()
        {
            return mTag;
        }

        public void setTag(String tag)
        {
            mTag = tag;
        }

        public void addAttributes(Bundle attrs)
        {
            mAttributes = attrs;
        }

        public void addAttributes(Attributes atts)
        {
            for (int n = 0; n < atts.getLength(); n++)
                addAttribute(atts.getQName(n), atts.getValue(n));
        }

        public void addAttribute(String name, String value)
        {
            mAttributes.putString(name, value);
        }

        public void addAttribute(String name, int value)
        {
            mAttributes.putString(name, String.valueOf(value));
        }

        public void addAttribute(String name, boolean value)
        {
            mAttributes.putString(name, Boolean.toString(value));
        }

        public String getStringAttr(String name, String defaultValue)
        {
            return mAttributes.getString(name, defaultValue);
        }

        public int getIntAttr(String name, int defaultValue)
        {
            String val = mAttributes.getString(name, String.valueOf(defaultValue));
            return Integer.parseInt(val);
        }

        public boolean getBoolAttr(String name, boolean defaultValue)
        {
            String val = mAttributes.getString(name);
            if (val == null)
                return defaultValue;
            else
                return val.equals("true");
        }

        public void clearSubSets()
        {
            mSet.clear();
        }

        public synchronized LinkedList<ConfigurationSet> getSubSets()
        {
            return mSet;
        }

        public synchronized String getSubSetValue(String tag)
        {
            ConfigurationSet set = getSubSet(tag, false);
            if (set != null)
                return set.getValue();
            return null;
        }
        
        public synchronized int getSubSetIntValue(String tag)
        {
            ConfigurationSet set = getSubSet(tag, false);
            if (set != null)
                return set.getIntegerValue();
            return 0;
        }
        
        public synchronized long getSubSetLongValue(String tag)
        {
            ConfigurationSet set = getSubSet(tag, false);
            if (set != null)
                return set.getLongValue();
            return 0;
        }
        
        public synchronized boolean getSubSetBoolValue(String tag)
        {
            ConfigurationSet set = getSubSet(tag, false);
            if (set != null)
                return set.getBooleanValue();
            return false;
        }

        public synchronized boolean setSubSetValue(String tag, boolean value)
        {
            return setSubSetValue(tag, String.valueOf(value));
        }

        public synchronized boolean setSubSetValue(String tag, String value)
        {
            ConfigurationSet set = getSubSet(tag, false);
            if (set != null)
            {
                set.setValue(value);
                return true;
            }
            return false;
        }

        public synchronized ConfigurationSet getSubSet(String tag)
        {
            return getSubSet(tag, false);
        }

        public synchronized ConfigurationSet getSubSet(String tag, boolean recursive)
        {
            for (ConfigurationSet set : mSet)
            {
                if (set.getTag().equals(tag))
                    return set;
                else if (recursive)
                {
                    ConfigurationSet ret = set.getSubSet(tag, recursive);
                    if (ret != null)
                        return ret;
                }
            }
            return null;
        }

        public synchronized ConfigurationSet addSubSet(ConfigurationSet set)
        {
            set.setParent(this);
            mSet.add(set);
            return set;
        }

        public synchronized ConfigurationSet addSubSet(String tag, String value, Bundle attrs)
        {
            ConfigurationSet set = new ConfigurationSet(this, tag);
            if (value != null)
                set.setValue(value);
            if (attrs != null)
                set.addAttributes(attrs);
            mSet.add(set);
            return set;
        }

        public synchronized ConfigurationSet addSubSet(String tag, Bundle attrs)
        {
            return addSubSet(tag, null, attrs);
        }

        public synchronized ConfigurationSet addSubSet(String tag, String value)
        {
            return addSubSet(tag, value, null);
        }

        public synchronized ConfigurationSet addSubSet(String tag)
        {
            return addSubSet(tag, null, null);
        }

        public synchronized Set<String> getAttrKeys()
        {
            return mAttributes.keySet();
        }
    }
}
