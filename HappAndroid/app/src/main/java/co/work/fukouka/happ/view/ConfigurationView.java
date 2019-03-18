package co.work.fukouka.happ.view;

import co.work.fukouka.happ.model.User;

/**
 * Created by tokikawateppei on 26/07/2017.
 */

public interface ConfigurationView {
    void onLoadUserInfo(User user);

    void onSuccess();

    void onFailed();

}
