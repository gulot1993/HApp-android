package co.work.fukouka.happ.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.features.camera.ImmediateCameraModule;
import com.esafirm.imagepicker.features.camera.OnImageReadyListener;
import com.esafirm.imagepicker.model.Image;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
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
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.Constant;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.GrayscaleImageLoader;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.helper.ScaleFile;
import co.work.fukouka.happ.interfaces.LoadResource;
import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.presenter.ProfilePresenter;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.utils.HappPreference;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ProfileView;
import io.fabric.sdk.android.Fabric;

public class EditProfileActivity extends AppCompatActivity implements ProfileView {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.iv_user_photo) ImageView ivUserPhoto;
    @BindView(R.id.et_name) EditText etName;
    @BindView(R.id.et_profile_statement) EditText etProfStatement;
    @BindView(R.id.cl_select_skills) ConstraintLayout clSelectskills;
    @BindView(R.id.tv_skills_selected) TextView tvSkillSelected;
    @BindView(R.id.tv_selection_skill) TextView tvSelectionSkill;
    @BindView(R.id.tv_selected_skills) TextView tvSelectedSkills;
    @BindView(R.id.tb_title) TextView tbTitle;
    @BindView(R.id.btn_save) Button btnSave;
    @BindView(R.id.tv_skills) TextView tvSkills;
    @BindView(R.id.tv_chars) TextView tvChars;
    @BindView(R.id.tv_happ_id) TextView tvHappId;

    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;
    static final String CHARS = "Q2xM5bt5LwTHIqOFgNI33Jtq4Th1";

    private SessionManager mSession;
    private ProfilePresenter mPresenter;
    private LoadResource mLoadResource;
    private HappHelper mHelper;
    private CameraModule mCamModule;
    private GetSystemValue systemValue;
    private HappPreference mPref;
    private ScaleFile mSf;

    private String name;
    private String photoUrl;
    private String profStatement;
    private List<Image> images = new ArrayList<>();
    private String skillKeys;
    private String happid;
    private boolean photoChanged;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());

        mHelper = new HappHelper(this);
        mHelper.setUpToolbar(toolbar);

        // Instantiation
        mPresenter = new ProfilePresenter(this, this);
        mSession = new SessionManager(getApplicationContext());
        mLoadResource = new CustomLoadResource(this);
        systemValue = new GetSystemValue(this);
        mPref = new HappPreference(this);
        mSf = new ScaleFile(this);

        getSystemValues();

        Intent intent = getIntent();
        String skillValues = intent.getStringExtra("skill_values");
        skillKeys = intent.getStringExtra("skill_keys");

        HashMap<String, String> info = mSession.getUserInfo();
        name = info.get(SessionManager.NAME);

        if (name != null) {
            if (skillValues != null) {
                String name = intent.getStringExtra("name");
                String statement = intent.getStringExtra("statement");
                photoUrl = intent.getStringExtra("photo_url");
                happid = info.get(SessionManager.HAPPID);

                loadUserInfo(name, photoUrl, statement, skillKeys, happid);
            } else {
                //get saved data from phone
                photoUrl = info.get(SessionManager.PHOTO_URL);
                profStatement = info.get(SessionManager.STATEMENT);
                skillKeys = info.get(SessionManager.SKILLS);
                happid = info.get(SessionManager.HAPPID);

                loadUserInfo(name, photoUrl, profStatement, skillKeys, happid);
            }

        } else {
            //phone db is empty
            String userId = mPresenter.getUserId();
            mPresenter.getUserInfo(userId);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.happ.check_user_session");
        registerReceiver(logoutUser, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(logoutUser);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = ImagePicker.getImages(data);
            photoUrl = images.get(0).getPath();

            photoChanged = true;

            loadImageFromGallery(photoUrl);
            return;
        }

        if (requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            getCameraModule().getImage(this, data, new OnImageReadyListener() {
                @Override
                public void onImageReady(List<Image> list) {
                    images = list;
                    photoUrl = images.get(0).getPath();

                    photoChanged = true;

                    loadFromCamera(photoUrl);

                }
            });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA) {
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    //attach user info to views
    @Override
    public void onLoadUserInfo(User user) {
        String name = user.getName();
        photoUrl = user.getPhotoUrl();
        String message = user.getMessage();
        skillKeys = user.getSkills();
        String skillValue = convertKeysToValues(skillKeys);
        String happId = user.getHappId();

        mHelper.loadRoundImage(ivUserPhoto, photoUrl);
        mHelper.setEditText(etName, name);
        mHelper.setEditText(etProfStatement, message);
        mHelper.setText(tvSelectedSkills, skillValue);
        mHelper.setText(tvHappId, happId);

        mSession.saveUserInfo(name, photoUrl, message, skillKeys, happId);
    }

    @Override
    public void onLoadPost(List<Post> post) {}

    @Override
    public void onUpdateSuccess(String message) {
        mHelper.hideProgressDialog();
        mHelper.hideKeyboard(this);
        new HappPreference(this).storeSkillIds(skillKeys);
        mSession.saveUserInfo(name, photoUrl, profStatement, skillKeys);

        //update user info in firebase
        mPresenter.updateUserInfoInFirebase();

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onGetPostFailed() {}

    @Override
    public void onUpdateFailed(String message) {
        mHelper.hideProgressDialog();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void userBlocked() {}

    @Override
    public void userUnblocked() {}

    @OnClick({R.id.btn_save, R.id.iv_user_photo, R.id.cl_select_skills})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_save:
                name = etName.getText().toString().trim();
                profStatement = etProfStatement.getText().toString().trim();
                if (!TextUtils.isEmpty(name)) {
                    new UpdateProfile().execute(name, skillKeys, profStatement);
                } else {
                    String missingField = systemValue.getValue("mess_fill_missing_field");
                    Toast.makeText(this, missingField != null ? missingField :
                            getString(R.string.missing_field), Toast.LENGTH_SHORT).show();
                }

                break;
            case R.id.iv_user_photo:
                pickImage();

                break;
            case R.id.cl_select_skills:
                String name = etName.getText().toString().trim();
                String statement = etProfStatement.getText().toString().trim();

                Intent intent = new Intent(this, SelectSkillsActivity.class);
                intent.putExtra("from_activity", "EditProfile");
                intent.putExtra("skill_keys", skillKeys);
                intent.putExtra("name", name);
                intent.putExtra("statement", statement);
                intent.putExtra("photo_url", photoUrl);
                intent.putExtra("photo_name", randomString(20));
                intent.putExtra("photo_changed", photoChanged);

                startActivity(intent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                break;
        }
    }

    private void pickImage() {
        ImagePicker imagePicker = ImagePicker.create(this)
                .theme(R.style.ImagePickerTheme)
                .returnAfterFirst(true)
                .folderMode(true)
                .folderTitle("Select image")
                .imageTitle("Tap to select")
                .imageLoader(new GrayscaleImageLoader())
                .single()
                .limit(1)
                .showCamera(true)
                .imageDirectory("Camera")
                .imageFullDirectory(Environment.getExternalStorageDirectory().getPath())
                .origin((ArrayList<Image>) images);

        imagePicker.start(RC_CODE_PICKER);
    }

    private ImmediateCameraModule getCameraModule() {
        if (mCamModule == null) {
            mCamModule = new ImmediateCameraModule();
        }
        return (ImmediateCameraModule) mCamModule;
    }

    private void captureImage() {
        startActivityForResult(getCameraModule().getCameraIntent(EditProfileActivity.this), RC_CAMERA);
    }

    private void loadImageFromGallery(String imageUrl) {
        new HappHelper(this).loadRoundImage(ivUserPhoto, imageUrl);
    }

    private void loadFromCamera(String imageUrl) {
        File imgFile = new File(imageUrl);
        Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

        int newSize = (int) ( bitmap.getHeight() * (512.0 / bitmap.getWidth()) );
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 512, newSize, true);

        ivUserPhoto.setImageBitmap(scaledBitmap);
    }

    /**
     * Load user info from phone DB or from cloud
     */
    private void loadUserInfo(String name, String photoUrl, String profStatement, String skills, String happid) {
        mHelper.loadRoundImage(ivUserPhoto, photoUrl );
        mHelper.setEditText(etName, name);
        mHelper.setEditText(etProfStatement, profStatement);
        String skillValue = convertKeysToValues(skills);
        mHelper.setText(tvSelectedSkills, skillValue);
        mHelper.setText(tvHappId, happid);
    }

    private String randomString( int len ) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( CHARS.charAt(random.nextInt(CHARS.length()) ) );
        return sb.toString() + ".png";
    }

    private String convertKeysToValues(String skills) {
        String skillValue = "";
        String emptySkills = systemValue.getValue("empty_skills");
        if (skills != null) {
            String[] skillKey = skills.split(",");

            for (String keys: skillKey) {
                skillValue += systemValue.getValue(keys) != null ?
                        new GetSystemValue(this).getValue(keys) + ", " : "";
            }
        }

        return !skillValue.equals("") ? skillValue.substring(0, skillValue.length() - 2) : emptySkills;
    }

    private class UpdateProfile extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mHelper.showProgressDialog(systemValue.getValue("mess_updating_profile"), true);
        }

        @Override
        protected String doInBackground(String... strings) {
            String name = strings[0];
            String skills = strings[1];
            String profStatement = strings[2];
            String response = null;

            try {
                URL url = new URL(Constant.API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(20000);
                conn.setConnectTimeout(30000);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
                reqEntity.addPart("sercret", new StringBody("jo8nefamehisd"));
                reqEntity.addPart("action", new StringBody("api"));
                reqEntity.addPart("ac", new StringBody("user_update"));
                reqEntity.addPart("d", new StringBody("0"));
                reqEntity.addPart("lang", new StringBody(mPresenter.getLanguage()));
                reqEntity.addPart("user_id", new StringBody(mPresenter.getUserId()));
                reqEntity.addPart("name", new StringBody(name, Charset.forName("UTF-8")));
                reqEntity.addPart("mess", new StringBody(profStatement, Charset.forName("UTF-8")));
                reqEntity.addPart("skills", new StringBody(skills, Charset.forName("UTF-8")));
                //reqEntity.addPart("targets", new StringBody("name,skills,mess"));

                if (!images.isEmpty() && images.size() > 0) {
                    String path = images.get(0).getPath();
                    Bitmap bitmap;

                    File file = new File(path);
                    long length = file.length() / 1024;

                    if (length > 800) {
                        //reduce size of image
                        bitmap = mSf.scaleFile(path);
                    } else {
                        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
                    }

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    ContentBody contentPart = new ByteArrayBody(bos.toByteArray(), randomString(20));

                    reqEntity.addPart("icon", contentPart);
                } else {
                    boolean photoChanged = getIntent().getBooleanExtra("photo_changed", false);
                    if (photoChanged) {
                        Bitmap bitmap;

                        String photoUrl = getIntent().getStringExtra("photo_url");
                        String photoName = getIntent().getStringExtra("photo_name");

                        File file = new File(photoUrl);
                        long length = file.length() / 1024;

                        if (length > 800) {
                            //reduce size of image
                            bitmap = mSf.scaleFile(photoUrl);
                        } else {
                            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                            bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(),bmOptions);
                        }

                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                        ContentBody contentPart = new ByteArrayBody(bos.toByteArray(), photoName);

                        reqEntity.addPart("icon", contentPart);
                    }
                }

                if (!images.isEmpty() && images.size() > 0) {
                    reqEntity.addPart("targets", new StringBody("name,skills,mess,icon"));
                } else {
                    boolean photoChanged = getIntent().getBooleanExtra("photo_changed", false);
                    if (photoChanged) {
                        reqEntity.addPart("targets", new StringBody("name,skills,mess,icon"));
                    } else {
                        reqEntity.addPart("targets", new StringBody("name,skills,mess"));
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
                return null;
            }

            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mHelper.hideProgressDialog();
            if (s != null) {
                try {
                    JSONObject object = new JSONObject(s);
                    boolean success = object.getBoolean("success");
                    if (success) {
                        JSONObject result = object.getJSONObject("result");
                        String message = result.getString("mess");
                        onUpdateSuccess(message);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    try {
                        JSONObject object = new JSONObject(s);
                        boolean error = object.getBoolean("error");

                        if (error) {
                            String message = object.getString("message");
                            onUpdateFailed(message);
                        }
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
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

    private BroadcastReceiver logoutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        String editProfile = systemValue.getValue("title_edit_profile");
        String save = systemValue.getValue("button_save");
        String skills = systemValue.getValue("subtitle_skills");
        String chars = systemValue.getValue("holder_15_more_char");
        String skillSelected = systemValue.getValue("selected_skill");
        String selectionSkill = systemValue.getValue("select_skill");
        String profStatement = systemValue.getValue("holder_profile_statement");

        mHelper.setText(tbTitle, editProfile);
        mHelper.setButtonText(btnSave, save);
        mHelper.setText(tvSkills, skills);
        mHelper.setText(tvChars, chars);
        mHelper.setText(tvSkillSelected, skillSelected);
        mHelper.setText(tvSelectionSkill, selectionSkill);
        mHelper.setEditTextHint(etProfStatement, profStatement);
    }

}
