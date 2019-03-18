package co.work.fukouka.happ.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class SystemValuesJp {
    private static final String PREF_NAME = "SystemValuesJp";

    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;
    private Context mContext;
    private int PRIVATE_MODE = 0;

    public SystemValuesJp(Context context) {
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

    public void assignValue(Object object, Method method, String key, String value) {
        Object[] parameters = new Object[2];
        parameters[0] = key;
        parameters[1] = value;
        try {
            method.invoke(object, parameters);
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
