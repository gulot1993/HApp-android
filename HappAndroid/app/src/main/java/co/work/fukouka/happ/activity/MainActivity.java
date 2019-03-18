package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.utils.SessionManager;
import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.btn_change_lang) Button btnChangeLang;
    @BindView(R.id.btn_register) Button btnRegister;
    @BindView(R.id.btn_login) Button btnLogin;

    private SessionManager mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        assignSystemValues();

        mSession = new SessionManager(getApplicationContext());

        if (!mSession.isFirstTimeLoad()) {
            btnChangeLang.setVisibility(View.GONE);
        } else {
            btnChangeLang.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.btn_register, R.id.btn_login, R.id.btn_change_lang})
    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                startActivity(new Intent(this, EulaActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
            case R.id.btn_login:
                startActivity(new Intent(this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
            case R.id.btn_change_lang:
                startActivity(new Intent(this, LangConfigActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mSession.isFirstTimeLoad()) {
            Intent intent = new Intent(this, LangConfigActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        } else {
            super.onBackPressed();
        }
    }

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String newMemberRegist = systemValue.getValue("button_regist");
        String login = systemValue.getValue("title_login");
        String changeLang = systemValue.getValue("sys_change_lang");

        SpannableString _changeLang = null;
        if (changeLang != null) {
            _changeLang = new SpannableString(changeLang);
            _changeLang.setSpan(new UnderlineSpan(), 0, _changeLang.length(), 0);
        }

        helper.setButtonText(btnRegister, newMemberRegist);
        helper.setButtonText(btnLogin, login);

        btnChangeLang.setText(_changeLang != null ? _changeLang : getString(R.string.change_lang));
    }
}
