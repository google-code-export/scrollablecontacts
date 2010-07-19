package com.boombuler.widgets.contacts;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class AdressFilter {

	private static final String TAG = "boombuler.AdressFilter";
	
	private String fFilter;
	private String[] fParams;
	
	public AdressFilter(Context context, long aGroupId) {
		fFilter = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'";
		if (aGroupId == 0) {
			fParams = null;
			return;
		}

		Cursor resC = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, 
				new String[] { ContactsContract.Contacts.LOOKUP_KEY }, 
				ContactsContract.Data.MIMETYPE + "=? AND " + 
				ContactsContract.Data.DATA1 + "=?",
				new String[] { 
					ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE, 
					String.valueOf(aGroupId)
				}, null);		
		
		fParams = new String[resC.getCount()];
		resC.moveToFirst();
		int i = 0;
		int count = resC.getCount();
		if (count == 0)
			return;
		fFilter += " AND (";
		Log.d(TAG, "Adr Count: "+ String.valueOf(count));
		while (!resC.isAfterLast()) {
			if (i > 0)
				fFilter += " OR ";
			fFilter += ContactsContract.Contacts.LOOKUP_KEY + " = ?";
			fParams[i++] = resC.getString(resC.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));			
			resC.moveToNext();
		}
		fFilter += ")";
	}
	
	public String getFilter()
	{
		return fFilter;		
	}
	
	public String[] getFilterParams() {
		return fParams;
	}
	
}
