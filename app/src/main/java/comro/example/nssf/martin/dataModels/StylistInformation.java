package comro.example.nssf.martin.dataModels;

public class StylistInformation {
    private String styleName, salonName, styleGender, styleCost, imageUrl, stylistId, salonId;

    public StylistInformation() {
    }

    public StylistInformation(String styleName, String salonName, String styleGender, String styleCost, String imageUrl, String stylistId, String salonId ) {
        this.styleName = styleName;
        this.salonName = salonName;
        this.styleGender = styleGender;
        this.styleCost = styleCost;
        this.imageUrl = imageUrl;
        this.stylistId = stylistId;
        this.salonId = salonId;
    }


    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getSalonName() {
        return salonName;
    }

    public void setSalonName(String salonName) {
        this.salonName = salonName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStyleGender() {
        return styleGender;
    }

    public void setStyleGender(String styleGender) {
        this.styleGender = styleGender;
    }

    public String getStyleCost() {
        return styleCost;
    }

    public void setStyleCost(String styleCost) {
        this.styleCost = styleCost;
    }

    public String getStylistId() {
        return stylistId;
    }

    public void setStylistId(String stylistId) {
        this.stylistId = stylistId;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }
}
