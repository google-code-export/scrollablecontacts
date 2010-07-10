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

import mobi.intuitit.android.content.LauncherIntent;
import android.appwidget.*;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Rect;
import android.provider.ContactsContract;
import android.provider.ContactsContract.QuickContact;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;


public class ContactWidget extends AppWidgetProvider {
	private static final String TAG = "boombuler.ContactWidget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// If no specific widgets requested, collect list of all
		if (appWidgetIds == null) {
			appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ContactWidget.class));
		}
		Log.d(TAG, "recieved onUpdate");

        // Construct views
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
        
        // Tell the widget manager
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }	
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		Log.d(TAG, "recieved -> " +  action);
		if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
			final int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
				this.onDeleted(context, new int[] { appWidgetId });
			}
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_READY)) {
			// Receive ready signal
			Log.d(TAG, "widget ready");
			ListViewManager.onAppWidgetReady(context, intent);
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_FINISH)) {

		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_ITEM_CLICK)) {
			// onItemClickListener
			onClick(context, intent);
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_VIEW_CLICK)) {
			// onClickListener
			onClick(context, intent);
		} else if (TextUtils.equals(action, LauncherIntent.Error.ERROR_SCROLL_CURSOR)) {
			// An error occurred
		    Log.d(TAG, intent.getStringExtra(LauncherIntent.Extra.EXTRA_ERROR_MESSAGE));
		} else
			super.onReceive(context, intent);
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
		SharedPreferences prefs = context.getSharedPreferences(ConfigurationActivity.PREFS_NAME, 0);
		Editor edit = prefs.edit();
		for(int appWId : appWidgetIds) {
			edit.remove(String.format(ConfigurationActivity.PREFS_GROUP_ID_PATTERN, appWId));
		}
		edit.commit();
	}
	
	/**
	 * On click of a child view in an item
	 */
	private void onClick(Context context, Intent intent) {
		String itemId = intent.getStringExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS);
		int viewId = intent.getIntExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, -1);

//		int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
//				AppWidgetManager.INVALID_APPWIDGET_ID);		
		if (viewId == R.id.photo) {
			Rect r = new Rect();
			
			DisplayMetrics dm = context.getResources().getDisplayMetrics();
			r.right = dm.widthPixels;
			r.bottom = dm.heightPixels;
			r.top = 0;
			r.left = 0;
			
			// TODO: determine the right position to display
			QuickContact.showQuickContact(context,r , 
									ContactsContract.Contacts.CONTENT_LOOKUP_URI.buildUpon().appendPath(itemId).build(), 
									QuickContact.MODE_LARGE, null);
			
		}
	}
	
    
}
