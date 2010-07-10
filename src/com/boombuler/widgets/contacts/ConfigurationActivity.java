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

import java.util.ArrayList;

import android.R.anim;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class ConfigurationActivity extends Activity {
    public static final String PREFS_NAME = "com.boombuler.widgets.contacts.PREFS";
    private static final String TAG = "boombuler.ConfigurationActivity";
    public static final String PREFS_GROUP_ID_PATTERN = "GroupId-%d";

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get any data we were launched with
        Intent launchIntent = getIntent();
        Log.d(TAG, "got intent: "+launchIntent.toString());
        Bundle extras = launchIntent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            Intent cancelResultValue = new Intent();
            cancelResultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_CANCELED, cancelResultValue);
        } else {
            finish();
        }

        setContentView(R.layout.configuration);

        final SharedPreferences config = getSharedPreferences(PREFS_NAME, 0);
        final Spinner spSelGroup = (Spinner) findViewById(R.id.selectGroup);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
        			android.R.layout.simple_spinner_item, getContactGroups(),
        			new String[] { ContactsContract.Groups.TITLE },
        			new int[] { android.R.id.text1 }
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSelGroup.setAdapter(adapter);
        
        Button saveButton = (Button) findViewById(R.id.btnSave);
        Button cancelButton = (Button) findViewById(R.id.btnCancel);

        cancelButton.setOnClickListener(new OnClickListener() {			
			public void onClick(View v) {
				finish();				
			}
		});
        

        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	long selItemId = spSelGroup.getSelectedItemId();
            	Log.d(TAG, String.valueOf(selItemId));
            	
                //int updateRateSeconds = Integer.parseInt(updateRateEntry.getText().toString());

                // store off the user setting for update timing
                SharedPreferences.Editor configEditor = config.edit();

                configEditor.putLong(String.format(PREFS_GROUP_ID_PATTERN, appWidgetId), selItemId);
                configEditor.commit();

                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {

                    // tell the app widget manager that we're now configured
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    setResult(RESULT_OK, resultValue);

                    Intent widgetUpdate = new Intent();
                    widgetUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    widgetUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] { appWidgetId });
                }

                // activity is now done
                finish();
            }
        });
    }
    
    private Cursor getContactGroups()
    {	
    	Log.d(TAG, "start getContactGroups");
    	Uri uri = ContactsContract.Groups.CONTENT_URI;
    	String[] projection = new String[] {
    			ContactsContract.Groups._ID,
    			ContactsContract.Groups.TITLE
    	};
    	String selection = ContactsContract.Groups.SYSTEM_ID + " is null";
    	String[] selectionArgs = null;
    	String sortOrder = null;
    	    	
    	return this.managedQuery(uri, projection, selection, selectionArgs, sortOrder);    	
    }
    
}
