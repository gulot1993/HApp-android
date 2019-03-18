package co.work.fukouka.happ.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.ViewPagerAdapter;
import co.work.fukouka.happ.app.HappApp;
import co.work.fukouka.happ.fragment.ConfigurationFragment;
import co.work.fukouka.happ.fragment.MessageFragment;
import co.work.fukouka.happ.fragment.ReservationFragment;
import co.work.fukouka.happ.fragment.SituationFragment;
import co.work.fukouka.happ.fragment.TimelineFragment;
import co.work.fukouka.happ.helper.AlertDialogHelper;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.ConfigurationPresenter;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.utils.SessionManager;
import io.fabric.sdk.android.Fabric;

public class DashboardActivity extends AppCompatActivity implements TabLayout.OnTabSelectedListener {

    @BindView(R.id.view_pager) ViewPager viewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;
    @BindView(R.id.appBarLayout) AppBarLayout appBarLayout;

    protected HappApp mHappApp;
    private HappPreference pref;
    private SessionManager mSession;
    private HappHelper mHelper;

    private DatabaseReference mDatabase;
    private ViewPagerAdapter mAdapter;
    private ConfigurationPresenter mPresenter;

    private boolean getNewData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());

        //get user email for fabric crash analytics
        logUser();

        setupViewPager(viewPager);

        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
        tabLayout.addOnTabSelectedListener(this);
        viewPager.setOffscreenPageLimit(4);

        //Instances
        mPresenter = new ConfigurationPresenter(this);
        mHappApp = (HappApp)this.getApplicationContext();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        pref = new HappPreference(this);
        mSession = new SessionManager(this);
        mHelper = new HappHelper(this);

        checkUserSession();
        displayView(0);

        //check for user updates
        String userId = mPresenter.getUserId();
        mPresenter.checkUinfoUpdates(userId);

        // Get tab notification badges
        getTimelineNotification();
        getMessageNotifications();
        getSituationNotifications();
        getReservationNotifications();
    }

    @Override
    protected void onStart() {
        super.onStart();
        enableAutoStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mHappApp.setCurrentActivity(this);

        IntentFilter iff = new IntentFilter();
        iff.addAction("com.happ.notif_count");
        registerReceiver(this.receiver, iff);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        clearReferences();
        
        unregisterReceiver(this.receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearReferences();
    }

    @Override
    public void onBackPressed() {
        int currentPosition = viewPager.getCurrentItem();

        if (currentPosition != 0) {
            displayView(0);
        } else {
            moveTaskToBack(true);
        }
        // overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int notifCount = intent.getIntExtra("notif_count", 0);

            if (notifCount != 0) {
                TabLayout.Tab messageTab = tabLayout.getTabAt(0);
                final TextView tvNotif = messageTab.getCustomView().findViewById(R.id.tv_notif);
                tvNotif.setVisibility(View.VISIBLE);
                tvNotif.setText(String.valueOf(notifCount));
            }
        }
    };

    private void displayView(int i) {
        int position = getIntent().getIntExtra("position", 0);

        if (position != 0) {
            viewPager.setCurrentItem(position);
        } else {
            viewPager.setCurrentItem(i);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mAdapter.addFragmentWithTitle(new TimelineFragment(), "Timeline");
        mAdapter.addFragmentWithTitle(new MessageFragment(), "Message");
        mAdapter.addFragmentWithTitle(new ReservationFragment(), "Reservation");
        mAdapter.addFragmentWithTitle(new SituationFragment(), "Situation");
        mAdapter.addFragmentWithTitle(new ConfigurationFragment(), "ConfigurationView");
        viewPager.setAdapter(mAdapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabIconColor = ContextCompat.getColor(this, R.color.alpha);
        TextView title = tab.getCustomView().findViewById(R.id.tv_title);
        ImageView icon = tab.getCustomView().findViewById(R.id.iv_icon);
        TextView tvNotif = tab.getCustomView().findViewById(R.id.tv_notif);
        icon.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
        title.setTextColor(tabIconColor);

        int position = tab.getPosition();
        switch (position) {
            case 0:
                tvNotif.setVisibility(View.GONE);

                Intent intent = new Intent("com.happ.get_new_post");
                intent.putExtra("get_new_post", getNewData);
                this.sendBroadcast(intent);

                resetTimelineBadgeCount();
                break;
            case 2:
                tvNotif.setVisibility(View.GONE);

                resetReservationBadgeCount();
                break;

            case 3:
                Intent sitIntent = new Intent("com.happ.refresh_situation");
                sitIntent.putExtra("get_new_post", getNewData);
                this.sendBroadcast(sitIntent);

                tvNotif.setVisibility(View.GONE);

                resetSituationBadgeCount();
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        int tabIconColor = ContextCompat.getColor(this, R.color.white);
        TextView title = tab.getCustomView().findViewById(R.id.tv_title);
        ImageView icon = tab.getCustomView().findViewById(R.id.iv_icon);
        icon.setColorFilter(tabIconColor, PorterDuff.Mode.SRC_IN);
        title.setTextColor(tabIconColor);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        Fragment f = mAdapter.getItem(0);

        if(f != null){
            if(tab.getPosition() == 0) {
                //TimelineFragment fragment = (TimelineFragment) f;
                //fragment.recyclerView.smoothScrollToPosition(0);

                TabLayout.Tab timelineTab = tabLayout.getTabAt(0);
                final TextView tvNotif;
                if (timelineTab != null) {
                    tvNotif = timelineTab.getCustomView().findViewById(R.id.tv_notif);
                    tvNotif.setVisibility(View.GONE);

                    Intent intent = new Intent("com.happ.get_new_post");
                    intent.putExtra("get_new_post", getNewData);
                    intent.putExtra("re_select", true);
                    this.sendBroadcast(intent);

                    resetTimelineBadgeCount();
                }
            }
        }
    }

    private void setupTabIcons() {
        GetSystemValue systemValue = new GetSystemValue(this);

        String timeline = systemValue.getValue("menu_timeline");
        String message = systemValue.getValue("menu_message");
        String reservation = systemValue.getValue("menu_reservation");
        String situation = systemValue.getValue("menu_situation");
        String configuration = systemValue.getValue("menu_configuration");

        setUpTabs(timeline != null? timeline: getString(R.string.timeline), R.mipmap.ic_timeline, 0);
        setUpTabs(message != null? message: getString(R.string.message), R.mipmap.ic_message, 1);
        setUpTabs(reservation != null? reservation: getString(R.string.reservation), R.mipmap.ic_reservation, 2);
        setUpTabs(situation != null? situation: getString(R.string.situation), R.mipmap.ic_rush, 3);
        setUpTabs(configuration != null? configuration: getString(R.string.configuration), R.mipmap.ic_settings, 4);
    }

    private void setUpTabs(String title, int icon, int position) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
        TextView tabTitle = (TextView) view.findViewById(R.id.tv_title);
        ImageView tabIcon = (ImageView) view.findViewById(R.id.iv_icon);
        tabIcon.setImageResource(icon);
        tabTitle.setText(title);
        LinearLayout.LayoutParams layoutParams=new LinearLayout
                .LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity= Gravity.CENTER;
        view.setLayoutParams(layoutParams);
        tabLayout.getTabAt(position).setCustomView(view);
    }

    private void clearReferences(){
        Activity currActivity = mHappApp.getCurrentActivity();
        if (this.equals(currActivity))
            mHappApp.setCurrentActivity(null);
    }

    private void getMessageNotifications() {
        TabLayout.Tab messageTab = tabLayout.getTabAt(1);
        final TextView tvBadge = (TextView) messageTab.getCustomView().findViewById(R.id.tv_notif);

        String userId = mPresenter.getUfbId();
        mDatabase.child("chat").child("last-message").child(userId)
                .orderByChild("read").equalTo(false)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            tvBadge.setVisibility(View.VISIBLE);
                            long count = dataSnapshot.getChildrenCount();
                            tvBadge.setText(String.valueOf(count));
                        } else {
                            tvBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getSituationNotifications() {
        TabLayout.Tab situationTab = tabLayout.getTabAt(3);
        final TextView tvBadge = situationTab.getCustomView().findViewById(R.id.tv_notif);

        String userId = mPresenter.getUfbId();
        mDatabase.child("user-badge").child("freetime").child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long count = (long) dataSnapshot.getValue();
                            if (count > 0) {
                                tvBadge.setVisibility(View.VISIBLE);
                                tvBadge.setText(String.valueOf(count));
                            } else {
                                tvBadge.setVisibility(View.GONE);
                            }
                        } else {
                            tvBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getReservationNotifications() {
        TabLayout.Tab reservationTab = tabLayout.getTabAt(2);
        final TextView tvBadge = reservationTab.getCustomView().findViewById(R.id.tv_notif);

        mDatabase.child("user-badge").child("reservation").child(mPresenter.getUfbId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long count = (long) dataSnapshot.getValue();
                            if (count > 0) {
                                tvBadge.setVisibility(View.VISIBLE);
                                tvBadge.setText(String.valueOf(count));
                            } else {
                                tvBadge.setVisibility(View.GONE);
                            }
                        } else {
                            tvBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
    
    private void getTimelineNotification() {
        TabLayout.Tab reservationTab = tabLayout.getTabAt(0);
        final TextView tvBadge = reservationTab.getCustomView().findViewById(R.id.tv_notif);

        mDatabase.child("user-badge").child("timeline").child(mPresenter.getUfbId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            long count = (long) dataSnapshot.getValue();
                            if (count > 0) {
                                tvBadge.setVisibility(View.VISIBLE);
                                tvBadge.setText(String.valueOf(count));

                                getNewData = true;

                                Intent intent = new Intent("com.happ.new_data");
                                intent.putExtra("new_data", getNewData);
                                DashboardActivity.this.sendBroadcast(intent);
                            } else {
                                tvBadge.setVisibility(View.GONE);
                                getNewData = false;

                                Intent intent = new Intent("com.happ.new_data");
                                intent.putExtra("new_data", getNewData);
                                DashboardActivity.this.sendBroadcast(intent);
                            }
                        } else {
                            getNewData = false;
                            tvBadge.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void resetTimelineBadgeCount() {
        mDatabase.child("user-badge").child("timeline").child(mPresenter.getUfbId()).setValue(0);
    }

    private void resetReservationBadgeCount() {
        mDatabase.child("user-badge").child("reservation").child(mPresenter.getUfbId()).setValue(0);
    }

    private void resetSituationBadgeCount() {
        mDatabase.child("user-badge").child("freetime").child(mPresenter.getUfbId()).setValue(0);
    }

    private void logUser() {
        String email = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            email = currentUser.getEmail();
        }

        Crashlytics.setUserEmail(email);
    }

    //save token to server
    private void saveTokenToServer() {
        HappPreference pref = new HappPreference(this);
        String token = pref.getToken();

        if (token != null) {
            mPresenter.saveTokenToServer(token);
        }
    }

    private void showEnableAutoStartDialog(final Intent intent) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.enable_auto_start_up));
        builder.setMessage(getString(R.string.enable_auto_start));

        String positiveText = getString(R.string.enable);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pref.enableAutoStart();
                        startActivity(intent);
                    }
                });

        String negativeText = getString(R.string.cancel);
        builder.setNegativeButton(negativeText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    private void enableAutoStart() {
        try {
            Intent intent = new Intent();
            String manufacturer = android.os.Build.MANUFACTURER;
            if ("xiaomi".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(
                        new ComponentName("com.miui.securitycenter",
                                "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            } else if ("oppo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(
                        new ComponentName("com.coloros.safecenter",
                                "com.coloros.safecenter.permission.startup.StartupAppListActivity"));
            } else if ("vivo".equalsIgnoreCase(manufacturer)) {
                intent.setComponent(
                        new ComponentName("com.vivo.permissionmanager",
                                "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"));
            }

            List<ResolveInfo> list = getPackageManager()
                    .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if  (list.size() > 0) {
                boolean autoStartEnabled = pref.isAutoStartEnabled();
                if (!autoStartEnabled) {
                    showEnableAutoStartDialog(intent);
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    private void checkUserSession() {
        final String id = getFbUid();
        if (id != null) {
            mDatabase.child("users").child(id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        Intent intent = new Intent("com.happ.check_user_session");
                        DashboardActivity.this.sendBroadcast(intent);

                        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
                        builder.setMessage(mHelper.accountDeleted())
                                .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mSession.logoutUser();
                                    }
                                })
                                .setNegativeButton(mHelper.cancel(), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mSession.logoutUser();
                                    }
                                });
                        // Create the AlertDialog object and return it
                        AlertDialog dialog = builder.create();
                        dialog.setCancelable(false);
                        dialog.show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private String getFbUid() {
        String user = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            user =  currentUser.getUid();
        }
        return user;
    }

}
