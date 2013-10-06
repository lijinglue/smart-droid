package org.solopie.smartdroid.service;

import android.content.Context;
import android.content.Intent;

import org.solopie.smartdroid.component.WebserviceResultReceiver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Hashtable;

/**
 * @author A.L.
 *
 */
public class RESTManagerIntentFactory {
    public static enum Task implements Serializable{
        QUERY, ANSWER
    }

    private static Hashtable<Task, IntentCreator> creatorRegister = new Hashtable<Task, IntentCreator>();

    static {

        creatorRegister.put(Task.QUERY, new IntentCreator() {
            @Override
            public Intent create(Context context, WebserviceResultReceiver receiver) {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, context, RESTManagerService.class);
                intent.putExtra(RESTManagerService.INTENT_BINDING.ENDPOINT, "/android/query");
                intent.putExtra(RESTManagerService.INTENT_BINDING.RECEIVER, receiver);
                intent.putExtra(RESTManagerService.INTENT_BINDING.HTTP_METHOD, HttpMethod.GET.name());
                intent.putExtra(RESTManagerService.INTENT_BINDING.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                intent.putExtra(RESTManagerService.INTENT_BINDING.ACCEPT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                intent.putExtra(RESTManagerService.INTENT_BINDING.HTTP_PARAMS, new HashMap<String,String>(1));
                intent.putExtra(RESTManagerService.INTENT_BINDING.PATH_PARAMS, new HashMap<String,String>(1));
                intent.putExtra(RESTManagerService.INTENT_BINDING.TASK,Task.QUERY);
                return intent;
            }
        });

        creatorRegister.put(Task.ANSWER, new IntentCreator() {
            @Override
            public Intent create(Context context, WebserviceResultReceiver receiver) {
                Intent intent = new Intent(Intent.ACTION_SYNC, null, context, RESTManagerService.class);
                intent.putExtra(RESTManagerService.INTENT_BINDING.RECEIVER, receiver);
                intent.putExtra(RESTManagerService.INTENT_BINDING.HTTP_METHOD, HttpMethod.POST.name());
                intent.putExtra(RESTManagerService.INTENT_BINDING.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
                intent.putExtra(RESTManagerService.INTENT_BINDING.ACCEPT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                HashMap<String,String> paramap = new HashMap<String, String>(2);
                intent.putExtra(RESTManagerService.INTENT_BINDING.TASK,Task.ANSWER);
                return intent;
            }
        });
    }

    private interface IntentCreator {
        Intent create(Context context,WebserviceResultReceiver resultReciever);
    }

    public static synchronized Intent getInstance(Task t,Context context, WebserviceResultReceiver receiver) {
        return creatorRegister.get(t).create(context,receiver);
    }
}
