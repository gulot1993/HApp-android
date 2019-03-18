package co.work.fukouka.happ.model;

import java.util.List;

public class ReservationList {
    String reservedDate;
    List<Reservation> reservationList;

    public ReservationList() {
    }

    public ReservationList(String reservedDate, List<Reservation> reservationList) {
        this.reservedDate = reservedDate;
        this.reservationList = reservationList;
    }

    public String getReservedDate() {
        return reservedDate;
    }

    public void setReservedDate(String reservedDate) {
        this.reservedDate = reservedDate;
    }

    public List<Reservation> getReservationList() {
        return reservationList;
    }

    public void setReservationList(List<Reservation> reservationList) {
        this.reservationList = reservationList;
    }
}
