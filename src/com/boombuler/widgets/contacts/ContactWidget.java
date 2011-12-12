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

import java.util.Set;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;


public abstract class ContactWidget extends AppWidgetProvider {

	public interface WidgetImplementation {
		public void setWidget(ContactWidget widget);
		public void onUpdate(Context context, int appWidgetId);
		public boolean onReceive(Context context, Intent intent);
	}
	
	
	// Tag for logging
	private static final String TAG = "boombuler.ContactWidget";

	private WidgetImplementation mImpl;
		
	public ContactWidget() {
		super();
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
			mImpl = new ImplSWA();
		else
			mImpl = new ImplHC();
		mImpl.setWidget(this);
		
	}
	
		
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// If no specific widgets requested, collect list of all

		if (appWidgetIds == null) {
			appWidgetIds = Preferences.getAllWidgetIds(context);
		}
		Log.d(TAG, "recieved onUpdate");

        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            // Construct views
        	int appWidgetId = appWidgetIds[i];
			mImpl.onUpdate(context, appWidgetId);
        }
	}

	public abstract int getWidth();

	public void logIntent(Intent intent, boolean extended) {
		if (extended)
			Log.d(TAG, "------------Log Intent------------");
		Log.d(TAG, "Action       : " + intent.getAction());
		if (!extended)
			return;
		Log.d(TAG, "Data         : " + intent.getDataString());
		Log.d(TAG, "Component    : " + intent.getComponent().toString());
		Log.d(TAG, "Package      : " + intent.getPackage());
		Log.d(TAG, "Flags        : " + intent.getFlags());
		Log.d(TAG, "Scheme       : " + intent.getScheme());
		Log.d(TAG, "SourceBounds : " + intent.getSourceBounds());
		Log.d(TAG, "Type         : " + intent.getType());
		Bundle extras = intent.getExtras();
		if (extras != null) {
			Log.d(TAG, "--Extras--");

			for(String key : extras.keySet()) {
				Log.d(TAG, key + " --> " + extras.get(key));
			}
			Log.d(TAG, "----------");
		}
		Set<String> cats = intent.getCategories();
		if (cats != null) {
			Log.d(TAG, "--Categories--");
			for(String cat : cats) {
				Log.d(TAG, " --> " + cat);
			}
			Log.d(TAG, "--------------");
		}
		Log.d(TAG, "----------------------------------");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		logIntent(intent, false);
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else {
			if (!mImpl.onReceive(context, intent))
				super.onReceive(context, intent);
		}
	}

	/**
	 * Will be executed when the widget is removed from the homescreen
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		// Drop the settings if the widget is deleted
		Preferences.DropSettings(context, appWidgetIds);
	}


	public void onClick(Context context, int appWidgetId, Rect targetRect, Uri uri) {
		try
		{
			int act = Preferences.getOnClickAction(context, appWidgetId);
            if (act == Preferences.CLICK_QCB) {
			    QuickContact.showQuickContact(context,targetRect ,
					uri, QuickContact.MODE_LARGE, null);
            } else if (act == Preferences.CLICK_SHWCONTACT || act == Preferences.CLICK_SMS) {
            	Intent launch = new Intent(Intent.ACTION_VIEW, uri);
    			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(launch);
            } else if (act == Preferences.CLICK_DIAL) {
            	Intent launch = new Intent(Intent.ACTION_CALL, uri);
    			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(launch);
            }
		}
		catch(ActivityNotFoundException expt)
		{
			Log.d(TAG, "FAILED: " + expt.getMessage());
		}
	}


	public static int calcWidthPixel(boolean horizontal, Context context, int appWidgetId, int width) {
		int spanx = Preferences.getSpanX(context, appWidgetId, width);
		if (horizontal)
			return 106 * spanx;
		else
			return 80 * spanx;
	}
}