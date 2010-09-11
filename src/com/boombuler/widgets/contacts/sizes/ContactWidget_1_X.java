package com.boombuler.widgets.contacts.sizes;

import com.boombuler.widgets.contacts.ContactWidget;

public class ContactWidget_1_X extends ContactWidget {
		
	@Override
	protected int getWidth(boolean horizontal) {
		if (horizontal)
			return 106;
		else
			return 80;
	}
}
