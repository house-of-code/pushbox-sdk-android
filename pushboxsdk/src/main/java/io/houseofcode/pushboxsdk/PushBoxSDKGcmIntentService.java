package io.houseofcode.pushboxsdk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.HashSet;

/**
 * Created by gsl on 24/04/15.
 */
public abstract class PushBoxSDKGcmIntentService extends IntentService
{
	private static final String TAG = "PushBoxSDKGcmService";

	public PushBoxSDKGcmIntentService()
	{
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);
		PushBoxMessage message = new PushBoxMessage(extras);
		if (message != null && message.getId() > 0)
		{
			int messageId = message.getId();
			if (PushBoxSDK.isInitialized())
			{
				PushBoxSDK.getInstance().setMessageInteraction(messageId);
			}
			else
			{
				SharedPreferences prefs = getSharedPreferences(PushBoxSDK.HoCPushBoxSDKSuitName, Context.MODE_PRIVATE);
				HashSet<String> strings = (HashSet<String>) prefs.getStringSet(PushBoxSDK.HoCPushBoxSDKDefaultsKeyPendingPushInteractions, new HashSet<String>());
				strings.add(String.format("%d", messageId)); // NON_NLS log
				prefs.edit().putStringSet(PushBoxSDK.HoCPushBoxSDKDefaultsKeyPendingPushInteractions, strings).commit();
			}
			handlePushReceived(message);
		}
	}

	abstract protected void handlePushReceived(PushBoxMessage message);

}
