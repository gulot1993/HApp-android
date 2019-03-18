package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.NotificationAdapter;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.helper.PaginationScrollListener;
import co.work.fukouka.happ.model.NotificationContent;
import io.fabric.sdk.android.Fabric;

public class NotificationsActivity extends AppCompatActivity implements NotificationAdapter.AdapterListener {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private HappHelper mHelper;
    private NotificationAdapter mAdapter;
    private DatabaseReference mDatabase;

    LinearLayoutManager manager;
    List<NotificationContent> list = new ArrayList<>();

    private static final int PAGE_START = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private int totalPages;
    private int currentPage = PAGE_START;
    private String cursorKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        //Instantiations
        mHelper = new HappHelper(this);
        mAdapter = new NotificationAdapter(this, this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mHelper.setUpToolbar(toolbar);

        manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //manager.setReverseLayout(true);

        recyclerView.setLayoutManager(manager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnScrollListener(new PaginationScrollListener(manager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1; //Increment page index to load the next on

                getNextPage();
            }

            @Override
            public int getTotalPageCount() {
                return totalPages;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        getPageCount();

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

    private void getPageCount() {
        mDatabase.child("notifications").child("app-notification").child("notification-user")
                .child(getUserId()).child("notif-list")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Long childCount = dataSnapshot.getChildrenCount();
                        totalPages = (int) (childCount / 5);
                        if (childCount % 5 != 0) {
                            totalPages = totalPages + 1;
                        }

                        //get first page after page count is fetched
                        getFirstPage();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getFirstPage() {
        final int[] count = {0};

        Query ref = mDatabase.child("notifications")
                .child("app-notification")
                .child("notification-user")
                .child(getUserId())
                .child("notif-list")
                .orderByKey()
                .limitToLast(6);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Long dsCount = dataSnapshot.getChildrenCount();
                if (dsCount == 0) {
                    progressBar.setVisibility(View.GONE);
                }

                final List<NotificationContent> notifList = new ArrayList<>();
                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    count[0]++;
                    if (count[0] == 1) {
                        cursorKey = snap.getKey();
                    }

                    final String key = snap.getKey();

                    final Boolean read = (Boolean) snap.child("read").getValue();

                    Query query = mDatabase.child("notifications")
                            .child("app-notification")
                            .child("notification-all")
                            .child(key);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                NotificationContent notification = dataSnapshot
                                        .getValue(NotificationContent.class);

                                if (read != null && notification != null) {
                                    notification.setNotifId(key);
                                    notification.setRead(read);
                                }

                                notifList.add(notification);

                                if (notifList.size() == dsCount) {
                                    progressBar.setVisibility(View.GONE);
                                    currentPage += 1;
                                    Collections.reverse(notifList);

                                    if (currentPage <= totalPages && totalPages > 1) {
                                        notifList.remove(notifList.size() - 1);
                                        mAdapter.updateList(notifList);
                                        mAdapter.addLoadingFooter();
                                    } else {
                                        mAdapter.updateList(notifList);
                                        isLastPage = true;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void getNextPage() {
        final int[] count = {0};

        Query ref = mDatabase.child("notifications")
                .child("app-notification")
                .child("notification-user")
                .child(getUserId())
                .child("notif-list")
                .orderByKey()
                .endAt(cursorKey)
                .limitToLast(6);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Long dsCount = dataSnapshot.getChildrenCount();

                final List<NotificationContent> notifList = new ArrayList<>();

                for (DataSnapshot snap: dataSnapshot.getChildren()) {
                    count[0]++;
                    if (count[0] == 1) {
                        cursorKey = snap.getKey();
                    }

                    final String key = snap.getKey();
                    final Boolean read = (Boolean) snap.child("read").getValue();

                    Query query = mDatabase.child("notifications")
                            .child("app-notification")
                            .child("notification-all")
                            .child(key);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            NotificationContent notification = dataSnapshot
                                    .getValue(NotificationContent.class);

                            if (read != null && notification != null) {
                                notification.setNotifId(key);
                                notification.setRead(read);
                            }

                            notifList.add(notification);

                            if (notifList.size() == dsCount) {
                                mAdapter.removeLoadingFooter();
                                isLoading = false;

                                Collections.reverse(notifList);

                                if (currentPage != totalPages) {
                                    notifList.remove(notifList.size() - 1);
                                    mAdapter.updateList(notifList);

                                    mAdapter.addLoadingFooter();
                                } else {
                                    mAdapter.updateList(notifList);
                                    isLastPage = true;
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String getUserId() {
        String user = null;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            user = currentUser.getUid();
        }

        return user;
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

        String notifications = systemValue.getValue("title_notification");

        helper.setText(tbTitle, notifications);
    }

    @Override
    public void scrollToTop() {
        manager.scrollToPositionWithOffset(0, 0);
    }
}
