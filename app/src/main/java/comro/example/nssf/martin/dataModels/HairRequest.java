package comro.example.nssf.martin.dataModels;

public class HairRequest {
    private String styleImageUrl, stylistName, styleName, phoneNumber, id, status;

    public HairRequest(String styleImageUrl, String stylistName, String styleName, String phoneNumber, String id, String status) {
        this.styleImageUrl = styleImageUrl;
        this.stylistName = stylistName;
        this.styleName = styleName;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.status = status;
    }

    public String getStyleImageUrl() {
        return styleImageUrl;
    }

    public void setStyleImageUrl(String styleImageUrl) {
        this.styleImageUrl = styleImageUrl;
    }

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
