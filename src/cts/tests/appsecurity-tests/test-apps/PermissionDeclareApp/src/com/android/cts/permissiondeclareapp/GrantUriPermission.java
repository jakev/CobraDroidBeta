package com.android.cts.permissiondeclareapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GrantUriPermission extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = (Intent)intent.getParcelableExtra("intent");
        boolean service = intent.getBooleanExtra("service", false);
        try {
            if (!service) {
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(newIntent);
            } else {
                context.startService(newIntent);
            }
            if (isOrderedBroadcast()) {
                setResultCode(101);
            }
        } catch (SecurityException e) {
            Log.i("GrantUriPermission", "Security exception", e);
            if (isOrderedBroadcast()) {
                setResultCode(100);
            }
        }
    }
}
