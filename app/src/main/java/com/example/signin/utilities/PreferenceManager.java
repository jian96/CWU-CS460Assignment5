package com.example.signin.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;

    /**
     * Binds the app's SharedPreference to sharedPreferences
     * @param context The Context object, in this case it is the app itself
     */
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Used to put a boolean value into the Preference object
     * @param key The value key
     * @param value The boolean value itself
     */
    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Used to retrieve a boolean value from a key
     * @param key The boolean value key
     * @return The boolean value
     */
    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    /**
     * Used to put a string value into the Preference object
     * @param key The String value key
     * @param value The string value itself
     */
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
    /**
     * Used to retrieve a String value from a key
     * @param key The String value key
     * @return The String value
     */
    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    /**
     * Clears the SharedPreference object of all key-value pairs
     */
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

}
