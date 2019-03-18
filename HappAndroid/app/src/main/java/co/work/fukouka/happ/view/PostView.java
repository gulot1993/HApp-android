package co.work.fukouka.happ.view;

import android.support.annotation.Nullable;

import java.util.List;

import co.work.fukouka.happ.model.Post;


public interface PostView {
    void onLoadPosts(List<Post> post, String action);

    void appendAuthorPost(List<Post> post);

    void appendNewPost(List<Post> post);

    void onSuccess(@Nullable String message);

    void onFailed(String message);

    void onNoNewPost();

    void onFreetimeFailed();

    void isFree(boolean isFree);

}
