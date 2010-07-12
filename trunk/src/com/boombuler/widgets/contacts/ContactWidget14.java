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
import android.content.Intent;

public class ContactWidget14 extends ContactWidget{

	@Override
	public int getListEntryLayoutId() {
		return R.layout.contactlistentry_noname;
	}
	
	@Override
	public void putMapping(Intent intent) {
		if (intent == null)
			return;

		final int NB_ITEMS_TO_FILL = 1;

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

		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_IDS, layoutIds);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_TYPES, viewTypes);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_VIEW_CLICKABLE, clickable);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_CURSOR_INDICES, cursorIndices);
		intent.putExtra(LauncherIntent.Extra.Scroll.Mapping.EXTRA_DEFAULT_RESOURCES, defResources);
	}
}
