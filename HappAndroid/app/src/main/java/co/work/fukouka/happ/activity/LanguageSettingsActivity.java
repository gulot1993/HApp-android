package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.ConfigurationPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.utils.SessionManager;
import io.fabric.sdk.android.Fabric;

public class LanguageSettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_language) TextView tvLanguage;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.tv_current_setting) TextView tvCurrentSetting;

    private ConfigurationPresenter mPresenter;
    private String mLanguage;
    private SessionManager mSession;
    private CustomLoadResource mRes;
    private HappHelper mHelper;

    private boolean refreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_settings);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);

        mPresenter = new ConfigurationPresenter(this);
        mSession = new SessionManager(getApplicationContext());
        mHelper = new HappHelper(this);

        getLanguage();

        refreshed = getIntent().getBooleanExtra("refreshed", false);
    }

    @OnClick({R.id.cl_change_language, R.id.btn_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cl_change_language:
                startActivity(new Intent(this, ChangeLanguageActivity.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.btn_save:
                changeLanguage(mLanguage);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (refreshed) {
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.putExtra("position", 4);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        }
    }

    /**
     * Get the selected language of the user
     **/
    private void getLanguage() {
        mLanguage = getIntent().getStringExtra("language");
        if (mLanguage != null) {
            tvLanguage.setText(mLanguage);
        } else {
            //get previously selected language
            HashMap<String, String> language = mSession.getLanguage();
            switch (language.get(SessionManager.LANGUAGE)) {
                case "jp":
                    tvLanguage.setText(mHelper.japanese());
                    break;
                case "en":
                    tvLanguage.setText(mHelper.english());
                    break;
            }
        }
    }

    private void changeLanguage(String language) {
        if (language != null) {
            switch (language) {
                case "日本語":
                    mPresenter.changeLanguage("jp");
                    refreshActivity();
                    break;
                case "English":
                    mPresenter.changeLanguage("en");
                    refreshActivity();
                    break;
            }
        }
    }

    private void refreshActivity() {
        Intent intent = new Intent(this, LanguageSettingsActivity.class);
        intent.putExtra("refreshed", true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    }

    private void getSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String langSettings = systemValue.getValue("title_language_settings");
        String save = systemValue.getValue("button_save");
        String currentSetting = systemValue.getValue("current_settings");

        helper.setText(tbTitle, langSettings);
        helper.setText(tvCurrentSetting, currentSetting);
        helper.setButtonText(btnSave, save);
    }
}
