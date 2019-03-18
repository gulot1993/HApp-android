package co.work.fukouka.happ.model;

import com.google.firebase.database.IgnoreExtraProperties;


@IgnoreExtraProperties
public class MessageContent {
    private String chatroomId;
    private String chatmateId;
    private String userId;
    private String name;
    private String photoUrl;
    private String message;
    private String lastMessage;
    private Long timestamp;
    private boolean isRead;

    public MessageContent() {
    }

    public MessageContent(String userId, String name, String photoUrl, String message, Long timestamp) {
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.message = message;
        this.timestamp = timestamp;
    }

    public MessageContent(String chatroomId, String chatmateId, String name, String photoUrl,
                          String lastMessage, Long timestamp, boolean isRead) {

        this.chatroomId = chatroomId;
        this.chatmateId = chatmateId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.isRead = isRead;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getChatmateId() {
        return chatmateId;
    }

    public void setChatmateId(String chatmateId) {
        this.chatmateId = chatmateId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
