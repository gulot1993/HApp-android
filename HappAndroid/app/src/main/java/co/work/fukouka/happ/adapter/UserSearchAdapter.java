package co.work.fukouka.happ.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.activity.ProfileActivity;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.interfaces.LoadResource;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.viewholder.UserSearchHolder;


public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchHolder>{
    private List<User> users = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private HappHelper mHelper;

    public UserSearchAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mHelper = new HappHelper(context);
    }

    public void updateList(List<User> userList) {
        users.clear();
        users = userList;
        notifyDataSetChanged();
    }

    public void clearList() {
        int size = this.users.size();
        this.users.clear();
        notifyItemRangeRemoved(0, size);
    }

    @Override
    public UserSearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.card_user_layout, parent, false);
        UserSearchHolder holder = new UserSearchHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(UserSearchHolder holder, int position) {
        final String userId = String.valueOf(users.get(position).getId());
        String name = users.get(position).getName();
        String photoUrl = users.get(position).getPhotoUrl();
        String skills = users.get(position).getSkills();
        String skillValues = convertKeysToValues(skills);
        String happId = users.get(position).getHappId();

        holder.ivUserPhoto.setImageDrawable(null);
        mHelper.loadRoundImage(holder.ivUserPhoto, photoUrl);
        mHelper.setText(holder.tvName, name);
        mHelper.setText(holder.tvSkills, skillValues);
        mHelper.setText(holder.tvHappId, happId);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("user_id", userId);
                mContext.startActivity(intent);
                ((Activity) mContext).overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    private String convertKeysToValues(String skills) {
        String[] skillKey = skills.split(",");
        String skillValue = "";

        for (String keys: skillKey) {
            skillValue += new GetSystemValue(mContext).getValue(keys) != null ?
                    new GetSystemValue(mContext).getValue(keys) + ", " : "";
        }

        return !skillValue.equals("") ? skillValue.substring(0, skillValue.length() - 2) : skillValue;
    }
}
