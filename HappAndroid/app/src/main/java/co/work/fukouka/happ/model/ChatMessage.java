package co.work.fukouka.happ.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

@IgnoreExtraProperties
public class ChatMessage {
    String userId;
    String name;
    String photoUrl;
    String message;
    Map<String, String> timestamp;

    public ChatMessage(){

    }

    public ChatMessage(String userId, String name, String photoUrl, String message, Map<String, String> timestamp) {
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.message = message;
        this.timestamp = timestamp;
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

    public Map<String, String> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, String> timestamp) {
        this.timestamp = timestamp;
    }
}
