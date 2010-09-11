package com.boombuler.widgets.contacts.sizes;

import com.boombuler.widgets.contacts.ContactWidget;

public class ContactWidget_2_X extends ContactWidget{
		
	@Override
	protected int getWidth(boolean horizontal) {
		if (horizontal)
			return 212;
		else
			return 160;
	}
}
