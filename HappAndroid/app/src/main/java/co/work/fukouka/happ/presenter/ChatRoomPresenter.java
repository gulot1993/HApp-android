package co.work.fukouka.happ.presenter;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import co.work.fukouka.happ.model.ChatMessage;
import co.work.fukouka.happ.model.Message;
import co.work.fukouka.happ.model.MessageContent;
import co.work.fukouka.happ.model.NotifMessage;
import co.work.fukouka.happ.model.User;
import co.work.fukouka.happ.view.ChatRoomView;


public class ChatRoomPresenter {

    private Context mContext;
    private ChatRoomView mView;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private String chatroomId;
    private String chatmateId;
    private String chatmateName;
    private String chatmatePhotoUrl;
    private String currentUname;
    private String currentUid;
    private String currentUphotoUrl;

    private boolean hasLoadedFirst;


    public ChatRoomPresenter(ChatRoomView view) {
        this.mView = view;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

    }
    public ChatRoomPresenter(Context context, ChatRoomView view) {
        this.mContext = context;
        this.mView = view;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Returns user's firebase id
     */
    public String getCurrentUid() {
        String userId = null;
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        return userId;
    }

    public void getCurrentUinfo() {
        mDatabase.child("users").child(getCurrentUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            User user = dataSnapshot.getValue(User.class);
                            currentUid = dataSnapshot.getKey();
                            if (user != null) {
                                currentUname = user.getName();
                                currentUphotoUrl = user.getPhotoUrl();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    /**
     * Returns user's chatmate firebase id
     */
    public void getChatmateInfo(String happId) {
        mDatabase.child("users").orderByChild("id").equalTo(Integer.parseInt(happId))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                                User user = snapshot.getValue(User.class);

                                if (user != null) {
                                    chatmateId = snapshot.getKey();
                                    chatmateName = user.getName();
                                    chatmatePhotoUrl = user.getPhotoUrl();

                                    listenForUserDataChanges(chatmateId);
                                }

                                if (chatmateName != null) {
                                    mView.onLoadChatmateName(chatmateName);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }

    public void sendMessage(String message) {
        boolean fetchData = false;

        if (chatroomId != null) {
            prepareSendMessage(chatroomId, message, fetchData);
        } else {
            fetchData = true;

            mDatabase.child("chat");
            chatroomId = mDatabase.push().getKey();
            prepareSendMessage(chatroomId, message, fetchData);
        }
    }

    public void getConversations(String chatroomId) {
        mDatabase.child("chat").child("messages").child(chatroomId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        MessageContent message = dataSnapshot.getValue(MessageContent.class);
                        mView.loadConversations(message);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {}

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

    }

    public void listenForUserDataChanges(String chatmateId) {
        mDatabase.child("users").child(chatmateId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String name = user.getName();
                            String photoUrl = user.getPhotoUrl();
                            if (!photoUrl.equals(chatmatePhotoUrl)) {
                                chatmatePhotoUrl = photoUrl;
                                chatmateName = name;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void getUserData(final MessageContent message) {
        String userId = message.getUserId();

        mDatabase.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user != null) {
                            String name = user.getName();
                            String photoUrl = user.getPhotoUrl();

                            message.setName(name);
                            message.setPhotoUrl(photoUrl);

                            mView.loadConversations(message);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void passChatmateInfo(String chatroomId, String chatmateId, String chatmateName,
                                 String chatmatePhotoUrl) {
        this.chatroomId = chatroomId;
        this.chatmateId = chatmateId;
        this.chatmateName = chatmateName;
        this.chatmatePhotoUrl = chatmatePhotoUrl;
    }

    private void prepareSendMessage(final String chatroomId, final String message,
                                    final boolean fetchData) {

        final Map<String, String> timestamp = ServerValue.TIMESTAMP;

        final DatabaseReference messageRef;
        final DatabaseReference memberRef;
        DatabaseReference lastMessageRef;
        DatabaseReference messageNotifRef;

        ChatMessage chatMessage = new ChatMessage(getCurrentUid(), currentUname, currentUphotoUrl,
                message, timestamp);
        Message senderMessage = new Message(chatroomId, chatmateId, chatmateName,
                chatmatePhotoUrl, message, timestamp, true);

        Message recipientMessage = new Message(chatroomId, currentUid, currentUname,
                currentUphotoUrl, message, timestamp, false);

        NotifMessage messageNotif = new NotifMessage(chatroomId, currentUid, currentUname,
                currentUphotoUrl, message);

        final Map<String, Boolean> members = new HashMap<>();
        members.put(getCurrentUid(), true);
        members.put(chatmateId, true);

        messageRef = mDatabase.child("chat").child("messages").child(chatroomId);
        memberRef = mDatabase.child("chat").child("members").child(chatroomId);
        lastMessageRef = mDatabase.child("chat").child("last-message");
        messageNotifRef = mDatabase.child("chat").child("message-notif").child(chatmateId);

        messageRef.push().setValue(chatMessage)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (fetchData) {
                            getConversations(chatroomId);
                        }
                        //mView.onMessageSent();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mView.onMessageSendFailed();
                    }
                });

        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    memberRef.setValue(members);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        lastMessageRef.child(currentUid).child(chatroomId).setValue(senderMessage);
        lastMessageRef.child(chatmateId).child(chatroomId).setValue(recipientMessage);
        messageNotifRef.setValue(messageNotif);

    }

    public void checkIfBlocked(String chatroomId) {
        mDatabase.child("chat").child("members").child(chatroomId).child("blocked")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            Boolean blocked = (Boolean) dataSnapshot.getValue();
                            if (blocked != null && blocked) {
                                mView.onUserBlocked();
                            } else {
                                mView.onUserNotBlocked();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}
