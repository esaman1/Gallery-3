package com.gallery.photosgallery.videogallery.bestgallery.Prefrences;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class SharedPrefrences {

    public static final String MyPREFERENCES = "Gallery";
    public static String VIEW_TYPE = "VIEW_TYPE";
    public static String SORTING_TYPE = "SORTING_TYPE";
    public static String SORTING_TYPE2 = "SORTING_TYPE2";
    public static String Column = "Column";
    public static String PROTECT_ = "PROTECT_";
    public static String PROTECT_FILE = "Protect_file";

    public static void setViewType(Context c1,String view_type) {
        SharedPreferences prefs= c1.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putString(VIEW_TYPE, view_type);
        edit.commit();
    }

    public static String getViewType(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String set = sharedpreferences.getString(VIEW_TYPE, "Grid");
        return set;
    }

    public static void setSortingType(Context c1,String view_type) {
        SharedPreferences prefs= c1.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putString(SORTING_TYPE, view_type);
        edit.commit();
    }

    public static String getSortingType(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String set = sharedpreferences.getString(SORTING_TYPE, SORTING_TYPE);
        return set;
    }

    public static void setSortingType1(Context c1,String view_type) {
        SharedPreferences prefs= c1.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putString(SORTING_TYPE2, view_type);
        edit.commit();
    }

    public static String getSortingType1(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String set = sharedpreferences.getString(SORTING_TYPE2, "");
        return set;
    }

    public static void setColumn(Context c1,int column) {
        SharedPreferences prefs= c1.getSharedPreferences(MyPREFERENCES,Context.MODE_PRIVATE);
        SharedPreferences.Editor edit=prefs.edit();
        edit.putInt(Column, column);
        edit.commit();
    }

    public static int getColumn(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        int set = sharedpreferences.getInt(Column, 2);
        return set;
    }

    public static String getSharedPreferenceUri(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String ans = sharedpreferences.getString(PROTECT_, "");
        return ans;
    }

    public static void setSharedPreferenceUri(Context c1, Uri hideFileList) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PROTECT_, String.valueOf(hideFileList));
        editor.apply();
    }

    public static String getSharedPreference(Context c1) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String ans = sharedpreferences.getString(PROTECT_FILE, "");
        return ans;
    }

    public static void setSharedPreference(Context c1, String hideFileList) {
        SharedPreferences sharedpreferences = c1.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(PROTECT_FILE, String.valueOf(hideFileList));
        editor.apply();
    }
}
