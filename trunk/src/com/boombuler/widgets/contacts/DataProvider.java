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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

public class DataProvider extends ContentProvider {
	public static final String TAG = "boombuler.DataProvider";

	private static final String AUTHORITY_BASE = "com.boombuler.widgets.contacts";
	public static final String AUTHORITY = AUTHORITY_BASE + ".provider";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri CONTENT_URI_MESSAGES = CONTENT_URI.buildUpon().appendEncodedPath("data").build();

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int URI_DATA = 0;

	public enum DataProviderColumns {
		_id, photo, name, contacturi
	}

	public static final String[] PROJECTION_APPWIDGETS = new String[] { DataProviderColumns._id.toString(),
			DataProviderColumns.photo.toString(), DataProviderColumns.name.toString(), DataProviderColumns.contacturi.toString()};

	private class ContObserver extends ContentObserver {

		public ContObserver() {
			super(null);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			int[] appWidgetIds = Preferences.getAllWidgetIds(ctx);

			for (int id : appWidgetIds)
				notifyDatabaseModification(id);
		}

	}

	private static Context ctx = null;

	static {
		URI_MATCHER.addURI(AUTHORITY, "data/*", URI_DATA);
	}

	@Override
	public boolean onCreate() {
		if (ctx == null) {
			ctx = getContext();

			ctx.getContentResolver().registerContentObserver(RawContacts.CONTENT_URI,
					true, new ContObserver());
		}

		return false;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int match = URI_MATCHER.match(uri);
		switch (match) {
			case URI_DATA:
				List<String> pathSegs = uri.getPathSegments();
				int appWId = Integer.parseInt(pathSegs.get(pathSegs.size() - 1));
				long GroupId = Preferences.getGroupId(ctx, appWId);
				int NameKind = Preferences.getNameKind(ctx, appWId);
				boolean withPhone = Preferences.getOnClickAction(ctx, appWId) == Preferences.CLICK_DIAL;
				if (GroupId == Preferences.GROUP_FACEBOOK){
					if (FacebookPluginBridge.IsFacebookPluginInstalled(ctx))
						return cloneCursorAndClose(ctx.getContentResolver().query(FacebookPluginBridge.CONTENTURI, projection, selection, selectionArgs, sortOrder));
					else
						GroupId = 0;
				}
				return loadNewData(this, projection, GroupId, NameKind, withPhone);
			default:
				throw new IllegalStateException("Unrecognized URI:" + uri);
		}
	}

	private Cursor cloneCursorAndClose(Cursor crs) {
		String[] colNames = crs.getColumnNames();
		ExtMatrixCursor result = new ExtMatrixCursor(colNames);
		crs.moveToFirst();

		while (!crs.isAfterLast()) {
			Object[] values = new Object[colNames.length];
			for (int i = 0, count = colNames.length; i < count; i++) {
				String column = colNames[i];
				if (DataProviderColumns._id.toString().equals(column)) {
					values[i] = crs.getInt(i);
				} else if (DataProviderColumns.name.toString().equals(column)) {
					values[i] = crs.getString(i);
				} else if (DataProviderColumns.photo.toString().equals(column)) {
					values[i] = crs.getBlob(i);
				} else if (DataProviderColumns.contacturi.toString().equals(column)) {
					values[i] = crs.getString(i);
				}
			}
			Log.d(TAG, "record copied");
			result.addRow(values);
			crs.moveToNext();
		}
		crs.close();
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

	public static void notifyDatabaseModification(int widgetId) {
		Uri widgetUri = CONTENT_URI_MESSAGES.buildUpon().appendEncodedPath(Integer.toString(widgetId)).build();
		Log.d(TAG, "notifyDatabaseModification -> UPDATE widgetUri : " + widgetUri);
		ctx.getContentResolver().notifyChange(widgetUri, null);
	}


	public static ExtMatrixCursor loadNewData(ContentProvider mcp, String[] projection, long GroupId, int NameKind, boolean withPhone) {
		ExtMatrixCursor ret = new ExtMatrixCursor(projection);

		Log.d(TAG, "... loading data");

		Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] src_projection = new String[] {
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.LOOKUP_KEY
        };
        AdressFilter flt = new AdressFilter(ctx, GroupId, NameKind);
        String src_sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        AdressFilter.NameResolver nameResolver = flt.getNameResolver();

        String needsPhone = withPhone ? ContactsContract.Contacts.HAS_PHONE_NUMBER + " = 1 AND " : "";

        Cursor cur = ctx.getContentResolver().query(uri, src_projection, needsPhone + flt.getFilter(), flt.getFilterParams(), src_sortOrder);
		if (cur == null || nameResolver == null) {
			return ret;
		}
        cur.moveToFirst();
		try
		{
			while (!cur.isAfterLast()) {
				boolean skip = false;
				Object[] values = new Object[projection.length];
				long id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));

				for (int i = 0, count = projection.length; i < count; i++) {
					String column = projection[i];
					if (DataProviderColumns._id.toString().equals(column)) {
						values[i] = id;
					} else if (DataProviderColumns.name.toString().equals(column)) {
						values[i] = nameResolver.updateName(
								    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)),
									cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
					} else if (DataProviderColumns.photo.toString().equals(column)) {
						values[i] = getImg(id);
					} else if (DataProviderColumns.contacturi.toString().equals(column)) {
						if (withPhone) {
							values[i] = getPhoneUri(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
							if (values[i] == null) {
								skip = true;
								break;
							}
						}
						else
						    values[i] = ContactsContract.Contacts.CONTENT_LOOKUP_URI.buildUpon()
						                .appendPath(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)))
						                .appendPath(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)))
						                .build().toString();
					}
				}
				if (!skip)
					ret.addRow(values);
				cur.moveToNext();
			}
		}
		finally
		{
			if (nameResolver != null)
				nameResolver.close();
			if (cur != null)
				cur.close();
		}
		return ret;
	}

	private static Uri getPhoneUri(String lookupKey) {
		Uri result = realGetPhoneUri(lookupKey, " AND " +ContactsContract.Data.IS_SUPER_PRIMARY + "= '1'");
		if (result == null)
			result = realGetPhoneUri(lookupKey, " AND " +ContactsContract.Data.IS_PRIMARY + "= '1'");
		if (result == null)
			result = realGetPhoneUri(lookupKey, "");
		return result;
	}

	private static Uri realGetPhoneUri(String lookupKey, String aPrimaryFilter) {
		Uri result = null;
		Cursor c = ctx.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
		          new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
				  ContactsContract.Data.LOOKUP_KEY + "=?" + " AND " +
		          ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" +
		          aPrimaryFilter,
		          new String[] {String.valueOf(lookupKey)}, null);
		if (c.getCount() > 0) {
			c.moveToFirst();
			result = Uri.parse("tel:"+c.getString(0));
		}
		c.close();
		return result;
	}

	public static byte[] getImg(long aId) {
		Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, aId);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(ctx.getContentResolver(), uri);
        if (input == null) {
        	return null;
		}
        try
        {
        	byte[] res = new byte[input.available()];
        	input.read(res);
        	input.close();
        	return res;
        }
        catch(IOException expt) {
        }
		return null;
	}

}