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
import java.io.IOException;

import android.util.Log;


/**
 * A helper class for querying a custom "build.prop" file.
 * @author Jake Valletta
 * @author 1.0, CobraDroidBeta
 */
public class BuildQuery {

	/* Private Values */
	/** @hide */	
	private final static String PROP_FILE_PATH = "/data/data/com.jakev.emucore/build.prop";
	private final static String UNKNOWN = "unknown";
        private final static String TAG = "BuildQuery";	
	private final static String DOES_NOT_EXIST = "doesnotexist";
	private final static String ERROR = "error";

	
	/** Gets value associated with a given property.
	 * @param value			The property to look up.
	 * @return			The property's value as a string or "UNKNOWN" if not found.
	 */
	public static String get(String value) {
		return getValue(value);
	}
	
	/** Gets a value associated with a given property, and a specified default value.
	 * @param value			The property to look up.
	 * @param default_value		Default value to return if property does not exist.
	 * @return			The property's value as a string.
         */
	public static String get(String value, String default_value) {
                String return_value = getValue(value);
    	
    		if (return_value.equals(DOES_NOT_EXIST) || return_value.equals(ERROR)) {
    			return default_value;
    		} else {
    			return return_value;
    		}
	}
    
	/** Gets a integer value associated with a given property, with a specified default value.
	 * @param value			The property to lookup.	
	 * @param default_value		Default value to return if propert deoes not exist.
	 * @return			The property's value as an integer.
	 */
	public static int getInt(String value, int default_value) {
		String return_value = getValue(value);
		
		if (return_value == null) {
			return default_value;
		} 
		else {
			int i;
			try {
				i = Integer.parseInt(return_value);
			}
			catch (NumberFormatException e) {
				return default_value;
			}
			return i;
		}
	}
	/* Private Method to actually query the file. 
	 * -This method has no sense of a "default." 
	 * -All erroes are treated the same at this point.
	 * -This method has to let the caller know the property does not exist  
	 * -Empty values are not allowed (the default or Null is returned)
	 */
	/** @hide */
	private static String getValue(String key) {
		File prop_file = new File(PROP_FILE_PATH);
        
		 try {
			BufferedReader br = new BufferedReader(new FileReader(prop_file));
			String line;

           		 while ((line = br.readLine()) != null) {
            	
				if ((!line.substring(0).equals("#")) && (line.matches(".*=.*"))) {
					String pair[] = line.split("=");
                	
					if (pair[0].equals(key)) {
                				if (pair.length == 1) {
							
							br.close();
                					return ERROR;
                				}

					br.close();
                			return pair[1];
            			    	}	
				}
			}
		}
		//TODO At some point, make these different errors
		catch (IOException e) {
			return ERROR;
		}
		catch (ArrayIndexOutOfBoundsException e) {
			return ERROR;
		}
		catch (NullPointerException e) {
			return ERROR;
		}
        
        	//The value was not in the file, return DOES_NOT_EXIST
		return DOES_NOT_EXIST;
	}
}
