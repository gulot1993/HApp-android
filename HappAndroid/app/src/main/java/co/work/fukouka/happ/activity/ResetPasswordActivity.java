package co.work.fukouka.happ.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GenericTextWatcher;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.AuthenticationPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.view.AuthenticationView;
import io.fabric.sdk.android.Fabric;

public class ResetPasswordActivity extends AppCompatActivity implements AuthenticationView{
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.tv_email) TextView tvEmail;
    @BindView(R.id.tv_reset_detail) TextView tvResetDetail;
    @BindView(R.id.btn_reset_pass) Button btnResetPass;

    private AuthenticationPresenter mPresenter;
    private CustomLoadResource mRes;
    private HappHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);
        mHelper = new HappHelper(this);
        mPresenter = new AuthenticationPresenter(this, this);

        etEmail.addTextChangedListener(new GenericTextWatcher(etEmail));
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

    @OnClick(R.id.btn_reset_pass)
    public void onButtonClick(View view) {
        String email = etEmail.getText().toString().trim();

        if (!TextUtils.isEmpty(email)) {
            mPresenter.resetPassword(email);
        } else {
            Toast.makeText(this, mHelper.fillOutFields(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSuccess(String response) {
        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFbLoginSuccess() {}

    @Override
    public void onFailed(String response) {
        Toast.makeText(this, response, Toast.LENGTH_SHORT).show();
    }

    private void getSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String sendResetEmail = systemValue.getValue("title_resetting_email");
        String email = systemValue.getValue("label_email_address");
        String resetDetail = systemValue.getValue("text_change_pass");
        String example = systemValue.getValue("holder_example@xxx.com");
        String reconfig = systemValue.getValue("subtitle_reconfiguration_URL");

        helper.setText(tbTitle, sendResetEmail);
        helper.setText(tvEmail, email);
        helper.setText(tvResetDetail, resetDetail);
        helper.setEditTextHint(etEmail, example);
        helper.setButtonText(btnResetPass, reconfig);
    }
}
