package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.HashMap;
import java.util.Locale;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.presenter.ConfigurationPresenter;
import co.work.fukouka.happ.utils.HappUtils;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ConfigurationView;
import io.fabric.sdk.android.Fabric;

public class StartupActivity extends AppCompatActivity implements ConfigurationView {

    private static final String TAG = "StartupActivity";
    private SessionManager mSession;
    private ConfigurationPresenter mPresenter;
    private HappUtils utils;

    private HappHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);
        Fabric.with(this, new Crashlytics());

        //Instances
        mPresenter = new ConfigurationPresenter(this, this);
        mSession = new SessionManager(getApplicationContext());
        utils = new HappUtils(this);
        mHelper = new HappHelper(this);

        setupUser();

    }

    private void setupUser() {
        if (utils.isNetworkAvailable()) {
            //get system values
            mPresenter.getSystemSkills();
            mPresenter.saveUserSkills();
            mPresenter.getSystemValues();
        } else {
            Toast.makeText(this, mHelper.noNetConnection(), Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    int pid = android.os.Process.myPid();
                    android.os.Process.killProcess(pid);
                }
            }, 3000);
        }
        //get language
        HashMap<String, String> lang = mSession.getLanguage();
        String locale = lang.get(SessionManager.LANGUAGE);
        if (locale != null) {
            mPresenter.changeLanguage(locale);
        } else {
            locale = Locale.getDefault().getLanguage();
            switch (locale) {
                case "en":
                    mPresenter.changeLanguage("en");
                    break;
                case "ja":
                    mPresenter.changeLanguage("jp");
                    break;
                default:
                    mPresenter.changeLanguage("en");
                    break;
            }
        }

    }

    @Override
    public void onLoadUserInfo(User user) {}

    @Override
    public void onSuccess() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                System.out.println("Debug here");
                //check user login status
                if (mSession.isLoggedIn()) {
                    System.out.println("Debug here 1");
                    intent = new Intent(StartupActivity.this, DashboardActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    System.out.println("Debug here 2");
                    intent = new Intent(StartupActivity.this, LangConfigActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 1000);
    }

    @Override
    public void onFailed() {
        Intent intent;
        //check user login status
        if (mSession.isLoggedIn()) {
            intent = new Intent(StartupActivity.this, DashboardActivity.class);
            startActivity(intent);
            finish();
        } else {
            intent = new Intent(StartupActivity.this, LangConfigActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }
}
