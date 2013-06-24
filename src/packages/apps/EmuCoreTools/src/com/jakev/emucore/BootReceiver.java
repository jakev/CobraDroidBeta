package com.jakev.emucore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private final String TAG = "BootReceiver";
	
	@Override
	public void onReceive(Context context, Intent arg1) {
		
		Log.d(TAG, "EMU Tools is intializing...");
		
		boolean success = FileManager.initialize(context);
		
		if (success) {
			Log.d(TAG, "Initialization completed successfully.");
		}
		else {
			Log.e(TAG, "Errors occured during initialization.");
		}
	}
}
