package ng.com.teddinsight.teddinsightchat.models;


import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;


public class User implements Parcelable {
    public String id;
    private String username;
    public String email;
    private String firstName;
    private String lastName;
    private String deviceToken;
    public String role;
    public boolean hasAccess;
    public String profileImageUrl;
    private long dateEmployed;
    private String address;
    private String phoneNumber;
    private double salary;
    private long dateRegistered;
    private long unreadCount;
    private String lastMessage;
    private long timeStamp;
    private String businessName;

    @Exclude
    public Map<String, Object> toChatMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("id", id);
        result.put("unreadCount", unreadCount);
        result.put("lastMessage", lastMessage);
        result.put("timeStamp", timeStamp);
        result.put("profileImageUrl", this.profileImageUrl);
        return result;
    }


    public static final String USER_ADMIN = "Admin";
    public static final String USER_HR = "Human Resource";
    public static final String USER_DESIGNER = "Creative Designer";
    public static final String USER_CONTENT = "Content Curator";
    public static final String USER_CLIENT = "Client";
    public static final String USER_PARTNER = "Partner";
    public static final String USER_SOCIAL = "Social Media Manager";

    public User() {
    }


    public User(String id, String username) {
        this.id = id;
        this.username = username;
    }

    public User(String id, String profileImageUrl, String username, String firstName, String lastName, long unreadCount, String lastMessage) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
        this.timeStamp = System.currentTimeMillis() * (-1);
        this.id = id;
        this.profileImageUrl = profileImageUrl;
    }


    protected User(Parcel in) {
        id = in.readString();
        username = in.readString();
        email = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        deviceToken = in.readString();
        role = in.readString();
        hasAccess = in.readByte() != 0;
        profileImageUrl = in.readString();
        dateEmployed = in.readLong();
        address = in.readString();
        phoneNumber = in.readString();
        salary = in.readDouble();
        dateRegistered = in.readLong();
        unreadCount = in.readLong();
        lastMessage = in.readString();
        timeStamp = in.readLong();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public long getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(long dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isHasAccess() {
        return hasAccess;
    }

    public void setHasAccess(boolean hasAccess) {
        this.hasAccess = hasAccess;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public long getDateEmployed() {
        return dateEmployed;
    }

    public void setDateEmployed(long dateEmployed) {
        this.dateEmployed = dateEmployed;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public double getSalary() {
        return salary;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }


    public void setRole(String role) {
        this.role = role;
    }


    public static String getTableName() {
        return "Users";
    }

    @Exclude
    public Map<String, Object> toMap(boolean isUpdate) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("deviceToken", deviceToken);
        result.put("role", role);
        result.put("id", id);
        result.put("dateRegistered", isUpdate ? this.dateRegistered : ServerValue.TIMESTAMP);
        result.put("hasAccess", this.hasAccess);
        result.put("dateEmployed", dateEmployed);
        result.put("salary", salary);
        result.put("address", address);
        result.put("phoneNumber", phoneNumber);
        result.put("profileImageUrl", profileImageUrl);
        result.put("businessName", businessName);
        return result;
    }

    @Exclude
    public Map<String, String> toStringMap() {
        HashMap<String, String> result = new HashMap<>();
        result.put("username", username);
        result.put("email", email);
        result.put("firstName", firstName);
        result.put("lastName", lastName);
        result.put("deviceToken", deviceToken);
        result.put("role", role);
        result.put("id", id);
        result.put("hasAccess", this.hasAccess ? "true" : "false");
        result.put("profileImageUrl", this.profileImageUrl);
        result.put("businessName", businessName);
        return result;
    }


    public User(String id, String username, String firstName, String lastName, String email, String role, String deviceToken) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.deviceToken = deviceToken;
        this.role = role;
        this.hasAccess = true;
    }

    public static User unWrapUserString(String userString) {
        String[] s = userString.split(",");
        User u = new User("", "", s[0], s[1], s[2], "", "");
        u.profileImageUrl = s[3];
        return u;
    }

    @NonNull
    @Override
    public String toString() {
        return firstName.concat(",").concat(lastName).concat(",").concat(email).concat(",").concat(profileImageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(deviceToken);
        dest.writeString(role);
        dest.writeByte((byte) (hasAccess ? 1 : 0));
        dest.writeString(profileImageUrl);
        dest.writeLong(dateEmployed);
        dest.writeString(address);
        dest.writeString(phoneNumber);
        dest.writeDouble(salary);
        dest.writeLong(dateRegistered);
        dest.writeLong(unreadCount);
        dest.writeString(lastMessage);
        dest.writeLong(timeStamp);
    }
}