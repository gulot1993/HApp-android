package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.AlertDialogHelper;
import co.work.fukouka.happ.helper.Constant;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.helper.ScaleFile;
import co.work.fukouka.happ.model.Skill;
import co.work.fukouka.happ.presenter.GetSkillPresenter;
import co.work.fukouka.happ.view.GetSkillView;

public class TargetSkillActivity extends AppCompatActivity implements GetSkillView {

    @BindView(R.id.main_layout) LinearLayout mainLayout;
    @BindView(R.id.tv_all_users) TextView tvAllUsers;
    @BindView(R.id.sc_all_users) SwitchCompat scAllUsers;
    @BindView(R.id.rl_all_users) RelativeLayout rlAllUsers;
    @BindView(R.id.btn_post) Button btnPost;

    private GetSkillPresenter mPresenter;
    private HappHelper mHelper;
    private GetSystemValue mSystem;

    private List<SwitchCompat> switchList = new ArrayList<>();
    private ArrayList<String> imagePath = new ArrayList<>();
    private List<String> blockedIds;
    private String body;
    private String skills;
    private String tempImage;

    static final String CHARS = "Q2xM5bt5LwTHIqOFgNI33Jtq4Th1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_skill);
        ButterKnife.bind(this);

        mPresenter = new GetSkillPresenter(this, this);
        mHelper = new HappHelper(this);
        mSystem = new GetSystemValue(this);

        getSystemValues();

        mPresenter.getSkill();

        scAllUsers.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return motionEvent.getActionMasked() == MotionEvent.ACTION_MOVE;
            }
        });

        scAllUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (scAllUsers.isChecked()) {
                    for (int i = 0; i < switchList.size(); i++) {
                        switchList.get(i).setChecked(true);
                    }
                } else {
                    for (int i = 0; i < switchList.size(); i++) {
                        switchList.get(i).setChecked(false);
                    }
                }
            }
        });

        imagePath = getIntent().getStringArrayListExtra("image_path");
        body = getIntent().getStringExtra("body");
        tempImage = getIntent().getStringExtra("temp_image");
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
    public void onGetSkill(Skill skill) {
        createLayout(skill);
    }

    private void createLayout(Skill skill) {
        rlAllUsers.setVisibility(View.VISIBLE);

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

            final SwitchCompat switchCompat = new SwitchCompat(this);
            switchCompat.setLayoutParams(rParams);
            switchCompat.setText(String.valueOf(postId));
            switchCompat.setTextColor(Color.TRANSPARENT);
            switchCompat.setPadding(0, 22, 22, 22);
            //switchCompat.setChecked(true);


            switchCompat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (scAllUsers.isChecked()) {
                        scAllUsers.setChecked(false);
                    }
                }
            });

            switchList.add(switchCompat);

            relativeLayout.addView(view);
            relativeLayout.addView(switchCompat);

            mainLayout.addView(relativeLayout);
        }
    }

    @OnClick(R.id.btn_post)
    public void onClicks(View view) {
        switch (view.getId()) {
            case R.id.btn_post:
                final String userId = mPresenter.getUserId();
                skills = getSkillIds();

                if (skills != null && !Objects.equals(skills, "")) {
                    AlertDialogHelper.showAlert(this, mHelper.sendPostMessage(),
                            mHelper.send(), mHelper.cancel(), new AlertDialogHelper.Callback() {
                                @Override
                                public void onPositiveButtonClick() {
                                    new UploadPost(TargetSkillActivity.this).execute(userId, body, skills);
                                }
                            });
                } else {
                    Toast.makeText(this, mHelper.noSelectedSkills(), Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    private String getSkillIds() {
        String skills = "";

        for (int i = 0; i < switchList.size(); i++) {
            if (switchList.get(i).isChecked()) {
                skills += switchList.get(i).getText().toString() + ",";
            }
        }

        return skills;
    }

    private String randomString( int len ) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( CHARS.charAt(random.nextInt(CHARS.length()) ) );
        return sb.toString() + ".png";
    }


    private class UploadPost extends AsyncTask<String, Void, String> {

        Context context;

        private UploadPost(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mHelper.showProgressDialog("", true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String userId = strings[0];
            String postBody = strings[1];
            String skills = strings[2];
            String response = null;

            try {
                URL url = new URL(Constant.API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(30000);
                conn.setConnectTimeout(30000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                reqEntity.addPart("sercret", new StringBody("jo8nefamehisd"));
                reqEntity.addPart("action", new StringBody("api"));
                reqEntity.addPart("ac", new StringBody("update_timeline"));
                reqEntity.addPart("d", new StringBody("0"));
                reqEntity.addPart("lang", new StringBody(mPresenter.getLanguage()));
                reqEntity.addPart("user_id", new StringBody(userId));
                reqEntity.addPart("body", new StringBody(postBody, Charset.forName("UTF-8")));
                reqEntity.addPart("skills", new StringBody(skills));

                if (imagePath != null && imagePath.size() > 0) {
                   for (int i = 0; i < imagePath.size(); i++) {
                       String filePath = imagePath.get(i);
                       String path = filePath.replace("file:", "");
                       Bitmap bitmap;

                       File file = new File(path);
                       long length = file.length() / 1024;

                       if (length > 700) {
                           //reduce size of image
                           ScaleFile sf = new ScaleFile(TargetSkillActivity.this);
                           bitmap = sf.scaleFile(path);
                       } else {
                           BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                           bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
                       }

                       ByteArrayOutputStream bos = new ByteArrayOutputStream();
                       bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                       ContentBody contentPart = new ByteArrayBody(bos.toByteArray(), randomString(20));

                       reqEntity.addPart("images["+i+"]", contentPart);
                   }
                }

                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
                conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

                OutputStream os = conn.getOutputStream();
                reqEntity.writeTo(conn.getOutputStream());
                os.close();
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    response = readStream(conn.getInputStream());
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mHelper.hideProgressDialog();
            blockedIds = new ArrayList<>();
            System.out.println("Response from post " +s);
            if (s != null) {
                try {
                    JSONObject object = new JSONObject(s);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        JSONObject result = object.getJSONObject("result");
                        JSONArray array = result.getJSONArray("blocks");
                        int postId = result.getInt("post_id");

                        if (array != null && array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                blockedIds.add(array.getString(i));
                            }
                        }

                        mPresenter.writeNotification(postId, "timeline", skills, blockedIds);

                        //Delete image from external storage
                        System.out.println("Success");
                        File image = new File(tempImage);
                        if (image.exists()) {
                            boolean isDeleted = image.delete();
                            if (isDeleted) {
                                System.out.println("Image deleted");
                            }
                        }

                        Intent in = new Intent(TargetSkillActivity.this, WritePostActivity.class);
                        in.putExtra("go_timeline", true);
                        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(in);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return builder.toString();
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        String post = mSystem.getValue("button_post");
        String allUser = mSystem.getValue("all_users");

        mHelper.setText(tvAllUsers, allUser);
        mHelper.setButtonText(btnPost, post);
    }
}
