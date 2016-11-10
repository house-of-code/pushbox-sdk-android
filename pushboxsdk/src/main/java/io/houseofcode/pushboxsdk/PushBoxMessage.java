package io.houseofcode.pushboxsdk;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gsl on 03/12/15.
 */
public class PushBoxMessage
{
	public PushBoxMessage()
	{

	}

	public PushBoxMessage(Bundle message)
	{
		if (!message.isEmpty())
		{
			try
			{
				String idString = message.getString(HoCPushJsonKeyMessageId, "0");
				if (!idString.equalsIgnoreCase("null"))
				{
					mId = Integer.parseInt(idString);
				}
				else
				{
					mId = 0;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			mTitle = message.getString(HoCPushJsonKeyTitle);
			mMessage = message.getString(HoCPushJsonKeyMessage);
			try
			{
				String payloadStr = message.getString(HoCPushJsonKeyPayload);
				if (payloadStr != null && payloadStr.length() > 0)
				{
					mPayload = new JSONObject(payloadStr);
				}

			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
			try
			{
				String dateStr = message.getString(HoCPushJsonKeyDeliverDateTime);
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm.ss.SSS'Z'");
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mDeliverDateTime =  df.parse(dateStr);
				}
				dateStr = message.getString(HoCPushJsonKeyReadDateTime);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mReadDateTime =  df.parse(dateStr);
				}
				dateStr = message.getString(HoCPushJsonKeyHandledDateTime);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mHandleDateTime =  df.parse(dateStr);
				}
				dateStr = message.getString(HoCPushJsonKeyExpirationDate);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mExpirationDateTime =  df.parse(dateStr);
				}
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}
	}

	public PushBoxMessage(JSONObject message)
	{
		try
		{
			mId = message.getInt(HoCPushJsonKeyMessageId);
			mTitle = message.getString(HoCPushJsonKeyTitle);
			mMessage = message.getString(HoCPushJsonKeyMessage);
			mPayload = new JSONObject();
			if (message.has(HoCPushJsonKeyPayload))
			{
				Object payload = message.get(HoCPushJsonKeyPayload);
				if (payload instanceof JSONObject)
				{
					mPayload = (JSONObject) payload;
				}
			}
			String dateStr;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			if (message.has(HoCPushJsonKeyDeliverDateTime))
			{
				dateStr = message.getString(HoCPushJsonKeyDeliverDateTime);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mDeliverDateTime = df.parse(dateStr);
				}
			}
			if (message.has(HoCPushJsonKeyReadDateTime))
			{
				dateStr = message.getString(HoCPushJsonKeyReadDateTime);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mReadDateTime = df.parse(dateStr);
				}
			}
			if (message.has(HoCPushJsonKeyHandledDateTime))
			{
				dateStr = message.getString(HoCPushJsonKeyHandledDateTime);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mHandleDateTime =  df.parse(dateStr);
				}
			}
			if (message.has(HoCPushJsonKeyExpirationDate))
			{
				dateStr = message.getString(HoCPushJsonKeyExpirationDate);
				if (dateStr != null && dateStr.length() > 0 && !dateStr.equals("null"))
				{
					mExpirationDateTime = df.parse(dateStr);
				}
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}
	public int getId()
	{
		return mId;
	}

	public void setId(int id)
	{
		this.mId = id;
	}

	public String getTitle()
	{
		return mTitle;
	}

	public void setTitle(String title)
	{
		this.mTitle = title;
	}

	public String getMessage()
	{
		return mMessage;
	}

	public void setMessage(String message)
	{
		this.mMessage = message;
	}

	public JSONObject getPayload()
	{
		if (mPayload == null)
		{
			mPayload = new JSONObject();
		}
		return mPayload;
	}

	public void setPayload(JSONObject payload)
	{
		this.mPayload = payload;
	}

	public Date getDeliverDateTime()
	{
		return mDeliverDateTime;
	}

	public void setDeliverDateTime(Date deliverDateTime)
	{
		this.mDeliverDateTime = deliverDateTime;
	}

	public Date getReadDateTime()
	{
		return mReadDateTime;
	}

	public void setReadDateTime(Date readDateTime)
	{
		this.mReadDateTime = readDateTime;
	}

	public Date getHandleDateTime()
	{
		return mHandleDateTime;
	}

	public void setHandleDateTime(Date handleDateTime)
	{
		this.mHandleDateTime = handleDateTime;
	}

	public Date getExpirationDateTime()
	{
		return mExpirationDateTime;
	}

	public void setExpirationDateTime(Date expirationDateTime)
	{
		this.mExpirationDateTime = expirationDateTime;
	}


	@Override
	public String toString()
	{
		return "" + getId() + ":" + getTitle() + ". Message:" + getMessage() + ". Payload: " + getPayload().toString();
	}

	private int mId;
	private String mTitle;
	private String mMessage;
	private JSONObject mPayload;
	private Date mDeliverDateTime;
	private Date mReadDateTime;
	private Date mHandleDateTime;
	private Date mExpirationDateTime;
	private static final String HoCPushJsonKeyDeliverDateTime = "deliver_datetime";
	private static final String HoCPushJsonKeyExpirationDate = "expiration_date";
	private static final String HoCPushJsonKeyMessageId = "id";
	private static final String HoCPushJsonKeyMessage = "message";
	private static final String HoCPushJsonKeyPayload = "payload";
	private static final String HoCPushJsonKeyReadDateTime = "read_datetime";
	private static final String HoCPushJsonKeyHandledDateTime = "handled_datetime";
	private static final String HoCPushJsonKeyTitle = "title";



}
