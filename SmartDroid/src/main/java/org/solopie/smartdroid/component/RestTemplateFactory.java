package org.solopie.smartdroid.component;

import com.google.gson.Gson;

import org.solopie.smartdroid.types.ConstantConfig;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author A.L.
 *
 * RestTemplate is thread-safe, ok to use singleton
 */
public class RestTemplateFactory {
    private static RestTemplate restTemplate;
    public static RestTemplate getInstance() {
        if(null!= restTemplate) {
            return restTemplate;
        }
        restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory restFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
        restFactory.setConnectTimeout(ConstantConfig.HTTP_CONNECTION_TIMEOUT);
        restFactory.setConnectTimeout(ConstantConfig.HTTP_CONNECTION_TIMEOUT);

        GsonHttpMessageConverter gsonHttpMessageConverter = new GsonHttpMessageConverter();
        gsonHttpMessageConverter.setGson(new Gson());
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        restTemplate.getMessageConverters().add(gsonHttpMessageConverter);
        restTemplate.getMessageConverters().add(stringHttpMessageConverter);
        restTemplate.getMessageConverters().add(formHttpMessageConverter);
        return restTemplate;
    }
}
