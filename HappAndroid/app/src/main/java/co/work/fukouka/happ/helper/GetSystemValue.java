package co.work.fukouka.happ.helper;

import android.content.Context;

import java.util.HashMap;

import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.utils.SystemValuesEn;
import co.work.fukouka.happ.utils.SystemValuesJp;

public class GetSystemValue {

    private Context mContext;
    private SystemValuesEn mValuesEn;
    private SystemValuesJp mValuesJp;
    private SessionManager mSession;

    public GetSystemValue(Context context) {
        this.mContext = context;
        mValuesEn = new SystemValuesEn(context);
        mValuesJp = new SystemValuesJp(context);
        mSession = new SessionManager(context);
    }

    public String getValue(String key) {
        String value;

        if (getLanguage().equals("jp")) {
            value = mValuesJp.getSystemValue(key);
        } else {
            value = mValuesEn.getSystemValue(key);
        }

        return value;
    }

    private String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }
}
