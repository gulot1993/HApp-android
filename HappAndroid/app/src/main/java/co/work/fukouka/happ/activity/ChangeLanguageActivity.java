package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.utils.CustomLoadResource;

public class ChangeLanguageActivity extends AppCompatActivity {
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tv_title) TextView tvTitle;
    @BindView(R.id.tv_japanese) TextView tvJapanese;
    @BindView(R.id.tv_english) TextView tvEnglish;

    private CustomLoadResource mRes;
    private HappHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);
        ButterKnife.bind(this);
        getSystemValues();

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);
        mHelper = new HappHelper(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.happ.check_user_session");
        this.registerReceiver(this.logOutUser, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        this.unregisterReceiver(this.logOutUser);
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

    @OnClick({R.id.cl_japanese, R.id.cl_english})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.cl_japanese:
                intent = new Intent(this, LanguageSettingsActivity.class);
                intent.putExtra("language", "日本語");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                //mPresenter.changeLanguage("jp");
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                break;
            case R.id.cl_english:
                intent = new Intent(this, LanguageSettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("language", "English");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                //mPresenter.changeLanguage("en");
                break;
        }
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String langSettings = systemValue.getValue("title_language_settings");
        String japanese = systemValue.getValue("label_ja");
        String english = systemValue.getValue("label_en");

        helper.setText(tvTitle, langSettings);
        helper.setText(tvJapanese, japanese);
        helper.setText(tvEnglish, english);
    }

}
