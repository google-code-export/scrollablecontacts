package com.boombuler.widgets.contacts.sizes;

import android.content.Context;

import com.boombuler.widgets.contacts.ContactWidget;
import com.boombuler.widgets.contacts.Preferences;
import com.boombuler.widgets.contacts.R;

public class ContactWidget_4_X extends ContactWidget {

	@Override
	protected int getListEntryLayoutId(Context aContext, int aAppWidgetId) {
		if (Preferences.getShowName(aContext, aAppWidgetId)) {
			if (Preferences.getBGImage(aContext, aAppWidgetId) == Preferences.BG_BLACK)
				return R.layout.entry_small_black;
			else
				return R.layout.entry_small_white;
		}		
		return R.layout.entry_small_noname;
	}
	
	@Override
	protected int getListViewLayoutId(Context aContext, int aAppWidgetId) {
		return R.layout.gridview;
	}

}
