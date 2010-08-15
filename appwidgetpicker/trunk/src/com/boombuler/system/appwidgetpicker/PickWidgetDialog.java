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
package com.boombuler.system.appwidgetpicker;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class PickWidgetDialog {

	private class ClickListener implements OnItemClickListener {

		AlertDialog fDialog;
		
		public ClickListener(AlertDialog dlg) {
			fDialog = dlg;
		}
		
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			SubItem subItem = (SubItem)arg1.getTag();
			fDialog.dismiss();
			
			PickWidgetDialog.this.showDialog(subItem);
		}
		
	}
	
	private class CancelListener implements OnCancelListener {
		private boolean fCancelOwner;
		
		public CancelListener(boolean cancelOwner) {
			fCancelOwner = cancelOwner;
		}
		
		@Override
		public void onCancel(DialogInterface dialog) {
			if (fCancelOwner) {
				PickWidgetDialog.this.fOwner.setResult(AppWidgetPickerActivity.RESULT_CANCELED);
				PickWidgetDialog.this.fOwner.finish();
			} else {
				PickWidgetDialog.this.showDialog(null);
			}
		}
	}
	
	private AppWidgetPickerActivity fOwner;
	
	public PickWidgetDialog(AppWidgetPickerActivity owner) {
		fOwner = owner;
	}	
	
	public void showDialog(SubItem subItem) {
		if (subItem == null || subItem instanceof Item) {
			AlertDialog.Builder ab = new AlertDialog.Builder(fOwner);
			ListView lv = new ListView(fOwner);
			if (subItem == null) {
				ab.setTitle(fOwner.getString(R.string.widget_picker_title));
				lv.setAdapter(new ItemAdapter(fOwner, 0, fOwner.getItems()));
			}
			else {
				Item itm = (Item)subItem;
				if (itm.getItems().size() == 1) {
					fOwner.finishOk(itm.getItems().get(0));
					return;
				}				
				
				ab.setTitle(subItem.getName());
				lv.setAdapter(new ItemAdapter(fOwner, 0, itm.getItems()));
			}
			
			ab.setView(lv);
			ab.setOnCancelListener(new CancelListener(subItem == null));
			AlertDialog dlg = ab.create();
			lv.setOnItemClickListener(new ClickListener(dlg));
			dlg.show();
		}
		else
			fOwner.finishOk(subItem);
	}
}
