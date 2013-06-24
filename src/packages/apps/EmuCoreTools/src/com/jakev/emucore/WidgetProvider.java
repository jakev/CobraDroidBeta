package com.jakev.emucore;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.jakev.ModState;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
	
	//private final static String TAG = "WidgetProvider";
	final static String UPDATE_ACTION = "update";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	
    	final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
                   
            //Build Button 
            Intent buildIntent = new Intent(context, WidgetProvider.class);
            buildIntent.setAction(ModState.BUILD);
            PendingIntent buildPendingIntent = PendingIntent.getBroadcast(context, 0, buildIntent, 0);
            views.setOnClickPendingIntent(R.id.buildButton, buildPendingIntent);
            
            //Telephony Manager Button
            Intent devidsIntent = new Intent(context, WidgetProvider.class);
            devidsIntent.setAction(ModState.DEVIDS);
            PendingIntent devidsPendingIntent = PendingIntent.getBroadcast(context, 0, devidsIntent, 0);
            views.setOnClickPendingIntent(R.id.devidsButton, devidsPendingIntent);
            
            //SSL Button
            Intent sslIntent = new Intent(context, WidgetProvider.class);
            sslIntent.setAction(ModState.SSL);
            PendingIntent sslPendingIntent = PendingIntent.getBroadcast(context, 0, sslIntent, 0);
            views.setOnClickPendingIntent(R.id.sslButton, sslPendingIntent);
            
            //Launcher Button
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, LaunchActivity.class), 0);
            views.setOnClickPendingIntent(R.id.launcherButton, pendingIntent);
            
            //Sync the Buttons
            views.setImageViewResource(R.id.buildIndicator, 
            		(ModState.getInstance(ModState.BUILD).getState() ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off));
            views.setImageViewResource(R.id.devidsIndicator, 
            		(ModState.getInstance(ModState.DEVIDS).getState() ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off));
            views.setImageViewResource(R.id.sslIndicator, 
            		(ModState.getInstance(ModState.SSL).getState() ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off));            
            
            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
    	super.onReceive(context, intent);
    	    	    	
    	RemoteViews localRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
    		
    	if (intent.getAction() == null) {
    		return;
    	}
    	
    	if (intent.getAction().equals(ModState.BUILD)) {

			ModState modstate = ModState.getInstance(intent.getAction());
			modstate.toggleState();
			modstate.save();
			
			Boolean modificationEnabled = modstate.getState();

			localRemoteViews.setImageViewResource(R.id.buildIndicator, 
								(modificationEnabled) ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off);
        }
    	
    	else if (intent.getAction().equals(ModState.DEVIDS)) {

			ModState modstate = ModState.getInstance(intent.getAction());
			modstate.toggleState();
			modstate.save();
			
			Boolean modificationEnabled = modstate.getState();
    	
			localRemoteViews.setImageViewResource(R.id.devidsIndicator, 
					(modificationEnabled) ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off);	
    	}
    	
    	else if (intent.getAction().equals(ModState.SSL)) {

			ModState modstate = ModState.getInstance(intent.getAction());
			modstate.toggleState();
			modstate.save();
			
			Boolean modificationEnabled = modstate.getState();
    	
			localRemoteViews.setImageViewResource(R.id.sslIndicator, 
					(modificationEnabled) ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off);
    	}
    	
    	else if (intent.getAction().equals(UPDATE_ACTION)) {
    		
    		localRemoteViews.setImageViewResource(R.id.buildIndicator, 
            		(ModState.getInstance(ModState.BUILD).getState() ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off));
            		
    		localRemoteViews.setImageViewResource(R.id.devidsIndicator, 
            		(ModState.getInstance(ModState.DEVIDS).getState() ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off));
            
    		localRemoteViews.setImageViewResource(R.id.sslIndicator, 
            		(ModState.getInstance(ModState.SSL).getState() ? R.drawable.ic_appwidget_ind_on : R.drawable.ic_appwidget_ind_off));
    	}
    	
    	else {
            super.onReceive(context, intent);
            return;
        }
    	
	    ComponentName localComponentName = new ComponentName(context, WidgetProvider.class);
	    AppWidgetManager.getInstance(context).updateAppWidget(localComponentName, localRemoteViews);
    }
}
