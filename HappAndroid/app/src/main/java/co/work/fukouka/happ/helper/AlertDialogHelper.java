package co.work.fukouka.happ.helper;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import co.work.fukouka.happ.R;


public class AlertDialogHelper {

    public static void showAlert(Context context, String message, String action, String dismiss, final Callback callback) {
        new AlertDialog.Builder(context).setMessage(message)
                .setPositiveButton(action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onPositiveButtonClick();
                    }
                }).setNegativeButton(dismiss, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //callback.onSucess();
                    }
        }).show();
    }

    public static void showAlert(Context context, String message, String action, final Callback callback) {
        new AlertDialog.Builder(context).setMessage(message)
                .setPositiveButton(action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onPositiveButtonClick();
                    }
                }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setCancelable(false).show();
    }

    public interface Callback {
        void onPositiveButtonClick();
    }

}
