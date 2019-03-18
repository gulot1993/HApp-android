package co.work.fukouka.happ.view;

import co.work.fukouka.happ.model.Congestion;
import co.work.fukouka.happ.model.User;

public interface SituationView {
    void loadCongestion(Congestion congestion);

    void loadAvailableUser(User user);

    void isLoaded();

    void clearList();

}
