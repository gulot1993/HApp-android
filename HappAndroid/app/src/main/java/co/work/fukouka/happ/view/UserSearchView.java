package co.work.fukouka.happ.view;

import java.util.List;

import co.work.fukouka.happ.model.User;

/**
 * Created by tokikawateppei on 28/07/2017.
 */

public interface UserSearchView {
    void onUserFound(List<User> userList);

    void onUserNotFound(String message);
}
