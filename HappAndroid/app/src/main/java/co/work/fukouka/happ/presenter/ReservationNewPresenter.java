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

import co.work.fukouka.happ.model.Office;
import co.work.fukouka.happ.model.Reservation;
import co.work.fukouka.happ.model.Room;
import co.work.fukouka.happ.utils.JsonObjectRequest;
import co.work.fukouka.happ.utils.SessionManager;
import co.work.fukouka.happ.view.ResView;

public class ReservationNewPresenter {

    private Context mContext;
    private SessionManager mSession;
    private RequestQueue requestQueue;
    private ResView mView;

    private List<Reservation> resList;
    private List<String> list;

    public ReservationNewPresenter(Context context) {
        this.mContext = context;
        this.mSession = new SessionManager(context);
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public ReservationNewPresenter(Context context, ResView mView) {
        this.mContext = context;
        this.mSession = new SessionManager(context);
        this.requestQueue = Volley.newRequestQueue(context);
        this.mView = mView;
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

    public void getOffice(final String caller, final String startDate, final String endDate) {
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

                                        Office office = new Office(id, officeNameJp, officeNameEn);

                                        //get rooms within office
                                        getMeetingRooms(office, caller, startDate, endDate);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            try {
                                boolean error = response.getBoolean("error");
                                if (error) {
                                    String message = response.getString("message");
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

    private void getMeetingRooms(final Office office, final String caller, final String startDate, final String endDate) {
        final int officeId = office.getOfficeId();

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
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        int id = jsonObject.getInt("ID");
                                        JSONObject fields = jsonObject.getJSONObject("fields");
                                        JSONObject officeObject = fields.getJSONObject("office");
                                        int roomOfficeId = officeObject.getInt("ID");

                                        if (officeId == roomOfficeId) {
                                            String roomNameJp = fields.getString("room_name_jp");
                                            String roomNameEn = fields.getString("room_name_en");

                                            Room room = new Room(id, roomNameJp, roomNameEn);
                                            int roomId = room.getRoomId();
                                            getReservation(office, room, caller, startDate, endDate);
                                        }
                                    }
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

    private void getReservation(final Office office, final Room room, String caller,
                               String startDate, String endDate) {
        final int officeId = office.getOfficeId();
        final int roomId = room.getRoomId();

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_resavation");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("office_id", String.valueOf(officeId));
        if (caller.equals("LIST")) {
            params.put("start", "");
            params.put("end", "");
        } else {
            params.put("start", startDate);
            params.put("end", endDate);
        }
        params.put("user_id", getUserId());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                resList = new ArrayList<>();
                                list = new ArrayList<>();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i)
                                            .getJSONObject("fields");

                                    int resId = jsonArray.getJSONObject(i).getInt("ID");
                                    String userId = null;
                                    Object object = jsonObject.get("user_id");
                                    String start = jsonObject.getString("start");
                                    String end = jsonObject.getString("end");
                                    JSONObject jsonRoom = jsonObject.getJSONObject("meeting_room_pid");
                                    int currentRoomId = jsonRoom.getInt("ID");

                                    if (object instanceof JSONObject) {
                                        userId = ((JSONObject) object).getString("ID");
                                    } else {
                                        userId = jsonObject.getString("user_id");
                                    }

                                    if (roomId == currentRoomId) {
                                        String reservedDate = start.split("\\s+")[0];
                                        String startTime = reservedDate + " 00:00:00";
                                        String endTme = reservedDate + " 23:59:00";

                                        if (!list.contains(reservedDate)) {
                                            list.add(reservedDate);

                                            Reservation reservation = new Reservation(reservedDate, startTime,
                                                    endTme);

                                            resList.add(reservation);
                                        }

                                    }
                                }

                                if (!resList.isEmpty() && resList.size() > 0) {
                                    for (int i = 0; i < resList.size(); i++) {
                                        String reservedDate = resList.get(i).getDate();
                                        String startTime = resList.get(i).getStartTime();
                                        String endTime = resList.get(i).getEndTime();

                                        Reservation resDate = new Reservation(reservedDate, startTime, endTime);
                                        getReservationWithinDay(office, room, resDate);
                                    }
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

    private void getReservationWithinDay(final Office office, final Room room, Reservation resDate) {
        int officeId = office.getOfficeId();
        final int roomId = room.getRoomId();

        final String reservedDate = resDate.getDate();
        String startTime = reservedDate + " 00:00:00";
        String endTime = reservedDate + " 23:59:00";

        final List<Reservation> reservations = new ArrayList<>();

        Map<String,String> params = new HashMap<>();
        params.put("sercret","jo8nefamehisd");
        params.put("action","api");
        params.put("ac","get_resavation");
        params.put("d","0");
        params.put("lang", getLanguage());
        params.put("office_id", String.valueOf(officeId));
        params.put("start", startTime);
        params.put("end", endTime);
        params.put("user_id", getUserId());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("result");
                            if (jsonArray != null && jsonArray.length() > 0) {
                                //reservationList.clear();
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i)
                                            .getJSONObject("fields");

                                    String userId = null;
                                    int resId = jsonArray.getJSONObject(i).getInt("ID");
                                    String start = jsonObject.getString("start");
                                    String end = jsonObject.getString("end");
                                    JSONObject jsonRoom = jsonObject.getJSONObject("meeting_room_pid");
                                    int currentRoomId = jsonRoom.getInt("ID");

                                    Object object = jsonObject.get("user_id");

                                    if (object instanceof JSONObject) {
                                        userId = ((JSONObject) object).getString("ID");
                                    } else {
                                        userId = jsonObject.getString("user_id");
                                    }

                                    if (roomId == currentRoomId) {
                                        String reservedDate = start.split("\\s+")[0];
                                        String startTime = start.split("\\s+")[1];
                                        String endTime = end.split("\\s+")[1];

                                        // Remove seconds
                                        if (startTime != null)
                                            startTime = startTime.substring(0, startTime.length() - 3);
                                        if (endTime != null)
                                            endTime = endTime.substring(0, endTime.length() - 3);

                                        String reservedTime = startTime + " ~ " + endTime;

                                        Reservation reservation = new Reservation(resId, userId,
                                                reservedTime);

                                        reservations.add(reservation);
                                    }
                                }

                                Reservation reservation = new Reservation(office, room,
                                        reservedDate, reservations);

                                mView.onLoadReservationNew(reservation);
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

}
