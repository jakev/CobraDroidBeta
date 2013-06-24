package com.jakev.emucore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.Context;
import android.content.res.AssetManager;
import android.telephony.TelephonyManager;
import android.util.Log;

public class FileManager {

	private final static String TAG = "FileManager";
	private static final String SYSTEM_BUILD_FILE = "/system/build.prop";
	
	final static String BUILD_PROP_FILE = "/data/data/com.jakev.emucore/build.prop";
	final static String MOD_STATE_FILE = "/data/data/com.jakev.emucore/modstate";
	final static String DEVICE_IDS_FILE = "/data/data/com.jakev.emucore/device_ids.txt";
	
	private static final String MOD_STATE_TEMPLATE = "modstate";
	
	static boolean initialize(Context context) {
		
		//check if build.prop exists
		checkModStateFile(context);
		
		//check if build.prop exists
		checkBuildPropFile();
		
		//check if deviceids file exists
		checkDeviceIdsFile(context);
		
		return true;
	}
	
	public static void reloadAll(Context context) {
		Log.d(TAG, "Reload all requested.");
		
		generateModStateFile(context);
		generateBuildPropFile();
		generateDeviceIdsFile(context);
		
		Log.d(TAG, "Reload Completed!");
		
	}
	
	private static void checkModStateFile(Context context) {
			
		Log.d(TAG, "Checking for \""+MOD_STATE_FILE+"\"");
		//check if the file exists.
		if (!fileExists(MOD_STATE_FILE)) {
			Log.d(TAG, "ModState file not found. Creating from template");
			generateModStateFile(context);
			
		}
	}

	private static void checkBuildPropFile() {
		
		Log.d(TAG, "Checking for \""+BUILD_PROP_FILE+"\".");
	
		//check if the file exists
		if (!fileExists(BUILD_PROP_FILE)) {
			Log.d(TAG, "Custom \"build.prop\" file not found. Replicating \"/system/build.prop\".");
			generateBuildPropFile();
		}
	}

	private static void checkDeviceIdsFile(Context context) {
		
		Log.d(TAG, "Checking for \""+DEVICE_IDS_FILE+"\"");
		
		//check if the file exists
		if (!fileExists(DEVICE_IDS_FILE)) {
			Log.d(TAG, "Device ID file not found. Creating.");
			generateDeviceIdsFile(context);
		}
	}

	private static boolean fileExists(String fileName) {
		
		File file = new File(fileName);
		return file.exists() ? true : false;
	}
	
	private static void generateModStateFile(Context context) {
		
		AssetManager assetManager = context.getAssets();

	    InputStream in = null;
	    OutputStream out = null;
	    
	    try {
	        in = assetManager.open(MOD_STATE_TEMPLATE);
	        out = new FileOutputStream(MOD_STATE_FILE);

	        byte[] buffer = new byte[1024];
	        int read;
	        
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        
	        in.close();
	        out.flush();
	        out.close();
	    } 
	    catch (Exception e) {
	        Log.e(TAG, "Error copying modstate template!");
	    }
	    
	    setReadable(MOD_STATE_FILE);
	}
	
	private static void generateBuildPropFile() {
		
		InputStream in = null;
	    OutputStream out = null;
	    
	    try {
	        in = new FileInputStream(SYSTEM_BUILD_FILE);
	        out = new FileOutputStream(BUILD_PROP_FILE);

	        byte[] buffer = new byte[1024];
	        int read;
	        
	        while ((read = in.read(buffer)) != -1) {
	            out.write(buffer, 0, read);
	        }
	        
	        in.close();
	        out.flush();
	        out.close();
	    } 
	    catch (Exception e) {
	        Log.e(TAG, "Error copying build.prop template!");
	    }
	    
	    setReadable(BUILD_PROP_FILE);
	}


	private static void generateDeviceIdsFile(Context context) {
		
		TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		
		try {
			File sdcard = new File(DEVICE_IDS_FILE);
        	FileWriter out = new FileWriter(sdcard,false);
	        
	        out.write("MDN:"+tMgr._getLine1Number()+"\n");
	        out.write("VMN:"+tMgr._getVoiceMailNumber()+"\n");
	        out.write("DevID:"+tMgr._getDeviceId()+"\n");
	        out.write("IMSI:"+tMgr._getSubscriberId()+"\n");
	        out.write("SIMSerial:"+tMgr._getSimSerialNumber()+"\n");
	        
	        out.close();
		} 

		catch (IOException e) {
			Log.e(TAG, "I/O Exception creating device ID file.");
		}
		
		setReadable(DEVICE_IDS_FILE);
	}
	
	private static void setReadable(String file) {

	    File mod_state_file = new File(file);
	    mod_state_file.setReadable(true, false);	    
	}
}
