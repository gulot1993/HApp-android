package co.work.fukouka.happ.activity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.features.camera.CameraModule;
import com.esafirm.imagepicker.features.camera.DefaultCameraModule;
import com.esafirm.imagepicker.features.camera.ImmediateCameraModule;
import com.esafirm.imagepicker.model.Image;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.AlertDialogHelper;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.GrayscaleImageLoader;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.utils.CustomLoadResource;
import io.fabric.sdk.android.Fabric;

public class WritePostActivity extends AppCompatActivity {

    private static final String TAG = "WritePostActivity";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.et_post_body) EditText etPostBody;
    @BindView(R.id.iv_image) ImageView ivImage;
    @BindView(R.id.main_layout) LinearLayout mainLayout;
    @BindView(R.id.btn_post) Button btnPost;

    private static final int RC_CODE_PICKER = 2000;
    private static final int RC_CAMERA = 3000;

    private CustomLoadResource mRes;
    private CameraModule mCamModule;
    private DefaultCameraModule cameraModule;
    private HappHelper mHelper;
    private GetSystemValue systemValue;

    private List<Image> images = new ArrayList<>();
    private List<String> imagePath = new ArrayList<>();
    private List<String> imageName;
    private String mImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        ButterKnife.bind(this);
        Fabric.with(this, new Crashlytics());

        mRes = new CustomLoadResource(this);
        mRes.setUpToolbar(toolbar);

        mHelper = new HappHelper(this);
        systemValue = new GetSystemValue(this);
        cameraModule = new DefaultCameraModule();

        getSystemValues();

        boolean goTimeline = getIntent().getBooleanExtra("go_timeline", false);
        if (goTimeline) {
            Intent i = new Intent(this, DashboardActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            i.putExtra("get_new_post", true);
            startActivity(i);
            //overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
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
        if (!etPostBody.getText().toString().isEmpty() || (imagePath != null && imagePath.size() > 0)) {
            AlertDialogHelper.showAlert(this, mHelper.discardPost(),
                    mHelper.discard(), mHelper.cancel(), new AlertDialogHelper.Callback() {
                        @Override
                        public void onPositiveButtonClick() {
                            File image = new File(mImagePath);
                            if (image.exists()) {
                                boolean isDeleted = image.delete();
                            }
                            setResult(Activity.RESULT_CANCELED);
                            finish();
                            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
                        }
                    });
        } else {
            setResult(Activity.RESULT_CANCELED);
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_top);
        }
    }

    @OnClick({R.id.btn_post, R.id.iv_photo, R.id.iv_camera})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_post:
                skillSelection();
                break;
            case R.id.iv_photo:
                pickImage();
                break;
            case R.id.iv_camera:
                //startActivityForResult(cameraModule.getCameraIntent(getApplicationContext()), RC_CAMERA);
                final Activity activity = WritePostActivity.this;
                final String[] permissions = new String[]{Manifest.permission.CAMERA};
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, RC_CAMERA);
                } else {
                    captureImage();
                }
                break;
        }
    }

    private void skillSelection() {
        if (TextUtils.isEmpty(etPostBody.getText().toString()) && imagePath.isEmpty()) {
            Toast.makeText(this, mHelper.fillOutFields(), Toast.LENGTH_SHORT).show();
        } else {
            if (TextUtils.isEmpty(etPostBody.getText().toString()) && imagePath.size() > 0) {
                etPostBody.setText(" ");
            }

            final String postBody = etPostBody.getText().toString();

            final String userId = mHelper.getUserId();
            if (userId != null) {
                Intent intent = new Intent(WritePostActivity.this, TargetSkillActivity.class);
                intent.putExtra("body", postBody);
                intent.putStringArrayListExtra("image_path", (ArrayList<String>) imagePath);
                intent.putStringArrayListExtra("image_name", (ArrayList<String>) imageName);
                intent.putExtra("temp_image", mImagePath);
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_CODE_PICKER && resultCode == RESULT_OK && data != null) {
            images = ImagePicker.getImages(data);
            int size = images.size();

            setupLayout(images, size);

            imagePath = new ArrayList<>();
            imageName = new ArrayList<>();

            //assign images name and path in separate list
            for (int i = 0; i < images.size(); i++) {
                String path = images.get(i).getPath();
                String name = images.get(i).getName();

                imagePath.add(path);
                imageName.add(name);
            }

           return;
        }

        if (requestCode == RC_CAMERA && resultCode == RESULT_OK) {
            File imgFile = new  File(mImagePath);
            if (imgFile.exists()) {
                imagePath = new ArrayList<>();
                imagePath.add(mImagePath);

                setPic(mImagePath);
            }
        }
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

    private void captureImage() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = timeStamp + ".jpg";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (storageDir != null) {
            if (storageDir.exists() && storageDir.isDirectory()) {
                mImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                File file = new File(mImagePath);
                Uri outputFileUri = Uri.fromFile(file);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(cameraIntent, RC_CAMERA);
            }
        }
    }

    private ImmediateCameraModule getCameraModule() {
        if (mCamModule == null) {
            mCamModule = new ImmediateCameraModule();
        }
        return (ImmediateCameraModule) mCamModule;
    }

    private void setPic(String mCurrentPhotoPath) {
        // Get the dimensions of the View
        int targetW = ivImage.getWidth();
        int targetH = ivImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

        LinearLayout.LayoutParams mainParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 800);

        mainLayout.removeAllViews();
        mainLayout.setLayoutParams(mainParam);

        final ImageView firstView = new ImageView(this);
        firstView.setLayoutParams(layoutParam);
        firstView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        firstView.setImageBitmap(bitmap);

        mainLayout.addView(firstView);

    }

    private void pickImage() {
        ImagePicker imagePicker = ImagePicker.create(this)
                .theme(R.style.ImagePickerTheme)
                .returnAfterFirst(false)
                .folderMode(true)
                .folderTitle("Folder")
                .imageTitle("Tap to select")
                .imageLoader(new GrayscaleImageLoader())
                .multi()
                .limit(3)
                .showCamera(false)
                .imageDirectory("Camera")
                .imageFullDirectory(Environment.getExternalStorageDirectory().getPath())
                .origin((ArrayList<Image>) images);

        imagePicker.start(RC_CODE_PICKER);
    }

    private void setupLayout(List<Image> images, int size) {
        ivImage.setVisibility(View.GONE);

        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );

       switch (size) {
           case 1:
               oneImageLayout(layoutParam, images);
               break;
           case 2:
               twoImageLayout(layoutParam, images);
               break;
           case 3:
               threeImageLayout(layoutParam, images);
               break;
       }
    }

    private void oneImageLayout(LinearLayout.LayoutParams layoutParam, final List<Image> images) {
        LinearLayout.LayoutParams mainParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 800);

        mainLayout.removeAllViews();
        mainLayout.setLayoutParams(mainParam);

        String firstImage = images.get(0).getPath();

        final ImageView firstView = new ImageView(this);
        firstView.setLayoutParams(layoutParam);
        firstView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mHelper.loadImage(firstView, firstImage);
        firstView.setTag(firstImage);

        mainLayout.addView(firstView);

        firstView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ListIterator<Image> iterator = images.listIterator(); iterator.hasNext();) {
                    String path = iterator.next().getPath();
                    if (path.equals(firstView.getTag())) {
                        //iterator.remove();
                    }
                }
            }
        });
    }

    private void twoImageLayout(LinearLayout.LayoutParams layoutParam, final List<Image> images) {
        mainLayout.removeAllViews();

        String firstImage = images.get(0).getPath();
        String secondImage = images.get(1).getPath();


        LinearLayout.LayoutParams marginParam = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);

        LinearLayout.LayoutParams mainParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 300);
        mainLayout.setLayoutParams(mainParam);

        mainLayout.setOrientation(LinearLayout.HORIZONTAL);

        //sub main
        RelativeLayout layout1 = new RelativeLayout(this);
        marginParam.rightMargin = 5;
        layout1.setLayoutParams(marginParam);

        //image sub main
        final ImageView firstView = new ImageView(this);
        firstView.setLayoutParams(layoutParam);
        firstView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mHelper.loadImage(firstView, firstImage);
        layout1.addView(firstView);

        //sub main
        RelativeLayout layout2 = new RelativeLayout(this);
        layout2.setLayoutParams(marginParam);

        final ImageView secondView = new ImageView(this);
        secondView.setLayoutParams(layoutParam);
        secondView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mHelper.loadImage(secondView, secondImage);
        firstView.setTag(firstImage);
        secondView.setTag(secondImage);

        layout2.addView(secondView);

        mainLayout.addView(layout1);
        mainLayout.addView(layout2);

        firstView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ListIterator<Image> iterator = images.listIterator(); iterator.hasNext();) {
                    String path = iterator.next().getPath();
                    if (path.equals(firstView.getTag())) {
                        //iterator.remove();
                    }
                }
            }
        });

        secondView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ListIterator<Image> iterator = images.listIterator(); iterator.hasNext();) {
                    String path = iterator.next().getPath();
                    if (path.equals(secondView.getTag())) {
                       // iterator.remove();
                    }
                }
            }
        });
    }

    private void threeImageLayout(LinearLayout.LayoutParams layoutParam, final List<Image> images) {
        LinearLayout.LayoutParams mainParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 800);

        mainLayout.removeAllViews();
        mainLayout.setLayoutParams(mainParam);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        String firstImage = images.get(0).getPath();
        String secondImage = images.get(1).getPath();
        String thirdImage = images.get(2).getPath();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.7f);

        LinearLayout.LayoutParams marginlayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);

        LinearLayout.LayoutParams marginParam = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

        LinearLayout.LayoutParams lowerLayoutParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);

        //sub main
        RelativeLayout upperLayout = new RelativeLayout(this);
        layoutParams.bottomMargin = 3;
        upperLayout.setLayoutParams(layoutParams);

        //image sub main
        final ImageView firstView = new ImageView(this);
        firstView.setLayoutParams(layoutParam);
        firstView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mHelper.loadImage(firstView, firstImage);

        upperLayout.addView(firstView);

        //sub main
        LinearLayout lowerLayout = new LinearLayout(this);
        lowerLayout.setLayoutParams(marginlayoutParams);
        lowerLayout.setOrientation(LinearLayout.HORIZONTAL);

        RelativeLayout lowerLeftLayout = new RelativeLayout(this);
        lowerLayoutParams.rightMargin = 3;
        lowerLeftLayout.setLayoutParams(lowerLayoutParams);

        final ImageView secondView = new ImageView(this);
        secondView.setLayoutParams(layoutParam);
        secondView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mHelper.loadImage(secondView, secondImage);

        lowerLeftLayout.addView(secondView);
        lowerLayout.addView(lowerLeftLayout);

        RelativeLayout lowerightLayout = new RelativeLayout(this);
        lowerightLayout.setLayoutParams(marginParam);

        final ImageView thirdView = new ImageView(this);
        thirdView.setLayoutParams(layoutParam);
        thirdView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        mHelper.loadImage(thirdView, thirdImage);

        lowerightLayout.addView(thirdView);
        lowerLayout.addView(lowerightLayout);

        firstView.setTag(firstImage);
        secondView.setTag(secondImage);
        thirdView.setTag(thirdImage);

        mainLayout.addView(upperLayout);
        mainLayout.addView(lowerLayout);

        firstView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ListIterator<Image> iterator = images.listIterator(); iterator.hasNext();) {
                    String path = iterator.next().getPath();
                    if (path.equals(firstView.getTag())) {
                        //iterator.remove();
                    }
                }
            }
        });

        secondView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ListIterator<Image> iterator = images.listIterator(); iterator.hasNext();) {
                    String path = iterator.next().getPath();
                    if (path.equals(secondView.getTag())) {
                       // iterator.remove();
                    }
                }
            }
        });

        thirdView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (ListIterator<Image> iterator = images.listIterator(); iterator.hasNext();) {
                    String path = iterator.next().getPath();
                    if (path.equals(thirdView.getTag())) {
                       // iterator.remove();
                    }
                }

            }
        });
    }

    private BroadcastReceiver logOutUser = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mHelper.showDeletedUserDialog();
        }
    };

    private void getSystemValues() {
        String _continue = systemValue.getValue("button_continue");
        String postContent = systemValue.getValue("holder_post_content");

        mHelper.setButtonText(btnPost, _continue);

        if (postContent != null) {
            etPostBody.setHint(postContent);
        }
    }

}
