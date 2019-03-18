package co.work.fukouka.happ.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class NotifMessage {
    String chatroomId;
    String chatmateId;
    String name;
    String photoUrl;
    String message;

    public NotifMessage() {
    }

    public NotifMessage(String chatroomId, String chatmateId, String name, String photoUrl, String message) {
        this.chatroomId = chatroomId;
        this.chatmateId = chatmateId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.message = message;
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
}
