package org.solopie.smartdroid.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.gson.Gson;

import org.solopie.smartdroid.component.RestTemplateFactory;
import org.solopie.smartdroid.types.ConstantConfig;
import org.solopie.smartdroid.types.HttpRequestStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.solopie.smartdroid.service.RESTManagerService.INTENT_BINDING.RESPONSE;

/**
 * @author A.L.
 *
 * IntentService that manages all REST network connection in this app
 * The intent must provide params according to intent binding
 *
 * @See INTENT_BINDING
 * @See RESTManagerIntentFactory
 */
public class RESTManagerService extends IntentService {
    private static String TAG = RESTManagerService.class.getCanonicalName();
    private final static Gson gson = new Gson();


    public RESTManagerService() {
        super(RESTManagerService.class.getSimpleName());
    }

    public RESTManagerService(String name) {
        super(name);
    }

    public static interface INTENT_BINDING {
        final static String RECEIVER = "RECEIVER";
        final static String RESPONSE = "RESPONSE";
        final static String HTTP_METHOD = "HTTP_METHOD";
        final static String HTTP_PARAMS = "HTTP_PARAMS";
        final static String PATH_PARAMS = "PATH_PARAMS";
        final static String ENDPOINT = "ENDPOINT";
        final static String CONTENT_TYPE = "CONTENT_TYPE";
        final static String ACCEPT_TYPE = "ACCEPT_TYPE";
        final static String TASK = "TASK_HANDLED";
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        final ResultReceiver receiver = intent.getParcelableExtra(INTENT_BINDING.RECEIVER);
        final Map params = (Map) intent.getSerializableExtra(INTENT_BINDING.HTTP_PARAMS);
        final Map pathParams = (Map) intent.getSerializableExtra(INTENT_BINDING.PATH_PARAMS);
        final RESTManagerIntentFactory.Task task = (RESTManagerIntentFactory.Task) intent.getSerializableExtra(INTENT_BINDING.TASK);
        final String endpoint = intent.getStringExtra(INTENT_BINDING.ENDPOINT);
        final String contentType = intent.getStringExtra(INTENT_BINDING.CONTENT_TYPE);
        final String httpMetod = intent.getStringExtra(INTENT_BINDING.HTTP_METHOD);
        final String acceptType = intent.getStringExtra(INTENT_BINDING.ACCEPT_TYPE);

        assert receiver != null;
        assert params != null;
        assert pathParams != null;
        assert task != null;
        assert endpoint != null;
        assert contentType != null;
        assert httpMetod != null;
        assert acceptType != null;

        Log.d(TAG, "Task handled" + task.name());
        Bundle b = new Bundle();
        String url = ConstantConfig.BASE_URL.concat(endpoint);

        RestTemplate restTemplate = RestTemplateFactory.getInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        assert receiver != null;

        b.putSerializable(INTENT_BINDING.TASK, task);
        receiver.send(HttpRequestStatus.IN_PROGRESS, b);
        try {
            Log.i(TAG, String.format("Method:%s, ContentType: %s, Url: %s ", httpMetod, contentType, url));
            HttpEntity entity = null;
            if (HttpMethod.valueOf(httpMetod).equals(HttpMethod.GET)) {
                entity = new HttpEntity(null, headers);
            } else {
                entity = new HttpEntity(new LinkedMultiValueMap<String, String>(params), headers);
            }

            Class<? extends Serializable> targetClass = null;
            if (MediaType.APPLICATION_JSON_VALUE.equals(acceptType)) {
                targetClass = HashMap.class;
            } else {
                targetClass = String.class;
            }

            ResponseEntity<Serializable> re = (ResponseEntity<Serializable>) restTemplate.exchange(url, HttpMethod.valueOf(httpMetod), entity, targetClass);
            b.putSerializable(RESPONSE, re.getBody());
            Log.d(TAG, String.format("msg recieved:%s", gson.toJson(re.getBody())));
            receiver.send(HttpRequestStatus.SUCCESS, b);
        } catch (Exception e) {
            Log.d(RESTManagerService.class.getCanonicalName(), "Error requesting http", e);
            b.putString(Intent.EXTRA_TEXT, e.toString());
            receiver.send(HttpRequestStatus.ERROR, b);
        }
    }
}

