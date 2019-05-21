package world.mycom.servicecallhelper;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.http.Multipart;
import world.mycom.MycomApplication;
import world.mycom.R;
import world.mycom.model.LoginInfo;
import world.mycom.utils.CommonUtils;
import world.mycom.utils.URLConstant;

	/* 
	*	created by Hiren Patel on 5th February 2015
	* common service response object
	*/

public class ServiceHelper {

    private static String TAG = "Service Helper";
    public static final String COMMON_ERROR = MyApplication.getContext().getString(R.string.common_error);
    public static final String COMMON_NETWORK_ERROR = MyApplication.getContext().getString(R.string.common_network_error);

    public enum RequestMethod {
        GET, POST, PUT, DELETE, MULTIPART
    }

    public interface ServiceHelperDelegate {
        /**
         * Calls when got the response from the API
         *
         * @param res Service Response Obejct
         */
        public void CallFinish(ServiceResponse res);

        /**
         * Service call fail with error message
         *
         * @param ErrorMessage Error Message
         */
        public void CallFailure(String ErrorMessage);
    }

	// declare object of interface method
    private ServiceHelperDelegate m_delegate = null;

    // for query string parameters
    private ArrayList<String> m_params = new ArrayList<String>();

	// for multipart request
    private HashMap<String, Object> m_formbody = new HashMap<>();


    // define request type GET request and you can change when you implement any call
    public RequestMethod RequestMethodType = RequestMethod.GET;

    // API method name
    String m_methodName = null;

    // if we need to do more than one api call and handle response that which response we are getting from which request so simply defin tag for all if needed
    int REQUEST_TAG = 0;

    //if we need to pass raw string data in post data call then use this
    String POSTDATA = "";

    // if request type is multipart
    MultipartBody.Builder body = null;


    /**
     * counstructor
	 * @params : requestMethod , postData
     */
    public ServiceHelper(RequestMethod requestMethod, String postData) {
        RequestMethodType = requestMethod;
        POSTDATA = postData;
    }

     /**
     * counstructor
	 * @params : requestMethod , postData
     */
    public ServiceHelper(RequestMethod requestMethod, HashMap<String, Object> postData) {
        RequestMethodType = requestMethod;
        m_formbody = postData;
    }

     /**
     * counstructor
	 * @params : requestMethod , builder
     */
    public ServiceHelper(RequestMethod requestMethod, MultipartBody.Builder builder) {
        RequestMethodType = requestMethod;
        body = builder;
    }

    /**
     * When using more than one call from one class and same delegate is used.
     * So the call response is identify by TAG
     */
    public void setTAG(String tag) {
        TAG = tag;
    }


    // for add query string parameters
    public void addParam(String key, Object value) {
        m_params.add(key + "=" + value);
    }

	// generating final url with all params or path
    public String getFinalUrl(String URL) {
        StringBuilder sb = new StringBuilder();
        sb.append(URL);
        if (RequestMethodType == RequestMethod.GET) {
            if (m_params.size() > 0) {
                String queryString = CommonUtils.join(m_params, "&");
                sb.append("?");
                sb.append(queryString);
            }
        }
        return sb.toString();
    }

    // call fucntion for start async task for background process
    public void call(String url, ServiceHelperDelegate delegate) {
        m_delegate = delegate;
        CallServiceAsync calling = new CallServiceAsync(url);
        calling.execute();

    }

    // call function which is call from async task
    private String call(String url) {
        StringBuilder builder = new StringBuilder();
        OkHttpClient client = null;

        try {
            // create client
            client = new OkHttpClient.Builder()
                    .readTimeout(1, TimeUnit.MINUTES)
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .build();

        } catch (Exception e) {
            builder.append(COMMON_ERROR);
            return builder.toString();
        }

        // define request
        Request request = null;

        //if request type GET then create request object with GET type
        if (RequestMethodType == RequestMethod.GET) {

            request = new Request.Builder().addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                    .url(getFinalUrl(url))
                    .build();
        } else if (RequestMethodType == RequestMethod.MULTIPART) {
            if (url.contains(URLConstant.REGISTER_USER)) {
                RequestBody requestBody = body.build();
                request = new Request.Builder()
                        .url(getFinalUrl(url))
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build();
            } else {
                RequestBody requestBody = body.build();
                request = new Request.Builder()
                        .url(getFinalUrl(url))
                        .addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build();
            }

        } else if (RequestMethodType == RequestMethod.POST) {
            //if request type POST then create request object with POST type
            if (m_params.size() > 0) {
                // if call need query param then add parameter in post request
                MediaType queryStringType
                        = MediaType.parse("application/x-www-form-urlencoded");
                String queryString = CommonUtils.join(m_params, "&");
                // create request body
                RequestBody body = RequestBody.create(queryStringType, queryString);
                //create request
                request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .url(getFinalUrl(url))
                        .post(body)
                        .build();
            } else if (m_formbody != null && m_formbody.size() > 0) {
                // if call need query param then add parameter in post request
                FormBody.Builder formbuilder = new FormBody.Builder();
                for (String key : m_formbody.keySet()) {
                    formbuilder.add(key, (String) m_formbody.get(key));
                }

                // create request body
                RequestBody body = formbuilder.build();
                //create request
                request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                        .url(getFinalUrl(url))
                        .post(body)
                        .build();
            } else {
                // if call raw string json then add parameter in post request
                MediaType JSON
                        = MediaType.parse("application/json");
                // create request body
                RequestBody body = RequestBody.create(JSON, POSTDATA);
                ////create request
                if (url.contains(URLConstant.LOGIN)) {
                    request = new Request.Builder()
                            .url(getFinalUrl(url))
                            .addHeader("Content-Type", "application/json")
                            .post(body)
                            .build();
                } else {
                    request = new Request.Builder().addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                            .url(getFinalUrl(url))
                            .post(body)
                            .build();
                }
            }
        } else if (RequestMethodType == RequestMethod.PUT) {
            if (m_params.size() > 0) {
                // if call need query param then add parameter in post request
                MediaType queryStringType
                        = MediaType.parse("application/x-www-form-urlencoded");
                String queryString = CommonUtils.join(m_params, "&");
                // create request body
                RequestBody body = RequestBody.create(queryStringType, queryString);
                //create request
                request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .url(getFinalUrl(url))
                        .put(body)
                        .build();

            } else {
                // if call raw string json then add parameter in post request
                MediaType JSON
                        = MediaType.parse("application/json");
                // create request body
                RequestBody body = RequestBody.create(JSON, POSTDATA);
                ////create request
                request = new Request.Builder()
                        .addHeader("Authorization", "Bearer " + LoginInfo.getToken())
                        .addHeader("Content-Type", "application/json")
                        .url(getFinalUrl(url))
                        .put(body)
                        .build();
            }
        }

        try {
            // execute request and it will return response
            Response response = client.newCall(request).execute();

            //get raw response string from response object
            if (response != null) {
                builder.append(response.body().string());
            } else {
                builder.append(COMMON_ERROR);
            }
        } catch (IOException e) {
            // handle catch exception if call fail cause of internet connection
            e.printStackTrace();
            builder.append(COMMON_ERROR);
        } catch (Exception ex) {
            // handle catch exception if call fail cause of internet connection
            ex.printStackTrace();
            builder.append(COMMON_ERROR);
        }
        return builder.toString();
    }

    // async task for do service call function execute in background thread
    class CallServiceAsync extends AsyncTask<Void, Void, ServiceResponse> {

        String m_url = null;


        // constructor
        public CallServiceAsync(String url) {
            m_url = url;
        }


        // Override method of asynctask for background
        @Override
        protected ServiceResponse doInBackground(Void... params) {
            //  check condition for Unittest environment
            String strResponse = "";
            // doing service call
            strResponse = call(m_url);

            //check condition if response string is not blank
            if (!strResponse.isEmpty()) {

                //create service response object
                ServiceResponse response = new ServiceResponse();
                response.RawResponse = strResponse;

                //return response object in post method
                return response;
            } else {
                return null;
            }
        }

        // Override method of async task and it will call when finish do in background proccess
        @Override
        protected void onPostExecute(ServiceResponse result) {
            // check condition for result response is not null
            if (result != null) {
                // call intgerface for same
                if (m_delegate != null) {
                    m_delegate.CallFinish(result);
                }
            } else {
                // call intgerface for same
                if (m_delegate != null) {
                    m_delegate.CallFailure(ServiceHelper.COMMON_ERROR);
                }
            }
            super.onPostExecute(result);
        }

    }
}
