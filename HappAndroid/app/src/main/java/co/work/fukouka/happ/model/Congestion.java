package co.work.fukouka.happ.model;


public class Congestion {
    int id;
    String dateModified;
    String percentage;

    public Congestion() {
    }

    public Congestion(int id, String dateModified, String percentage) {
        this.id = id;
        this.dateModified = dateModified;
        this.percentage = percentage;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }
}
