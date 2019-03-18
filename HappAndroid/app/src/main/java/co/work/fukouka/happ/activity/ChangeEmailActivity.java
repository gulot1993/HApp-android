package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.presenter.ConfigurationPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ConfigurationView;

public class ChangeEmailActivity extends AppCompatActivity implements ConfigurationView{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_email) EditText etEmail;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.tv_email) TextView tvEmail;
    @BindView(R.id.btn_save) Button btnSave;

    private SessionManager mSession;
    private ConfigurationPresenter mPresenter;
    private CustomLoadResource mRes;
    private String email;
    private HappHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);
        ButterKnife.bind(this);
        getSystemValues();

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);

        mSession = new SessionManager(getApplicationContext());
        mPresenter = new ConfigurationPresenter(this, this);
        mHelper = new HappHelper(this);

        etEmail.addTextChangedListener(new GenericTextWatcher(etEmail));

        /**
         * Get user id
         */
        email = mPresenter.getUserEmail();
        if (email != null) {
            etEmail.setText(email);
        } else {
            String userId = mPresenter.getUserId();
            if (userId != null) {
                mPresenter.getUserInfo(userId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.happ.check_user_session");
        this.registerReceiver(this.userSession, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(this.userSession);
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

    /**
     * Handles action of button save
     */
    @OnClick(R.id.btn_save)
    public void onClick(View view) {
        String userId = mPresenter.getUserId();
        String email = etEmail.getText().toString().trim();
        if (userId != null && !TextUtils.isEmpty(email)) {
            if (!this.email.equals(email)) {
                mRes.showProgressDialog("", true);
                mPresenter.changeEmail(userId, email);
            } else {
                Toast.makeText(this, getString(R.string.same_email), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.missing_field, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onLoadUserInfo(User user) {
        String email = user.getEmail();
        if (email != null) {
            this.email = email;
            etEmail.setText(email);
        }
    }

    @Override
    public void onSuccess() {
        mRes.hideProgressDialog();
        Toast.makeText(this, getString(R.string.email_updated), Toast.LENGTH_SHORT).show();
        email = etEmail.getText().toString().trim();
        mSession.saveUserEmail(email);
    }

    @Override
    public void onFailed() {
        Toast.makeText(this, getString(R.string.failed), Toast.LENGTH_SHORT).show();
    }

    private BroadcastReceiver userSession = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String emailChange = systemValue.getValue("label_e-mail_address_change");
        String email = systemValue.getValue("label_e-mail_address");
        String save = systemValue.getValue("button_save");

        helper.setText(tbTitle, emailChange);
        helper.setButtonText(btnSave, save);
        helper.setText(tvEmail, email);
    }
}
