package co.work.fukouka.happ.view;

/**
 * Created by tokikawateppei on 21/07/2017.
 */

public interface AuthenticationView {
    void onSuccess(String response);

    void onFbLoginSuccess();

    void onFailed(String response);
}
