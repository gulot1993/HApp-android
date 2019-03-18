package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.presenter.NotifDetailsPresenter;
import co.work.fukouka.happ.presenter.ProfilePresenter;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.view.NotifDetailView;
import io.fabric.sdk.android.Fabric;

public class NotifDetailsActivity extends AppCompatActivity implements NotifDetailView{

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.iv_user_photo) ImageView ivUserPhoto;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.tv_post_text) TextView tvPostText;
    @BindView(R.id.iv_first_image) ImageView ivFirstImage;
    @BindView(R.id.iv_second_image) ImageView ivSecondImage;
    @BindView(R.id.iv_third_image) ImageView ivThirdImage;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.cl_main) ConstraintLayout clMain;
    @BindView(R.id.tv_notif) TextView tvNotifiDetails;
    @BindView(R.id.btn_message) Button btnMessage;

    private HappHelper mHelper;
    private NotifDetailsPresenter mPresenter;
    private ProfilePresenter mProfPres;

    private String currentUserId;
    private String authorId;
    private String author;
    private String authorProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif_details);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        mHelper = new HappHelper(this);
        mPresenter = new NotifDetailsPresenter(this, this);
        mProfPres = new ProfilePresenter(this);

        mHelper.setUpToolbar(toolbar);

        currentUserId = mPresenter.getUserId();

        Intent intent = getIntent();
        int notifId = intent.getIntExtra("notif_id", 0);

        String skills = new HappPreference(this).getSkillIds();
        if (notifId != 0) {
            mPresenter.getNotifDetails(notifId, skills);
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

    @OnClick({R.id.iv_user_photo, R.id.tv_name, R.id.btn_message})
    public void onClicks(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", authorId);

        switch (view.getId()) {
            case R.id.iv_user_photo:
                startActivity(intent);
                break;
            case R.id.tv_name:
                startActivity(intent);
                break;
            case R.id.btn_message:
                String chatroomId = mProfPres.getChatroomId();
                String chatmateId = ProfilePresenter.chatmateId;

                if (authorId != null) {
                    Intent in;
                    if (chatroomId != null) {
                        in = new Intent(this, ChatRoomActivity.class);
                        in.putExtra("chatroom_id", chatroomId);
                        in.putExtra("chatmate_id", chatmateId);
                        in.putExtra("chatmate_name", author);
                        in.putExtra("chatmate_photoUrl", authorProfile);
                        startActivity(in);
                    } else {
                        in = new Intent(this, ChatRoomActivity.class);
                        in.putExtra("user_id", authorId);
                        startActivity(in);
                    }
                }
                break;
        }

    }

    @Override
    public void onLoadPostNotif(Post post) {
        Intent intent = getIntent();
        author = intent.getStringExtra("name");
        authorProfile = intent.getStringExtra("photo_url");
        String content = post.getBody();
        List<String> images = post.getImages();
        String date = post.getDateModified();
        String dateMod = date.substring(0, date.length() - 3);
        authorId = post.getFromUserId();

        if (authorId != null) {
            mProfPres.getMessageThread(Integer.parseInt(authorId));
        }

        btnMessage.setVisibility(View.VISIBLE);

       // setupLayout(post, imageCount);
        loadNotifDetails(author, authorProfile, dateMod, content, images);
    }

    @Override
    public void onFailed(String message) {
        clMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void loadNotifDetails(String author, String authorProfile, String dateMod,
                                 String content, List<String> images) {

        clMain.setVisibility(View.VISIBLE);

        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        params.bottomMargin = 64;

        mHelper.setText(tvName, author);
        mHelper.setText(tvDate, dateMod);
        mHelper.setText(tvPostText, content);
        mHelper.loadRoundImage(ivUserPhoto, authorProfile);
        final List<ImageView> imageViews = Arrays.asList(ivFirstImage, ivSecondImage, ivThirdImage);

        if (images.isEmpty() && images.size() < 1) {
            progressBar.setVisibility(View.GONE);
        }

        for (int i = 0; i < images.size(); i++) {
            final int finalI = i;
            Glide.with(this).load(images.get(i))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target,
                                                    boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            imageViews.get(finalI).setLayoutParams(params);
                            return false;
                        }
                    })
                    .into(imageViews.get(i));
        }
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

        String notification = systemValue.getValue("title_notification");

        helper.setText(tvNotifiDetails, notification);
    }


}
