package co.work.fukouka.happ.view;

import co.work.fukouka.happ.model.Post;

public interface NotifDetailView {
    void onLoadPostNotif(Post post);

    void onFailed(String message);
}
