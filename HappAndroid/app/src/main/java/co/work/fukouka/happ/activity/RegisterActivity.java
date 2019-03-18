package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.HashMap;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GenericTextWatcher;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.AuthenticationPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.utils.HappUtils;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.AuthenticationView;
import io.fabric.sdk.android.Fabric;

public class RegisterActivity extends AppCompatActivity implements AuthenticationView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_name) EditText etName;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.et_re_password) EditText etRePassword;
    @BindView(R.id.tv_basic_info) TextView tvBasicInfo;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_email) TextView tvEmail;
    @BindView(R.id.tv_password) TextView tvPassword;
    @BindView(R.id.tv_re_pass) TextView tvRePass;
    @BindView(R.id.tv_skills) TextView tvSkills;
    @BindView(R.id.tv_selected_skills) TextView tvSelectedSkills;
    @BindView(R.id.cl_select_skills) ConstraintLayout clSelectSkills;
    @BindView(R.id.tv_skills_selected) TextView tvSkillSelected;
    @BindView(R.id.tv_selection_skill) TextView tvSelectionSkill;
    @BindView(R.id.btn_register) Button btnRegister;

    private AuthenticationPresenter mAuthPres;
    private CustomLoadResource mRes;
    private HappUtils happUtils;
    private HappHelper mHelper;
    private GetSystemValue systemValue;
    private SessionManager mSession;

    private String mEmail;
    private String mPassword;
    private String mName;
    private String response;
    private String mLang;

    private String skillKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);

        //Instances
        mAuthPres = new AuthenticationPresenter(this, this);
        happUtils = new HappUtils(this);
        mHelper = new HappHelper(this);
        systemValue = new GetSystemValue(this);
        mSession = new SessionManager(this);

        assignSystemValues();
        getPrevDataInput();
        setUp();

        etEmail.addTextChangedListener(new GenericTextWatcher(etEmail));
        etName.addTextChangedListener(new GenericTextWatcher(etName));
        etPassword.addTextChangedListener(new GenericTextWatcher(etPassword));
        etRePassword.addTextChangedListener(new GenericTextWatcher(etRePassword));

        Intent intent = getIntent();
        String skillValues = intent.getStringExtra("skill_values");
        skillKeys = intent.getStringExtra("skill_keys");

        mHelper.setText(tvSelectedSkills, skillValues);

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
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }

    @OnClick({R.id.btn_register, R.id.cl_select_skills})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.btn_register:
                prepareRegisteration();
                break;
            case R.id.cl_select_skills:
                String email = etEmail.getText().toString().trim();
                String name = etName.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String rePassword = etRePassword.getText().toString().trim();
                String lang = mAuthPres.getLanguage() != null ? mAuthPres.getLanguage() : "en";

                Intent intent = new Intent(this, SelectSkillsActivity.class);
                intent.putExtra("skill_keys", skillKeys);
                intent.putExtra("name", name);
                intent.putExtra("email", email);
                intent.putExtra("password", password);
                intent.putExtra("re_pass", rePassword);
                intent.putExtra("from_activity", "Register");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void prepareRegisteration() {
        mEmail = etEmail.getText().toString().trim();
        mName = etName.getText().toString().trim();
        mPassword = etPassword.getText().toString().trim();
        String rePassword = etRePassword.getText().toString().trim();
        mLang = mAuthPres.getLanguage() != null ? mAuthPres.getLanguage() : "en";

        if (happUtils.isNetworkAvailable()) {
            if (!mName.isEmpty() && !mEmail.isEmpty() && !mPassword.isEmpty() && !rePassword.isEmpty()) {
                if (mPassword.equals(rePassword)) {
                    if (mPassword.length() >= 6) {
                        if (skillKeys == null) {
                            skillKeys = "";
                        }
                        mAuthPres.register(mEmail, mName, mPassword, skillKeys, mLang);
                    } else {
                        mHelper.throwToastMessage(systemValue.getValue("mess_password_min_char"),
                                getString(R.string.password_min_char));
                    }
                } else {
                    mHelper.throwToastMessage(systemValue.getValue("mess_password_not_match"),
                            getString(R.string.password_mismatch));
                }

            } else {
                mHelper.throwToastMessage(systemValue.getValue("mess_fill_missing_field"),
                        getString(R.string.missing_field));
            }

        } else {
            mHelper.throwToastMessage(systemValue.getValue("mess_no_net_connection"),
                    getString(R.string.no_net_connection));
        }
    }

    @Override
    public void onSuccess(String userId) {
        this.response = response;
        // register user to firebase
        mHelper.showProgressDialog("", true);
        mAuthPres.fbRegister(userId, mEmail, mPassword, mName, skillKeys, mLang);
    }

    @Override
    public void onFbLoginSuccess() {
        mAuthPres.saveFirebaseIdToWp(mEmail, mPassword);

        clearViews();
        mHelper.hideProgressDialog();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
        finish();
    }

    @Override
    public void onFailed(String response) {
        mHelper.hideProgressDialog();
        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    private void getPrevDataInput() {
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String password = intent.getStringExtra("password");
        String rePass = intent.getStringExtra("re_pass");

        mHelper.setEditText(etName, name);
        mHelper.setEditText(etEmail, email);
        mHelper.setEditText(etPassword, password);
        mHelper.setEditText(etRePassword, rePass);
    }

    private void setUp() {
        //get language
        HashMap<String, String> lang = mSession.getLanguage();
        String locale = lang.get(SessionManager.LANGUAGE);
        if (locale != null) {
            changeLanguage(locale);
        } else {
            locale = Locale.getDefault().getLanguage();
            switch (locale) {
                case "en":
                    changeLanguage("en");
                    break;
                case "ja":
                    changeLanguage("jp");
                    break;
                default:
                    changeLanguage("jp");
                    break;
            }
        }
    }

    private void changeLanguage(String language) {
        Locale locale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(locale);
        res.updateConfiguration(conf, dm);
    }

    private void clearViews() {
        etName.setText(null);
        etEmail.setText(null);
        etPassword.setText(null);
        etRePassword.setText(null);
    }

    private void assignSystemValues() {
        String memRegistration = systemValue.getValue("button_new_member_registration");
        String name = systemValue.getValue("text_name");
        String emailAdd = systemValue.getValue("label_e-mail_address");
        String password = systemValue.getValue("label_Password");
        String rePassword = systemValue.getValue("label_re-enter_password");
        String skills = systemValue.getValue("subtitle_skills");
        String basicInfo = systemValue.getValue("subtitle_basic_information");
        String nameChars = systemValue.getValue("holder_15_more_char");
        String example = systemValue.getValue("holder_ex.@xx.com");
        String passChars = systemValue.getValue("holder_6_or_more_char");
        String reEnterPass = systemValue.getValue("label_re-enter_password");
        String skillSelected = systemValue.getValue("selected_skill");
        String selectionSkill = systemValue.getValue("selection_skill");
        String regNewMember = systemValue.getValue("btn_regist_new_member");

        mHelper.setText(tbTitle, memRegistration);
        mHelper.setText(tvName, name);
        mHelper.setText(tvEmail, emailAdd);
        mHelper.setText(tvPassword, password);
        mHelper.setText(tvRePass, rePassword);
        mHelper.setText(tvBasicInfo, basicInfo);
        mHelper.setText(tvSkills, skills);
        mHelper.setText(tvSkillSelected, skillSelected);
        mHelper.setText(tvSelectionSkill, selectionSkill);
        mHelper.setEditTextHint(etName, nameChars);
        mHelper.setEditTextHint(etEmail, example);
        mHelper.setEditTextHint(etPassword, passChars);
        mHelper.setEditTextHint(etRePassword, reEnterPass);
        mHelper.setButtonText(btnRegister, regNewMember);
    }

}
