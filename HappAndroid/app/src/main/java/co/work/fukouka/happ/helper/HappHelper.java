package co.work.fukouka.happ.helper;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.utils.RoundedImageTransform;
import co.work.fukouka.happ.utils.SessionManager;


public class HappHelper {

    private Context mContext;
    private ProgressDialog mDialog;
    private GetSystemValue mSystem;
    private SessionManager mSession;

    public HappHelper(Context context) {
        this.mContext = context;
        mDialog = new ProgressDialog(context);
        mSystem = new GetSystemValue(context);
        mSession = new SessionManager(context);
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public String getUfbId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }
        return userId;
    }

    public String getLanguage() {
        String lang;

        HashMap<String, String> user = mSession.getLanguage();
        lang = user.get(SessionManager.LANGUAGE);

        return lang;
    }

    public void setUpToolbar(Toolbar toolbar) {
        ((AppCompatActivity)mContext).setSupportActionBar(toolbar);
        if (((AppCompatActivity)mContext).getSupportActionBar() != null) {
            ((AppCompatActivity)mContext).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity)mContext).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

    }

    public void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Turn drawable into byte array.
     *
     * @param drawable data
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;
    }

    public void loadImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null) {
            //Picasso.with(mContext).load(imageUrl).into(imageView);
            RequestOptions options = new RequestOptions().centerCrop();
            Glide.with(mContext).load(imageUrl).apply(options).into(imageView);
        } else {
            imageView.setImageDrawable(null);
        }
    }

    public void loadImageWithProgressBar(ImageView imageView, String imageUrl, final ProgressBar progressBar) {
        if (imageUrl != null) {
            //Picasso.with(mContext).load(imageUrl).into(imageView);
            RequestOptions options = new RequestOptions().centerCrop();
            Glide.with(mContext).load(imageUrl).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).apply(options).into(imageView);
        } else {
            imageView.setImageDrawable(null);
        }
    }

    public void loadRoundImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.equals("") && !imageUrl.equals("null")) {
            RequestOptions options = new RequestOptions().centerCrop()
                    .transform(new RoundedImageTransform(mContext));
            //options.centerCrop().transform(new RoundedImageTransform(mContext));
            Glide.with(mContext).load(imageUrl).apply(options).into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.profile_placeholder);
        }
    }

    public void setText(TextView textView, String text) {
        if (text != null && !text.equals(" ")) {
            textView.setText(text);
        }
    }

    public void setTextGoneIfNull(TextView textView, String text) {
        if (text != null && !text.equals(" ")) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(text);
        } else {
            textView.setVisibility(View.GONE);
        }
    }

    public void setButtonText(Button button, String text) {
        if (text != null) {
            button.setText(text);
        }
    }

    public void setEditText(EditText editText, String text) {
        if (text != null) {
            editText.setText(text);
        }
    }

    public void setEditTextHint(EditText editText, String text) {
        if (text != null && !text.equals(" ")) {
            editText.setHint(text);
        }
    }

    public void setSwitchText(SwitchCompat switchCompat, String text) {
        if (text != null && !text.equals(" ")) {
            switchCompat.setText(text);
        }
    }

    public void throwToastMessage(String originalMess, String backUpMess) {
        if (originalMess != null && !originalMess.isEmpty()) {
            Toast.makeText(mContext, originalMess, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, backUpMess, Toast.LENGTH_SHORT).show();
        }
    }

    public void showProgressDialog(String message, Boolean cancellable) {
        mDialog.setMessage(message);
        mDialog.setCancelable(cancellable);

        mDialog.show();
    }

    public void hideProgressDialog() {
        mDialog.dismiss();
    }

    public String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));

        return String.valueOf((cal.get(Calendar.DAY_OF_MONTH)));
    }

    public String getMessageDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
        cal.setTimeInMillis(time);

        return String.valueOf((cal.get(Calendar.DAY_OF_MONTH)));
    }

    public String convertTimeWithTimeZome(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
        cal.setTimeInMillis(time);

        String strMinute;
        int minute = cal.get(Calendar.MINUTE);
        if (minute < 10) {
            strMinute = "0" +String.valueOf(minute);
        } else {
            strMinute = String.valueOf(minute);
        }
        return (cal.get(Calendar.DAY_OF_MONTH) + "/"
                + (cal.get(Calendar.MONTH) + 1) + "/"
                + cal.get(Calendar.YEAR) + " "
                + cal.get(Calendar.HOUR_OF_DAY) + ":"
                + strMinute);
    }

    public String getDateOnly(String dateTime) {
        String date = null;
        try {
            Date simpleDate = new SimpleDateFormat("dd/MM/yyyy").parse(dateTime);
            DateFormat formater = new SimpleDateFormat("MMM dd yyyy");
            String strDate = formater.format(simpleDate);
            int i = strDate.indexOf(" ", strDate.indexOf(" ") + 1);
            date = strDate.substring(0, i);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public String getTimeOnly(String dateTime) {
        String[] str = dateTime.split("\\s");

        return str[1];
    }

    public String getCompletetime(String date, String time) {
        return date + " at " + time;
    }

    // Convert to japanese date
    public String getJapaneseDate(String strDate) {
        String jaDate = null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(strDate);
            DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.JAPAN);
            jaDate = df.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return jaDate;
    }

    // Convert html entities to special characters
    public String convertHtmlEntities(String text) {
        String converted;
        if (text.contains("&gt;") || text.contains("&lt;") || text.contains("&#039;") ||
                text.contains("&quot;") || text.contains("&amp;")) {
            if (Build.VERSION.SDK_INT >= 24) {
                converted = Html.fromHtml(text.replace("\n","<br />"), Html.FROM_HTML_MODE_LEGACY).toString();
            } else {
                converted = Html.fromHtml(text.replace("\n","<br />")).toString();
            }
            return converted;
        } else {
            return text;
        }
    }

    public void showDeletedUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(accountDeleted())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mSession.logoutUser();
                    }
                })
                .setNegativeButton(cancel(), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mSession.logoutUser();
                    }
                });
        // Create the AlertDialog object and return it
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * Application messages
     **/

    public String fillOutFields() {
        String message = mSystem.getValue("mess_fill_missing_field");

        return message != null ? message : mContext.getString(R.string.missing_field);
    }

    public String sendPostMessage() {
        String message = mSystem.getValue("mess_send_post_promt");

        return message != null ? message : mContext.getString(R.string.send_post);
    }

    public String send() {
        String message = mSystem.getValue("label_send");

        return message != null ? message : mContext.getString(R.string.send);
    }

    public String cancel() {
        String message = mSystem.getValue("btn_cancel");

        return message != null ? message : mContext.getString(R.string.cancel);
    }

    public String noSelectedSkills() {
        String message = mSystem.getValue("no_selected_skill");

        return message != null ? message : mContext.getString(R.string.no_selected_skills);
    }

    public String discardPost() {
        String message = mSystem.getValue("mess_discard_post");

        return message != null ? message : mContext.getString(R.string.discard_post);
    }

    public String discard() {
        String message = mSystem.getValue("discard");

        return message != null ? message : mContext.getString(R.string.discard);
    }

    public String logOut() {
        String message = mSystem.getValue("subtitle_log_out");

        return message != null ? message : mContext.getString(R.string.logout);
    }

    public String logoutPrompt() {
        String message = mSystem.getValue("logout_message");

        return message != null ? message : mContext.getString(R.string.continue_logout);
    }

    public String noNetConnection() {
        String message = mSystem.getValue("mess_no_net_connection");

        return message != null ? message : mContext.getString(R.string.no_net_connection);
    }

    public String english() {
        String message = mSystem.getValue("label_en");

        return message != null ? message : mContext.getString(R.string.english);
    }

    public String japanese() {
        String message = mSystem.getValue("label_ja");

        return message != null ? message : mContext.getString(R.string.japanese);
    }

    public String block() {
        String message = mSystem.getValue("to_block");

        return message != null ? message : mContext.getString(R.string.block);
    }

    public String unBlock() {
        String message = mSystem.getValue("button_unblock");

        return message != null ? message : mContext.getString(R.string.unblock);
    }

    public String reservationMess() {
        String message = mSystem.getValue("notif_reservation_mess");

        return message != null ? message : mContext.getString(R.string.reservation_mess);
    }

    public String timelineMess() {
        String message = mSystem.getValue("notif_timeline_mess");

        return message != null ? message : mContext.getString(R.string.posted_timeline);
    }

    public String freeTimeMess() {
        String message = mSystem.getValue("notif_freetime_mess");

        return message != null ? message : mContext.getString(R.string.now_free);
    }

    public String managePostDialogTitle() {
        String message = mSystem.getValue("more_title");

        return message != null ? message : mContext.getString(R.string.more_title);
    }

    public String blockUser() {
        String message = mSystem.getValue("block_user");

        return message != null ? message : mContext.getString(R.string.block_user);
    }

    public String reportPost() {
        String message = mSystem.getValue("report_post");

        return message != null ? message : mContext.getString(R.string.report_post);
    }

    public String report() {
        String message = mSystem.getValue("report");

        return message != null ? message : mContext.getString(R.string.report);
    }

    public String delete() {
        String message = mSystem.getValue("button_delete");

        return message != null ? message : mContext.getString(R.string.delete);
    }

    public String licenseAgreement() {
        String message = mSystem.getValue("license_agreement");

        return message != null ? message : mContext.getString(R.string.licence_agreement);
    }

    public String accept() {
        String message = mSystem.getValue("button_accept");

        return message != null ? message : mContext.getString(R.string.accept);
    }

    public String today() {
        String value = mSystem.getValue("today");

        return value != null ? value : mContext.getString(R.string.today);
    }

    public String yesterday() {
        String value = mSystem.getValue("yesterday");

        return value != null ? value : mContext.getString(R.string.yesterday);
    }

    public String reservationCreated() {
        String value = mSystem.getValue("done_reservation");

        return value != null ? value : mContext.getString(R.string.reservation_created);
    }

    public String deletePost() {
        String value = mSystem.getValue("delete_post_mess");

        return value != null ? value : mContext.getString(R.string.delete_this_post);
    }

    public String notAllowedToView() {
        return mSystem.getValue("not_allowed_to_view");
    }

    public String accountDeleted() {
        return mSystem.getValue("message_account_deleted");
    }

    public String failedToRefresh() {
        return mSystem.getValue("failed_refresh");
    }


    /**
     * End of application messages
     */

}
