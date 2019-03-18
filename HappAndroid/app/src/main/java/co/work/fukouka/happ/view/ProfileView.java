package co.work.fukouka.happ.view;

import java.util.List;

import co.work.fukouka.happ.model.Post;
import co.work.fukouka.happ.model.User;

public interface ProfileView {
    void onLoadUserInfo(User user);

    void onLoadPost(List<Post> post);

    void onGetPostFailed();

    void onUpdateSuccess(String message);

    void onUpdateFailed(String message);

    void userBlocked();

    void userUnblocked();
}
