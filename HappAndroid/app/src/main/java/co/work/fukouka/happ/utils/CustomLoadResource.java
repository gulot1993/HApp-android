package co.work.fukouka.happ.utils;


import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.interfaces.LoadResource;


public class CustomLoadResource implements LoadResource {
    private Context mContext;
    private ProgressDialog mDialog;
    private onSnackbarListener listener;
    private SystemValuesEn valuesEn;
    private SystemValuesJp valuesJp;
    private HappUtils happUtils;

    private String lang;

    public CustomLoadResource(Context context) {
        this.mContext = context;
        happUtils = new HappUtils(context);
        valuesEn = new SystemValuesEn(context);
        valuesJp = new SystemValuesJp(context);
        lang =  happUtils.getLanguage();
        mDialog = new ProgressDialog(context);
    }

    private interface onSnackbarListener{
        void onSnackbarClick();
    }

    @Override
    public void loadRoundImage(String photoUrl, ImageView imageView) {
        if (photoUrl != null && !photoUrl.equals("") && !photoUrl.equals("null")) {
            RequestOptions options = new RequestOptions().centerCrop()
                    .transform(new RoundedImageTransform(mContext));
            Glide.with(mContext).load(photoUrl).apply(options).into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.profile_placeholder);
        }
    }

    @Override
    public void loadText(String text, TextView view) {
        if (text != null && !text.equals("")) {
            view.setText(text);
        }
    }

    @Override
    public void onSystemValError(EditText editText, String key) {
        String error;

        if (lang.equals("jp")) {
            error = valuesJp.getSystemValue(key);
        } else {
            error = valuesEn.getSystemValue(key);
        }
        editText.setError(error);
        editText.requestFocus();
    }

    @Override
    public void onAppValError(EditText editText, String error) {
        editText.setError(error);
        editText.requestFocus();
    }

    @Override
    public void onToastError(String key) {
        String error;

        if (lang.equals("jp")) {
            error = valuesJp.getSystemValue(key);
        } else {
            error = valuesEn.getSystemValue(key);
        }
        Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setUpToolbar(Toolbar toolbar) {
        ((AppCompatActivity)mContext).setSupportActionBar(toolbar);
        ((AppCompatActivity)mContext).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)mContext).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));

        return String.valueOf((cal.get(Calendar.DAY_OF_MONTH)));
    }

    @Override
    public String getMessageDate(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        cal.setTimeInMillis(time);

        return String.valueOf((cal.get(Calendar.DAY_OF_MONTH)));
    }

    @Override
    public String convertTimeWithTimeZome(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        cal.setTimeInMillis(time);
        return (cal.get(Calendar.DAY_OF_MONTH) + "/"
                + (cal.get(Calendar.MONTH) + 1) + "/"
                + cal.get(Calendar.YEAR) + " "
                +  cal.get(Calendar.HOUR_OF_DAY) + ":"
                + cal.get(Calendar.MINUTE));
    }

    @Override
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

    @Override
    public String getTimeOnly(String dateTime) {
        String[] str = dateTime.split("\\s");

        return str[1];
    }

    @Override
    public String getCompletetime(String date, String time) {
        return date + " at " + time;
    }

    @Override
    public void showProgressDialog(String message, Boolean cancellable) {
        mDialog.setMessage(message);
        mDialog.setCancelable(cancellable);

        mDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        mDialog.dismiss();
    }


}
