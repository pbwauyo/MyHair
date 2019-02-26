package comro.example.nssf.martin.dataModels;

public class ProfilePhoto {
    private String profilePicUrl;

    public ProfilePhoto() {
    }

    public ProfilePhoto(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
}
