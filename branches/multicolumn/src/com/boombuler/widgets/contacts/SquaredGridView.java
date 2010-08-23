package com.boombuler.widgets.contacts;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.LinearLayout;

public class SquaredGridView extends GridView {
	private int mMyColumns;
	public SquaredGridView(Context context) {
		super(context);
	}
	public SquaredGridView(Context context, AttributeSet attrs) {
		this(context, attrs,android.R.attr.gridViewStyle);
	}
	public SquaredGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected boolean addViewInLayout(View child, int index,
			android.view.ViewGroup.LayoutParams params,
			boolean preventRequestLayout) {
        if(getMeasuredWidth()>0 && this.mMyColumns>0){
        	int width=getMeasuredWidth()/this.mMyColumns;
        	View imageView=child.findViewById(R.id.photo);
        	if(imageView!=null)
        		imageView.setLayoutParams(new LinearLayout.LayoutParams(width, width));
        }
		return super.addViewInLayout(child, index, params, preventRequestLayout);
	}
	
	@Override
	public void setNumColumns(int numColumns) {
		this.mMyColumns=numColumns;
		super.setNumColumns(numColumns);
	}
}
