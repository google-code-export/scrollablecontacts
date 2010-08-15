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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

public class AdressFilter {

	private static final String TAG = "boombuler.AdressFilter";
	
	private String fFilter;
	private String[] fParams;
	private NameResolver fNameResolver = null;
	private int fNameKind;
	private long fGroupId;
	
	public AdressFilter(Context context, long aGroupId, int NameKind) {
		fNameKind = NameKind;
		fGroupId = aGroupId;
		fFilter = "";
		if (aGroupId == Preferences.GROUP_ALLCONTACTS) {
			fParams = null;
			fNameResolver = new NameResolver(context);
			return;
		}
		if (aGroupId == Preferences.GROUP_STARRED) {
			fParams = null;
			fFilter += " AND (STARRED = '1')";
			fNameResolver = new NameResolver(context);
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
		if (resC == null)
			return;
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
		fNameResolver = new NameResolver(context);
	}
	
	public String getFilter()
	{
		return ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'" + fFilter;		
	}
	
	public String[] getFilterParams() {
		return fParams;
	}
	
	public NameResolver getNameResolver() {
		return fNameResolver;
	}
	
	public class NameResolver {
		private Cursor fCursor = null;
		
		private NameResolver(Context context) {
			if (AdressFilter.this.fNameKind == Preferences.NAME_DISPLAY_NAME)
				return;
				
			
			ContentResolver resolver = context.getContentResolver();
			
			String name = ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME;
			if (AdressFilter.this.fNameKind == Preferences.NAME_FAMILY_NAME)
				name = ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME;
			
			String filter = "";
			String[] params = null;
			if (AdressFilter.this.fGroupId > 0) {
				filter = AdressFilter.this.fFilter;
				params = AdressFilter.this.fParams;
			}
			
			filter = ContactsContract.CommonDataKinds.StructuredName.MIMETYPE + " = '"+ ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE+"' " + filter;
			
			fCursor = resolver.query(ContactsContract.Data.CONTENT_URI, 
							 new String[] { ContactsContract.CommonDataKinds.StructuredName.LOOKUP_KEY, name},
							 filter, params, null);
			
		}
	
		public void close() {
			if (fCursor != null)
				fCursor.close();
			fCursor = null;
		}
	
		public String updateName(String lookupKey, String DisplayName) {
			if (AdressFilter.this.fNameKind == Preferences.NAME_DISPLAY_NAME)
				return DisplayName;
			else
			{
				fCursor.moveToFirst();
				while(!fCursor.isAfterLast()) {
					if (fCursor.getString(0).equals(lookupKey))
					{
						if (fCursor.isNull(1))
							return DisplayName;
						return fCursor.getString(1);
					}
					
					fCursor.moveToNext();
				}
				Log.d(TAG, "will return displayname:" + DisplayName);
				return DisplayName;
			}
		}
	}
	
}
