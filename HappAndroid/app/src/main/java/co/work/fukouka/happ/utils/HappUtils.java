package co.work.fukouka.happ.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;

import co.work.fukouka.happ.interfaces.Utils;


public class HappUtils implements Utils{
    private Context mContext;
    private SessionManager mSession;

    public HappUtils(Context context) {
        this.mContext = context;
        mSession = new SessionManager(context);
    }

    @Override
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @Override
    public String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }
}
