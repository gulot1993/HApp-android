package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.ConfigurationPresenter;
import co.work.fukouka.happ.utils.SessionManager;
import io.fabric.sdk.android.Fabric;

public class LangConfigActivity extends AppCompatActivity implements
        CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.sc_english) SwitchCompat scEnglish;
    @BindView(R.id.sc_japanese) SwitchCompat scJapanese;
    @BindView(R.id.btn_continue) Button btnContinue;
    @BindView(R.id.tv_english) TextView tvEnglish;
    @BindView(R.id.tv_japanese) TextView tvJapanese;
    @BindView(R.id.tb_title) TextView tbTitle;

    private SessionManager mSession;
    private ConfigurationPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Debug here 2");
        setContentView(R.layout.activity_lang_config);
        ButterKnife.bind(this);
        assignSystemValues();
        Fabric.with(this, new Crashlytics());

        mPresenter = new ConfigurationPresenter(this);
        mSession = new SessionManager(getApplicationContext());

        String lang = mPresenter.getLanguage();
        if (lang != null && lang.equals("jp")) {
            scJapanese.setChecked(true);
        } else {
            scEnglish.setChecked(true);
        }
        System.out.println("Debug here 4");

        if (!mSession.isFirstTimeLoad()) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        scEnglish.setOnCheckedChangeListener(this);
        scJapanese.setOnCheckedChangeListener(this);

        System.out.println("Debug here 5");

    }

    @OnClick(R.id.btn_continue)
    public void onClick(View view) {
        String selectedLanguage = getSelectedLanguage();
        mPresenter.changeLanguage(selectedLanguage);

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private String getSelectedLanguage() {
        String selectedLanguage = null;
        if (scEnglish.isChecked()) {
            selectedLanguage = "en";
        } else {
            selectedLanguage = "jp";
        }

        return selectedLanguage;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.sc_english:
                if (b) {scJapanese.setChecked(false);} else {scJapanese.setChecked(true);}
                break;
            case R.id.sc_japanese:
                if (b) {scEnglish.setChecked(false);} else {scEnglish.setChecked(true);}
                break;
        }
    }

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String chooseLang = systemValue.getValue("title_choose_lang");
        String _continue = systemValue.getValue("button_continue");
        String english = systemValue.getValue("label_en");
        String japanese = systemValue.getValue("label_ja");

        helper.setText(tbTitle, chooseLang);
        helper.setButtonText(btnContinue, _continue);
        helper.setText(tvEnglish, english);
        helper.setText(tvJapanese, japanese);
    }


}
