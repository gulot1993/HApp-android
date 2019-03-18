package co.work.fukouka.happ.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.HappHelper;

public class EulaActivity extends AppCompatActivity {

    @BindView(R.id.btn_accept) Button btnAccept;
    @BindView(R.id.scroll_view) ScrollView scrollView;
    @BindView(R.id.tv_title) TextView tvTitle;

    private HappHelper mHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eula);
        ButterKnife.bind(this);

        mHelper = new HappHelper(this);

        assignSystemValues();

    }

    @OnClick(R.id.btn_accept)
    public void onClicks(View view) {
        switch (view.getId()) {
            case R.id.btn_accept:
                startActivity(new Intent(this, RegisterActivity.class));
                overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                //register();
                break;
        }
    }

    private void assignSystemValues() {
        tvTitle.setText(mHelper.licenseAgreement());
        btnAccept.setText(mHelper.accept());
    }
}
