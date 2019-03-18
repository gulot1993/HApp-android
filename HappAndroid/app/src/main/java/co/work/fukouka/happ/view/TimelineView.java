package co.work.fukouka.happ.view;

import co.work.fukouka.happ.model.User;

/**
 * Created by tokikawateppei on 24/07/2017.
 */

public interface TimelineView {
    //void loadTimeline(Post post);

    void onUserFound(User user);

    void onUserNotFound(String response);

}
