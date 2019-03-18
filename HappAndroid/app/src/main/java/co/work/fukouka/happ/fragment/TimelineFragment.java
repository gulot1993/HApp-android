package co.work.fukouka.happ.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paginate.Paginate;
import com.paginate.recycler.LoadingListItemCreator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.NotificationsActivity;
import co.work.fukouka.happ.activity.UserSearchActivity;
import co.work.fukouka.happ.activity.WritePostActivity;
import co.work.fukouka.happ.adapter.TimelineAdapter;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.helper.SpeedyLinearLayoutManager;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.presenter.PostPresenter;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.utils.HappUtils;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.PostView;

/**
 * A simple {@link Fragment} subclass.
 */
public class TimelineFragment extends Fragment implements PostView,
        SwipeRefreshLayout.OnRefreshListener, Paginate.Callbacks,
        TimelineAdapter.adapterListener {

    @BindView(R.id.pull_to_refresh) SwipeRefreshLayout refresh;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.iv_logo) ImageView ivLogo;
    @BindView(R.id.iv_notification) ImageButton ivNotification;
    @BindView(R.id.iv_search) ImageButton ivSearch;
    @BindView(R.id.sc_freetime_status) SwitchCompat scFreetimeStatus;
    @BindView(R.id.ll_app_notif) LinearLayout llAppNotif;
    @BindView(R.id.tv_free_time) TextView tvFreeTime;
    @BindView(R.id.tv_badge) TextView tvBadge;

    public RecyclerView recyclerView;
    public ConstraintLayout clTimelineView;

    private SpeedyLinearLayoutManager layoutManager;
    private TimelineAdapter mAdapter;
    private PostPresenter mPostPres;
    private AppCompatActivity activity;
    private SessionManager mSession;
    private HappUtils utils;
    private String userId;
    private String skills;
    private DatabaseReference mDatabase;
    private HappHelper mHelper;

    private boolean mLoading = false;
    private boolean hasLoaded;
    private int mCurrentPage = 0;
    private Paginate mPaginate;
    private boolean getNewData;
    private List<String> userIds;
    private float offset = 0;

    public TimelineFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);
        ButterKnife.bind(this, view);
        assignSystemValues();

        recyclerView = view.findViewById(R.id.recycler_view);

        //Instances
        activity = (AppCompatActivity) getActivity();
        mAdapter = new TimelineAdapter(getActivity(), this);
        mPostPres = new PostPresenter(getActivity(), this);
        mSession = new SessionManager(getActivity());
        utils = new HappUtils(getActivity());
        userId = mPostPres.getUserId();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mHelper = new HappHelper(getActivity());

        layoutManager = new SpeedyLinearLayoutManager(getActivity());
        clTimelineView = view.findViewById(R.id.cl_timeline_view);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        //needed for the back button in toolbar to work
        setHasOptionsMenu(true);
        refresh.setOnRefreshListener(this);

        skills = new HappPreference(getActivity()).getSkillIds();

        if (utils.isNetworkAvailable()) {
            hasLoaded = true;
            //get skills ids
            mPostPres.getUserSessionStatus();
            getAppNotification();

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mPostPres.getUserSessionStatus();
                    handler.postDelayed(this, 60 * 5000);
                }
            }, 60 * 5000);
        }

        scFreetimeStatus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE;
            }
        });

        scFreetimeStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scFreetimeStatus.isChecked()) {
                    mPostPres.freetimeStatusOn(userId);
                } else {
                    mPostPres.freetimeStatusOff(userId);
                }
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (offset == 0) {
                    int position = layoutManager.findFirstVisibleItemPosition();
                    View firstItemView = layoutManager.findViewByPosition(position);
                    offset = firstItemView.getTop();
                }

                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == 0 && getNewData) {
                    mDatabase.child("user-badge").child("timeline").child(mPostPres.getFbUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Long count = (Long) dataSnapshot.getValue();
                                    if (count != null && count != 0) {
                                        if (!isLoading()) {
                                            mPostPres.getNewPost("all", skills);

                                            mDatabase.child("user-badge").child("timeline")
                                                    .child(mPostPres.getFbUid()).setValue(0);
                                        } else {
                                            Toast.makeText(getActivity(),
                                                    mHelper.failedToRefresh(),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        });

        loadFirstPage();

        return view;
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

    private BroadcastReceiver newPostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean fetch = intent.getBooleanExtra("get_new_post", false);
            if (fetch) {
                if (!isLoading()) {
                    mPostPres.getNewPost("author", skills);
                } else {
                    Toast.makeText(getActivity(), mHelper.failedToRefresh(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private BroadcastReceiver fetchNewPost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean fetch = intent.getBooleanExtra("get_new_post", false);
            boolean reSelect = intent.getBooleanExtra("re_select", false);

            if (reSelect) {
                int position = layoutManager.findFirstVisibleItemPosition();
                View firstItemView = layoutManager.findViewByPosition(position);
                float currentOffset = firstItemView.getTop();

                if (currentOffset != offset) {
                    recyclerView.smoothScrollToPosition(0);
                }

            }

            if (fetch) {
                if (!isLoading()) {
                    mPostPres.getNewPost("all", skills);
                }
            }
        }
    };

    private BroadcastReceiver newPost = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean fetch = intent.getBooleanExtra("new_data", false);
            getNewData = fetch;
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        boolean getnewPost = getActivity().getIntent().getBooleanExtra("get_new_post", false);
        if (getnewPost) {
            if (!isLoading()) {
                mPostPres.getNewPost("author", skills);
                getActivity().getIntent().removeExtra("get_new_post");
            }
        }

        IntentFilter iff = new IntentFilter();
        iff.addAction("android.net.conn.CONNECTIVITY_CHANGE");

        IntentFilter iff1 = new IntentFilter();
        iff1.addAction("com.happ.get_new_post");

        IntentFilter iff2 = new IntentFilter();
        iff2.addAction("com.happ.new_data");

        IntentFilter iff3 = new IntentFilter();
        iff3.addAction("com.hfukouka.happ.get.post");

        getActivity().registerReceiver(this.mBroadcastReceiver, iff);
        getActivity().registerReceiver(this.fetchNewPost, iff1);
        getActivity().registerReceiver(this.newPost, iff2);
        getActivity().registerReceiver(this.newPostReceiver, iff3);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.mBroadcastReceiver);
        getActivity().unregisterReceiver(this.newPostReceiver);
        getActivity().unregisterReceiver(this.fetchNewPost);
        getActivity().unregisterReceiver(this.newPost);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @OnClick({R.id.iv_search, R.id.iv_notification, R.id.fab_write_post})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_search:
                startActivity(new Intent(getActivity(), UserSearchActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.iv_notification:
                removeBadge();
                startActivity(new Intent(getActivity(), NotificationsActivity.class));
                getActivity().overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;
            case R.id.fab_write_post:
                Intent intent = new Intent(getActivity(), WritePostActivity.class);
                startActivityForResult(intent, 123);
                getActivity().overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
                break;
        }
    }

    private void loadFirstPage() {
        if (hasLoadedAllItems()) {
            PostPresenter.hasLoadedAllItems = false;
        }

        if (mPaginate != null) {
            mPaginate.unbind();
        }

        mPaginate = Paginate.with(recyclerView, this)
                .setLoadingTriggerThreshold(0)
                .addLoadingListItem(true)
                .setLoadingListItemCreator(new CustomLoadingListItemCreator())
                .build();

        // Load first page
        mLoading = true;
        mCurrentPage += 1;
        mPostPres.getPosts(userId, String.valueOf(mCurrentPage), skills, "refresh");
    }

    @Override
    public void onLoadPosts(List<Post> post, String action) {
        if (refresh.isRefreshing()) {
            refresh.setRefreshing(false);
        }
        mAdapter.addPost(post, action);
        mLoading = false;
    }

    @Override
    public void appendAuthorPost(List<Post> post) {
        if (refresh.isRefreshing()) {
            refresh.setRefreshing(false);
        }
        mAdapter.appendAuthorNewPost(post);
    }

    @Override
    public void appendNewPost(List<Post> post) {
        if (refresh.isRefreshing()) {
            refresh.setRefreshing(false);
        }
        mAdapter.appendNewPost(post);
    }

    @Override
    public void onSuccess(@Nullable String message) {
        mSession.freeStatusOn("on");
    }

    @Override
    public void onFailed(String message) {
        if (isAdded()) {
            if (refresh.isRefreshing()) {
                refresh.setRefreshing(false);
            }

            if (mPaginate != null) {
                mPaginate.setHasMoreDataToLoad(false);
            }
        }
    }

    @Override
    public void onNoNewPost() {
        if (refresh.isRefreshing()) {
            refresh.setRefreshing(false);
        }
    }

    @Override
    public void onFreetimeFailed() {
        scFreetimeStatus.setChecked(false);
    }

    @Override
    public void isFree(boolean isFree) {
        if (isFree) {
            scFreetimeStatus.setChecked(true);
        } else {
            scFreetimeStatus.setChecked(false);
        }
    }

    @Override
    public void onRefresh() {
        if (!utils.isNetworkAvailable()) {
            mPaginate.setHasMoreDataToLoad(false);
            refresh.setRefreshing(false);
            Toast.makeText(getActivity(), mHelper.noNetConnection() , Toast.LENGTH_SHORT)
                    .show();
        } else {
            if (!isLoading() || hasLoadedAllItems()) {
                if (hasLoadedAllItems()) {
                    PostPresenter.hasLoadedAllItems = false;
                }
                mPaginate.setHasMoreDataToLoad(false);

                //refreshTimeline();
                mCurrentPage = 1;
                mLoading = true;
                mAdapter.clearList();

                if (mPaginate != null) {
                    mPaginate.unbind();

                    mPaginate = Paginate.with(recyclerView, this)
                            .setLoadingTriggerThreshold(0)
                            .addLoadingListItem(true)
                            .setLoadingListItemCreator(new CustomLoadingListItemCreator())
                            .build();
                }
                mPostPres.getPosts(userId, String.valueOf(mCurrentPage), skills, "refresh");
            } else {
                refresh.setRefreshing(false);
            }
        }
    }

    @Override
    public void onLoadMore() {
        if (utils.isNetworkAvailable()) {
            mLoading = true;
            mCurrentPage += 1;
            mPostPres.getPosts(userId, String.valueOf(mCurrentPage), skills, "get");
        } else {
            mPaginate.setHasMoreDataToLoad(false);
            Toast.makeText(getActivity(), mHelper.noNetConnection(), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean isLoading() {
        return mLoading;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return PostPresenter.hasLoadedAllItems;
    }

    @Override
    public void updateTimeline() {
        recyclerView.smoothScrollToPosition(0);
    }

    @Override
    public void refreshTimeline() {
        if (!isLoading() || hasLoadedAllItems()) {
            if (hasLoadedAllItems()) {
                PostPresenter.hasLoadedAllItems = false;
            }
            mCurrentPage = 1;
            mLoading = true;
            mAdapter.clearList();

            if (mPaginate != null) {
                mPaginate.unbind();

                mPaginate = Paginate.with(recyclerView, this)
                        .setLoadingTriggerThreshold(0)
                        .addLoadingListItem(true)
                        .setLoadingListItemCreator(new CustomLoadingListItemCreator())
                        .build();
            }
            mPostPres.getPosts(userId, String.valueOf(mCurrentPage), skills, "refresh");
        } else {
            if (refresh.isRefreshing()) {
                refresh.setRefreshing(false);
            }
            Toast.makeText(getActivity(), mHelper.failedToRefresh(), Toast.LENGTH_SHORT).show();
        }
    }

    private class CustomLoadingListItemCreator implements LoadingListItemCreator {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.custom_loading_list_item, parent, false);
            return new VH(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }
    }

    private class VH extends RecyclerView.ViewHolder {

        private VH(View itemView) {
            super(itemView);

        }
    }

    private void getAppNotification() {
        mDatabase.child("notifications").child("app-notification").child("notification-user")
                .child(mPostPres.getFbUid())
                .child("unread").child("count")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Long count = (Long) dataSnapshot.getValue();
                            if (count != null && count != 0) {
                                tvBadge.setVisibility(View.VISIBLE);
                                tvBadge.setText(String.valueOf(count));
                            } else {
                                tvBadge.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void removeBadge() {
        mDatabase.child("notifications").child("app-notification").child("notification-user")
                .child(mPostPres.getFbUid())
                .child("unread").child("count").setValue(0);
    }

    private void assignSystemValues() {
        GetSystemValue systemValue = new GetSystemValue(getActivity());

        String freeTime = systemValue.getValue("text_now_free");
        if (freeTime != null) {
            tvFreeTime.setText(freeTime);
        }
    }

}
