package ng.com.teddinsight.teddinsightchat;

public class Notifications {

    public int count;
    public String message;
    public String type;
    public boolean newTaskReceived;

    public static String getTableName() {
        return "notifications";
    }

    public Notifications() {
    }

}
