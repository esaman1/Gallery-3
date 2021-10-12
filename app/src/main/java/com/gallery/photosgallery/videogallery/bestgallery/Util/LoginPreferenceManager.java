package com.gallery.photosgallery.videogallery.bestgallery.Util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by Umiya Mataji on 5/24/2016.
 */
public class LoginPreferenceManager
{
    public Context context;

    public LoginPreferenceManager(Context context) {
        this.context = context;
    }





    public static void SaveIntgData(Context context, String Key, int data)
    {
        SharedPreferences preferences = context.getSharedPreferences(Utils.LOGIN_PREFERENCE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(Key, data);
        editor.commit();

    }

    public static int GetIntData(Context context, String Key)
    {
        SharedPreferences preferences = context.getSharedPreferences(Utils.LOGIN_PREFERENCE, Context.MODE_PRIVATE);
        return preferences.getInt(Key, -1);
    }


}
