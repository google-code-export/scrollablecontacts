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

import mobi.intuitit.android.content.LauncherIntent;
import mobi.intuitit.android.widget.BoundRemoteViews;
import mobi.intuitit.android.widget.SimpleRemoteViews;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.QuickContact;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;


public abstract class ContactWidget extends AppWidgetProvider {
	// Tag for logging
	private static final String TAG = "boombuler.ContactWidget";

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
        	updateGroupTitleAndBackground(context, appWidgetId);
        }
	}

	protected void updateGroupTitleAndBackground(Context context, int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
        String text = Preferences.getDisplayLabel(context, appWidgetId);

        boolean withHeader = text != "";

        if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_BLACK) {
            views.setImageViewResource(R.id.backgroundImg, withHeader ? R.drawable.background_dark_header : R.drawable.background_dark);
            views.setTextColor(R.id.group_caption, Color.WHITE);
        }
        else {
        	views.setImageViewResource(R.id.backgroundImg, withHeader ? R.drawable.background_light_header : R.drawable.background_light);
        	views.setTextColor(R.id.group_caption, Color.BLACK);
        }
        // First set the display label
        views.setTextViewText(R.id.group_caption, text);
        // and if it is empty hide it
        views.setViewVisibility(R.id.group_caption, withHeader ? View.VISIBLE : View.GONE);
        if (Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.ECLAIR_MR1) {
        	views.setInt(R.id.backgroundImg, "setAlpha", Preferences.getBackgroundAlpha(context, appWidgetId));
        }

        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        awm.updateAppWidget(appWidgetId, views);
	}

	protected abstract int getWidth();

	private void logIntent(Intent intent, boolean extended) {
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
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_READY)) {
			// Receive ready signal
			Log.d(TAG, "widget ready");
			onAppWidgetReady(context, intent);
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
		} else if (action.equals("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")) {
			updateSize(context, intent);
		} else {
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

	/**
	 * On click of a child view in an item
	 */
	private void onClick(Context context, Intent intent) {
		Log.d(TAG, "starting onClick");
		int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		Log.d(TAG, "got appWidgetId: "+ appWidgetId);

		Uri uri = Uri.parse(intent.getStringExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS));

		Rect r;
		if (intent.hasExtra(LauncherIntent.Extra.Scroll.EXTRA_SOURCE_BOUNDS)) {
			Log.d(TAG, "got rect from launcher");
			r = (Rect)intent.getParcelableExtra(LauncherIntent.Extra.Scroll.EXTRA_SOURCE_BOUNDS);
		} else {
		    r = intent.getSourceBounds();
		}
		try
		{
			int act = Preferences.getOnClickAction(context, appWidgetId);
            if (act == Preferences.CLICK_QCB) {
			    QuickContact.showQuickContact(context,r ,
					uri, QuickContact.MODE_LARGE, null);
            } else if (act == Preferences.CLICK_SHWCONTACT) {
            	Intent launch = new Intent(Intent.ACTION_VIEW, uri);
    			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(launch);
            } else if (act == Preferences.CLICK_DIAL) {
            	Intent launch = new Intent(Intent.ACTION_DIAL, uri);
    			launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(launch);
            }
		}
		catch(ActivityNotFoundException expt)
		{
			Log.d(TAG, "FAILED: " + expt.getMessage());
		}
	}

	private void updateSize(Context context, Intent intent) {
		int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
		int spanX = intent.getIntExtra("spanX", this.getWidth());
		int oldSpanX = Preferences.getSpanX(context, appWidgetId, getWidth());
		if (spanX != oldSpanX) {
			Preferences.setSpanX(context, appWidgetId, spanX);
			context.sendBroadcast(CreateMakeScrollableIntent(context, appWidgetId));
		}
	}

	private int calcWidthPixel(boolean horizontal, Context context, int appWidgetId) {
		int spanx = Preferences.getSpanX(context, appWidgetId, this.getWidth());
		if (horizontal)
			return 106 * spanx;
		else
			return 80 * spanx;
	}

	/**
	 * Receive ready intent from Launcher, prepare scroll view resources
	 */
	public void onAppWidgetReady(Context context, Intent intent) {
		if (intent == null)
			return;

		int APIVersion = intent.getExtras().getInt(LauncherIntent.Extra.EXTRA_API_VERSION, 1);
		Log.d(TAG, "current launcher API version: " +  APIVersion);

		int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);

		if (appWidgetId < 0) {
			return;
		}

		if (APIVersion < 2) {
			AppWidgetManager awm = AppWidgetManager.getInstance(context);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
			views.setTextViewText(R.id.loading, context.getString(R.string.launcher_too_old));
	        if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_BLACK) {
	            views.setImageViewResource(R.id.backgroundImg, R.drawable.background_dark);
	            views.setTextColor(R.id.loading, Color.WHITE);
	        }
	        else {
	        	views.setImageViewResource(R.id.backgroundImg, R.drawable.background_light);
	        	views.setTextColor(R.id.loading, Color.BLACK);
	        }

			awm.updateAppWidget(appWidgetId, views);
			return;
		}

		updateGroupTitleAndBackground(context, appWidgetId);
		Intent replaceDummy = CreateMakeScrollableIntent(context, appWidgetId);

		// Send it out
		context.sendBroadcast(replaceDummy);
	}

	/**
	 * Constructs a Intent that tells the launcher to replace the dummy with the ListView
	 */
	public Intent CreateMakeScrollableIntent(Context context, int appWidgetId) {
		String widgeturi = DataProvider.CONTENT_URI_MESSAGES.buildUpon().appendEncodedPath(
				Integer.toString(appWidgetId)).toString();

		Log.d(TAG, "creating ACTION_SCROLL_WIDGET_START intent");
		Intent result = new Intent(LauncherIntent.Action.ACTION_SCROLL_WIDGET_START);

		// Put widget info
		result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		result.putExtra(LauncherIntent.Extra.EXTRA_VIEW_ID, R.id.widget_content);

		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_DATA_PROVIDER_ALLOW_REQUERY, true);

		final int colCount = Preferences.getColumnCount(context, appWidgetId);

		// Give a layout resource to be inflated. If this is not given, the launcher will create one
		SimpleRemoteViews gridViews = new SimpleRemoteViews(R.layout.gridview);
		gridViews.setInt(R.id.my_gridview, "setNumColumns", colCount);
		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_LISTVIEW_REMOTEVIEWS, gridViews);
		BoundRemoteViews itemViews = new BoundRemoteViews(R.layout.gridviewitem);

		itemViews.setBoundCharSequence(R.id.displayname, "setText",
				DataProvider.DataProviderColumns.name.ordinal(),0);
		itemViews.setBoundBitmap(R.id.photo, "setImageBitmap",
				DataProvider.DataProviderColumns.photo.ordinal(), R.drawable.no_image);

		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		display.getMetrics(dm);
		int width = calcWidthPixel((display.getOrientation() % 2) == 1, context, appWidgetId); // get the widget width in dip
		width = width - (colCount * 5) - 5; // grid view spacing...
		width = (int)(((width - 24) / colCount) * dm.density);

		itemViews.setViewWidth(R.id.photo, width);
		itemViews.setViewHeight(R.id.photo, width);

		Intent intent = new Intent(context, this.getClass());
		intent.setAction(LauncherIntent.Action.ACTION_VIEW_CLICK);
		intent.setData(Uri.parse(widgeturi));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		itemViews.SetBoundOnClickIntent(R.id.photo, pendingIntent,
				LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS,
				DataProvider.DataProviderColumns.contacturi.ordinal());
		itemViews.SetBoundOnClickIntent(R.id.displayname, pendingIntent,
				LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS,
				DataProvider.DataProviderColumns.contacturi.ordinal());

		if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_WHITE) {
			itemViews.setTextColor(R.id.displayname, Color.BLACK);
		}

		itemViews.setViewVisibility(R.id.displayname, Preferences.getShowName(context, appWidgetId) ?
				View.VISIBLE : View.GONE);

		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_LAYOUT_REMOTEVIEWS, itemViews);

		putProvider(result, widgeturi);

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
	protected void putProvider(Intent intent, String widgetUri) {
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
}