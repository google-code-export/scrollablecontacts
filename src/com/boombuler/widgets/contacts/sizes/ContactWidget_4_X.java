package com.boombuler.widgets.contacts.sizes;

import com.boombuler.widgets.contacts.ContactWidget;

public class ContactWidget_4_X extends ContactWidget {

	@Override
	protected int getWidth(boolean horizontal) {
		if (horizontal)
			return 424;
		else
			return 320;
	}
}
