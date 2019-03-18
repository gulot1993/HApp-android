package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.presenter.ProfilePresenter;
import io.fabric.sdk.android.Fabric;

public class PostDetailsActivity extends AppCompatActivity {

    @BindView(R.id.iv_user_photo) ImageView ivUserPhoto;
    @BindView(R.id.tv_name) TextView tvName;
    @BindView(R.id.tv_date) TextView tvDate;
    @BindView(R.id.tv_post_text) TextView tvPostText;
    @BindView(R.id.iv_first_image) ImageView ivFirstImage;
    @BindView(R.id.iv_second_image) ImageView ivSecondImage;
    @BindView(R.id.iv_third_image) ImageView ivThirdImage;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.scroll_view) ScrollView scrollView;
    @BindView(R.id.btn_message) Button btnMessage;

    private HappHelper mHelper;
    private ProfilePresenter mPresenter;

    private String userId;
    private String author;
    private String authorProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());
        getSystemValues();

        //Instantiation
        mHelper  = new HappHelper(this);
        mPresenter = new ProfilePresenter(this);

        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        author = intent.getStringExtra("author");
        authorProfile = intent.getStringExtra("author_profile");
        String dateMod = intent.getStringExtra("date_mod");
        String content = intent.getStringExtra("post_content");
        ArrayList<String> images = getIntent().getStringArrayListExtra("images");
        loadPostDetails(author, authorProfile, dateMod, content, images);

        if (userId != null) {
            mPresenter.getMessageThread(Integer.parseInt(userId));
        }

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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @OnClick({R.id.iv_user_photo, R.id.tv_name, R.id.btn_message})
    public void onClicks(View view) {
        String userId = getIntent().getStringExtra("user_id");
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("user_id", userId);

        switch (view.getId()) {
            case R.id.iv_user_photo:
                startActivity(intent);
                break;
            case R.id.tv_name:
                startActivity(intent);
                break;
            case R.id.btn_message:
                String chatroomId = mPresenter.getChatroomId();
                String chatmateId = ProfilePresenter.chatmateId;

                if (userId != null) {
                    Intent in;
                    if (chatroomId != null) {
                        in = new Intent(this, ChatRoomActivity.class);
                        in.putExtra("chatroom_id", chatroomId);
                        in.putExtra("chatmate_id", chatmateId);
                        in.putExtra("chatmate_name", author);
                        in.putExtra("chatmate_photoUrl", authorProfile);
                        in.putExtra("blocked", false);
                        startActivity(in);
                    } else {
                        in = new Intent(this, ChatRoomActivity.class);
                        in.putExtra("user_id", userId);
                        in.putExtra("blocked", false);
                        startActivity(in);
                    }
                }
                break;
        }
    }

    private void loadPostDetails(String author, String authorProfile, String dateMod,
                                 String content, final List<String> images) {

        final String currentUserId = mPresenter.getUserId();

        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);

        params.bottomMargin = 64;

        mHelper.setText(tvName, author);
        mHelper.setText(tvDate, dateMod);
        mHelper.setText(tvPostText, content);
        mHelper.loadRoundImage(ivUserPhoto, authorProfile);
        final List<ImageView> imageViews = Arrays.asList(ivFirstImage, ivSecondImage, ivThirdImage);

        if (images != null && !images.isEmpty()) {
            for (int i = 0; i < images.size(); i++) {
                final int finalI = i;
                Glide.with(this).load(images.get(i))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                progressBar.setVisibility(View.GONE);
                                imageViews.get(finalI).setLayoutParams(params);

                                if (!currentUserId.equals(userId)) {
                                    btnMessage.setVisibility(View.VISIBLE);
                                }

                                return false;
                            }
                        })
                        .into(imageViews.get(i));
            }
        } else {
            progressBar.setVisibility(View.GONE);
            if (!currentUserId.equals(userId)) {
                btnMessage.setVisibility(View.VISIBLE);
            }else {
                btnMessage.setVisibility(View.GONE);
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
        HappHelper helper = new HappHelper(this);
        GetSystemValue systemValue = new GetSystemValue(this);

        String message = systemValue.getValue("menu_message");

        helper.setButtonText(btnMessage, message);
    }
}
