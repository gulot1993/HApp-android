package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.ProfileAdapter;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.presenter.ProfilePresenter;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.view.ProfileView;
import io.fabric.sdk.android.Fabric;

public class ProfileActivity extends AppCompatActivity implements ProfileView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.iv_user_photo) ImageView ivUserPhoto;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_happ_id) TextView tvHappId;
    @BindView(R.id.tv_skills) TextView tvSkills;
    @BindView(R.id.tv_statement) TextView tvStatement;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.btn_block) Button btnBlock;
    @BindView(R.id.btn_message) Button btnMessage;
    @BindView(R.id.nested_scroll) NestedScrollView scrollView;

    private ProfilePresenter mPresenter;
    private HappHelper mHelper;
    private ProfileAdapter mAdapter;
    private LinearLayoutManager mManager;
    private HappPreference mPref;

    private String mName;
    private String mPhotoUrl;
    private String id;
    private boolean mIsblocked;
    private boolean isLoading;
    private String skills;
    private int currentPage = 1;
    private boolean isAttached;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        mPresenter = new ProfilePresenter(this, this);
        mHelper = new HappHelper(this);
        mAdapter = new ProfileAdapter(this);
        mPref = new HappPreference(this);

        mManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mManager);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mAdapter);

        mHelper.setUpToolbar(toolbar);
        ProfilePresenter.hasLoadedAllItems = false;

        String currentUserId = mPresenter.getUserId();
        id = getIntent().getStringExtra("user_id");
        skills = mPref.getSkillIds();
        if (id != null) {
            if (currentUserId.equals(id)) {
                btnMessage.setVisibility(View.GONE);
                btnBlock.setVisibility(View.GONE);
            } else {
                btnMessage.setVisibility(View.VISIBLE);
                btnBlock.setVisibility(View.VISIBLE);
            }
            //check user if blocked
            mPresenter.checkIfblocked(id);
            mPresenter.getUserInfo(id);
            fetchData(currentPage);
            mPresenter.getMessageThread(Integer.parseInt(id));
        }

        scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView
                        .getScrollY()));

                if (diff == 0 && !isLoading) {
                    if (!ProfilePresenter.hasLoadedAllItems) {
                        currentPage += 1;
                        fetchData(currentPage);
                    }
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.getMessageThread(Integer.parseInt(id));

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
        String sender =  getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("notification")) {
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
        } else {
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }

    @Override
    public void onLoadUserInfo(User user) {
        String name = user.getName();
        String photoUrl = user.getPhotoUrl();
        String happId = user.getHappId();
        String skills = user.getSkills();
        String skillValue = convertKeysToValues(skills);
        String statement = user.getMessage();

        // Convert html entities
        name = mHelper.convertHtmlEntities(name);
        statement = mHelper.convertHtmlEntities(statement);

        if (isAttached)
            mHelper.loadRoundImage(ivUserPhoto, photoUrl);
        mHelper.setTextGoneIfNull(tvName, name);
        if (happId != null && !happId.equals("")) {
            mHelper.setTextGoneIfNull(tvHappId, happId);
        } else {
            tvHappId.setVisibility(View.GONE);
        }
        mHelper.setTextGoneIfNull(tvSkills, skillValue);
        mHelper.setTextGoneIfNull(tvStatement, statement);

        mName = name;
        mPhotoUrl = photoUrl;
    }

    @Override
    public void onLoadPost(List<Post> post) {
        isLoading = false;
        progressBar.setVisibility(View.GONE);
        mAdapter.updateList(post);
    }

    @Override
    public void onGetPostFailed() {
        progressBar.setVisibility(View.GONE);
    }

    @OnClick({R.id.btn_message, R.id.btn_block})
    public void onClick(View view) {
        String id = getIntent().getStringExtra("user_id");

        switch (view.getId()) {
            case R.id.btn_message:
                String chatroomId = mPresenter.getChatroomId();
                String chatmateId = ProfilePresenter.chatmateId;

                if (id != null) {
                    if (chatroomId != null) {
                        Intent intent = new Intent(this, ChatRoomActivity.class);
                        intent.putExtra("chatroom_id", chatroomId);
                        intent.putExtra("chatmate_id", chatmateId);
                        intent.putExtra("chatmate_name", mName);
                        intent.putExtra("chatmate_photoUrl", mPhotoUrl);
                        intent.putExtra("blocked", mIsblocked);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(this, ChatRoomActivity.class);
                        intent.putExtra("user_id", id);
                        intent.putExtra("blocked", mIsblocked);
                        startActivity(intent);
                    }
                }
                break;
            case R.id.btn_block:
                if (id != null) {
                    if (mIsblocked) {
                        mHelper.showProgressDialog("", true);
                        mPresenter.unBlockUser(id);
                    } else {
                        mHelper.showProgressDialog("", true);
                        mPresenter.blockUser(id);
                    }
                }

                break;
        }
    }

    private void fetchData(int page) {
        isLoading = true;
        mPresenter.getUserPost(id, skills, String.valueOf(page));
    }

    private String convertKeysToValues(String skills) {
        String[] skillKey = skills.split(",");
        String skillValue = "";

        for (String keys: skillKey) {
            skillValue += new GetSystemValue(this).getValue(keys) != null ?
                    new GetSystemValue(this).getValue(keys) + ", " : "";
        }

        return !skillValue.equals("") ? skillValue.substring(0, skillValue.length() - 2) : skillValue;
    }

    @Override
    public void onUpdateSuccess(String message) {
        //Unused method
    }

    @Override
    public void onUpdateFailed(String message) {
        mHelper.hideProgressDialog();
    }

    @Override
    public void userBlocked() {
        mHelper.hideProgressDialog();
        mIsblocked = true;
        btnBlock.setText(mHelper.unBlock());
        btnBlock.setBackgroundResource(R.drawable.button_background_red);
    }

    @Override
    public void userUnblocked() {
        mHelper.hideProgressDialog();
        mIsblocked = false;
        btnBlock.setText(mHelper.block());
        btnBlock.setBackgroundResource(R.drawable.button_background);
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

        String message = systemValue.getValue("menu_message");
        String toBlock = systemValue.getValue("to_block");

        helper.setButtonText(btnMessage, message);
        helper.setButtonText(btnBlock, toBlock);
    }
}
