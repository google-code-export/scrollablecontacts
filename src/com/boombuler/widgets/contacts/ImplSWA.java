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
import mobi.intuitit.android.widget.BoundRemoteViews;
import mobi.intuitit.android.widget.SimpleRemoteViews;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;


public class ImplSWA implements ContactWidget.WidgetImplementation {
	private ContactWidget mWidget;
	
	// Tag for logging
	private static final String TAG = "boombuler.ContactWidget.ImplSWA";


	public void setWidget(ContactWidget widget) {
		mWidget = widget;
	}

	public void onUpdate(Context context, int appWidgetId) {
		final boolean isICS = Preferences.getBGImage(context, appWidgetId) == Preferences.BG_ICS;
        RemoteViews views = new RemoteViews(context.getPackageName(), isICS ? R.layout.main_swa_ics : R.layout.main);
        String text = Preferences.getDisplayLabel(context, appWidgetId);

        if (!isICS) {
	        boolean withHeader = text != "";
	
	        if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_BLACK) {
	            views.setImageViewResource(R.id.backgroundImg, withHeader ? R.drawable.background_dark_header : R.drawable.background_dark);
	            views.setTextColor(R.id.group_caption, Color.WHITE);
	        } else if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_WHITE) {
	        	views.setImageViewResource(R.id.backgroundImg, withHeader ? R.drawable.background_light_header : R.drawable.background_light);
	        	views.setTextColor(R.id.group_caption, Color.BLACK);
	        } else {
	        	views.setImageViewResource(R.id.backgroundImg, Color.TRANSPARENT);
	            views.setTextColor(R.id.group_caption, Color.WHITE);
	        }
	
	        // First set the display label
	        views.setTextViewText(R.id.group_caption, text);
	        // and if it is empty hide it 
	        views.setViewVisibility(R.id.group_caption, withHeader ? View.VISIBLE : View.GONE);
	        
	        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
	        	views.setInt(R.id.backgroundImg, "setAlpha", Preferences.getBackgroundAlpha(context, appWidgetId));
	        }
        }
        
        AppWidgetManager awm = AppWidgetManager.getInstance(context);
        awm.updateAppWidget(appWidgetId, views);
	}
	
	public boolean onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (TextUtils.equals(action, LauncherIntent.Action.ACTION_READY)) {
			onAppWidgetReady(context, intent);
			return true;
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_FINISH)) {
			return true;
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_ITEM_CLICK)) {
			onClick(context, intent);
			return true;
		} else if (TextUtils.equals(action, LauncherIntent.Action.ACTION_VIEW_CLICK)) {
			onClick(context, intent);
			return true;
		} else if (TextUtils.equals(action, LauncherIntent.Error.ERROR_SCROLL_CURSOR)) {
		    Log.d(TAG, intent.getStringExtra(LauncherIntent.Extra.EXTRA_ERROR_MESSAGE));
			return true;
		} else if (action.equals("com.motorola.blur.home.ACTION_SET_WIDGET_SIZE")) {
			updateSize(context, intent);
			return true;
		}
		return false;
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
		mWidget.onClick(context, appWidgetId, r, uri);
	}
	
	private void updateSize(Context context, Intent intent) {
		int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);		
		int spanX = intent.getIntExtra("spanX", mWidget.getWidth());
		int oldSpanX = Preferences.getSpanX(context, appWidgetId, mWidget.getWidth());
		if (spanX != oldSpanX) {
			Preferences.setSpanX(context, appWidgetId, spanX);
			context.sendBroadcast(CreateMakeScrollableIntent(context, appWidgetId));
		}
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
		
		final boolean isICS = Preferences.getBGImage(context, appWidgetId) == Preferences.BG_ICS;
		
		final int colCount = Preferences.getColumnCount(context, appWidgetId);

		// Give a layout resource to be inflated. If this is not given, the launcher will create one
		SimpleRemoteViews gridViews = new SimpleRemoteViews( isICS ? R.layout.gridview_ics : R.layout.gridview);
		if (!isICS)
			gridViews.setInt(R.id.my_gridview, "setNumColumns", colCount);
		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_LISTVIEW_REMOTEVIEWS, gridViews);

		boolean autosizeImages = true;
		int itemresid = isICS ? R.layout.gridviewitem_ics : R.layout.gridviewitem;
		int textVisibility = Preferences.getShowName(context, appWidgetId) ?
				View.VISIBLE : View.GONE;

		if (textVisibility == View.VISIBLE) {
			autosizeImages = false;
			if (!isICS) {
				switch(Preferences.getTextAlign(context, appWidgetId)) {
					case Preferences.ALIGN_RIGHT:
						itemresid = R.layout.gridviewitem_txt_right; break;
					case Preferences.ALIGN_LEFT:
						itemresid = R.layout.gridviewitem_txt_left; break;
					case Preferences.ALIGN_CENTER:
						autosizeImages = true; break;
				}
			}
		} 

		BoundRemoteViews itemViews = new BoundRemoteViews(itemresid);
		if (textVisibility == View.VISIBLE) {
			itemViews.setBoundCharSequence(R.id.displayname, "setText",
					DataProvider.DataProviderColumns.name.ordinal(),0);
		} 
		itemViews.setBoundBitmap(R.id.photo, "setImageBitmap",
				DataProvider.DataProviderColumns.photo.ordinal(), R.drawable.no_image);

		if (autosizeImages) {
			int width = ContactWidget.calcWidthPixel(context, appWidgetId, mWidget.getWidth()); // get the widget width in dip
			itemViews.setViewWidth(R.id.photo, width);
			itemViews.setViewHeight(R.id.photo, width);
		}

		Intent intent = new Intent(context, this.getClass());
		intent.setAction(LauncherIntent.Action.ACTION_VIEW_CLICK);
		intent.setData(Uri.parse(widgeturi));
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		itemViews.SetBoundOnClickIntent(R.id.photo, pendingIntent,
				LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS,
				DataProvider.DataProviderColumns.contacturi.ordinal());
		if (textVisibility == View.VISIBLE) {
			itemViews.SetBoundOnClickIntent(R.id.displayname, pendingIntent,
					LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS,
					DataProvider.DataProviderColumns.contacturi.ordinal());
		}
		itemViews.setViewVisibility(R.id.displayname, textVisibility);
		if (isICS)
			itemViews.setViewVisibility(R.id.label_overlay, textVisibility);
		else if (textVisibility == View.VISIBLE &&
        	Preferences.getBGImage(context, appWidgetId) == Preferences.BG_WHITE) {
    			itemViews.setTextColor(R.id.displayname, Color.BLACK);
        }

		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_LAYOUT_REMOTEVIEWS, itemViews);

		putProvider(result, widgeturi);

		// Launcher can set onClickListener for each children of an item. Without
		// explicitly put this
		// extra, it will just set onItemClickListener by default
		result.putExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_CHILDREN_CLICKABLE, true);
		return result;
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
			final boolean isICS = Preferences.getBGImage(context, appWidgetId) == Preferences.BG_ICS;
			
			RemoteViews views = new RemoteViews(context.getPackageName(), isICS ? R.layout.main_swa_ics : R.layout.main);
			views.setTextViewText(R.id.loading, context.getString(R.string.launcher_too_old));
	        if (Preferences.getBGImage(context, appWidgetId) == Preferences.BG_BLACK) {
	            views.setImageViewResource(R.id.backgroundImg, R.drawable.background_dark);
	            views.setTextColor(R.id.loading, Color.WHITE);
	        }
	        else if (!isICS){
	        	views.setImageViewResource(R.id.backgroundImg, R.drawable.background_light);
	        	views.setTextColor(R.id.loading, Color.BLACK);
	        }

			awm.updateAppWidget(appWidgetId, views);
			return;
		}

		onUpdate(context, appWidgetId);
		Intent replaceDummy = CreateMakeScrollableIntent(context, appWidgetId);

		// Send it out
		context.sendBroadcast(replaceDummy);
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