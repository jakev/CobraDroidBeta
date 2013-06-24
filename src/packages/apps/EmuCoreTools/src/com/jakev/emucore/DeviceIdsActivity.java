package com.jakev.emucore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class DeviceIdsActivity extends Activity  {
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_devids);
        
        //Get Buttons
        EditText editTextMDN = (EditText)findViewById(R.id.editTextMDN);
        EditText editTextVMN = (EditText)findViewById(R.id.editTextVMN);
        EditText editTextDevID = (EditText)findViewById(R.id.editTextDevID);
        EditText editTextIMSI = (EditText)findViewById(R.id.editTextIMSI);
        EditText editTextSIMSerial = (EditText)findViewById(R.id.editTextSIMSerial);
                
        File sdcard = new File(FileManager.DEVICE_IDS_FILE);
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdcard));
            String line;

            while ((line = br.readLine()) != null) {
            	String pair[] = line.split(":");
            	
            	if (pair[0].equals("MDN")) {
            		editTextMDN.setText(pair[1], BufferType.EDITABLE);
            	}
            	else if (pair[0].equals("VMN")) {
            		editTextVMN.setText(pair[1], BufferType.EDITABLE);
            	}
            	else if (pair[0].equals("DevID")) {
            		editTextDevID.setText(pair[1], BufferType.EDITABLE);
            	}
            	else if (pair[0].equals("IMSI")) {
            		editTextIMSI.setText(pair[1], BufferType.EDITABLE);
            	}
            	else if (pair[0].equals("SIMSerial")) {
            		editTextSIMSerial.setText(pair[1], BufferType.EDITABLE);
            	}
            }
            
            br.close();
            makeToast("Device ID File Successfully Loaded!");
        }
        
        catch (IOException e) {
            makeToast("Error Reading File!");
        }
        catch (NullPointerException e) {
        	makeToast("Error Parsing File!");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        	makeToast("Error Parsing File!");
        }
   }
/*****************************************/  

    
    public void makeToast(String msg) {
    	Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, msg, duration);
    	toast.show();
   }
            
    public void SubmitOnClick(View v) {
    	
        EditText editTextMDN = (EditText)findViewById(R.id.editTextMDN);
        EditText editTextVMN = (EditText)findViewById(R.id.editTextVMN);
        EditText editTextDevID = (EditText)findViewById(R.id.editTextDevID);
        EditText editTextIMSI = (EditText)findViewById(R.id.editTextIMSI);
        EditText editTextSIMSerial = (EditText)findViewById(R.id.editTextSIMSerial);
        
        String mdnValue = editTextMDN.getText().toString();
        String vmnValue = editTextVMN.getText().toString();
        String devidValue = editTextDevID.getText().toString();
        String imsiValue = editTextIMSI.getText().toString();
        String simserialValue = editTextSIMSerial.getText().toString();
        
        //Validation
        if (!validateMDN(mdnValue)) {
        	makeToast("Error with MDN Value!");
        	return;
        }
        
        if (!validateVMN(vmnValue)) {
        	makeToast("Error with Voicemail Number Value!");
        	return;
        }
        
        if (!validateDevID(devidValue)) {
        	makeToast("Error with IMEI/MEID Value!");
        	return;
        }
        
        if (!validateIMSI(imsiValue)) {
        	makeToast("Error with IMSI Value!");
        	return;
        }
        
        if (!validateSIMSerial(simserialValue)) {
        	makeToast("Error with SIM Serial Value!");
        	return;
        }
                
        try {
        	File sdcard = new File(FileManager.DEVICE_IDS_FILE);
        	FileWriter fw = new FileWriter(sdcard,false);
        	fw.write("MDN:"+mdnValue+"\n");
        	fw.write("VMN:"+vmnValue+"\n");
        	fw.write("DevID:"+devidValue+"\n");
        	fw.write("IMSI:"+imsiValue+"\n");
        	fw.write("SIMSerial:"+simserialValue+"\n");
        	makeToast("Values Updated Successfully!");
        	fw.close();
        	
        	} catch (java.io.IOException e) {
        		makeToast("Error Writing to File!");
        	}
        
    }    

	private boolean validateMDN(String mdnValue) {
		
    	if (mdnValue != null && !mdnValue.equals("") && !mdnValue.trim().equals("")) {
    		
    		if (mdnValue.matches("^[+]?[0-9]{11,15}$")) {
    			return true;
    		}
       	}
    	
		return false;
	}
	
	private boolean validateVMN(String vmnValue) {
    	
		if (vmnValue != null && !vmnValue.equals("") && !vmnValue.trim().equals("")) {
			
    		if (vmnValue.matches("^[+]?[0-9]{11,15}$")) {
    			return true;
    		}
       	}
    	
		return false;
	}	
	
	private boolean validateDevID(String devidValue) {

    	if (devidValue != null && !devidValue.equals("") && !devidValue.trim().equals("")) {

    		if (devidValue.matches("^[0-9a-fA-F]{14,16}$")) {
     			return true;
    		}
       	}
    	
		return false;
	}	
	
	private boolean validateIMSI(String imsiValue) {

    	if (imsiValue != null && !imsiValue.equals("") && !imsiValue.trim().equals("")) {
    		
    		if (imsiValue.matches("^[0-9]{1,15}$")) {
    			return true;
    		}
       	}
    	
		return false;	
	}
    
	private boolean validateSIMSerial(String simserialValue) {

    	if (simserialValue != null && !simserialValue.equals("") && !simserialValue.trim().equals("")) {
    		
    		if (simserialValue.matches("^[0-9]{15,21}$")) {
    			return true;
    		}
       	}
    	
		return false;	
	}
}
