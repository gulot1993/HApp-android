package co.work.fukouka.happ.model;

import java.util.List;

public class Reservation {
    int resId;
    String authorId;
    String date;
    String startTime;
    String endTime;
    String time;
    String officeName;
    String roomName;
    List<Reservation> reservationList;
    ReservationList reservation;
    Office office;
    Room room;

    public Reservation() {
    }

    public Reservation(int resId, String authorId, String time) {
        this.resId = resId;
        this.authorId = authorId;
        this.time = time;
    }

    public Reservation(String date, String startTime, String endTime) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Reservation(int resId, String authorId, String date, String time) {
        this.resId = resId;
        this.authorId = authorId;
        this.date = date;
        this.time = time;
    }

    public Reservation(String date, List<Reservation> reservationList) {
        this.date = date;
        this.reservationList = reservationList;
    }

    public Reservation(Office office, Room room, String date, List<Reservation> reservationList) {
        this.office = office;
        this.room = room;
        this.date = date;
        this.reservationList = reservationList;
    }

    public Reservation(String date, List<Reservation> reservationList, String officeName, String roomName) {
        this.date = date;
        this.reservationList = reservationList;
        this.officeName = officeName;
        this.roomName  = roomName;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getDate() { return date; }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Reservation> getReservationList() {
        return reservationList;
    }

    public void setReservationList(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }

    public String getOfficeName() {
        return officeName;
    }

    public void setOfficeName(String officeName) {
        this.officeName = officeName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public ReservationList getReservation() {
        return reservation;
    }

    public void setReservation(ReservationList reservation) {
        this.reservation = reservation;
    }

    public Office getOffice() {
        return office;
    }

    public void setOffice(Office office) {
        this.office = office;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }
}
