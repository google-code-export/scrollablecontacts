package com.boombuler.widgets.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Preferences {
    public static final String GROUP_ID = "GroupId-%d";
    public static final String QUICKCONTACT_SIZE = "QCBarSize-%d";
    public static final String DISPLAY_LABEL = "DisplayLabel-%d";
    public static final String SHOW_NAME = "ShowName-%d";
    
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
    
    public static int getQuickContactSize(Context context, int aAppWidgetId) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Preferences.get(Preferences.QUICKCONTACT_SIZE, aAppWidgetId), "0"));    	
    }
    
    public static String getDisplayLabel(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getString(Preferences.get(Preferences.DISPLAY_LABEL, aAppWidgetId), "");
    }
    
    public static int getBGImage(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(Preferences.get(Preferences.BGIMAGE, aAppWidgetId), "0"));
    }
    
    public static boolean getShowName(Context context, int aAppWidgetId) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(Preferences.get(Preferences.SHOW_NAME, aAppWidgetId), true);    	
    }
    
    public static void DropSettings(Context context, int[] appWidgetIds) {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = prefs.edit();
		for(int appWId : appWidgetIds) {
			edit.remove(Preferences.get(Preferences.GROUP_ID, appWId));
			edit.remove(Preferences.get(Preferences.QUICKCONTACT_SIZE, appWId));
			edit.remove(Preferences.get(Preferences.BGIMAGE, appWId));
			edit.remove(Preferences.get(Preferences.SHOW_NAME, appWId));
			edit.remove(Preferences.get(Preferences.DISPLAY_LABEL, appWId));
		}
		edit.commit();
    }
}
