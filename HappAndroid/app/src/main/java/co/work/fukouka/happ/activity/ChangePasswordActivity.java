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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GenericTextWatcher;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.ChangePassPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.view.ChangePassView;

public class ChangePasswordActivity extends AppCompatActivity implements ChangePassView{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_current_pass) EditText etCurrentPass;
    @BindView(R.id.et_new_pass) EditText etNewPass;
    @BindView(R.id.et_re_pass) EditText etRePass;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.tv_current_pass) TextView tvCurrentPass;
    @BindView(R.id.tv_new_pass) TextView tvNewPass;
    @BindView(R.id.tv_re_pass) TextView tvRepass;

    private ChangePassPresenter mPresenter;
    private CustomLoadResource mRes;
    private HappHelper helper;
    private GetSystemValue systemValue;

    private String newPassword;
    private String currentPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ButterKnife.bind(this);

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);
        helper = new HappHelper(this);
        systemValue = new GetSystemValue(this);

        getSystemValues();

        mPresenter = new ChangePassPresenter(this, this);

        etCurrentPass.addTextChangedListener(new GenericTextWatcher(etCurrentPass));
        etNewPass.addTextChangedListener(new GenericTextWatcher(etNewPass));
        etRePass.addTextChangedListener(new GenericTextWatcher(etRePass));
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
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @OnClick({R.id.btn_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                currentPass = etCurrentPass.getText().toString().trim();
                newPassword = etNewPass.getText().toString().trim();
                String rePassword = etRePass.getText().toString().trim();

                if (checkFieldIfFilled(currentPass, newPassword, rePassword)) {
                    if (checkPassIfMatch(newPassword, rePassword)) {
                        mPresenter.checkCredential(currentPass);
                    } else {
                        helper.throwToastMessage(systemValue.getValue("mess_password_not_match"),
                                getString(R.string.password_mismatch));
                    }
                } else {
                    helper.throwToastMessage(systemValue.getValue("mess_fill_missing_field"),
                            getString(R.string.missing_field));
                }

                break;
        }
    }

    @Override
    public void isAuthenticated() {
        if (!currentPass.equals(newPassword)) {
            mPresenter.changeFbPassword(currentPass, newPassword);
            mRes.showProgressDialog(getString(R.string.updating_password), false);
        } else {
            helper.throwToastMessage(systemValue.getValue("mess_same_password"),
                    getString(R.string.same_password_entered));
        }
    }

    @Override
    public void onUpdateSuccess() {
        mRes.hideProgressDialog();
        clearEditText();
        helper.throwToastMessage(systemValue.getValue("mess_password_updated"),
                getString(R.string.password_updated));
    }

    @Override
    public void onUpdateFailed() {
        mRes.hideProgressDialog();
        Toast.makeText(this, getString(R.string.password_not_updated), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFbUpdateSuccess() {
        mPresenter.changePassword(newPassword);
    }

    @Override
    public void onFbUpdateFailed(String error) {
        mRes.hideProgressDialog();
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    private boolean checkFieldIfFilled(String currentPass, String newPass, String rePass) {
        boolean filled;

        if (!TextUtils.isEmpty(currentPass) && !TextUtils.isEmpty(newPass)
                && !TextUtils.isEmpty(rePass)) {
            filled = true;
        } else {
            filled = false;
        }

        return filled;
    }

    private boolean checkPassIfMatch(String pass, String newPass) {
        boolean match;

        if (pass.equals(newPass)) {
            match = true;
        } else {
            match = false;
        }

        return match;
    }

    private void clearEditText() {
        etCurrentPass.setText(null);
        etNewPass.setText(null);
        etRePass.setText(null);
    }

    private void getSystemValues() {
        String changePass = systemValue.getValue("label_change-password");
        String currentPass = systemValue.getValue("label_current_password");
        String newPass = systemValue.getValue("label_new_password");
        String rePass = systemValue.getValue("label_re-enter_password");
        String save = systemValue.getValue("button_save");
        String chars = systemValue.getValue("holder_6_or_more_char");

        helper.setText(tbTitle, changePass);
        helper.setButtonText(btnSave, save);
        helper.setText(tvCurrentPass, currentPass);
        helper.setText(tvNewPass, newPass);
        helper.setText(tvRepass, rePass);
        helper.setEditTextHint(etCurrentPass, chars);
        helper.setEditTextHint(etNewPass, chars);
        helper.setEditTextHint(etRePass, rePass);
    }
}
