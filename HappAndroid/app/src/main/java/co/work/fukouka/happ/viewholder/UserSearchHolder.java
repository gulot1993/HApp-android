package co.work.fukouka.happ.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import co.work.fukouka.happ.R;

public class UserSearchHolder extends RecyclerView.ViewHolder {
    public ImageView ivUserPhoto;
    public TextView tvName;
    public TextView tvHappId;
    public TextView tvSkills;

    public UserSearchHolder(View itemView) {
        super(itemView);
        ivUserPhoto = (ImageView) itemView.findViewById(R.id.iv_user_photo);
        tvName = (TextView) itemView.findViewById(R.id.tv_name);
        tvHappId = (TextView) itemView.findViewById(R.id.tv_happ_id);
        tvSkills = (TextView) itemView.findViewById(R.id.tv_skills);
    }
}
