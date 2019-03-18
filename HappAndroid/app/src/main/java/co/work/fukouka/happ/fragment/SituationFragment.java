package co.work.fukouka.happ.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.adapter.CongestionAdapter;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Congestion;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.presenter.SituationPresenter;
import co.work.fukouka.happ.view.SituationView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SituationFragment extends Fragment implements
        SituationView {

    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.layout_image) LinearLayout layoutImage;
    @BindView(R.id.tv_percentage) TextView tvPercentage;
    @BindView(R.id.tv_congestion) TextView tvCongestion;
    @BindView(R.id.tv_now_free) TextView tvNowFree;
    @BindView(R.id.tb_title) TextView tbTitle;

    private CongestionAdapter mAdapter;
    private SituationPresenter mPresenter;
    private DatabaseReference mDatabase;

    private boolean isLoaded;
    private boolean dataChanged;

    public SituationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_situation, container, false);
        ButterKnife.bind(this, view);
        assignSystemValues();

        mAdapter = new CongestionAdapter(getActivity());

        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setAdapter(mAdapter);

        mPresenter = new SituationPresenter(getActivity(), this);
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //get congestion
        String officeId = "32";
        mPresenter.getCongestion(officeId);
        mPresenter.getAvailableUser(officeId);

        checkDataChange();

        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && isLoaded) {
            mPresenter.refreshAvailableUser("32", dataChanged);
            if (dataChanged)
                dataChanged = false;
        }
    }

    @Override
    public void loadCongestion(Congestion congestion) {
        String percentage = congestion.getPercentage();
        if (percentage != null) {
            changeImageColorRating(Integer.parseInt(percentage));
            String value = percentage +"%";
            tvPercentage.setText(value);
        }
    }

    @Override
    public void loadAvailableUser(User user) {
        isLoaded = true;
        mAdapter.addUser(user);
    }

    @Override
    public void isLoaded() {
        isLoaded = true;
    }

    @Override
    public void clearList() {
        mAdapter.clearList();
    }

    private void changeImageColorRating(int percentage) {
        float percent = (float) percentage / 100;
        float imageWidth = 275 * percent;

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 146,
                getResources().getDisplayMetrics());
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, imageWidth,
                getResources().getDisplayMetrics());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
        layoutImage.setLayoutParams(params);

    }

    private void checkDataChange() {
        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataChanged = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(getActivity());
        GetSystemValue systemValue = new GetSystemValue(getActivity());

        String situation = systemValue.getValue("menu_situation");
        String congesSituation = systemValue.getValue("label_congestion_situation");
        String imFree = systemValue.getValue("text_now_free");

        helper.setText(tbTitle, situation);
        helper.setText(tvCongestion, congesSituation);
        helper.setText(tvNowFree, imFree);
    }
}
