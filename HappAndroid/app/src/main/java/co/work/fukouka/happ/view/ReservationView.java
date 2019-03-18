package co.work.fukouka.happ.view;

import java.util.List;

import co.work.fukouka.happ.model.RoomOffice;
import co.work.fukouka.happ.model.Reservation;

public interface ReservationView {
    void onLoadReservation(Reservation reservation);

    void onLoadReservation(String officeName, String roomName, List<Reservation> reservations);

    void onLoadMeetingRoom(List<RoomOffice> room);

    void onLoadMeetingRoom(RoomOffice room, RoomOffice office);

    void onLoadOffice(RoomOffice office);

    void onLoadOffice(List<RoomOffice> office);

    void onSuccess(String message);

    void onFailed(String message);
}
