package co.work.fukouka.happ.utils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import co.work.fukouka.happ.helper.Constant;


public class JsonObjectRequest extends Request<JSONObject> {
    //private static final String URL = "https://happ.biz/wp-admin/admin-ajax.php";
    private static final String URL = Constant.API_URL;
    private Response.Listener<JSONObject> listener;
    private Map<String, String> params;

    public JsonObjectRequest(String url, Map<String, String> params,
                             Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    //TODO: modify all api calls and remove URL
    public JsonObjectRequest(int method, Map<String, String> params,
                             Response.Listener<JSONObject> reponseListener, Response.ErrorListener errorListener) {
        super(method, URL, errorListener);
        this.listener = reponseListener;
        this.params = params;
    }

    protected Map<String, String> getParams()
            throws com.android.volley.AuthFailureError {
        return params;
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(new JSONObject(jsonString),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        // TODO Auto-generated method stub
        listener.onResponse(response);
    }
}
