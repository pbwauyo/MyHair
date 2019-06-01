package comro.example.nssf.martin.dataModels;

public class StylistDetails {
    private String name, email, contact, pswd, time;

    public StylistDetails() {
    }

    public StylistDetails(String name, String email, String contact,  String pswd, String time) {
        this.name = name;
        this.email = email;
        this.contact = contact;
        this.pswd = pswd;
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPswd() {
        return pswd;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
