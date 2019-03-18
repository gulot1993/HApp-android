package co.work.fukouka.happ.model;

public class RoomOffice {
    int id;
    String roomNameJp;
    String roomNameEn;

    public RoomOffice() {
    }

    public RoomOffice(int id, String roomNameJp, String roomNameEn) {
        this.id = id;
        this.roomNameJp = roomNameJp;
        this.roomNameEn = roomNameEn;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
