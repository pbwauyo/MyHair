package comro.example.nssf.martin.dataModels;

import java.io.Serializable;

public class Style implements Serializable{
    private String name;
    private String gender;
    private String cost;
    private String image;
    private String id;
    private String salonId;


    public Style(String name, String gender, String cost,String image, String id, String salonId) {
        this.name = name;
        this.gender = gender;
        this.cost = cost;
        this.image =image;
        this.id = id;
        this.salonId = salonId;
    }

    public Style(String name, String cost, String image) {
        this.name = name;
        this.cost = cost;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getimage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSalonId() {
        return salonId;
    }

    public void setSalonId(String salonId) {
        this.salonId = salonId;
    }
}
