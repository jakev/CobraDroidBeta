package com.jakev.emucore;

import android.app.Activity;
import android.content.Intent;
import android.jakev.ModState;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{

    private static final String TAG = "MainActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Set the layout
        setContentView(R.layout.activity_main);
        
        //Register onClickListeners
        Button btreset = (Button)findViewById(R.id.buttonResetAll);
        Button btmodids = (Button)findViewById(R.id.buttonModIDs);
        Button btbuild = (Button)findViewById(R.id.buttonPropEditor);
        CheckBox cbbuild = (CheckBox)findViewById(R.id.checkBoxBuild);
        CheckBox cbdevids = (CheckBox)findViewById(R.id.checkBoxDevids);
        CheckBox cbssl = (CheckBox)findViewById(R.id.checkBoxSsl);
        btreset.setOnClickListener(this);
        btmodids.setOnClickListener(this);
        btbuild.setOnClickListener(this);
        cbbuild.setOnClickListener(this);
        cbdevids.setOnClickListener(this);
        cbssl.setOnClickListener(this);
        
      
        //Make sure our checkboxes are up to date
		updateBuildBox();
		updateDevidsBox();
		updateSslBox();
    }

	@Override
    public void onRestart() {
    	super.onRestart();
    	
    	//Log.d(TAG, "onRestart called.");
    	updateBuildBox();
    	updateDevidsBox();
    	updateSslBox();
    }
    
	private void updateBuildBox() {
		ModState buildState = ModState.getInstance(ModState.BUILD);
		CheckBox cbbuild = (CheckBox)findViewById(R.id.checkBoxBuild);
		cbbuild.setChecked(buildState.getState());
	}
	
	private void updateDevidsBox() {
		ModState devidsState = ModState.getInstance(ModState.DEVIDS);
		CheckBox cbdevids = (CheckBox)findViewById(R.id.checkBoxDevids);
		cbdevids.setChecked(devidsState.getState());
	}
	
	private void updateSslBox() {
		ModState sslState = ModState.getInstance(ModState.SSL);
		CheckBox cbssl = (CheckBox)findViewById(R.id.checkBoxSsl);
		cbssl.setChecked(sslState.getState());
	}

	private void updateWidgets() {
		
        Intent sslIntent = new Intent(this, WidgetProvider.class);
        sslIntent.setAction(WidgetProvider.UPDATE_ACTION);
        sendBroadcast(sslIntent);
	}
	
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.buttonModIDs:
				
				this.startActivity(new Intent(this, DeviceIdsActivity.class));
				break;
				
			case R.id.buttonResetAll:
				
				//Reset the files
				FileManager.reloadAll(this);
				
				//Sync the CheckBoxes
				updateBuildBox();
				updateDevidsBox();
				updateSslBox();
				
				//Sync the widgets
				updateWidgets();
				
				//Tell user with short toast
				Toast.makeText(getApplicationContext(),"Reset is Complete!",Toast.LENGTH_SHORT).show();
				break;
				
			case R.id.buttonPropEditor:
				
				this.startActivity(new Intent(this, PropEditActivity.class));
				break;	
							
			case R.id.checkBoxBuild:
				//Log.d(TAG, "Build Checkbox");
				
				//Update the state
				ModState buildState = ModState.getInstance(ModState.BUILD);
				buildState.toggleState();
				buildState.save();
				
				updateBuildBox();
				updateWidgets();				
				break;
			
			case R.id.checkBoxDevids:
				//Log.d(TAG, "Devids Checkbox");
				
				//Update the state
				ModState devidsState = ModState.getInstance(ModState.DEVIDS);
				devidsState.toggleState();
				devidsState.save();
				
				updateDevidsBox();
				updateWidgets();
				break;
				
			case R.id.checkBoxSsl:
				//Log.d(TAG, "Ssl Checkbox");
				
				//Update the state
				ModState sslState = ModState.getInstance(ModState.SSL);
				sslState.toggleState();
				sslState.save();
				
				updateSslBox();
				updateWidgets();
				break;
				
			default:
				break;
	    }
	}
}
