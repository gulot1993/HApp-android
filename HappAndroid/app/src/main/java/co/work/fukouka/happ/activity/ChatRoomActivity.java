package co.work.fukouka.happ.activity;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.ChatAdapter;
import co.work.fukouka.happ.app.HappApp;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.helper.PaginationScrollUpListener;
import co.work.fukouka.happ.model.MessageContent;
import co.work.fukouka.happ.presenter.ChatRoomPresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.utils.HappUtils;
import co.work.fukouka.happ.view.ChatRoomView;
import io.fabric.sdk.android.Fabric;

public class ChatRoomActivity extends AppCompatActivity implements ChatRoomView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view) RecyclerView rv;
    @BindView(R.id.et_message) EditText etMessage;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.ll_app_notif) LinearLayout llAppNotif;
    @BindView(R.id.tv_send) TextView tvSend;
    @BindView(R.id.include) View include;
    @BindView(R.id.layout_block) LinearLayout layoutBlock;
    @BindView(R.id.tv_layout_mess) TextView tvMessage;

    private CustomLoadResource mRes;
    private ChatRoomPresenter mPresenter;
    private ChatAdapter mAdapter;
    private HappUtils utils;
    private LinearLayoutManager manager;
    private HappHelper helper;
    private GetSystemValue systemValue;

    protected HappApp mHappApp;
    private DatabaseReference mDatabase;
    private HappHelper mHelper;

    List<MessageContent> chatList;

    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int totalPages;
    private int currentPage = PAGE_START;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());

        logUser();

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);
        helper = new HappHelper(this);
        systemValue = new GetSystemValue(this);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mHelper = new HappHelper(this);

        getSystemValues();

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mPresenter = new ChatRoomPresenter(this, this);
        mAdapter = new ChatAdapter(this);
        mHappApp = (HappApp)this.getApplicationContext();
        utils = new HappUtils(this);

        rv.setAdapter(mAdapter);

        Intent intent = getIntent();
        String chatroomId = intent.getStringExtra("chatroom_id");
        final String chatmateId = intent.getStringExtra("chatmate_id");
        String chatmateName = intent.getStringExtra("chatmate_name");
        String chatmatePhotoUrl = intent.getStringExtra("chatmate_photoUrl");
        Boolean blocked = intent.getBooleanExtra("blocked", false);

        rv.addOnScrollListener(new PaginationScrollUpListener(manager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1; //Increment page index to load the next on
            }

            @Override
            public boolean isLastPage() {
                return false;
            }

            @Override
            public boolean isLoading() {
                return false;
            }
        });

        etMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        manager.scrollToPosition(mAdapter.getItemCount() - 1);
                        rv.setLayoutManager(manager);
                    }
                }, 300);
                return false;
            }
        });

        if (chatmateName != null) {
            tvName.setText(chatmateName);
            tvName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (chatmateId != null) {
                        mAdapter.getUserHappId(chatmateId);
                    }
                }
            });
        }

        getChatmatesInfo();

        if (chatroomId != null) {
            mPresenter.checkIfBlocked(chatroomId);
            mPresenter.passChatmateInfo(chatroomId, chatmateId, chatmateName, chatmatePhotoUrl);
            mPresenter.getConversations(chatroomId);
            mPresenter.listenForUserDataChanges(chatmateId);
        }

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            if (activeNetwork != null) { // connected to the internet
                llAppNotif.setVisibility(View.GONE);
            } else {
                llAppNotif.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        mHappApp.setCurrentActivity(this);
        IntentFilter iff = new IntentFilter();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.happ.check_user_session");
        iff.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.registerReceiver(this.mBroadcastReceiver, iff);
        this.registerReceiver(this.logOutUser, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        clearReferences();
        this.unregisterReceiver(this.mBroadcastReceiver);
        this.unregisterReceiver(this.logOutUser);
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
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

    @OnClick(R.id.tv_send)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_send:
                String message = etMessage.getText().toString().trim();
                if (!TextUtils.isEmpty(message)) {
                    etMessage.setText(null);
                    mPresenter.sendMessage(message);
                }
                break;
        }

    }

    @Override
    public void onLoadChatmateName(String name) {
        tvName.setText(name);
    }

    /**
     * Dismiss message push notifications if message is set to read
     */
    @Override
    public void dismissNotification() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    private void getChatmatesInfo() {
        String currentUid = mPresenter.getCurrentUid();
        String happId = getIntent().getStringExtra("user_id");
        if (happId != null) {
            mPresenter.getChatmateInfo(happId);
        }
        if (currentUid != null) {
            mPresenter.getCurrentUinfo();
        }
    }

    @Override
    public void onMessageSent() {
//        View view = getLayoutInflater().inflate(R.layout.primary_chat_message, null);
//        TextView tvDate = (TextView) view.findViewById(R.id.tv_date);
//        tvDate.setVisibility(View.VISIBLE);
    }

    @Override
    public void onMessageSendFailed() {

    }

    @Override
    public void onUserBlocked() {
        tvName.setClickable(false);
        mAdapter.isBlocked(true);

        showSnackbar();
    }

    @Override
    public void onUserNotBlocked() {
        tvName.setClickable(true);
        mAdapter.isBlocked(false);

        hideSnackbar();
    }

    @Override
    public void loadConversations(MessageContent message) {
        mAdapter.updateList(message);

        manager.scrollToPosition(mAdapter.getItemCount() - 1);
        rv.setLayoutManager(manager);
    }

    private void clearReferences(){
        Activity currActivity = mHappApp.getCurrentActivity();
        if (this.equals(currActivity))
            mHappApp.setCurrentActivity(null);
    }

    private void logUser() {
        String email = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
        }

        Crashlytics.setUserEmail(email);
    }

    private void showSnackbar() {
        include.setVisibility(View.GONE);

        String message = systemValue.getValue("mess_block_convo");

        layoutBlock.setVisibility(View.VISIBLE);
        tvMessage.setText(message != null ? message : getString(R.string.cant_reply));
    }

    private void hideSnackbar() {
        include.setVisibility(View.VISIBLE);

        layoutBlock.setVisibility(View.INVISIBLE);
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };


    private void getSystemValues() {
        String enterMess = systemValue.getValue("label_enter_message");
        String send = systemValue.getValue("label_send");

        helper.setText(tvSend, send);

        if (enterMess != null) {
            etMessage.setHint(enterMess);
        }
    }

}
