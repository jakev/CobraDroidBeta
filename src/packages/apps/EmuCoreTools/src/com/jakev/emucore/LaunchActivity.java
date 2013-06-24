package com.jakev.emucore;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LaunchActivity extends Activity {

	private static final String TAG = "LaunchActivity";
	AlertDialog errorDialog; 

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);	
        setContentView(R.layout.activity_launch);
        
        /* Compatibility checking
         * 
         * First we check if we can get a ModState handle using reflection, 
         *  then we make sure the TelephonyManager class is modified.
         * Assuming everything is OK, we can launch the MainActivity!
         */  
        
        boolean valid_modstate = true;
        boolean valid_telephony = true;

        try {
        	Class ModState = Class.forName("android.jakev.ModState"); // We need the ModState class
        } 
        catch (ClassNotFoundException e) {
        	Log.e(TAG, "Could not find required ModState libraries, is this CobraDroid?");
        	valid_modstate = false;
        	showError("Could not load the required class (ModState), are you using the correct version of CobraDroid?");
        	
        }

        if (valid_modstate) {
        	 
        	if (!checkModdedTelephony()) {
             	Log.e(TAG, "The TelephonyManager class is not correct, is this CobraDroid?");
             	valid_telephony =  false;
             	showError("Could not call modified TelephonyManager methods, are you using the correct version of CobraDroid?");
             }
     	
        	// Everything checks out, we can launch
        	 if (valid_telephony) {
                 Intent intent = new Intent(this, MainActivity.class);
                 startActivity(intent);      
                 finish();       		 
        	 }
        }
    }

	// To use EmuCoreTools, the TelephonyManager class mush be modified.
	// We simply check to see if the required methods exist.
    private boolean checkModdedTelephony() {
    	
    	try {
        	Class<?> c = Class.forName("android.telephony.TelephonyManager");
        	String methodName = "_getDeviceId";
    		Object method = c.getMethod(methodName);
    	} 
    	catch (ClassNotFoundException e) { return false; } 
    	catch (SecurityException e) { return false; }
    	catch (NoSuchMethodException e) { return false; }		
    	
    return true;
    }

    // Just a easy way to create and show dialogs
  	private void showError(String msg) {

  		AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
  		builder.setCancelable(false)
				.setMessage(msg)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						finish();
					}
				});
	
	errorDialog = builder.create();
	errorDialog.show();
	}
}