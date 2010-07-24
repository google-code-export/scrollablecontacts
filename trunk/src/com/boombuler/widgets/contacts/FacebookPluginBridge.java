package com.boombuler.widgets.contacts;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class FacebookPluginBridge {

	public static final String AUTHORITY = "com.boombuler.widgets.contacts.facebook.provider";
	
	public static final Uri CONTENTURI = Uri.parse("content://"+AUTHORITY + "/data");
	
	
	public static boolean IsFacebookPluginInstalled(Context context) {		
		PackageManager pm = context.getPackageManager();
		return pm.resolveContentProvider("com.boombuler.widgets.contacts.facebook.provider", 0) != null;
	}
	
	public static Cursor QueryFacebook(Context context) {
		Cursor crs = context.getContentResolver().query(CONTENTURI, null, null, null, null);
		if (crs != null)
		{
			crs.moveToFirst();
			Log.d("FROM FACEBOOK", crs.getString(0));
		} else
		{
			Log.d("FROM FACEBOOK","NO FACEBOOK PROVIDER INSTALLED");
		}
		
		return crs;
	}
}
