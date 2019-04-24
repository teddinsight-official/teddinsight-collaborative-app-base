package ng.com.teddinsight.teddinsightchat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ng.com.teddinsight.teddinsightchat.models.User;

public class ExtraUtils {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    public static int getColor(int status) {
        switch (status) {
            case 0:
                return Color.parseColor("#F44336");
            case 1:
                return Color.parseColor("#4CAF50");
            default:
                return Color.parseColor("#F44336");
        }
    }

    public static String getStatText(int status) {
        switch (status) {
            case 0:
                return "Incomplete";
            case 1:
                return "Complete";
            default:
                return "Incomplete";
        }
    }

    public static Spannable getSpannableText(String text, int color, int start, int end) {
        Spannable wordtoSpan = new SpannableString(text);
        wordtoSpan.setSpan(new ForegroundColorSpan(color), start, end,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return wordtoSpan;
    }

    public static String getHumanReadableString(long timestamp) {
        DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm a", Locale.ENGLISH);
        return df.format(new Date(timestamp));
    }

    public static String getHumanReadableString(long timestamp, boolean noTime) {
        String format;

        if (noTime)
            format = "EEE, d MMM yyyy";
        else
            format = "EEE, d MMM yyyy, HH:mm a";
        DateFormat df = new SimpleDateFormat(format, Locale.ENGLISH);
        return df.format(new Date(timestamp));
    }

    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }

    public static void registerRevokeListener(Activity activity, Class destinationClass) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String id = user.getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(User.getTableName()).child(id).child("hasAccess");
            ValueEventListener revokeEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Log.e("APP TAG", dataSnapshot.getValue().toString());
                    if (!(boolean) dataSnapshot.getValue()) {
                        FirebaseAuth.getInstance().signOut();
                        Intent i = new Intent(activity, destinationClass);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("revoked", "revoke");
                        activity.startActivity(i);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };
            ref.addValueEventListener(revokeEventListener);
        }
    }

}
