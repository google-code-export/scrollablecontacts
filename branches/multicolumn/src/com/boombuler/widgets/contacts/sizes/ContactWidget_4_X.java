package com.boombuler.widgets.contacts.sizes;

import android.content.Context;

import com.boombuler.widgets.contacts.ContactWidget;
import com.boombuler.widgets.contacts.R;

public class ContactWidget_4_X extends ContactWidget {

	
	@Override
	protected int getListViewLayoutId(Context aContext, int aAppWidgetId) {
		return R.layout.gridview;
	}

}
