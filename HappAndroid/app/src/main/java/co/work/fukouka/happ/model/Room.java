package co.work.fukouka.happ.model;

public class Room {
    int roomId;
    String roomNameJp;
    String roomNameEn;

    public Room() {
    }

    public Room(int roomId, String roomNameJp, String roomNameEn) {
        this.roomId = roomId;
        this.roomNameJp = roomNameJp;
        this.roomNameEn = roomNameEn;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomNameJp() {
        return roomNameJp;
    }

    public void setRoomNameJp(String roomNameJp) {
        this.roomNameJp = roomNameJp;
    }

    public String getRoomNameEn() {
        return roomNameEn;
    }

    public void setRoomNameEn(String roomNameEn) {
        this.roomNameEn = roomNameEn;
    }
}
