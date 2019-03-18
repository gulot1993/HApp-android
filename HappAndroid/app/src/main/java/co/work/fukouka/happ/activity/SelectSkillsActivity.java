package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Skill;
import co.work.fukouka.happ.presenter.GetSkillPresenter;
import co.work.fukouka.happ.view.GetSkillView;

public class SelectSkillsActivity extends AppCompatActivity implements GetSkillView {

    private static final String TAG = "SelectSkillsActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.main_layout) LinearLayout mainLayout;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.btn_save) Button btnSave;

    private HappHelper mHelper;
    private GetSystemValue mSystem;
    private GetSkillPresenter mPresenter;
    private String fromActivity;

    private List<SwitchCompat> switchList = new ArrayList<>();
    private List<TextView> textViewList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_skills);
        ButterKnife.bind(this);

        mHelper = new HappHelper(this);
        mSystem = new GetSystemValue(this);
        mPresenter = new GetSkillPresenter(this, this);
        mHelper.setUpToolbar(toolbar);

        getSystemValues();

        mPresenter.getSkill();
        fromActivity = getIntent().getStringExtra("from_activity");
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

    @OnClick(R.id.btn_save)
    public void onClicks(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                String skillKeys = getSkillKeys();
                String skillValues = getSkillValues();

                Intent in = getIntent();
                String name = in.getStringExtra("name");
                String statement = in.getStringExtra("statement");
                String photoUrl = in.getStringExtra("photo_url");
                String email = in.getStringExtra("email");
                String password = in.getStringExtra("password");
                String rePass = in.getStringExtra("re_pass");
                String photoName = in.getStringExtra("photo_name");
                boolean photoChanged = in.getBooleanExtra("photo_changed", false);

                if (fromActivity.equals("Register")) {
                    Intent intent = new Intent(this, RegisterActivity.class);
                    intent.putExtra("skill_values", skillValues);
                    intent.putExtra("skill_keys", skillKeys);
                    intent.putExtra("name", name);
                    intent.putExtra("email", email);
                    intent.putExtra("password", password);
                    intent.putExtra("re_pass", rePass);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                } else {
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    intent.putExtra("skill_values", skillValues);
                    intent.putExtra("skill_keys", skillKeys);
                    intent.putExtra("name", name);
                    intent.putExtra("statement", statement);
                    intent.putExtra("photo_url", photoUrl);
                    intent.putExtra("photo_name", photoName);
                    intent.putExtra("photo_changed", photoChanged);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                }

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
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onGetSkill(Skill skill) {
        createLayout(skill);
    }

    private void createLayout(Skill skill) {
        String category = skill.getCategory();
        int listSize = skill.getSkills().size();

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        LinearLayout.LayoutParams wrapParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        RelativeLayout.LayoutParams rParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.alpha));

        TextView textView = new TextView(this);
        textView.setLayoutParams(wrapParams);
        textView.setText(category);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setTextColor(ContextCompat.getColor(this, R.color.jet));
        textView.setPadding(0, 4, 0, 4);
        linearLayout.addView(textView);

        mainLayout.addView(linearLayout);

        for (int j = 0; j < listSize; j++) {
            //create list
            int postId = skill.getSkills().get(j).getSkillId();
            String skillName = skill.getSkills().get(j).getName();
            RelativeLayout relativeLayout = new RelativeLayout(this);
            relativeLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.layout_bottom_border));
            relativeLayout.setLayoutParams(params);

            TextView view = new TextView(this);
            view.setLayoutParams(wrapParams);
            view.setText(skillName);
            view.setPadding(22, 22, 0, 22);

            textViewList.add(view);

            final SwitchCompat switchCompat = new SwitchCompat(this);
            switchCompat.setLayoutParams(rParams);
            switchCompat.setText(String.valueOf(postId));
            switchCompat.setTextColor(Color.TRANSPARENT);
            switchCompat.setPadding(0, 22, 22, 22);

            switchList.add(switchCompat);

            relativeLayout.addView(view);
            relativeLayout.addView(switchCompat);

            mainLayout.addView(relativeLayout);
        }

        getPrevSelectedSkills();
    }



    private String getSkillKeys() {
        String skills = "";

        for (int i = 0; i < switchList.size(); i++) {
            if (switchList.get(i).isChecked()) {
                skills += switchList.get(i).getText().toString() + ",";
            }
        }

        return !skills.equals("") ? skills.substring(0, skills.length() - 1) : skills;
    }

    private String getSkillValues() {
        String skillValue = "";

        for (int i = 0; i < switchList.size(); i++) {
            if (switchList.get(i).isChecked()) {
                skillValue += textViewList.get(i).getText().toString() + ", ";
            }
        }

        return !skillValue.equals("") ? skillValue.substring(0, skillValue.length() - 2) : skillValue;
    }

    private void getPrevSelectedSkills() {
        String skills = getIntent().getStringExtra("skill_keys");

        if (skills != null && !skills.equals("")) {
            String[] skillsList = skills.split(",");

            for (String aSkillsList : skillsList) {
                for (int j = 0; j < switchList.size(); j++) {
                    if (aSkillsList.equals(switchList.get(j).getText().toString())) {
                        switchList.get(j).setChecked(true);
                    }
                }
            }
        }
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        String skillSelect = mSystem.getValue("selection_skill");
        String save = mSystem.getValue("button_save");

        mHelper.setText(tbTitle, skillSelect);
        mHelper.setButtonText(btnSave, save);
    }

}
