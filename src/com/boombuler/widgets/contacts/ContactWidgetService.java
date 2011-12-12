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
import java.util.List;

import mobi.intuitit.android.content.LauncherIntent;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class ContactWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ContactRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ContactRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    public static final String TAG = "boombuler.ContactRemoteViewsFactory";
    private Context mContext;
    private int mAppWidgetId;
	private int mDefWidth;
	private Cursor mCursor;	

    public ContactRemoteViewsFactory(Context context, Intent intent) {
		Log.d(TAG, "ContactRemoteViewsFactory created");
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
		mDefWidth = intent.getIntExtra(ImplHC.EXTRA_DEFAULT_WIDTH, 1);
    }

    public void onCreate() {
    }

    public void onDestroy() {
		if (mCursor != null) {
			mCursor.close();
			mCursor = null;
		}
    }

    public int getCount() {
		int count = mCursor.getCount();
		Log.d(TAG, "Found " + count + " entries");
        return count;
    }

    public RemoteViews getViewAt(int position) {
		Log.d(TAG, "get item at position: "+position);
        // position will always range from 0 to getCount() - 1.
		mCursor.moveToPosition(position);
		
		String displayName = mCursor.getString(DataProvider.DataProviderColumns.name.ordinal());
		String contactUri = mCursor.getString(DataProvider.DataProviderColumns.contacturi.ordinal());
		
		
		int textVisibility = Preferences.getShowName(mContext, mAppWidgetId) ? View.VISIBLE : View.GONE;

		int itemresid = R.layout.gridviewitem_hc;
		if (textVisibility == View.VISIBLE) {
			switch(Preferences.getTextAlign(mContext, mAppWidgetId)) {
				case Preferences.ALIGN_RIGHT:
					itemresid = R.layout.gridviewitem_txt_right; break;
				case Preferences.ALIGN_LEFT:
					itemresid = R.layout.gridviewitem_txt_left; break;
			}
		}
		
		
		// We construct a remote views item based on our widget item xml file, and set the
        // text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), itemresid);
		
		if (textVisibility == View.VISIBLE) {
			rv.setTextViewText(R.id.displayname, displayName);			
		} else {
			rv.setViewVisibility(R.id.displayname, textVisibility);
		}
		
		byte[] blob = mCursor.getBlob(DataProvider.DataProviderColumns.photo.ordinal());
		Bitmap img = null;
		if (blob != null && blob.length > 0)		
			img = BitmapFactory.decodeByteArray(blob, 0, blob.length);
		if (img != null)
			rv.setImageViewBitmap(R.id.photo, img);
		else
			rv.setImageViewResource(R.id.photo, R.drawable.no_image);

        // Next, we set a fill-intent which will be used to fill-in the pending intent template
        // which is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(LauncherIntent.Extra.Scroll.EXTRA_ITEM_POS, contactUri);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.displayname, fillInIntent);
		rv.setOnClickFillInIntent(R.id.photo, fillInIntent);

        // Return the remote views object.
        return rv;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

	@Override
    public void onDataSetChanged() {
		Log.d(TAG, "Start Query!");
		Uri dataUri = DataProvider.CONTENT_URI_MESSAGES.buildUpon().appendEncodedPath(Integer.toString(mAppWidgetId)).build();
	
		DataProvider prov = new DataProvider();
		mCursor = prov.query(dataUri, DataProvider.PROJECTION_APPWIDGETS, null, null, null);
    }
}