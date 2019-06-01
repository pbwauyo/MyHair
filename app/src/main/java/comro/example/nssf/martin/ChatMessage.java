package comro.example.nssf.martin;

public class ChatMessage {
    private String body, senderId, senderImageUrl, imageMessage, senderName;

    public ChatMessage() {
    }

    public ChatMessage(String body, String senderId, String senderImageUrl, String imageMessage, String senderName) {
        this.body = body;
        this.senderId = senderId;
        this.senderImageUrl = senderImageUrl;
        this.imageMessage = imageMessage;
        this.senderName = senderName;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderImageUrl() {
        return senderImageUrl;
    }

    public void setSenderImageUrl(String senderImageUrl) {
        this.senderImageUrl = senderImageUrl;
    }

    public String getImageMessage() {
        return imageMessage;
    }

    public void setImageMessage(String imageMessage) {
        this.imageMessage = imageMessage;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }
}
