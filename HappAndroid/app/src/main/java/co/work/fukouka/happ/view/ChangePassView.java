package co.work.fukouka.happ.view;

/**
 * Created by tokikawateppei on 27/07/2017.
 */

public interface ChangePassView {
    void onUpdateSuccess();

    void onUpdateFailed();

    void onFbUpdateSuccess();

    void onFbUpdateFailed(String error);

    void isAuthenticated();
}
