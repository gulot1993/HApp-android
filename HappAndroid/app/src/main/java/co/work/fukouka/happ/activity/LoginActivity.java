package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

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

public class LoginActivity extends AppCompatActivity implements AuthenticationView{
    private static final String TAG = "LoginActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.et_password) EditText etPassword;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.tv_email) TextView tvEmail;
    @BindView(R.id.tv_password) TextView tvPassword;
    @BindView(R.id.btn_forgot_pass) Button btnForgotPass;
    @BindView(R.id.btn_login) Button btnLogin;

    private AuthenticationPresenter mPresenter;
    private SessionManager mSession;
    private CustomLoadResource mRes;
    private HappUtils happUtils;
    private HappHelper mHelper;
    private GetSystemValue systemValue;

    private String mResponse;
    private String mEmail;
    private String mPassword;

    private String registToken;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Fabric.with(this, new Crashlytics());
        ButterKnife.bind(this);

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);

        mPresenter = new AuthenticationPresenter(this, this);
        mSession = new SessionManager(getApplicationContext());
        happUtils = new HappUtils(this);
        mHelper = new HappHelper(this);
        systemValue = new GetSystemValue(this);

        assignSystemValues();

        etEmail.addTextChangedListener(new GenericTextWatcher(etEmail));
        etPassword.addTextChangedListener(new GenericTextWatcher(etPassword));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseAuth.getInstance().signOut();
        }

        HashMap<String, String> id = mSession.getUserId();
        if (id.get(SessionManager.KEY_USER_ID) != null) {

        }

        //get registration token
        registToken = FirebaseInstanceId.getInstance().getToken();

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

    @OnClick({R.id.btn_login, R.id.btn_forgot_pass})
    public void onButtonClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                mEmail = etEmail.getText().toString().trim();
                mPassword = etPassword.getText().toString().trim();

                if (happUtils.isNetworkAvailable()) {
                    if (!TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mPassword)) {
                        new HappHelper(this).hideKeyboard(this);
                        mPresenter.login(mEmail, mPassword, registToken);
                        mRes.showProgressDialog(systemValue.getValue("mess_authenticating"), true);
                    } else {
                        mHelper.throwToastMessage(systemValue.getValue("mess_fill_missing_field"),
                                getString(R.string.missing_field));
                        //Toast.makeText(this, R.string.missing_field, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mHelper.throwToastMessage(systemValue.getValue("mess_no_net_connection"),
                            getString(R.string.no_net_connection));
                }

                break;
            case R.id.btn_forgot_pass:
                startActivity(new Intent(this, ResetPasswordActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
        }

    }

    @Override
    public void onSuccess(String response) {
        mResponse = response;
        mPresenter.saveUserSkills(response);
        mPresenter.fbLogin(mEmail, mPassword);
    }

    @Override
    public void onFbLoginSuccess() {
        String email = etEmail.getText().toString();
        mSession.createLoginSession(mResponse);
        mSession.saveUserEmail(email);
        mSession.flagFirstTimeLoad();

        mPresenter.getUserInfo(mResponse);
        mRes.hideProgressDialog();
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    }


    @Override
    public void onFailed(String response) {
        mRes.hideProgressDialog();
        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String login = systemValue.getValue("title_login");
        String emailAdd = systemValue.getValue("label_e-mail_address");
        String password = systemValue.getValue("label_Password");
        String forgotPass = systemValue.getValue("label_forgot_password");
        String _chars = systemValue.getValue("holder_6_or_more_char");
        String dummyEmail = systemValue.getValue("holder_ex.@xx.com");
        
        SpannableString _forgotPass = null;
        if (forgotPass != null) {
            _forgotPass = new SpannableString(forgotPass);
            _forgotPass.setSpan(new UnderlineSpan(), 0, _forgotPass.length(), 0);
        }

        helper.setButtonText(btnLogin, login);
        helper.setText(tvEmail, emailAdd);
        helper.setText(tvPassword, password);
        helper.setEditTextHint(etPassword, _chars);
        helper.setEditTextHint(etEmail, dummyEmail);
        helper.setText(tbTitle, login);

        if (forgotPass != null) {
            btnForgotPass.setText(_forgotPass);
        }
    }
}
