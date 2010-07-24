package com.boombuler.widgets.contacts.facebook;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class FacebookProvider extends ContentProvider{
	public static final String TAG = "boombuler.FacebookProvider";
	
	private static final String AUTHORITY_BASE = "com.boombuler.widgets.contacts.facebook";
	public static final String AUTHORITY = AUTHORITY_BASE + ".provider";
	
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	public static final Uri CONTENT_URI_MESSAGES = CONTENT_URI.buildUpon().appendEncodedPath("data").build();
	
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int URI_DATA = 0;	
	
	
	private static Context fContext = null;
	
	static {
		URI_MATCHER.addURI(AUTHORITY, "data/*", URI_DATA);
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
	public boolean onCreate() {
		fContext = getContext();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		MatrixCursor result = new MatrixCursor(projection);
		
		Object[] row = new Object[] {0, null, "test", "content://contactURI" };
		result.addRow(row);
		
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}

}
