/*
 * Copyright (C) 2010 Florian Sundermann
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


package com.boombuler.widgets.contacts;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.ContactsContract;


public class ConfigurationActivity extends PreferenceActivity {

	private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
	  
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
        // Build GUI from resource
		addPreferencesFromResource(R.xml.preferences);
		
		// Get the starting Intent
		Intent launchIntent = getIntent();
		Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            // Cancel by default
            Intent cancelResultValue = new Intent();
            cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_CANCELED, cancelResultValue);
        } else {
            finish();
        }
        

        
        // prepare the GUI components
        prepareShowName();
        prepareDisplayLabel();
        prepareContactGroups();
		prepareQCBSizes();
				
		prepareBGImage();
		
		prepareFBContacts();
		prepareSaveBtn();
	}

	private void prepareShowName() {
		// Find control and set the right preference-key for the AppWidgetId
		CheckBoxPreference showName = (CheckBoxPreference)findPreference(Preferences.SHOW_NAME);
		showName.setKey(Preferences.get(Preferences.SHOW_NAME, appWidgetId));
	}	
	
	private void prepareDisplayLabel() {
		// Find control and set the right preference-key for the AppWidgetId
		EditTextPreference displayLabel = (EditTextPreference)findPreference(Preferences.DISPLAY_LABEL);
		displayLabel.setKey(Preferences.get(Preferences.DISPLAY_LABEL, appWidgetId));
		// Set summary on value changed
		displayLabel.setOnPreferenceChangeListener(new SetCurValue(null, null));
	}
	
	private void prepareContactGroups() {
		// Find control and set the right preference-key for the AppWidgetId
		ListPreference selectGroup = (ListPreference)findPreference(Preferences.GROUP_ID);
		selectGroup.setKey(Preferences.get(Preferences.GROUP_ID, appWidgetId));

    	Uri uri = ContactsContract.Groups.CONTENT_URI;
    	String[] projection = new String[] {
    			ContactsContract.Groups._ID,
    			ContactsContract.Groups.TITLE
    	};
    	// read the ContactGroups
    	Cursor orgCs = this.managedQuery(uri, projection, null, null, null);
    	
    	CharSequence[] Titles = new CharSequence[orgCs.getCount()+1];
    	CharSequence[] Values = new CharSequence[orgCs.getCount()+1];
    	
    	int pos = 0;
    	// First add the "virtual" group "All Contacts"
    	Titles[pos] = getString(R.string.allcontacts);
    	Values[pos++] = "0";
    	
    	
    	orgCs.moveToFirst();
    	while (!orgCs.isAfterLast()) {
    		// Then add one entry for each contact group
    		Values[pos] = orgCs.getString(orgCs.getColumnIndex(ContactsContract.Groups._ID));
        	Titles[pos++] = orgCs.getString(orgCs.getColumnIndex(ContactsContract.Groups.TITLE));
    		orgCs.moveToNext();
    	}    	    
		orgCs.close();

		// Set the summary on value change
		selectGroup.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));		
		
		selectGroup.setEntries(Titles);
		selectGroup.setEntryValues(Values);
	}

	private void prepareQCBSizes(){
		// Find control and set the right preference-key for the AppWidgetId
		ListPreference qcbSizes = (ListPreference)findPreference(Preferences.QUICKCONTACT_SIZE);
		qcbSizes.setKey(Preferences.get(Preferences.QUICKCONTACT_SIZE, appWidgetId));
		// Add the options for "large" and "medium"
		CharSequence[] Titles = new CharSequence[] { getString(R.string.qcsLarge), getString(R.string.qcsMedium) };
		CharSequence[] Values = new CharSequence[] { String.valueOf(ContactsContract.QuickContact.MODE_LARGE), 
				String.valueOf(ContactsContract.QuickContact.MODE_MEDIUM) };
		// set summary on value change
		qcbSizes.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));
		
		qcbSizes.setEntries(Titles);
		qcbSizes.setEntryValues(Values);
		qcbSizes.setValue(String.valueOf(ContactsContract.QuickContact.MODE_LARGE));
	}

	private void prepareFBContacts() {
		Preference FB = findPreference("FACEBOOK");
		// Bind the "onClick" for the save preferences to close the activity
		// and postback "OK"
		FB.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
			                Intent myIntent = new Intent(preference.getContext(), FBLogin.class);
			                startActivityForResult(myIntent, 0);
			            	return true;
			}
		});
	}
	
	private void prepareBGImage() {
		ListPreference bgimage = (ListPreference)findPreference(Preferences.BGIMAGE);
		bgimage.setKey(Preferences.get(Preferences.BGIMAGE, appWidgetId));
		CharSequence[] Titles = new CharSequence[] { getString(R.string.black), getString(R.string.white) };
		CharSequence[] Values = new CharSequence[] { String.valueOf(Preferences.BG_BLACK), String.valueOf(Preferences.BG_WHITE) };
		bgimage.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));
		
		bgimage.setEntries(Titles);
		bgimage.setEntryValues(Values);
		bgimage.setValue(String.valueOf(Preferences.BG_BLACK));
	}
	
	private void prepareSaveBtn() {
		Preference pref = findPreference("SAVE");
		// Bind the "onClick" for the save preferences to close the activity
		// and postback "OK"
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(final Preference preference) {
				Intent resultValue = new Intent();                    
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                setResult(RESULT_OK, resultValue);
                finish();
                return false;
			}
		});
		
		
	}
	
	
	
	
	// OnPreferenceChangeListener to set the summary of the preference
	// to the display text of the new value 
	private class SetCurValue implements OnPreferenceChangeListener {
		private CharSequence[] fValues, fTitles;
		public SetCurValue(CharSequence[] Titles, CharSequence[] Values) {
			fValues = Values;
			fTitles = Titles;
		}
	
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			CharSequence curVal = null;
			if (preference instanceof ListPreference) {
				for(int i = 0; i < fValues.length; i++) {
					if (fValues[i].equals(newValue)) {
						curVal = fTitles[i];
						break;
					}
				}								
			}
			else if (preference instanceof EditTextPreference) {
				curVal = newValue.toString();
			}
			preference.setSummary(curVal);
			return true;
		}
	}



}