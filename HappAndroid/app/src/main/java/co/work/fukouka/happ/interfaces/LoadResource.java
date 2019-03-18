package co.work.fukouka.happ.interfaces;

import android.support.v7.widget.Toolbar;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by tokikawateppei on 28/07/2017.
 */

public interface LoadResource {
    void loadRoundImage(String photoUrl, ImageView imageView);

    void loadText(String text, TextView view);

    void onSystemValError(EditText editText, String key);

    void onAppValError(EditText editText, String error);

    void onToastError(String key);

    void setUpToolbar(Toolbar toolbar);

    String getCurrentDate();

    String getMessageDate(long time);

    String convertTimeWithTimeZome(long time);

    String getDateOnly(String dateTime);

    String getTimeOnly(String dateTime);

    String getCompletetime(String date, String time);

    void showProgressDialog(String message, Boolean cancellable);

    void hideProgressDialog();

}
