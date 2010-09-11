package com.boombuler.widgets.contacts.sizes;

import com.boombuler.widgets.contacts.ContactWidget;

public class ContactWidget_3_X extends ContactWidget {
		
	@Override
	protected int getWidth(boolean horizontal) {
		if (horizontal)
			return 318;
		else
			return 240;
	}
}
