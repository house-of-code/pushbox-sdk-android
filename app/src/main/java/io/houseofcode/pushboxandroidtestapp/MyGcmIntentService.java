package io.houseofcode.pushboxandroidtestapp;

import android.content.Intent;
import android.util.Log;

import io.houseofcode.pushboxsdk.PushBoxSDKGcmIntentService;
import io.houseofcode.pushboxsdk.PushBoxMessage;

/**
 * Created by gsl on 04/12/15.
 */
public class MyGcmIntentService extends PushBoxSDKGcmIntentService
{

	@Override
	protected void onHandleIntent(Intent intent)
	{
		super.onHandleIntent(intent);
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	@Override
	protected void handlePushReceived(PushBoxMessage message)
	{
		Log.d("jojo", "Daz msg:" + message);
	}
}
