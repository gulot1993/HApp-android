package co.work.fukouka.happ.model;


import java.util.List;

public class ReservationCopy {
    Office office;
    Room room;
    List<Reservation> reservationList;

    public ReservationCopy() {
    }

    public ReservationCopy(Office office, Room room, List<Reservation> reservationList) {
        this.office = office;
        this.room = room;
        this.reservationList = reservationList;
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

    public List<Reservation> getReservationList() {
        return reservationList;
    }

    public void setReservationList(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }
}
