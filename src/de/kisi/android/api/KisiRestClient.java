package de.kisi.android.api;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;

import com.loopj.android.http.*;

public class KisiRestClient {

	private static final String BASE_URL = "https://www.kisi.de/";
	private static final String URL_SUFFIX = ".json";
	
	
	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(Context context, String url, AsyncHttpResponseHandler responseHandler) {
		SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
		String authToken = settings.getString("authentication_token", "");
		if(authToken != null) 
			client.get(getAbsoluteUrl(url, authToken), responseHandler);
		else
			client.get(getAbsoluteUrl(url), responseHandler);
	}

	public static void post(Context context, String url, JSONObject data, AsyncHttpResponseHandler responseHandler) {
		SharedPreferences settings = context.getSharedPreferences("Config", Context.MODE_PRIVATE);
		String authToken = settings.getString("authentication_token", "");
		if(authToken != null) 
			client.post(context, getAbsoluteUrl(url, authToken), JSONtoStringEntity(data), "application/json", responseHandler);
		else
			client.post(context, getAbsoluteUrl(url), JSONtoStringEntity(data), "application/json", responseHandler);
	}
	
	
	public static void delete(String url, AsyncHttpResponseHandler responseHandler) {
		client.delete(getAbsoluteUrl(url),  responseHandler);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl + URL_SUFFIX;
	}
	
	private static String getAbsoluteUrl(String relativeUrl, String authToken) {
		return BASE_URL + relativeUrl + URL_SUFFIX + "?auth_token=" + authToken;
	}

	private static StringEntity JSONtoStringEntity (JSONObject json) {
		StringEntity entity = null;
        try {
			entity = new StringEntity(json.toString());
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        return entity;
	}
}