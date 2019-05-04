package comro.example.nssf.martin.dataModels;

public class Booking {
    private String styleImage, customerName, styleName, status, id, phoneNumber;

    public Booking(String styleImage, String customerName, String styleName, String status, String id, String phoneNumber) {
        this.styleImage = styleImage;
        this.customerName = customerName;
        this.styleName = styleName;
        this.status = status;
        this.id = id;
        this.phoneNumber = phoneNumber;
    }

    public String getStyleImage() {
        return styleImage;
    }

    public void setStyleImage(String styleImage) {
        this.styleImage = styleImage;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
