package co.work.fukouka.happ.interfaces;

/**
 * Created by tokikawateppei on 01/08/2017.
 */

public interface Situation {
    String getUserId();

    String getLanguage();

    void getCongestion(String officeId);

    void getAvailableUser(String officeId);
}
