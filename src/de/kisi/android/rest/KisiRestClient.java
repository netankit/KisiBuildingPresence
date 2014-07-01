package de.kisi.android.rest;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;



import android.util.Log;

import com.loopj.android.http.*;

import de.kisi.android.KisiApplication;
import de.kisi.android.api.KisiAPI;

public class KisiRestClient {

	private static KisiRestClient instance;
	
	private static final String BASE_URL = "https://www.kisi.de/";
	private static final String URL_SUFFIX = ".json";
	
	private  AsyncHttpClient client;
	
	public static KisiRestClient getInstance() {
		if(instance == null){
			instance =  new KisiRestClient();
		}
		return instance;
	}
	
	private KisiRestClient() {
		 client = new AsyncHttpClient();
		 client.setCookieStore(new BlackholeCookieStore());
		 client.setUserAgent("de.kisi.android");
	}

	public void get(String path, AsyncHttpResponseHandler responseHandler) {
		Log.d("get", "get path: " + path);
		
		String url = getAbsoluteUrl(path, true);
		
		Log.d("get", "get url: " + url); //TODO: remove this in release versions!
		client.get(url, responseHandler);
	}

	public void post(String path, JSONObject data, AsyncHttpResponseHandler responseHandler) {
		Log.d("post", "post path: " + path);
		
		String url = getAbsoluteUrl(path, true);
		
		Log.d("post", "post url: " + url); //TODO: remove this in release versions!
		if(data!=null){
			Log.d("post", "post data: " + data.toString()); //TODO: remove this in release versions!
		}
		client.post(KisiApplication.getInstance(), url, JSONtoStringEntity(data), "application/json", responseHandler);
	}
	
	public void postNoAuth(String path, JSONObject data, AsyncHttpResponseHandler responseHandler) {
		Log.d("postNoAuth", "post path: " + path);
		
		String url = getAbsoluteUrl(path, false);
		
		Log.d("postNoAuth", "post url: " + url); //TODO: remove this in release versions!
		Log.d("postNoAuth", "post data: " + data); //TODO: remove this in release versions!
		client.post(KisiApplication.getInstance(), url, JSONtoStringEntity(data), "application/json", responseHandler);
	}
	
	public  void delete(String url, AsyncHttpResponseHandler responseHandler) {
		if(KisiAPI.getInstance().getUser() != null) {
			client.delete(getAbsoluteUrl(url,true),  responseHandler);
		}else{
			client.delete(getAbsoluteUrl(url,false),  responseHandler);
		}

	}
	
	private  String getAbsoluteUrl(String relativeUrl, boolean useAuthToken) {
		if(!useAuthToken)
			return BASE_URL + relativeUrl + URL_SUFFIX;

		String authToken = null;
		if(KisiAPI.getInstance().getUser() != null) {
			authToken = KisiAPI.getInstance().getUser().getAuthentication_token();
		}
		
		if(authToken != null) {
			return BASE_URL + relativeUrl + URL_SUFFIX + "?auth_token=" + authToken;
		}
		else {
			return BASE_URL + relativeUrl + URL_SUFFIX;
		}
	}
	

	private  StringEntity JSONtoStringEntity (JSONObject json) {
		StringEntity entity = null;
        try {
			entity = new StringEntity(json.toString());
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
        //TODO: entity can still be null! Fix that.
        entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        return entity;
	}
	
}
