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
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.interfaces.LoadResource;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.utils.CustomLoadResource;
import co.work.fukouka.happ.viewholder.CongestionHolder;


public class CongestionAdapter extends RecyclerView.Adapter<CongestionHolder> {
    private List<User> users = new ArrayList<>();
    private Context mContext;
    private LayoutInflater mInflater;
    private HappHelper mHelper;
    private Activity mActivity;

    public CongestionAdapter(Context context) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        mHelper = new HappHelper(context);
        mActivity = (Activity) context;
    }

    public void addUser(User user) {
        if (!contains(user.getId())) {
            users.add(user);
            notifyItemInserted(users.size() - 1);
        }
    }

    public void clearList() {
        int size = this.users.size();
        this.users.clear();
        notifyItemRangeRemoved(0, size);
    }

    private boolean contains(int id) {
        for (User user: users) {
            if (user.getId() == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public CongestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_user_list, parent, false);

        return new CongestionHolder(view);
    }

    @Override
    public void onBindViewHolder(CongestionHolder holder, int position) {
        final String id = String.valueOf(users.get(position).getId());
        String name = users.get(position).getName();
        String photoUrl = users.get(position).getPhotoUrl();

        mHelper.setText(holder.tvName, name);
        mHelper.loadRoundImage(holder.ivUserPhoto, photoUrl);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("user_id", id);
                mContext.startActivity(intent);
                mActivity.overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
