package co.work.fukouka.happ.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class NotificationContent {
    String notifId;
    int id;
    String userId;
    String name;
    String photoUrl;
    String type;
    Long timestamp;
    boolean read;

    public NotificationContent() {
    }

    public NotificationContent(int id, String userId, String name, String photoUrl, String type,
                        Long timestamp) {

        this.id = id;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.timestamp = timestamp;

    }

    public NotificationContent(int id, String userId, String name, String photoUrl, String type,
                               Long timestamp, boolean read) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.type = type;
        this.timestamp = timestamp;
        this.read = read;
    }

    public String getNotifId() {
        return notifId;
    }

    public void setNotifId(String notifId) {
        this.notifId = notifId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
