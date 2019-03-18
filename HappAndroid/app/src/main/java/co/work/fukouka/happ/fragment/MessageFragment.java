package co.work.fukouka.happ.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.work.fukouka.happ.R;
import co.work.fukouka.happ.helper.GetSystemValue;
import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.MessageContent;
import co.work.fukouka.happ.viewholder.MessageHolder;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageFragment extends Fragment {

    @BindView(R.id.recycler_view) RecyclerView rv;
    @BindView(R.id.tb_title) TextView tbTitle;

    private FirebaseRecyclerAdapter mAdapter;
    private DatabaseReference mDatabase;

    public MessageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        ButterKnife.bind(this, view);
        assignSystemValues();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setReverseLayout(true);
        manager.setStackFromEnd(true);

        rv.setLayoutManager(manager);

        Query mRef = FirebaseDatabase.getInstance().getReference()
                .child("chat").child("last-message")
                .child(getUserId()).orderByChild("timestamp");

        setUpFirebaseAdapter(mRef);

        return view;
    }

    private void setUpFirebaseAdapter(Query ref) {
        mAdapter = new FirebaseRecyclerAdapter<MessageContent, MessageHolder>
                (MessageContent.class, R.layout.card_message_list, MessageHolder.class, ref) {

            @Override
            protected void populateViewHolder(final MessageHolder viewHolder, final MessageContent model, int position) {
                viewHolder.bindMessages(model);
            }
        };

        rv.setAdapter(mAdapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
    }

    public String getUserId() {
        String userId = null;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }

    private void assignSystemValues() {
        HappHelper helper = new HappHelper(getActivity());
        GetSystemValue systemValue = new GetSystemValue(getActivity());

        String message = systemValue.getValue("title:message");

        helper.setText(tbTitle, message);
    }
}
