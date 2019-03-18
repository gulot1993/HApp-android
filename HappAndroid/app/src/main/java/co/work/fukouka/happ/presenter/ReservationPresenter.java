package co.work.fukouka.happ.presenter;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.work.fukouka.happ.helper.HappHelper;
import co.work.fukouka.happ.model.Reservation;
import co.work.fukouka.happ.model.ReservationList;
import co.work.fukouka.happ.model.RoomOffice;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ReservationView;


public class ReservationPresenter {

    private Context mContext;
    private ReservationView mView;
    private SessionManager mSession;
    private RequestQueue requestQueue;
    private HappHelper mHelper;

    private List<String> reservations = new ArrayList<>();
    private List<Reservation> reservationList = new ArrayList<>();
    private List<Reservation> list = new ArrayList<>();
    private List<RoomOffice> roomList;
    private List<RoomOffice> officeList = new ArrayList<>();
    private List<Reservation> listReservation = new ArrayList<>();
    final List<ReservationList> newList = new ArrayList<>();


    public ReservationPresenter(Context context) {
        this.mContext = context;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        mHelper = new HappHelper(context);
    }

    public ReservationPresenter(Context context, ReservationView view) {
        this.mView = view;
        this.mContext = context;
        mSession = new SessionManager(context);
        requestQueue = Volley.newRequestQueue(context);
        mHelper = new HappHelper(context);
    }

    public String getUserId() {
        String userId;

        HashMap<String, String> user = mSession.getUserId();
        userId = user.get(SessionManager.KEY_USER_ID);

        return userId;
    }

    public String getFbUid() {
        String user = null;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            user = currentUser.getUid();
        }

        return user;
    }

    public String getLanguage() {
        String language;

        HashMap<String, String> lang = mSession.getLanguage();
        language = lang.get(SessionManager.LANGUAGE);

        return language;
    }

    public void getDayReservations(String startDate, String endDate) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_resavation");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("office_id", "");
        params.put("start", startDate);
        params.put("end", endDate);
        params.put("user_id", "");

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                for (int i = 0; i < jsonArray.length(); i++) {

                                    JSONObject jsonObject = jsonArray.getJSONObject(i)
                                            .getJSONObject("fields");

                                    String userId = null;
                                    int resId = jsonArray.getJSONObject(i).getInt("ID");
                                    String start = jsonObject.getString("start");
                                    String end = jsonObject.getString("end");

                                    Object object = jsonObject.get("user_id");

                                    if (object instanceof JSONObject) {
                                        userId = ((JSONObject) object).getString("ID");
                                    } else {
                                        userId = jsonObject.getString("user_id");
                                    }

                                    String startTime = start.split("\\s+")[1];
                                    String endTime = end.split("\\s+")[1];

                                    // Remove seconds
                                    if (startTime != null)
                                        startTime = startTime.substring(0, startTime.length() - 3);
                                    if (endTime != null)
                                        endTime = endTime.substring(0, endTime.length() - 3);

                                    String reservedTime = startTime + " ~ " + endTime;

                                    Reservation reservation = new Reservation(resId, userId, reservedTime);
                                    mView.onLoadReservation(reservation);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    public void getMeetingRooms(final int officeId) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_meeting_room");
        params.put("d","0");
        params.put("lang", getLanguage());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray jsonArray = response.getJSONArray("result");
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    roomList = new ArrayList<>();
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        int id = jsonObject.getInt("ID");
                                        JSONObject fields = jsonObject.getJSONObject("fields");
                                        JSONObject office = fields.getJSONObject("office");
                                        int roomOfficeId = office.getInt("ID");

                                        if (officeId == roomOfficeId) {
                                            String roomNameJp = fields.getString("room_name_jp");
                                            String roomNameEn = fields.getString("room_name_en");

                                            RoomOffice room = new RoomOffice(id, roomNameJp, roomNameEn);
                                            roomList.add(room);
                                        }
                                    }
                                    mView.onLoadMeetingRoom(roomList);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            try {
                                boolean error = response.getBoolean("error");
                                if (error) {
                                    String message = response.getString("message");
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsObjRequest);
    }

    public void getMeetingOffice() {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_office");
        params.put("d","0");
        params.put("lang", getLanguage());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                JSONArray jsonArray = response.getJSONArray("result");
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        int id = jsonObject.getInt("ID");
                                        JSONObject fields = jsonObject.getJSONObject("fields");
                                        String officeNameJp = fields.getString("office_name_jp");
                                        String officeNameEn = fields.getString("office_name_en");

                                        RoomOffice office = new RoomOffice(id, officeNameJp, officeNameEn);
                                        officeList.add(office);
                                    }
                                    mView.onLoadOffice(officeList);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            try {
                                boolean error = response.getBoolean("error");
                                if (error) {
                                    String message = response.getString("message");
                                    Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsObjRequest);
    }


    public void makeReservation(String roomId, String start, String end) {
        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","update_resavation");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("meeting_room_pid", roomId);
        params.put("start", start);
        params.put("end", end);
        params.put("user_id", getUserId());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            boolean success = response.getBoolean("success");
                            if (success) {
                                mView.onSuccess(mHelper.reservationCreated());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            try {
                                boolean error = response.getBoolean("error");
                                if (error) {
                                    String message = response.getString("message");
                                    mView.onFailed(message);
                                }
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                mView.onFailed("");
            }
        });

        requestQueue.add(jsObjRequest);
    }

}
