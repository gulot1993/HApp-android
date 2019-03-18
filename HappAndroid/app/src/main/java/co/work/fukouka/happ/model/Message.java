package co.work.fukouka.happ.model;

import java.util.Map;


public class Message {
    String chatroomId;
    String chatmateId;
    String userId;
    String name;
    String photoUrl;
    String message;
    String lastMessage;
    boolean read;
    Map<String, String> timestamp;

    public Message() {
    }


    public Message(String chatroomId, String chatmateId, String name, String photoUrl,
                   String lastMessage, Map<String, String> timestamp, boolean read) {

        this.chatroomId = chatroomId;
        this.chatmateId = chatmateId;
        this.name = name;
        this.photoUrl = photoUrl;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.read = read;
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

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public Map<String, String> getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Map<String, String> timestamp) {
        this.timestamp = timestamp;
    }
}
