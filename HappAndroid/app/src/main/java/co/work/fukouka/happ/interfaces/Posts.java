package co.work.fukouka.happ.interfaces;

import com.esafirm.imagepicker.model.Image;

import java.util.List;

public interface Posts {
    String getUserId();

    String getLanguage();

    void getPosts(String userId, String page, String skills);

    void getNewPost(String caller, String skills);

    void getLatestPost();

    void writePost(String userId, String post, List<Image> images);

    void deletePost(int postId);

    void updateFreetimeStatus(String userId);

}
