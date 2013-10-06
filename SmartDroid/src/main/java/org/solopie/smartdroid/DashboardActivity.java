package org.solopie.smartdroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;

import org.solopie.smartdroid.component.WebserviceResultReceiver;
import org.solopie.smartdroid.service.RESTManagerIntentFactory;
import org.solopie.smartdroid.service.RESTManagerService;
import org.solopie.smartdroid.types.HttpRequestStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends Activity implements WebserviceResultReceiver.Receiver {
    private final static String TAG = DashboardActivity.class.getCanonicalName();
    private final static Gson gson = new Gson();

    WebserviceResultReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        receiver = new WebserviceResultReceiver(new Handler());
        receiver.setReceiver(this);

        final View requestBtn = findViewById(R.id.dashbd_answer_request_btn);
        final Intent restManagerIntent = RESTManagerIntentFactory.getInstance(RESTManagerIntentFactory.Task.QUERY, this, this.receiver);// new Intent(Intent.ACTION_SYNC, null, this, RESTManagerService.class);
        requestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(restManagerIntent);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        receiver.setReceiver(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dashboard, menu);
        return true;
    }

    @Override
    public void onReceiveResult(int resultCode, final Bundle resultData) {
        final RESTManagerIntentFactory.Task taskHandled = (RESTManagerIntentFactory.Task) resultData.getSerializable(RESTManagerService.INTENT_BINDING.TASK);
        Log.d(TAG,"Task handled:"+ taskHandled.name());
        final Context context = getApplicationContext();
        switch (resultCode) {
            case HttpRequestStatus.IN_PROGRESS:
                Log.d(TAG,"Request in progress msg notified main thread");
                break;
            case HttpRequestStatus.ERROR:
                Log.d(TAG,"Request error msg notified main thread");
                break;
            case HttpRequestStatus.SUCCESS:
                Log.d(TAG,"Request Success, ui thread try updating ui");
                final Intent answerIntent = RESTManagerIntentFactory.getInstance(RESTManagerIntentFactory.Task.ANSWER, this, receiver);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Log.d(TAG,"Worker thread stating");
                            if (RESTManagerIntentFactory.Task.QUERY.equals(taskHandled)) {
                                Map<String, String> response = (Map<String, String>) resultData.getSerializable(RESTManagerService.INTENT_BINDING.RESPONSE);
                                if (response.containsKey("key") && response.containsKey("url")) {
                                    answerIntent.putExtra(RESTManagerService.INTENT_BINDING.ENDPOINT, response.get("url"));
                                    HashMap<String, String> jsonBody = new HashMap<String, String>(2);
                                    jsonBody.put("question","what is the answer");
                                    jsonBody.put("key", response.get("key"));
                                    HashMap<String, List<String>> postBody = new HashMap<String,List<String>>(1);
                                    postBody.put("json",Arrays.asList(gson.toJson(jsonBody)));
                                    answerIntent.putExtra(RESTManagerService.INTENT_BINDING.HTTP_PARAMS, postBody);
                                    startService(answerIntent);
                                }else{
                                    Log.d(TAG,"unexpected response body, task ending");
                                }
                            } else if (RESTManagerIntentFactory.Task.ANSWER.equals(taskHandled)) {
                                final String response = resultData.getString(RESTManagerService.INTENT_BINDING.RESPONSE);
                                DashboardActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, response, 3).show();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.d(TAG,"Fail to update UI with data returned",e);
                        }

                    }
                });
                t.start();
                break;
            default:
        }
    }
}
