package co.work.fukouka.happ.view;

import co.work.fukouka.happ.model.MessageContent;

/**
 * Created by tokikawateppei on 04/08/2017.
 */

public interface ChatRoomView {
    void onMessageSent();

    void loadConversations(MessageContent message);

    void onLoadChatmateName(String name);

    void dismissNotification();

    void onMessageSendFailed();

    void onUserBlocked();

    void onUserNotBlocked();
}
