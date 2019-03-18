package co.work.fukouka.happ.viewholder;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.ChatRoomActivity;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.MessageContent;


public class MessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public ImageView ivUserPhoto;
    public TextView tvName;
    private TextView tvMessage;
    public TextView tvDate;
    public ConstraintLayout mainLayout;

    private View mView;
    private Context mContext;
    private HappHelper mHelper;
    private DatabaseReference mDatabase;
    private Activity activity;

    private String mChatroomId;
    private String mChatmateId;
    private String mName;
    private String mPhotoUrl;
    private Boolean blocked;

    public MessageHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mContext = itemView.getContext();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mHelper = new HappHelper(itemView.getContext());
        activity = (Activity) itemView.getContext();
        itemView.setOnClickListener(this);

    }

    public void bindMessages(MessageContent message) {
        ivUserPhoto =  mView.findViewById(R.id.iv_user_photo);
        tvName =  mView.findViewById(R.id.tv_name);
        tvMessage =  mView.findViewById(R.id.tv_message);
        tvDate = mView.findViewById(R.id.tv_date);
        mainLayout = mView.findViewById(R.id.main_layout);

        mChatroomId = message.getChatroomId();
        mChatmateId = message.getChatmateId();
        mName = message.getName();
        mPhotoUrl = message.getPhotoUrl();
        String lastMessage = message.getLastMessage();
        Long timestamp = message.getTimestamp();
        boolean isRead = message.isRead();


        //get the date of the message
        String dateTime = mHelper.convertTimeWithTimeZome(timestamp);
        String date = mHelper.getDateOnly(dateTime);
        String time = mHelper.getTimeOnly(dateTime);
        String completeTime = mHelper.getCompletetime(date, time);

        //get current day of the current date
        int currentDate = Integer.parseInt(mHelper.getCurrentDate());
        int messageDate = Integer.parseInt(mHelper.getMessageDate(timestamp));

        if (!isRead) {
            tvMessage.setTypeface(Typeface.DEFAULT_BOLD);
            tvMessage.setTextColor(ContextCompat.getColor(mContext, R.color.beta));
        } else {
            tvMessage.setTypeface(Typeface.DEFAULT);
            tvMessage.setTextColor(ContextCompat.getColor(mContext, R.color.charlie));
        }

        mHelper.loadRoundImage(ivUserPhoto, mPhotoUrl);
        mHelper.setText(tvName, mName);
        mHelper.setText( tvMessage, lastMessage);

        if (currentDate == messageDate) {
            mHelper.setText(tvDate, mHelper.today() + " " + time);
        } else {
            if (currentDate - 1 == messageDate) {
                mHelper.setText(tvDate, mHelper.yesterday() + " " + time);
            } else {
                mHelper.setText( tvDate, completeTime);
            }
        }

        checkIfblocked(mChatroomId);
    }

    @Override
    public void onClick(View view) {
        int position = getLayoutPosition();
        //mark message as read
        markMessageAsRead(mChatroomId);

        Intent intent = new Intent(mContext, ChatRoomActivity.class);
        intent.putExtra("chatroom_id", mChatroomId);
        intent.putExtra("chatmate_id", mChatmateId);
        intent.putExtra("chatmate_name", mName);
        intent.putExtra("chatmate_photoUrl", mPhotoUrl);
        intent.putExtra("blocked", blocked);
        mContext.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

        System.out.println("Photo " +mPhotoUrl + " Name " +mName);
    }

    private void markMessageAsRead(String chatroomId) {
        final DatabaseReference ref = mDatabase.child("chat").child("last-message").child(getUserId())
                .child(chatroomId).child("read");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean read = (boolean) dataSnapshot.getValue();
                        if (!read) {
                            ref.setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    NotificationManager notificationManager = (NotificationManager)
                                            mContext.getApplicationContext()
                                            .getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.cancelAll();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void checkIfblocked(final String chatroomId) {
        mDatabase.child("chat").child("members").child(chatroomId).child("blocked")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        blocked = (Boolean) dataSnapshot.getValue();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private String getUserId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }
}
