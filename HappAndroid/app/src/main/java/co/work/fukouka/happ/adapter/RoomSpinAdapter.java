package co.work.fukouka.happ.adapter;


import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import co.work.fukouka.happ.R;
import co.work.fukouka.happ.model.RoomOffice;
import co.work.fukouka.happ.utils.SessionManager;

public class RoomSpinAdapter extends ArrayAdapter<RoomOffice> {
    private LayoutInflater mInflater;
    private List<RoomOffice> roomList = new ArrayList<>();

    private SessionManager mSession;

    public RoomSpinAdapter(@NonNull Context context, @LayoutRes int resource,
                           List<RoomOffice> roomList) {
        super(context, resource, roomList);
        this.roomList = roomList;
        this.mInflater = LayoutInflater.from(context);
        mSession = new SessionManager(context);
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Nullable
    @Override
    public RoomOffice getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_spinner_item, parent, false);

            holder.roomName = convertView.findViewById(R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.roomName.setText(roomList.get(position).getRoomNameEn());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_spinner_item, parent, false);

            holder.roomName = convertView.findViewById(R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String language = getLanguage();
        if(language.equals("jp")) {
            holder.roomName.setText(roomList.get(position).getRoomNameJp());
        } else {
            holder.roomName.setText(roomList.get(position).getRoomNameEn());
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView roomName;
    }

    private String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }
}
