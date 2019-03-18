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

public class OfficeSpinAdapter extends ArrayAdapter<RoomOffice> {

    private LayoutInflater mInflater;
    private List<RoomOffice> officeList = new ArrayList<>();

    private SessionManager mSession;

    public OfficeSpinAdapter(@NonNull Context context, @LayoutRes int resource,
                             List<RoomOffice> officeList) {
        super(context, resource, officeList);
        this.officeList = officeList;
        this.mInflater = LayoutInflater.from(context);
        mSession = new SessionManager(context);
    }

    @Override
    public int getCount() {
        return officeList.size();
    }

    @Nullable
    @Override
    public RoomOffice getItem(int position) {
        return officeList.get(position);
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

            holder.officeName = convertView.findViewById(R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String language = getLanguage();
        if(language.equals("jp")) {
            holder.officeName.setText(officeList.get(position).getRoomNameJp());
        } else {
            holder.officeName.setText(officeList.get(position).getRoomNameEn());
        }

        return convertView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.layout_spinner_item, parent, false);

            holder.officeName = convertView.findViewById(R.id.tv_name);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String language = getLanguage();
        if(language.equals("jp")) {
            holder.officeName.setText(officeList.get(position).getRoomNameJp());
        } else {
            holder.officeName.setText(officeList.get(position).getRoomNameEn());
        }

        return convertView;
    }

    public static class ViewHolder {
        TextView officeName;
    }

    private String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }
}
