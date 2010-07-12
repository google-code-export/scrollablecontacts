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
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import mobi.intuitit.android.content.LauncherIntent;

public class ListViewManager {
	private static final String TAG = "boombuler.ListViewManager";
	/**
	 * Receive ready intent from Launcher, prepare scroll view resources
	 */
	public static void onAppWidgetReady(Context context, Intent intent) {
		if (intent == null)
			return;

		// try new method
		int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);

		if (appWidgetId < 0) {
			return;
		}
		Intent replaceDummy = CreateMakeScrollableIntent(appWidgetId);

		// Send it out
		context.sendBroadcast(replaceDummy);
	}
	
	public static Intent CreateMakeScrollableIntent(int appWidgetId) {
		Log.d(TAG, "creating ACTION_SCROLL_WIDGET_START intent");
		Intent result = new Intent(LauncherIntent.Action.ACTION_SCROLL_WIDGET_START);

		// Put widget info
		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		result.putExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, R.id.widget_content);

		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_DATA_PROVIDER_ALLOW_REQUERY, true);

		// Give a layout resource to be inflated. If this is not given, Home++
		// will create one

		// Put adapter info
		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_LISTVIEW_LAYOUT_ID,
				R.layout.listview);
		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_LAYOUT_ID, R.layout.contactlistentry);
		putProvider(result, DataProvider.CONTENT_URI_MESSAGES.buildUpon().appendEncodedPath(
				Integer.toString(appWidgetId)).toString());
		putMapping(result);

		// Launcher can set onClickListener for each children of an item. Without
		// explicitly put this
		// extra, it will just set onItemClickListener by default
		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_CHILDREN_CLICKABLE, true);
		return result;
	}
	
	/**
	 * Put provider info as extras in the specified intent
	 * 
	 * @param intent
	 */
	public static void putProvider(Intent intent, String widgetUri) {
		if (intent == null)
			return;

		String whereClause = null;
		String orderBy = null;
		String[] selectionArgs = null;

		// Put the data uri in as a string. Do not use setData, Home++ does not
		// have a filter for that
		intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_DATA_URI, widgetUri);

		// Other arguments for managed query
		intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_PROJECTION, DataProvider.PROJECTION_APPWIDGETS);
		intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_SELECTION, whereClause);
		intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_SELECTION_ARGUMENTS, selectionArgs);
		intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_SORT_ORDER, orderBy);

	}

	/**
	 * Put mapping info as extras in intent
	 */
	public static void putMapping(Intent intent) {
		if (intent == null)
			return;

		final int NB_ITEMS_TO_FILL = 2;

		int[] cursorIndices = new int[NB_ITEMS_TO_FILL];
		int[] viewTypes = new int[NB_ITEMS_TO_FILL];
		int[] layoutIds = new int[NB_ITEMS_TO_FILL];
		boolean[] clickable = new boolean[NB_ITEMS_TO_FILL];
		int[] defResources = new int[NB_ITEMS_TO_FILL];

		int iItem = 0;
		
		intent.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_ACTION_VIEW_URI_INDEX, 
				DataProvider.DataProviderColumns.lookupkey.ordinal());
		
		cursorIndices[iItem] = DataProvider.DataProviderColumns.photo.ordinal();
		viewTypes[iItem] = LauncherIntent.Extra.Scroll.Types.IMAGEBLOB;
		layoutIds[iItem] = R.id.photo;
		clickable[iItem] = true;
		defResources[iItem] = R.drawable.identity;

		iItem++;
		
		cursorIndices[iItem] = DataProvider.DataProviderColumns.name.ordinal();
		viewTypes[iItem] = LauncherIntent.Extra.Scroll.Types.TEXTVIEW;
		layoutIds[iItem] = R.id.displayname;
		clickable[iItem] = false;
		defResources[iItem] = 0;

		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_IDS, layoutIds);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_TYPES, viewTypes);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_CLICKABLE, clickable);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_CURSOR_INDICES, cursorIndices);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_DEFAULT_RESOURCES, defResources);
	}
}
