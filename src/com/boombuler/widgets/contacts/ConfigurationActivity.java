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

import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
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
        prepareDisplayLabel();
        prepareContactGroups();
        prepareColumnCount();
		prepareShowName();
		prepareNameKinds();

		prepareBGImage();
		prepareOnClick();
		prepareBackgroundAlpha();
		prepareSaveBtn();
		prepareHelpBtn();
		prepareAboutBtn();
	}

	private void prepareBackgroundAlpha() {
		DialogSeekBarPreference backgroundAlpha = (DialogSeekBarPreference)findPreference(Preferences.BACKGROUND_ALPHA);
		if (Integer.parseInt(Build.VERSION.SDK) <= Build.VERSION_CODES.ECLAIR_MR1) {
			backgroundAlpha.setEnabled(false);
			backgroundAlpha.setSummary(getString(R.string.needs_froyo));
		} else {
			backgroundAlpha.setKey(Preferences.get(Preferences.BACKGROUND_ALPHA, appWidgetId));
			backgroundAlpha.setOnPreferenceChangeListener(new SetCurValue(null, null));
		}
	}

	private void prepareColumnCount() {
		DialogSeekBarPreference columnCount = (DialogSeekBarPreference)findPreference(Preferences.COLUMN_COUNT);
		columnCount.setKey(Preferences.get(Preferences.COLUMN_COUNT, appWidgetId));
		columnCount.setMin(1);
		columnCount.setMax(6);
		columnCount.setOnPreferenceChangeListener(new SetCurValue(null, null));
	}

	private void prepareShowName() {
		// Find control and set the right preference-key for the AppWidgetId
		CheckBoxPreference showName = (CheckBoxPreference)findPreference(Preferences.SHOW_NAME);
		showName.setKey(Preferences.get(Preferences.SHOW_NAME, appWidgetId));
	}

	private void prepareNameKinds() {
		ListPreference nameKinds = (ListPreference)findPreference(Preferences.NAME_KIND);
		nameKinds.setKey(Preferences.get(Preferences.NAME_KIND, appWidgetId));
		nameKinds.setDependency(Preferences.get(Preferences.SHOW_NAME, appWidgetId));

		CharSequence[] Titles = new CharSequence[] {
				getString(R.string.displayname),
				getString(R.string.givenname),
				getString(R.string.familyname)};
		CharSequence[] Values = new CharSequence[] {
				String.valueOf(Preferences.NAME_DISPLAY_NAME),
				String.valueOf(Preferences.NAME_GIVEN_NAME),
				String.valueOf(Preferences.NAME_FAMILY_NAME)};
		nameKinds.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));

		nameKinds.setEntries(Titles);
		nameKinds.setEntryValues(Values);
		nameKinds.setValue(String.valueOf(Preferences.NAME_DISPLAY_NAME));
	}

	private void prepareOnClick() {
		ListPreference onClick = (ListPreference)findPreference(Preferences.ON_CLICK);
		onClick.setKey(Preferences.get(Preferences.ON_CLICK, appWidgetId));

		CharSequence[] Titles = new CharSequence[] {
				getString(R.string.quickcontactbar),
				getString(R.string.directdial),
				getString(R.string.showcontact)};
		CharSequence[] Values = new CharSequence[] {
				String.valueOf(Preferences.CLICK_QCB),
				String.valueOf(Preferences.CLICK_DIAL),
				String.valueOf(Preferences.CLICK_SHWCONTACT)};
		onClick.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));

		onClick.setEntries(Titles);
		onClick.setEntryValues(Values);
		onClick.setValue(String.valueOf(Preferences.CLICK_QCB));
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

    	boolean facebook = FacebookPluginBridge.IsFacebookPluginInstalled(this);
    	int virtualGroups = facebook ? Preferences.VIRTUAL_GROUP_COUNT : Preferences.VIRTUAL_GROUP_COUNT - 1;

    	CharSequence[] Titles = new CharSequence[orgCs.getCount()+virtualGroups];
    	CharSequence[] Values = new CharSequence[orgCs.getCount()+virtualGroups];

    	int pos = 0;
    	// First add the "virtual" group "All Contacts"
    	Titles[pos] = getString(R.string.allcontacts);
    	Values[pos++] = String.valueOf(Preferences.GROUP_ALLCONTACTS);

    	// First add the "stared" group "All Contacts"
    	Titles[pos] = getString(R.string.starred);
    	Values[pos++] = String.valueOf(Preferences.GROUP_STARRED);


    	if (facebook) {
    		Titles[pos] = getString(R.string.facebook);
    		Values[pos++] = String.valueOf(Preferences.GROUP_FACEBOOK);
    	}


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

	private void prepareBGImage() {
		ListPreference bgimage = (ListPreference)findPreference(Preferences.BGIMAGE);
		bgimage.setKey(Preferences.get(Preferences.BGIMAGE, appWidgetId));
		CharSequence[] Titles = new CharSequence[] {
				getString(R.string.black),
				getString(R.string.white)};
		CharSequence[] Values = new CharSequence[] {
				String.valueOf(Preferences.BG_BLACK),
				String.valueOf(Preferences.BG_WHITE)};
		bgimage.setOnPreferenceChangeListener(new SetCurValue(Titles, Values));

		bgimage.setEntries(Titles);
		bgimage.setEntryValues(Values);
		bgimage.setValue(String.valueOf(Preferences.BG_BLACK));
	}

	private void prepareHelpBtn() {
		Preference pref = findPreference("HELP");
		pref.setOnPreferenceClickListener(new HelpButtonClick(this, true));
	}

	private void prepareAboutBtn() {
		Preference pref = findPreference("ABOUT");
		pref.setOnPreferenceClickListener(new HelpButtonClick(this, false));
	}

	private class HelpButtonClick implements OnPreferenceClickListener {
		private final Context fContext;
		private final boolean fShowHelp;

		public HelpButtonClick(Context context, boolean showHelp) {
			fContext = context;
			fShowHelp = showHelp;
		}

		public boolean onPreferenceClick(Preference preference) {
			AlertDialog alertDialog;
			alertDialog = new AlertDialog.Builder(fContext).create();
			if (fShowHelp) {
				alertDialog.setTitle(fContext.getString(R.string.help));
				alertDialog.setMessage(fContext.getString(R.string.helptext));
			}
			else {
				alertDialog.setTitle(fContext.getString(R.string.about));
				alertDialog.setMessage(fContext.getString(R.string.abouttext));
			}
			alertDialog.setButton(fContext.getString(R.string.okbtn), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alertDialog.show();
			return false;
		}

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
		private final CharSequence[] fValues, fTitles;
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
			else if (preference instanceof DialogSeekBarPreference) {
				curVal = newValue.toString();
			}
			preference.setSummary(curVal);
			return true;
		}
	}
}