package comro.example.nssf.martin.dataModels;

public class Message {
    private String message;
    private String senderName;
    private String senderId;
    private String senderImage;
    private String receiverName;
    private String receiverId;
    private String messageId;
    private String status;
    private String styleName;
    private String styleImage, senderContact, receiverContact;

    public Message(String message, String senderName, String senderId, String senderImage, String receiverName, String receiverId, String messageId, String status, String styleName, String styleImage, String senderContact, String receiverContact) {
        this.message = message;
        this.senderName = senderName;
        this.senderId = senderId;
        this.senderImage = senderImage;
        this.receiverName = receiverName;
        this.receiverId = receiverId;
        this.messageId = messageId;
        this.status = status;
        this.styleName = styleName;
        this.styleImage = styleImage;
        this.senderContact = senderContact;
        this.receiverContact = receiverContact;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStyleName() {
        return styleName;
    }

    public void setStyleName(String styleName) {
        this.styleName = styleName;
    }

    public String getStyleImage() {
        return styleImage;
    }

    public void setStyleImage(String styleImage) {
        this.styleImage = styleImage;
    }

    public String getSenderContact() {
        return senderContact;
    }

    public void setSenderContact(String senderContact) {
        this.senderContact = senderContact;
    }

    public String getReceiverContact() {
        return receiverContact;
    }

    public void setReceiverContact(String receiverContact) {
        this.receiverContact = receiverContact;
    }
}
