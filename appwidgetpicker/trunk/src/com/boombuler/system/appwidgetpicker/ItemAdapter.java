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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ItemAdapter extends ArrayAdapter<SubItem> {
	private ArrayList<SubItem> items;
	private Context fContext;
	
	public ItemAdapter(Context context, int textViewResourceId, ArrayList<SubItem> items) {
        super(context, textViewResourceId, items);
        this.items = items;
        fContext = context;
	}
		
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater)fContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.appwidgetpicker, null);
        }
        SubItem o = items.get(position);
        v.setTag(o);
        if (o != null) {
            TextView tv = (TextView) v.findViewById(R.id.appwidgetpicker_textview);
            ImageView iv = (ImageView) v.findViewById(R.id.appwidgetpicker_imageview);
            if (tv != null) {
                  tv.setText(o.getName());                            
            }
            if(iv != null){
            	iv.setImageDrawable(o.getImage());
            }
        }
        return v;
    }
}
