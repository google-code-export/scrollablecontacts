package com.boombuler.widgets.contacts;

import java.util.ArrayList;
import java.util.List;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import com.boombuler.widgets.contacts.sizes.ContactWidget_1_2;
import com.boombuler.widgets.contacts.sizes.ContactWidget_1_3;
import com.boombuler.widgets.contacts.sizes.ContactWidget_1_4;
import com.boombuler.widgets.contacts.sizes.ContactWidget_2_2;
import com.boombuler.widgets.contacts.sizes.ContactWidget_2_3;
import com.boombuler.widgets.contacts.sizes.ContactWidget_2_4;
import com.boombuler.widgets.contacts.sizes.ContactWidget_3_2;
import com.boombuler.widgets.contacts.sizes.ContactWidget_3_3;
import com.boombuler.widgets.contacts.sizes.ContactWidget_3_4;
import com.boombuler.widgets.contacts.sizes.ContactWidget_4_2;
import com.boombuler.widgets.contacts.sizes.ContactWidget_4_3;
import com.boombuler.widgets.contacts.sizes.ContactWidget_4_4;

public class Preferences {
	public static final long GROUP_FACEBOOK = -1;
	public static final long GROUP_ALLCONTACTS = 0;
	public static final long GROUP_STARRED = -2;

	public static final int VIRTUAL_GROUP_COUNT = 3;

	public static final int NAME_DISPLAY_NAME = 0;
	public static final int NAME_GIVEN_NAME = 1;
	public static final int NAME_FAMILY_NAME = 2;

	public static final String SPAN_X = "SpanX-%d";

    public static final String GROUP_ID = "GroupId-%d";
    public static final String DISPLAY_LABEL = "DisplayLabel-%d";
    public static final String SHOW_NAME = "ShowName-%d";
    public static final String NAME_KIND = "NameKind-%d";
    public static final String BACKGROUND_ALPHA = "BackgroundAlpha-%d";

    public static final String COLUMN_COUNT = "CoulumnCount-%d";

    public static final String ON_CLICK = "onClick-%d";
    public static final int CLICK_QCB = 0;
    public static final int CLICK_DIAL = 1;
    public static final int CLICK_SHWCONTACT = 2;
    public static final int CLICK_SMS = 3;

    public static final String BGIMAGE = "BGImage-%d";
    public static final int BG_BLACK = 0;
    public static final int BG_WHITE = 1;
    public static final int BG_TRANS = 2;

    public static String get(String aPref, int aAppWidgetId) {
    	return String.format(aPref, aAppWidgetId);
    }

    public static long getGroupId(Context context, int aAppWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Long.parseLong(prefs.getString(Preferences.get(Preferences.GROUP_ID, aAppWidgetId), "0"));
    }

    public static String getDisplayLabel(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(Preferences.get(Preferences.DISPLAY_LABEL, aAppWidgetId), "");
    }

    public static int getBGImage(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Preferences.get(Preferences.BGIMAGE, aAppWidgetId), "0"));
    }

    public static int getNameKind(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Preferences.get(Preferences.NAME_KIND, aAppWidgetId), "0"));
    }

    public static boolean getShowName(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(Preferences.get(Preferences.SHOW_NAME, aAppWidgetId), true);
    }

    public static int getColumnCount(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(Preferences.get(Preferences.COLUMN_COUNT, aAppWidgetId), 1);
    }

    public static int getBackgroundAlpha(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(Preferences.get(Preferences.BACKGROUND_ALPHA, aAppWidgetId), 255);
    }

    public static int getSpanX(Context context, int aAppWidgetId, int defaultValue) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getInt(Preferences.get(Preferences.SPAN_X, aAppWidgetId), defaultValue);
    }

    public static int getOnClickAction(Context context, int aAppWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Preferences.get(Preferences.ON_CLICK, aAppWidgetId), String.valueOf(CLICK_QCB)));
    }

    public static void setSpanX(Context context, int aAppWidgetId, int value) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		edit.putInt(Preferences.get(SPAN_X, aAppWidgetId), value);
		edit.commit();
    }

    public static void DropSettings(Context context, int[] appWidgetIds) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		for(int appWId : appWidgetIds) {
			edit.remove(Preferences.get(Preferences.GROUP_ID, appWId));
			edit.remove(Preferences.get(Preferences.BGIMAGE, appWId));
			edit.remove(Preferences.get(Preferences.SHOW_NAME, appWId));
			edit.remove(Preferences.get(Preferences.DISPLAY_LABEL, appWId));
			edit.remove(Preferences.get(Preferences.COLUMN_COUNT, appWId));
			edit.remove(Preferences.get(Preferences.NAME_KIND, appWId));
			edit.remove(Preferences.get(Preferences.BACKGROUND_ALPHA, appWId));
			edit.remove(Preferences.get(Preferences.SPAN_X, appWId));
			edit.remove(Preferences.get(Preferences.ON_CLICK, appWId));
		}
		edit.commit();
    }

    public static int[] getAllWidgetIds(Context context) {
    	AppWidgetManager awm = AppWidgetManager.getInstance(context);
    	List<int[]> result = new ArrayList<int[]>();

    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_1_2.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_1_3.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_1_4.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_2_2.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_2_3.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_2_4.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_3_2.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_3_3.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_3_4.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_4_2.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_4_3.class)));
    	result.add(awm.getAppWidgetIds(new ComponentName(context, ContactWidget_4_4.class)));

    	int i = 0;
    	for(int[] arr : result)
    	  i += arr.length;

    	int[] res = new int[i];
    	i = 0;
    	for (int[] arr : result) {
    		for (int id : arr) {
    			res[i++] = id;
    		}
    	}

    	return res;
    }
}
