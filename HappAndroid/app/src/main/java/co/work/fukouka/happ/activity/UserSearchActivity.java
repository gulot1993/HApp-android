package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.UserSearchAdapter;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.presenter.UserSearchPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.view.UserSearchView;
import io.fabric.sdk.android.Fabric;

public class UserSearchActivity extends AppCompatActivity implements TextWatcher, UserSearchView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.main_layout) ConstraintLayout layout;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.et_search) EditText etSearch;
    @BindView(R.id.tv_cancel) TextView tvCancel;
    @BindView(R.id.tb_title) TextView tbTitle;

    private UserSearchAdapter mAdapter;
    private UserSearchPresenter mPresenter;
    private CustomLoadResource mRes;
    private HappHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        assignSystemValues();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);

        mAdapter = new UserSearchAdapter(this);
        mPresenter = new UserSearchPresenter(this, this);
        mHelper = new HappHelper(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);

        etSearch.addTextChangedListener(this);

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

    @OnClick({R.id.et_search, R.id.tv_cancel})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                tvCancel.setVisibility(View.GONE);
                etSearch.setText(null);
                break;
            case R.id.et_search:
                tvCancel.setVisibility(View.VISIBLE);
                etSearch.setGravity(Gravity.LEFT);
                etSearch.setCursorVisible(true);
                break;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        mAdapter.clearList();
        String name = charSequence.toString();
        mPresenter.searchUser(name);
    }

    @Override
    public void afterTextChanged(Editable editable) {}

    @Override
    public void onUserFound(List<User> userList) {
        mAdapter.updateList(userList);
    }

    @Override
    public void onUserNotFound(String message) {}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String searchUsers = systemValue.getValue("title_search_users");
        String searchHint = systemValue.getValue("search_placeholder");
        String cancel = systemValue.getValue("btn_cancel");

        helper.setText(tbTitle, searchUsers);
        helper.setEditTextHint(etSearch, searchHint);
        helper.setText(tvCancel, cancel);

    }
}
