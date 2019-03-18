package co.work.fukouka.happ.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.NotifDetailsActivity;
import co.work.fukouka.happ.activity.ProfileActivity;
import co.work.fukouka.happ.activity.ReservedActivity;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.NotificationContent;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.JsonObjectRequest;


public class NotificationAdapter extends RecyclerView.Adapter {

    private List<NotificationContent> notifications = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private HappHelper mHelper;
    private DatabaseReference mDatabase;
    private Activity mActivity;
    private GetSystemValue mSystem;

    private AdapterListener mListener;
    private boolean isLoadingAdded = false;

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    public interface AdapterListener {
        void scrollToTop();
    }

    public NotificationAdapter(Context context, AdapterListener listener) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mHelper = new HappHelper(context);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mActivity = (Activity) context;
        mListener = listener;
        mSystem = new GetSystemValue(context);
    }

    private class NotificationHolder extends RecyclerView.ViewHolder {
        ImageView ivUserPhoto;
        TextView tvNotifDetail;
        TextView tvDate;

        private NotificationHolder(View itemView) {
            super(itemView);

            ivUserPhoto = (ImageView) itemView.findViewById(R.id.iv_user_photo);
            tvNotifDetail = (TextView) itemView.findViewById(R.id.tv_notif_detail);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
        }
    }

    private class LoadingHolder extends RecyclerView.ViewHolder {
        private LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    public void updateList(List<NotificationContent> notification) {
        for (NotificationContent content: notification) {
            notifications.add(content);
            notifyItemInserted(notifications.size() - 1);
        }
    }

    public void add(NotificationContent notification) {
        notifications.add(notification);
        notifyItemInserted(notifications.size() - 1);
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new NotificationContent());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = notifications.size() - 1;
        NotificationContent item = getItem(position);

        if (item != null) {
            notifications.remove(position);
            notifyItemRemoved(position);
        }
    }

    public NotificationContent getItem(int position) {
        return notifications.get(position);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case ITEM:
                view = mInflater.inflate(R.layout.notification_item_layout, parent, false);

                return new NotificationHolder(view);
            case LOADING:
                view = mInflater.inflate(R.layout.custom_loading_list_item, parent, false);

                return new LoadingHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM:
                final String notifId = notifications.get(position).getNotifId();
                final int id = notifications.get(position).getId();
                final String userId = notifications.get(position).getUserId();
                final String name = notifications.get(position).getName();
                final String photoUrl = notifications.get(position).getPhotoUrl();
                Long timestamp = notifications.get(position).getTimestamp();
                String type = notifications.get(position).getType();
                boolean read = notifications.get(position).isRead();

                final NotificationHolder notificationHolder = (NotificationHolder) holder;

                if (!read) {
                    notificationHolder.tvNotifDetail.setTypeface(Typeface.DEFAULT_BOLD);
                    notificationHolder.tvNotifDetail.setTextColor(ContextCompat.getColor(mContext, R.color.beta));
                } else {
                    notificationHolder.tvNotifDetail.setTypeface(Typeface.DEFAULT);
                    notificationHolder.tvNotifDetail.setTextColor(ContextCompat.getColor(mContext, R.color.charlie));
                }

                if (timestamp != null) {
                    String dateTime = mHelper.convertTimeWithTimeZome(timestamp);
                    String date = mHelper.getDateOnly(dateTime);
                    String time = mHelper.getTimeOnly(dateTime);
                    String completeTime = mHelper.getCompletetime(date, time);

                    int currentDate = Integer.parseInt(mHelper.getCurrentDate());
                    int notifDate = Integer.parseInt(mHelper.getMessageDate(timestamp));

                    if (currentDate == notifDate) {
                        mHelper.setText(notificationHolder.tvDate, mContext.getString(R.string.today) + " " + time);
                    } else {
                        if (currentDate - 1 == notifDate) {
                            mHelper.setText(notificationHolder.tvDate, mContext.getString(R.string.yesterday) + " " + time);
                        } else {
                            mHelper.setText(notificationHolder.tvDate, completeTime);
                        }
                    }
                }

        //        SpannableString name = new SpannableString(username);
        //        name.setSpan(new StyleSpan(Typeface.BOLD), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                mHelper.loadRoundImage(notificationHolder.ivUserPhoto, photoUrl);

                if (type != null) {
                    switch (type) {
                        case "timeline":
                            mHelper.setText(notificationHolder.tvNotifDetail, name +" " +mHelper.timelineMess());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    markNotifAsRead(notifId, notificationHolder);

                                    Intent intent = new Intent(mContext, NotifDetailsActivity.class);
                                    intent.putExtra("notif_id", id);
                                    intent.putExtra("name", name);
                                    intent.putExtra("photo_url", photoUrl);
                                    intent.putExtra("sender", "notification");
                                    mContext.startActivity(intent);
                                    mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                                }
                            });

                            break;
                        case "free-time":
                            mHelper.setText(notificationHolder.tvNotifDetail, name +" " +mHelper.freeTimeMess());

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    mHelper.showProgressDialog("", true);
                                    checkIfBlocked(userId, notifId, notificationHolder);
                                }
                            });
                            break;
                        case "reservation":
                            mHelper.setText(notificationHolder.tvNotifDetail, mHelper.reservationMess());

                            notificationHolder.ivUserPhoto.setImageDrawable(ContextCompat
                                    .getDrawable(mContext, R.mipmap.ic_calendar));
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    markNotifAsRead(notifId, notificationHolder);

                                    Intent intent = new Intent(mContext, ReservedActivity.class);
                                    intent.putExtra("sender", "notification");
                                    mContext.startActivity(intent);
                                    mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                                }
                            });
                            break;
                    }
                }

                break;
            case LOADING:
                LoadingHolder loadingHolder = (LoadingHolder) holder;
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == notifications.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    private void markNotifAsRead(String notifId, final NotificationHolder holder) {
        mDatabase.child("notifications").child("app-notification").child("notification-user")
                .child(getUFbId()).child("notif-list")
                .child(notifId).child("read").setValue(true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        holder.tvNotifDetail.setTypeface(Typeface.DEFAULT);
                        holder.tvNotifDetail.setTextColor(ContextCompat.getColor(mContext, R.color.charlie));
                    }
                });
    }

    private String getUFbId() {
        String userId = null;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }

    private void checkIfBlocked(final String fbUid, final String notifId, final NotificationHolder holder) {
        //Convert fbUid to wordpress id
        mDatabase.child("users").child(fbUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    int id = user.getId();

                    Map<String, String> params = new HashMap<>();
                    params.put("sercret","jo8nefamehisd");
                    params.put("action","api");
                    params.put("ac","get_block_list");
                    params.put("d","0");
                    params.put("lang", "en");
                    params.put("user_id", String.valueOf(id));

                    RequestQueue requestQueue = Volley.newRequestQueue(mContext);
                    JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        boolean isBlocked = false;
                                        JSONArray array = response.getJSONArray("result");
                                        if (array != null && array.length() > 0) {
                                            for (int i = 0; i < array.length(); i++) {
                                                JSONObject jsonObject = array.getJSONObject(i).getJSONObject("fields");
                                                String blockedId = jsonObject.getString("block_user_id");
                                                if (blockedId.equals(mHelper.getUserId())) {
                                                    isBlocked = true;
                                                    break;
                                                }
                                            }
                                        }

                                        if (!isBlocked) {
                                            mHelper.hideProgressDialog();
                                            viewUserProfile(fbUid, notifId, holder);
                                        } else {
                                            mHelper.hideProgressDialog();
                                            Toast.makeText(mContext, mHelper.notAllowedToView(), Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

                    requestQueue.add(jsObjRequest);
                } else {
                    mHelper.hideProgressDialog();
                    Toast.makeText(mContext, mHelper.notAllowedToView(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void viewUserProfile(String fbUid, final String notifId, final NotificationHolder holder) {
        mDatabase.child("users").child(fbUid).child("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Long id = (Long) dataSnapshot.getValue();
                            if (id != null) {
                                markNotifAsRead(notifId, holder);

                                Intent intent = new Intent(mContext, ProfileActivity.class);
                                intent.putExtra("sender", "notification");
                                intent.putExtra("user_id", String.valueOf(id));
                                mContext.startActivity(intent);
                                mActivity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
