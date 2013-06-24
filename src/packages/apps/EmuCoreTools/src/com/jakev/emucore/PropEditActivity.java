package com.jakev.emucore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.TextView;

public class PropEditActivity extends ListActivity implements OnClickListener {

	private static final String TAG = "PropEditActivity";
	private static final String BUILD_PROP_FILE = "/data/data/com.jakev.emucore/build.prop";

	private ListView list_view;
	private Properties property_list;
	
	String[] prop_keys = null;
	final List<String> prop_values = new ArrayList<String>();
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_propedit);

        list_view = getListView();
        property_list = loadProperties();
        
        if (property_list == null) {
        	makeToast ("There was an error reading the custom \"build.prop\" file.");
        } 
        else {
        	
        	//Initially set the adapter
        	setAdapter();
                                	
        	//Set our onClickListener
        	list_view.setOnItemClickListener(new OnItemClickListener()
        	{

        		public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        			editPropertyValueDialog(prop_keys[position], property_list.getProperty(prop_keys[position]));
                }
            });
        	
        	//Enable and set our onLongClickListener
        	list_view.setLongClickable(true);
        	list_view.setOnItemLongClickListener(new OnItemLongClickListener()
        	{
                public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
                    showLongClickOptions(prop_keys[position]);
    				return true;
                }
            });
        	
        	//Set our button listener
        	Button btnewitem = (Button)findViewById(R.id.buttonAddItem);
        	btnewitem.setOnClickListener(this);
        }

    }
	/**********************************/
    
    //Sets up a new adapter for the ListView
   	private void setAdapter() {

       	//Put keys into a String array
        prop_keys = property_list.keySet().toArray(new String[0]);
           
        //Put values into a String array by looking up keys
		prop_values.clear();
		for (int i = 0; i < prop_keys.length; i++) {
   			prop_values.add(property_list.getProperty(prop_keys[i]));
		}
   
		//Create an ArrayList of Map(String, String) objects
		ArrayList<Map<String, String>> data = makeArrayList(prop_keys, prop_values);
           
        int resource = R.layout.two_line_list_item;
       	String[] from = { "key", "value" };
       	int[] to = { R.id.key, R.id.value };
       	
       	//Apply
        setListAdapter(new SimpleAdapter(getApplicationContext(), data, resource, from, to));
           
   	}
        
    //Load properties from build.prop into a Property object
    private Properties loadProperties() {
    	try {
    		Properties pl = new Properties();
			pl.load(new BufferedReader(new FileReader(BUILD_PROP_FILE)));
			return pl;
			
		} catch (FileNotFoundException e) {
			Log.e(TAG, "File not found exception trying to load custom build.prop");
			return null;
			
		} catch (IOException e) {
			Log.e(TAG, "I/O exception trying to load custom build.prop");
			return null;
		}
	}
    
    //Add HashMaps to ArrayList, return ArrayList
	private ArrayList<Map<String, String>> makeArrayList(String[] key_list, List<String> value_list) {
    		ArrayList<Map<String, String>> al = new ArrayList<Map<String, String>>();

    			for (int i = 0; i < key_list.length; ++i) {
    					al.add(makeHashMap(key_list[i], value_list.get(i)));
    			}

    			return al;
    }
	
    //Make a HashMap for the given strings: { "key" : key, "value" : value } 
    private HashMap<String, String> makeHashMap(String key, String value) {
    	HashMap<String, String> hm = new HashMap<String, String>();

    	hm.put("key", key);
    	hm.put("value", value);

    	return hm;
    }
   
    //Change the value of a property in a Property object 
    private void editPropertyValueDialog(final String key, String value) {
   
       AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);  
       adBuilder.setTitle(key);  
       final EditText input = new EditText(this);  
       input.setSingleLine();  
       input.setText(value);
       adBuilder.setView(input); 
       adBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() 
       {  
    	   public void onClick(DialogInterface dialog, int which) {  
          
    		   changePropertyValue(key, input.getText().toString());
    	   }  
       });  
       
       adBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
       {  public void onClick(DialogInterface dialog, int which) {} });  
       
       adBuilder.create().show();  
        
		
	} 
	
	//Writes the Properties object to build.prop file
	private void writeFile(){
		
		FileWriter fw;
		String comment = "This file is managed by EmuCoreTools, but you can edit it:)";
		
		try {
			fw = new FileWriter(new File(BUILD_PROP_FILE));
			property_list.store(fw, comment);
		
		} catch (IOException e) {
			makeToast("There was an issue writing the new file!");
		}
		
	}
    
    /**********Dialogs**************/
	//onClick listener, for our button
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.buttonAddItem:
			
			addPropertyDialog();
			break;
		}
		
	}
	//Add a property
    private void addPropertyDialog() {
    	
    	LayoutInflater factory = LayoutInflater.from(this);
    	final View textEntryView = factory.inflate(R.layout.new_item_entry, null);
    	//text_entry is an Layout XML file containing two text field to display in alert dialog
    	 
    	final EditText input1 = (EditText) textEntryView.findViewById(R.id.editTextProperty);
    	final EditText input2 = (EditText) textEntryView.findViewById(R.id.editTextValue);
        input1.setText("", TextView.BufferType.EDITABLE);
        input2.setText("", TextView.BufferType.EDITABLE);
    	
    	
    	AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);  
	    adBuilder.setTitle("Add a New Property");  

	    adBuilder.setView(textEntryView); 
	    adBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() 
	    {  
	 	   public void onClick(DialogInterface dialog, int which) {  
	       
	 		   addProperty(input1.getText().toString(),input2.getText().toString());
	 	   }  
	    });  
	    
	    adBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	    {  public void onClick(DialogInterface dialog, int which) {} });  
	    
	    adBuilder.create().show(); 
    }
		
	//Change the property title
    private void editPropertyDialog(final String key) {
    	
	    AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);  
	    adBuilder.setTitle("Changing \""+key+"\"");  
	    final EditText input = new EditText(this);  
	    input.setSingleLine();  
	    input.setText(key);
	    adBuilder.setView(input); 
	    adBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() 
	    {  
	 	   public void onClick(DialogInterface dialog, int which) {  
	       
	 		   changeProperty(key, input.getText().toString());
	 	   }  
	    });  
	    
	    adBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
	    {  public void onClick(DialogInterface dialog, int which) {} });  
	    
	    adBuilder.create().show(); 
    }
    
    
    protected void showLongClickOptions(final String property) {
        
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(property)
               .setItems(R.array.long_click_options, new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int which) {
                	   switch (which){
                	   
                	   case 0:
                		   //Log.d(TAG,"They clicked change");
                		   editPropertyDialog(property);                		   
                		   break;
                	   case 1:
                		   //Log.d(TAG,"They clicked delete");
                		   deleteEntry(property);
                		   break;
                		      
                	   }
                   }
               });
               
       builder.create();
       builder.show();
    }
    
    
    /************Actions*******/
	protected void addProperty(String property, String value) {
		
		//check for property being empty or null
		if (property == null || property.isEmpty()) {
			makeToast("Property must be supplied!");
			return;
		}
		//if value is null, make it ""
		if (value == null) {
			value = "";
		}

		//Is the property already in our list?
		if (property_list.containsKey(property)) {
			makeToast("Supplied property already exists!");
			return;
		}
	
		//This must be legit. Add to property_list+file and update adapter
		property_list.setProperty(property, value);
		
		setAdapter();
		writeFile();
	}
    

	protected void deleteEntry(String property) {
		
		property_list.remove(property);
		
		setAdapter();
		writeFile();
        
		makeToast("Deleted entry \""+property+"\"!");
	}

	private void changeProperty(String old_property, String new_property) {
		
		String value = property_list.getProperty(old_property);
		
		property_list.remove(old_property);
		property_list.setProperty(new_property, value);
		
		setAdapter();
		writeFile();
	}
	
	private void changePropertyValue(String property, String value) {
	
		property_list.remove(property);
		property_list.setProperty(property, value);
		
		setAdapter();
		writeFile();
	}
	
	/************Other************/
	//Make and display a toast
	private void makeToast(String string) {
		Toast toast = Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT);
		toast.show();
	}
}
