package co.work.fukouka.happ.model;


public class Office {
    int officeId;
    String officeNameJp;
    String officeNameEn;

    public Office() {
    }

    public Office(int officeId, String officeNameJp, String officeNameEn) {
        this.officeId = officeId;
        this.officeNameJp = officeNameJp;
        this.officeNameEn = officeNameEn;
    }

    public int getOfficeId() {
        return officeId;
    }

    public void setOfficeId(int officeId) {
        this.officeId = officeId;
    }

    public String getOfficeNameJp() {
        return officeNameJp;
    }

    public void setOfficeNameJp(String officeNameJp) {
        this.officeNameJp = officeNameJp;
    }

    public String getOfficeNameEn() {
        return officeNameEn;
    }

    public void setOfficeNameEn(String officeNameEn) {
        this.officeNameEn = officeNameEn;
    }
}
