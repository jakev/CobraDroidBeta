/*
 * CobraDroid 1.0 Beta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.jakev;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/* The ModState class allows a user to define custom properties with a Boolean value.
 * This class allows access and manipulation of these states.  
 *
 * The EmuCoreTools application uses the following states:
 *   'build'	Used to tell the application to query custom system properties.
 *   'devids'	Used to tell the application to query custom radio properties.
 *   'ssl'	 Determine if SSL CA validation should occur. 
 *
 * To use this class, obtain an instance using the "getInstance(String mod)" method.
 * The 'mod' string can be supplied using included static members, or any string you'd
 * like to save the file. The mod string and state can be queried with "getMod()" and 
 * "getState()," respectively. To change the state of your instance, use the method 
 * "toogleState()." To commit the instance state to disk, use the "save()" method.
 */

/**
 * Provides an interface for interacting with CobraDroid modifcation settings.
 * @author Jake Valletta
 * @version 1.0, CobraDroidBeta
 */
public class ModState {

	/** Value used to access the custom build.prop modification state. */
	public static final String BUILD = "build";
	
	/** Value used to access the custom TelephonyManager modification state. */
	public static final String DEVIDS = "devids";
	
	/** Value used to access the SSL CA validation bypass modification state. */
	public static final String SSL = "ssl";
	
	// Internal only members
	/** @hide */
	private static final String TAG = "ModState";
	
	/** @hide */
	private static String MOD_STATE_FILE = "/data/data/com.jakev.emucore/modstate";
	
	// Private members; access with "getState()" and "getMod()"
	/** @hide */
	private Boolean state = false;
	
	/** @hide */
	private String mod = "";
	
	/** Returns a ModState instance for the specfified modification. 
	 * 
	 * @param mod		The string value of a desired modification. 		
	 * @return		A ModState object for the specified modification.
	 */	
	public static ModState getInstance(String mod) {
		
		Boolean localState;
		localState = getEnabled(mod);
		
		ModState bs = new ModState();
		bs.mod = mod;
		bs.setState(localState);
		return bs;
	}
	
	/** Accessor method for the Boolean state of the modification.
	 *
	 * @return		A Boolean value of the state of the modification
	 */
	public Boolean getState() {
		return this.state;
	}
	/** Accessor method for the modification name.
	 *
	 * @return		The String value of the name of the modification
	 */
	public String getMod() {
		return this.mod;
	}
	
	// This method should not be called.  All modification to the state 
	// should be done with "toggleState()".
	/** @hide */
	private void setState(Boolean paramState) {
		this.state = paramState;
	}
	
	/** Method to save the current state of the modification to the state file.
	 *
	 */
	public void save() {

		String stringValue = (this.getState()) ? "1" : "0";
		
		try {
        	File sdcard = new File(MOD_STATE_FILE);
            BufferedReader br = new BufferedReader(new FileReader(sdcard));
            
            String line;
            String modData = "";
            
            while ((line = br.readLine()) != null)
            {
            	String pair[] = line.split(":");
            	
            	if (pair[0].equals(this.mod)) {
            		modData += this.mod+":"+stringValue+"\n";
            	}
            	else {
            		modData += line+"\n";
            	}
           }
            
			br.close(); 
			
			FileWriter fw = new FileWriter(sdcard,false);
			fw.write(modData.substring(0, modData.length() - 1));
			fw.close();
            
        }
        
        catch (IOException e) {
        	/* TODO:  Logging in libcore */
		//Log.w(TAG, "IOException at save() for "+MOD_STATE_FILE+".");
        }
        catch (ArrayIndexOutOfBoundsException e) {
        	/* TODO:  Logging in libcore */
		//Log.w(TAG, "ArrayIndexOutOfBoundsException at save() for "+MOD_STATE_FILE+".");
        }
        catch (NullPointerException e) {
		/* TODO:  Logging in libcore */
        	//Log.w(TAG, "NullPointerException at save() for "+MOD_STATE_FILE+".");
        }
	}
	
	// This method allows you to change the state of your ModState instance.
	// To save the results, call the "save()" method.
	/** Method to toggle the state of the local ModState object.
	 */
	public void toggleState() {
		this.state = (this.getState()) ? false : true;
	}
	
	// Private method to interface the direct querying of the modstate file
	// and a boolean representation for the state.
	/** @hide */
	private static Boolean getEnabled(String value) {
		return getValue(value).equals("1") ? true : false;
	}
	
	// The method to actually access the modstate file.  This returns a "0"
	// Under ALL error conditions, resulting in a disabled state returned to
	// caller.
	/** @hide */
	private static String getValue(String key) {
		File sdcard = new File(MOD_STATE_FILE);
        
		try {
			BufferedReader br = new BufferedReader(new FileReader(sdcard));
			String line;

			while ((line = br.readLine()) != null) {
				String pair[] = line.split(":");
				
				if (pair[0].equals(key)) {
					return pair[1];
				}
			}
			br.close();
		}
		
		catch (IOException e) {
			// Too loud
			//Log.w(TAG, "IOException at getValue() parsing \'"+MOD_STATE_FILE+"\'");
		}
		catch (ArrayIndexOutOfBoundsException e) {
			/* TODO:  Logging in libcore */
			//Log.w(TAG, "ArrayIndexOutOfBoundsException at getValue() parsing \'"+MOD_STATE_FILE+"\'");
		}
		catch (NullPointerException e) {
			/* TODO:  Logging in libcore */
			//Log.w(TAG, "NullPointerException at getValue() parsing \'"+MOD_STATE_FILE+"\'");
		}
		
		return "0";
	}
}
