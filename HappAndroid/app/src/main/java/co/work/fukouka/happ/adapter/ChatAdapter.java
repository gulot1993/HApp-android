package co.work.fukouka.happ.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.ProfileActivity;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.MessageContent;
import co.work.fukouka.happ.utils.CustomLoadResource;

public class ChatAdapter extends RecyclerView.Adapter {
    List<MessageContent> messages = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private CustomLoadResource mRes;
    private HappHelper mHelper;
    private DatabaseReference mDatabase;

    private final int CURRENT_USER = 0;
    private final int CHATMATE = 1;
    private static final int LOADING = 3;

    private boolean isBlocked;
    private boolean isLoadingAdded = false;
    private String mPhotoUrl;

    private static class PrimaryViewHolder extends RecyclerView.ViewHolder{
        TextView tvMessage;
        TextView tvDate;
        ImageView ivUserThumb;

        PrimaryViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
        }
    }

    private static class SecondaryViewHolder extends RecyclerView.ViewHolder{
        TextView tvMessage;
        TextView tvDate;
        ImageView ivUserThumb;

        SecondaryViewHolder(View itemView) {
            super(itemView);
            tvMessage = (TextView) itemView.findViewById(R.id.tv_message);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            ivUserThumb = (ImageView) itemView.findViewById(R.id.iv_user_thumb);
        }
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {
        private LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    public ChatAdapter(Context context) {
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mRes = new CustomLoadResource(context);
        mHelper = new HappHelper(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public void updateList(MessageContent message){
        messages.add(message);
        notifyItemInserted(messages.size());
    }

    public void updateList(List<MessageContent> message) {
        for (MessageContent content: message) {
            messages.add(content);
            notifyItemInserted(messages.size() - 1);
        }
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new MessageContent());
    }

    public void add(MessageContent message) {
        messages.add(3, message);
        notifyItemInserted(3);
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = messages.size() - 1;
        MessageContent item = getItem(position);

        if (item != null) {
            messages.remove(position);
            notifyItemRemoved(position);
        }
    }

    public MessageContent getItem(int position) {
        return messages.get(position);
    }

    public void isBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case CURRENT_USER:
                view = mInflater.inflate(R.layout.primary_chat_message, parent, false);
                return new PrimaryViewHolder(view);

            case CHATMATE:
                view = mInflater.inflate(R.layout.secondary_chat_message, parent, false);
                return new SecondaryViewHolder(view);

            case LOADING:
                view = mInflater.inflate(R.layout.custom_loading_list_item, parent, false);

                return new LoadingHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case CURRENT_USER:
                PrimaryViewHolder primaryHolder = (PrimaryViewHolder) holder;

                final String userId = messages.get(position).getUserId();
                String message = messages.get(position).getMessage();
                String photoUrl = messages.get(position).getPhotoUrl();
                Long timestamp = messages.get(position).getTimestamp();

                int currentDate = 0;
                int messageDate = 0;
                String time = null;
                String completeTime = null;

                //get the date of the message
                if (timestamp != null) {
                    String dateTime = mHelper.convertTimeWithTimeZome(timestamp);
                    String date = mHelper.getDateOnly(dateTime);
                    time = mHelper.getTimeOnly(dateTime);
                    completeTime = mHelper.getCompletetime(date, time);

                    //get current day of the current date
                    currentDate = Integer.parseInt(mHelper.getCurrentDate());
                    messageDate = Integer.parseInt(mHelper.getMessageDate(timestamp));
                }

                mRes.loadRoundImage(photoUrl, primaryHolder.ivUserThumb);
                mRes.loadText(message, primaryHolder.tvMessage);

                if (time != null && currentDate == messageDate) {
                    mRes.loadText(mHelper.today() + " " + time, primaryHolder.tvDate);
                } else {
                    if (currentDate - 1 == messageDate) {
                        mRes.loadText(mHelper.yesterday() + " " + time, primaryHolder.tvDate);
                    } else {
                        mRes.loadText(completeTime, primaryHolder.tvDate);
                    }
                }

                primaryHolder.ivUserThumb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getUserHappId(userId);
                    }
                });

                break;

            case CHATMATE:
                SecondaryViewHolder secondaryHolder = (SecondaryViewHolder) holder;

                final String userId1 = messages.get(position).getUserId();
                String message1 = messages.get(position).getMessage();
                String photoUrl1 = messages.get(position).getPhotoUrl();
                Long timestamp1 = messages.get(position).getTimestamp();

                if (mPhotoUrl == null) {
                    mPhotoUrl = photoUrl1;
                }

                int currentDate1 = 0;
                int messageDate1 = 0;
                String time1 = null;
                String completeTime1 = null;

                //get the date of the message
                if (timestamp1 != null) {
                    String dateTime = mHelper.convertTimeWithTimeZome(timestamp1);
                    String date = mHelper.getDateOnly(dateTime);
                    time1 = mHelper.getTimeOnly(dateTime);
                    completeTime1 = mHelper.getCompletetime(date, time1);

                    //get current day of the current date
                    currentDate1 = Integer.parseInt(mHelper.getCurrentDate());
                    messageDate1 = Integer.parseInt(mHelper.getMessageDate(timestamp1));
                }


                if (mPhotoUrl != null) {
                    mRes.loadRoundImage(mPhotoUrl, secondaryHolder.ivUserThumb);
                } else {
                    mRes.loadRoundImage(photoUrl1, secondaryHolder.ivUserThumb);
                }

                mRes.loadText(message1, secondaryHolder.tvMessage);

                if (time1 != null && currentDate1 == messageDate1) {
                    mRes.loadText(mContext.getString(R.string.today) + " " + time1, secondaryHolder.tvDate);
                } else {
                    if (currentDate1 - 1 == messageDate1) {
                        mRes.loadText(mContext.getString(R.string.yesterday) + " " + time1, secondaryHolder.tvDate);
                    } else {
                        mRes.loadText(completeTime1, secondaryHolder.tvDate);
                    }
                }

//                if (!isBlocked) {
//                    secondaryHolder.ivUserThumb.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            getUserHappId(userId1);
//                        }
//                    });
//                } else {
//                    secondaryHolder.ivUserThumb.setClickable(false);
//                }

                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        String messageAuthorId  = messages.get(position).getUserId();
        String currentUid = getCurrentUserId();

        if (position == messages.size() - 1 && isLoadingAdded) {
            return LOADING;
        } else {
            if (messageAuthorId != null && messageAuthorId.equals(currentUid)) {
                return CURRENT_USER;
            } else {
                return CHATMATE;
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String getCurrentUserId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }

    public void getUserHappId(String fbId) {
        mDatabase.child("users").child(fbId).child("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String happId = dataSnapshot.getValue().toString();
                        viewProfile(happId);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void viewProfile(String userId) {
        Intent intent = new Intent(mContext, ProfileActivity.class);
        intent.putExtra("user_id", userId);
        mContext.startActivity(intent);
    }
}

