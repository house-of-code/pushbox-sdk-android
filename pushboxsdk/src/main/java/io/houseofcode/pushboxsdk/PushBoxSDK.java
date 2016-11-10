package io.houseofcode.pushboxsdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by gsl on 02/12/15.
 */
public class PushBoxSDK
{

	/**
	 * Call back interface for fetching messages
	 */
	public interface PushboxInboxCallback
	{
		public void onSuccess(List<PushBoxMessage> messages);
		public void onFailure(String error);
	}

	public final static int HOC_PUSHBOX_SDK_GENDER_UNKNOWN = 0;
	public final static int HOC_PUSHBOX_SDK_GENDER_FEMALE = 1;
	public final static int HOC_PUSHBOX_SDK_GENDER_MALE = 2;

	// user defaults
	public static final String HoCPushBoxSDKSuitName = "HoCPushBoxSDK.suit.name"; // NON_NLS log
	public static final String HoCPushBoxSDKDefaultsKeyPendingPushInteractions = "pending_interactions";  // NON_NLS log

	/**
	 * Sets context, GCM sender id, api key and api secret
	 *
	 * @param context   Context to use - could be application Context
	 * @param senderId	Sender id for GCM
	 * @param apiKey    Api key for authenticating against the API
	 * @param apiSecret Api secret for authenticating against the API

	 */
	public static void setContextAndSenderIdAndKeys(Context context, String senderId, String apiKey, String apiSecret)
	{
		mContext = context;
		mApiKey = apiKey;
		mApiSecret = apiSecret;
		mSenderId = senderId;
		getInstance();
	}


	/**
	 * Return a shared instance of the SDK
	 *
	 * @return shared instance of the SDK
	 */
	public static PushBoxSDK getInstance()
	{
		if (mContext == null)
		{
			throw new IllegalArgumentException("PushBoxSDK needs context to be set before starting");
		}
		if (mApiKey == null || mApiSecret == null)
		{
			throw new IllegalArgumentException("Api key and/or secret not set");
		}
		if (mInstance == null)
		{
			mInstance = new PushBoxSDK();
		}

		return mInstance;
	}

	/**
	 * Return true if context, api key and api secret is set
	 * @return true if sdk is ready to work
	 */
	public static boolean isInitialized()
	{
		return mContext != null && mApiKey != null && mApiSecret != null;
	}


	/**
	 * Fetches the profiles messages
	 * @param callback callback to be called when fetch is done
	 */
	public void getInbox(PushboxInboxCallback callback)
	{
		handleInboxRequest(callback);
	}

	/**
	 * Mark message as received by the gcm intent service
	 * @param id id of message
	 */
	public void setMessageInteraction(int id)
	{
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyPushId, id);
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:SS");
			object.put(HoCPushBoxSDKJSONKeyPushReadTime, df.format(new Date()));
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodPushInteracted);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

		/**
	 * Tells the api that a message is read
	 * @param message message
	 */
	public void setMessageRead(PushBoxMessage message)
	{
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyPushId, message.getId());
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:SS");
			object.put(HoCPushBoxSDKJSONKeyPushReadTime, df.format(new Date()));
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodPushRead);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets age of profile
	 * @param age
	 */
	public void setAge(int age)
	{
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyAge, age);
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodSetAge);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Sets the birthday
	 * @param year year born. eg. 1975
	 * @param month month born, eg. 9 for september
	 * @param day day born, eg. 20 for the 20th day in the month
	 */
	public void setBirthday(int year, int month, int day)
	{
		JSONObject object = defaultJson();
		try
		{
			// TODO: set birthday
			String birthdayString = String.format("%d/%s%d/%s%d", year, (month < 10 ? "0" : ""), month, (day < 10 ? "0" : ""), day);
			object.put(HoCPushBoxSDKJSONKeyBirthday, birthdayString);
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodSetBirthday);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets the channels for the profile
	 * @param channels list of strings
	 */
	public void setChannels(List<String> channels)
	{
		JSONObject object = defaultJson();
		try
		{
			JSONArray arr = new JSONArray();
			for (String channel : channels)
			{
				arr.put(channel);
			}
			object.put(HoCPushBoxSDKJSONKeyChannels, arr);
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodSetChannels);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Sets the gender for the profile
	 * @param gender either HOC_PUSHBOX_SDK_GENDER_UNKNOWN, HOC_PUSHBOX_SDK_GENDER_FEMALE or HOC_PUSHBOX_SDK_GENDER_MALE. Defaults to HOC_PUSHBOX_SDK_GENDER_UNKNOWN if any other value is used
	 */
	public void setGender(int gender)
	{
		if (gender < HOC_PUSHBOX_SDK_GENDER_UNKNOWN || gender > HOC_PUSHBOX_SDK_GENDER_MALE)
		{
			gender = HOC_PUSHBOX_SDK_GENDER_UNKNOWN;
		}
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyGender, gender);
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodSetGender);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Logs an event
	 * @param event event to log
	 */
	public void logEvent(String event)
	{
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyEvent, event);
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodLogEvent);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * Logs a location with latitude and longitude
	 * @param latitude latitude of location
	 * @param longitude longitude of location
	 */
	public void logLocation(double latitude, double longitude)
	{
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyLocationLatitude, latitude);
			object.put(HoCPushBoxSDKJSONKeyLocationLongitude, longitude);
			addJsonMethodToQueue(object, HoCPushBoxSDKMethodLogLocation);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Private area
	 */
	// statics
	private static Context mContext = null;
	private static PushBoxSDK mInstance = null;
	private static String mApiKey = null;
	private static String mApiSecret = null;
	private static String mSenderId = null;

	// guards
	private boolean mIsWorking = false;
	private boolean mTokenSend = false;

	// properties for sdk
	private String mProfileIdentifier = null;
	private String mUid = null;
	private String mToken = null;
	private GoogleCloudMessaging mGcm;

	// preferences for queue
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefsEditor;

	private static final String TAG = "PushboxSDK";

	// algorithm for hmac
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1"; //NON-NLS log
	// urls
	private static final String HoCPushBoxSDKApiUrl = "https://api.pushboxsdk.com/v1/"; // NON_NLS log
	// JSON keys
	private static final String HoCPushBoxSDKJSONKeyHMAC = "hmac"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyTS = "ts"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyApiKey = "app_key"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyToken = "push_token"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyUid = "uid"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyProfileId = "profile_identifier"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyPlatform = "platform"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyOccurenceTimestamp = "timestamp"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyAge = "age"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyBirthday = "birthday"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyGender = "gender"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyEvent = "event"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyChannels = "channels"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyLocationLatitude = "latitude"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyLocationLongitude = "longitude"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeySuccess = "success"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyMessage = "message"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyMessages = "messages"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyPushId = "push_id"; // NON_NLS log
	private static final String HoCPushBoxSDKJSONKeyPushReadTime = "read_datetime"; // NON_NLS log
	// api methods
	private static final String HoCPushBoxSDKMethodSetToken = "set_token"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodSetAge = "set_age"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodSetBirthday = "set_birthday"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodLogEvent = "log_event"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodLogLocation = "log_location"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodSetGender = "set_gender"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodSetChannels = "set_channels"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodPushInteracted = "push_interaction"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodPushRead = "push_read"; // NON_NLS log
	private static final String HoCPushBoxSDKMethodInbox = "inbox"; // NON_NLS log
	// JSON values
	private static final String HoCPushBoxSDKJSONValuePlatform = "Android"; // NON_NLS log


	// User defaults keys
	private static final String HoCPushBoxSDKDefaultsUid = "uid"; // NON_NLS log
	private static final String HoCPushBoxSDKDefaultsQueue = "queue"; // NON_NLS log
	private static final String HoCPushBoxSDKDefaultsKeyMethod = "method"; // NON_NLS log
	private static final String HoCPushBoxSDKDefaultsKeyDict = "dict";  // NON_NLS log
	private static final String HoCPushBoxSDKDefaultsKeyToken = "token";  // NON_NLS log
	private static final String HoCPushBoxSDKDefaultsKeyAppVersion = "app_version";  // NON_NLS log


	private PushBoxSDK()
	{
		mContext.registerReceiver(this.mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		registerInBackground();
		takeNext();
	}

	// region helpers
	private boolean isReady()
	{
		if (!isInitialized() || mToken == null)
		{
			return false;
		}

		if (!netWorkAvailable())
		{
			return false;
		}
		return true;
	}

	private String hmacForTimeStamp(int timestamp)
	{
		String result = ""; // NON_NLS log
		try
		{
			String data = String.format("%s:%d", mApiKey, timestamp); // NON_NLS log
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			SecretKeySpec secret = new SecretKeySpec(mApiSecret.getBytes(), HMAC_SHA1_ALGORITHM);
			mac.init(secret);
			byte[] digest = mac.doFinal(data.getBytes());
			BigInteger hash = new BigInteger(1, digest);
			result = hash.toString(16);

			if (result.length() % 2 != 0)
			{
				result = "0" + result; // NON_NLS log
			}
		} catch (Exception e)
		{
		}
		return result;
	}


	private int timestamp()
	{
		return (int) (System.currentTimeMillis() / 1000L);
	}

	private JSONObject defaultJson()
	{
		JSONObject object = new JSONObject();
		try
		{
			int ts = timestamp();
			object.put(HoCPushBoxSDKJSONKeyApiKey, mApiKey);
			object.put(HoCPushBoxSDKJSONKeyPlatform, HoCPushBoxSDKJSONValuePlatform);
			object.put(HoCPushBoxSDKJSONKeyOccurenceTimestamp, ts);
			if (mProfileIdentifier != null)
			{
				object.put(HoCPushBoxSDKJSONKeyProfileId, mProfileIdentifier);
			}
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return object;
	}

	private JSONObject tokenObject()
	{
		JSONObject object = defaultJson();
		try
		{
			object.put(HoCPushBoxSDKJSONKeyToken, mToken);
			return object;
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private JSONObject finalizedJson(JSONObject object)
	{
		int timestamp = timestamp();
		String hmac = hmacForTimeStamp(timestamp);
		try
		{
			object.put(HoCPushBoxSDKJSONKeyHMAC, hmac);
			object.put(HoCPushBoxSDKJSONKeyTS, timestamp);
			if (mUid != null)
			{
				object.put(HoCPushBoxSDKJSONKeyUid, mUid);
			}
		} catch (JSONException e)
		{
			e.printStackTrace();
		}
		return object;

	}

	private boolean netWorkAvailable()
	{
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		if (mContext == null)
		{
			return false;
		}
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null)
		{
			return false;
		}
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		if (netInfo == null)
		{
			return false;
		}
		for (NetworkInfo ni : netInfo)
		{
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
			{
				if (ni.isConnected())
				{
					haveConnectedWifi = true;
				}
			}
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
			{
				if (ni.isConnected())
				{
					haveConnectedMobile = true;
				}
			}
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

	// endregion


	// region preferences
	public SharedPreferences.Editor getUserPrefsEditor()
	{
		if (mPrefsEditor == null)
		{
			mPrefsEditor = getUserPrefs().edit();
		}
		return mPrefsEditor;

	}

	public SharedPreferences getUserPrefs()
	{
		if (mPrefs == null)
		{
			mPrefs = mContext.getSharedPreferences(HoCPushBoxSDKSuitName, Context.MODE_PRIVATE);
		}
		return mPrefs;
	}

	private String serialize(Serializable obj)
	{
		if (obj == null)
		{
			return "";
		}
		try
		{
			ByteArrayOutputStream serialObj = new ByteArrayOutputStream();
			ObjectOutputStream objStream = new ObjectOutputStream(serialObj);
			objStream.writeObject(obj);
			objStream.close();
			return encodeBytes(serialObj.toByteArray());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private Object deserialize(String str)
	{
		if (str == null || str.length() == 0)
		{
			return null;
		}
		try
		{
			ByteArrayInputStream serialObj = new ByteArrayInputStream(decodeBytes(str));
			ObjectInputStream objStream = new ObjectInputStream(serialObj);
			return objStream.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private String encodeBytes(byte[] bytes)
	{
		StringBuffer strBuf = new StringBuffer();

		for (int i = 0; i < bytes.length; i++)
		{
			strBuf.append((char) (((bytes[i] >> 4) & 0xF) + ((int) 'a')));
			strBuf.append((char) (((bytes[i]) & 0xF) + ((int) 'a')));
		}

		return strBuf.toString();
	}

	private byte[] decodeBytes(String str)
	{
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < str.length(); i+=2)
		{
			char c = str.charAt(i);
			bytes[i/2] = (byte) ((c - 'a') << 4);
			c = str.charAt(i+1);
			bytes[i/2] += (c - 'a');
		}
		return bytes;
	}

	// endregion

	// region queue
	private void addJsonMethodToQueue(JSONObject json, String method)
	{

		try
		{
			JSONObject object = new JSONObject();
			object.put(HoCPushBoxSDKDefaultsKeyMethod, method);
			object.put(HoCPushBoxSDKDefaultsKeyDict, json);
			ArrayList<String> queue = (ArrayList) deserialize(getUserPrefs().getString(HoCPushBoxSDKDefaultsQueue, serialize(new ArrayList())));
			queue.add(object.toString());
			getUserPrefsEditor().putString(HoCPushBoxSDKDefaultsQueue, serialize(queue)).commit();

		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		takeNext();
	}



	private JSONObject peekQueue()
	{
		ArrayList<String> queue = (ArrayList) deserialize(getUserPrefs().getString(HoCPushBoxSDKDefaultsQueue, serialize(new ArrayList())));
		if (queue.size() > 0)
		{
			String top = queue.get(0);
			try
			{
				JSONObject object = new JSONObject(top);
				return object;
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		return null;

	}

	private JSONObject popQueue()
	{
		ArrayList<String> queue = (ArrayList) deserialize(getUserPrefs().getString(HoCPushBoxSDKDefaultsQueue, serialize(new ArrayList())));
		if (queue.size() > 0)
		{
			String top = queue.remove(0);
			getUserPrefsEditor().putString(HoCPushBoxSDKDefaultsQueue, serialize(queue)).commit();
			try
			{
				JSONObject object = new JSONObject(top);
				return object;
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}

		}
		return null;
	}

	// endregion

	// region job execution
	private void takeNext()
	{
		if (!isReady())
		{
			return;
		}
		if (mIsWorking)
		{
			return;
		}
		mIsWorking = true;
		if (!mTokenSend)
		{
			JSONObject tokenObject = tokenObject();
			handleRequest(HoCPushBoxSDKMethodSetToken, tokenObject);
		}
		else
		{
			HashSet<String> strings = (HashSet<String>) getUserPrefs().getStringSet(HoCPushBoxSDKDefaultsKeyPendingPushInteractions, null);
			if (strings != null)
			{
				for (String str : strings)
				{
					int messageId = Integer.parseInt(str);
					setMessageInteraction(messageId);
				}
				getUserPrefsEditor().remove(HoCPushBoxSDKDefaultsKeyPendingPushInteractions).commit();
			}

			JSONObject top = peekQueue();
			if (top != null)
			{
				String method = null;
				JSONObject json = null;
				try
				{
					method = top.getString(HoCPushBoxSDKDefaultsKeyMethod);
					json = top.getJSONObject(HoCPushBoxSDKDefaultsKeyDict);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				if (mUid != null)
				{
					if (method != null && json != null)
					{
						handleRequest(method, json);
					}
					else
					{
						popQueue();
						mIsWorking = false;
						takeNext();
					}
				}
				else
				{
					takeNext();
				}
			}
		}
	}

	private void handleInboxRequest(final PushboxInboxCallback callback)
	{
		new AsyncTask<Void, Void, Boolean>()
		{

			String reason = "";
			List<PushBoxMessage> inboxMessages;
			@SuppressWarnings("deprecation")
			@Override
			protected Boolean doInBackground(Void... params)
			{
				try
				{
					JSONObject json = finalizedJson(defaultJson());
					//instantiates httpclient to make request
					DefaultHttpClient httpclient = new DefaultHttpClient();

					//url with the post data
					HttpPost httpost = new HttpPost(HoCPushBoxSDKApiUrl + HoCPushBoxSDKMethodInbox);

					//passes the results to a string builder/entity
					StringEntity se = new StringEntity(finalizedJson(json).toString());

					//sets the post request as the resulting string
					httpost.setEntity(se);
					httpost.setHeader("Accept", "application/json"); // NON_NLS log
					httpost.setHeader("Content-type", "application/json"); // NON_NLS log

					HttpResponse httpResponse = httpclient.execute(httpost);
					if (httpResponse.getStatusLine().getStatusCode() == 200)
					{
						JSONObject result = parseJson(httpResponse);
						JSONArray messages = result.getJSONArray(HoCPushBoxSDKJSONKeyMessages);
						inboxMessages = new ArrayList<PushBoxMessage>();
						for (int i = 0; i < messages.length(); i++)
						{
							JSONObject msg = messages.getJSONObject(i);
							PushBoxMessage pm =new PushBoxMessage(msg);
							inboxMessages.add(pm);
						}
						return true;
					}
					else
					{
						JSONObject result = parseJson(httpResponse);
						if (result != null)
						{
							if (result.has(HoCPushBoxSDKJSONKeyMessage))
							{
								reason= result.getString(HoCPushBoxSDKJSONKeyMessage);
							}
							Log.e(TAG, "failed to get inbox: " + result.toString());
						}
					}
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				} catch (ClientProtocolException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean success)
			{
				super.onPostExecute(success);
				if (callback != null)
				{
					if (success)
					{
						callback.onSuccess(inboxMessages);
					}
					else
					{
						callback.onFailure(reason);
					}
				}
			}
		}.execute();
	}

	private void handleRequest(final String method, final JSONObject json)
	{
		new AsyncTask<Void, Void, Boolean>()
		{


			@SuppressWarnings("deprecation")
			@Override
			protected Boolean doInBackground(Void... params)
			{
				try
				{
					//instantiates httpclient to make request
					DefaultHttpClient httpclient = new DefaultHttpClient();

					//url with the post data
					HttpPost httpost = new HttpPost(HoCPushBoxSDKApiUrl + method);

					//passes the results to a string builder/entity
					StringEntity se = new StringEntity(finalizedJson(json).toString());

					//sets the post request as the resulting string
					httpost.setEntity(se);
					httpost.setHeader("Accept", "application/json"); // NON_NLS log
					httpost.setHeader("Content-type", "application/json"); // NON_NLS log

					HttpResponse httpResponse = httpclient.execute(httpost);

					if (httpResponse.getStatusLine().getStatusCode() == 200)
					{
						JSONObject result = parseJson(httpResponse);
						if (method.equals(HoCPushBoxSDKMethodSetToken))
						{
							// get uid
							mUid = result.getString(HoCPushBoxSDKJSONKeyUid);
							mTokenSend = true;
						}
						else
						{
							// remove
							popQueue();
						}
					}
					else
					{
						// TODO: handle error
						JSONObject result = parseJson(httpResponse);
						if (!method.equals(HoCPushBoxSDKMethodSetToken))
						{
							popQueue();
						}
						Log.e("PushBoxSDK", "result:" + result.toString());
					}

					return true;
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
				catch (UnsupportedEncodingException e)
				{
					e.printStackTrace();
				} catch (ClientProtocolException e)
				{
					e.printStackTrace();
				} catch (IOException e)
				{
					e.printStackTrace();
				}

				return false;
			}

			@Override
			protected void onPostExecute(Boolean aBoolean)
			{
				super.onPostExecute(aBoolean);
				mIsWorking = false;
				takeNext();
			}
		}.execute(null, null, null);
	}


	private JSONObject parseJson(HttpResponse httpResponse) throws  IOException, JSONException
	{

		BufferedReader inBuffer = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

		StringBuffer stringBuffer = new StringBuffer("");
		String line = "";
		String newLine = System.getProperty("line.separator");
		while ((line = inBuffer.readLine()) != null)
		{
			stringBuffer.append(line + newLine);
		}
		inBuffer.close();

		return new JSONObject(stringBuffer.toString());
	}

	// endregion


	// push stuff
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId()
	{
		final SharedPreferences prefs = getUserPrefs();
		String registrationId = prefs.getString(HoCPushBoxSDKDefaultsKeyToken, null); // NON_NSL log
		if (registrationId == null || registrationId.isEmpty())
		{
			return null; // NON_NSL log
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing registration ID is not guaranteed to work with
		// the new app version.
		int registeredVersion = prefs.getInt(HoCPushBoxSDKDefaultsKeyAppVersion, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion)
		{
			return null;
		}
		return registrationId;
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion()
	{
		try
		{
			PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
			return packageInfo.versionCode;
		}
		catch (PackageManager.NameNotFoundException e)
		{
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param regId registration ID
	 */
	private void storeRegistrationId(String regId)
	{
		int appVersion = getAppVersion();
		getUserPrefsEditor().putString(HoCPushBoxSDKDefaultsKeyToken, regId).putInt(HoCPushBoxSDKDefaultsKeyAppVersion, appVersion).commit();
	}

	private void registerInBackground()
	{
		if (!isInitialized())
		{

		}
		mGcm = GoogleCloudMessaging.getInstance(mContext);
		mToken = getRegistrationId();
		if (mToken == null)
		{
			new AsyncTask<Void, Boolean, Boolean>()
			{

				@Override
				protected Boolean doInBackground(Void... params)
				{
					if (mGcm == null)
					{
						mGcm = GoogleCloudMessaging.getInstance(mContext);
					}
					try
					{
						mToken = mGcm.register(mSenderId);
						storeRegistrationId(mToken);

						return true;
					}
					catch (IOException e)
					{
						e.printStackTrace();

					}
					return false;
				}

				@Override
				protected void onPostExecute(Boolean aBoolean)
				{
					super.onPostExecute(aBoolean);

					if (aBoolean)
					{
						takeNext();
					}
				}
			}.execute(null, null, null);
		}
		else
		{
			takeNext();
		}
	}
	// endregion

	// region network monitor
	private BroadcastReceiver mConnReceiver = new BroadcastReceiver()
	{
		public void onReceive(Context context, Intent intent)
		{
			takeNext();
		}
	};

	// endregion
}
