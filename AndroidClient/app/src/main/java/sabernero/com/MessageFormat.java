package sabernero.com;

public class MessageFormat {

    private String Username;
    private String Message;
    private String UniqueId;
    private String Room;

    public MessageFormat(String uniqueId, String username, String message, String room) {
        Username = username;
        Message = message;
        UniqueId = uniqueId;
        Room = room;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getUniqueId() {
        return UniqueId;
    }

    public void setUniqueId(String uniqueId) {
        UniqueId = uniqueId;
    }

    public String getRoom() {
        return Room;
    }

    public void setRoom(String room) {
        Room = room;
    }
}
