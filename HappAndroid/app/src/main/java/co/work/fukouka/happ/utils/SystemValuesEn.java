package co.work.fukouka.happ.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

public class SystemValuesEn {
    private static final String PREF_NAME = "SystemValuesEn";

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private Context mContext;
    private int PRIVATE_MODE = 0;

    public SystemValuesEn(Context context) {
        this.mContext = context;
        mPref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPref.edit();
    }

    public void saveSystemValue(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    public String getSystemValue(String key) {
        String value = null;

        HashMap<String, String> hashMap = new HashMap<String, String>();
        hashMap.put(key, mPref.getString(key, null));

        value = hashMap.get(key);

        return value;
    }
}
