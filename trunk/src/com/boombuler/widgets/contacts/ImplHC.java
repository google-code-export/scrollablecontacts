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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.net.Uri;
import android.util.Log;
import android.text.TextUtils;

public class ImplHC implements ContactWidget.WidgetImplementation {
	public static final String TAG = "boombuler.ImplHC";
	private ContactWidget mWidget;
	
	public static final String EXTRA_DEFAULT_WIDTH = "EXTRA_DEFAULT_WIDTH";

	public void setWidget(ContactWidget widget) {
		Log.d(TAG, "setting owner widget");
		mWidget = widget;
	}
	
	public void onUpdate(Context context, int appWidgetId) {
		Log.d(TAG, "onUpdate called!");
		// Here we setup the intent which points to the StackViewService which will
        // provide the views for this collection.
        Intent intent = new Intent(context, ContactWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.putExtra(EXTRA_DEFAULT_WIDTH, mWidget.getWidth());
		
		// When intents are compared, the extras are ignored, so we need to embed the extras
		// into the data so that the extras will not be ignored.
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		
		RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.main_ics);

        rv.setRemoteAdapter(appWidgetId, R.id.my_gridview, intent);


		// Here we setup the a pending intent template. Individuals items of a collection
		// cannot setup their own pending intents, instead, the collection as a whole can
		// setup a pending intent template, and the individual items can set a fillInIntent
		// to create unique before on an item to item basis.
        Intent clickIntent = new Intent(context, mWidget.getClass());
		clickIntent.setAction(LauncherIntent.Action.ACTION_ITEM_CLICK);
		clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
		PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		rv.setPendingIntentTemplate(R.id.my_gridview, clickPendingIntent);

		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
		appWidgetManager.updateAppWidget(appWidgetId, rv);
	}
	
	public boolean onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (TextUtils.equals(action, LauncherIntent.Action.ACTION_ITEM_CLICK)) {
			int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
			Uri uri = Uri.parse(intent.getStringExtra(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS));
			
			mWidget.onClick(context, appWidgetId, intent.getSourceBounds(), uri);
			return true;
		}
		return false;
	}
}