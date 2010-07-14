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
		addPreferencesFromResource(R.xml.preferences);
		
		Intent launchIntent = getIntent();
		Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            Intent cancelResultValue = new Intent();
            cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_CANCELED, cancelResultValue);
        } else {
            finish();
        }
        prepareDisplayLabel();
        prepareContactGroups();
		prepareQCBSizes();
		
		prepareSaveBtn();
	}
		
	private void prepareDisplayLabel() {
		EditTextPreference displayLabel = (EditTextPreference)findPreference(Preferences.DISPLAY_LABEL);
		displayLabel.setKey(Preferences.get(Preferences.DISPLAY_LABEL, appWidgetId));
		displayLabel.setOnPreferenceChangeListener(new SetCurValue(null, null));
	}
	
	private void prepareContactGroups() {

		ListPreference selectGroup = (ListPreference)findPreference(Preferences.GROUP_ID);
		selectGroup.setKey(Preferences.get(Preferences.GROUP_ID, appWidgetId));

		
    	Uri uri = ContactsContract.Groups.CONTENT_URI;
    	String[] projection = new String[] {
    			ContactsContract.Groups._ID,
    			ContactsContract.Groups.TITLE
    	};
    	Cursor orgCs = this.managedQuery(uri, projection, null, null, null);
    	
    	CharSequence[] Titles = new CharSequence[orgCs.getCount()+1];
    	CharSequence[] Values = new CharSequence[orgCs.getCount()+1];
    	
    	int pos = 0;
    	
    	Titles[pos] = getString(R.string.allcontacts);
    	Values[pos++] = "0";
    	
    	orgCs.moveToFirst();
    	while (!orgCs.isAfterLast()) {
    		Values[pos] = orgCs.getString(orgCs.getColumnIndex(ContactsContract.Groups._ID));
        	Titles[pos++] = orgCs.getString(orgCs.getColumnIndex(ContactsContract.Groups.TITLE));
    		orgCs.moveToNext();
    	}    	    
		orgCs.close();

		selectGroup.setOnPreferenceChangeListener(new DualOnPreferenceChangeListener(
			new OnPreferenceChangeListener() {
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					// ToDo: Set DisplayLabel if it is still like the old title...
					return true;
				}
			}, new SetCurValue(Titles, Values)));		
		
		selectGroup.setEntries(Titles);
		selectGroup.setEntryValues(Values);
	}

	private void prepareQCBSizes(){
		ListPreference qcbSizes = (ListPreference)findPreference(Preferences.QUICKCONTACT_SIZE);
		qcbSizes.setKey(Preferences.get(Preferences.QUICKCONTACT_SIZE, appWidgetId));
		CharSequence[] Titles = new CharSequence[] { getString(R.string.qcsLarge), getString(R.string.qcsMedium) };
		CharSequence[] Values = new CharSequence[] { String.valueOf(ContactsContract.QuickContact.MODE_LARGE), 
				String.valueOf(ContactsContract.QuickContact.MODE_MEDIUM) };
		qcbSizes.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));				
		qcbSizes.setEntries(Titles);
		qcbSizes.setEntryValues(Values);
		qcbSizes.setValue(String.valueOf(ContactsContract.QuickContact.MODE_LARGE));
	}

	private void prepareSaveBtn() {
		Preference pref = findPreference("SAVE");
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

	private class DualOnPreferenceChangeListener implements OnPreferenceChangeListener {
		private OnPreferenceChangeListener fFirst, fSecond;
		
		public DualOnPreferenceChangeListener(OnPreferenceChangeListener aFirst, OnPreferenceChangeListener aSecond) {
			fFirst = aFirst;
			fSecond = aSecond;
		}
	
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean first = fFirst.onPreferenceChange(preference, newValue);
			boolean second = fSecond.onPreferenceChange(preference, newValue);
			return first && second;
		}
	
	}
	
	private class SetCurValue implements OnPreferenceChangeListener {
		private CharSequence[] fValues, fTitles;
		public SetCurValue(CharSequence[] Titles, CharSequence[] Values) {
			fValues = Values;
			fTitles = Titles;
		}
	
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			CharSequence curVal = null;
			if (preference instanceof ListPreference) {
				ListPreference lp = (ListPreference)preference;

				for(int i = 0; i < fValues.length; i++) {
					if (fValues[i].equals(newValue)) {
						curVal = fTitles[i];
						break;
					}
				}								
			}
			else if (preference instanceof EditTextPreference) {
				EditTextPreference etp = (EditTextPreference)preference;
				curVal = newValue.toString();
			}
			preference.setSummary(curVal);
			return true;
		}
	}
}