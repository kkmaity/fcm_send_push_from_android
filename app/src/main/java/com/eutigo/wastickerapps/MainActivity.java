package com.eutigo.wastickerapps;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import static com.eutigo.wastickerapps.R.id.txt;

public class MainActivity extends AppCompatActivity {
	private String AUTH_KEY = "";
	private TextView mTextView;
	private DatabaseReference msgFirebaseDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = findViewById(txt);
		AUTH_KEY=getResources().getString(R.string.server_key);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			String tmp = "";
			for (String key : bundle.keySet()) {
				Object value = bundle.get(key);
				tmp += key + ": " + value + "\n\n";
			}
			mTextView.setText(tmp);
		}
		getAllUsersToken();
	}

	private void getAllUsersToken() {

		msgFirebaseDatabase = FirebaseDatabase.getInstance().getReference("fcm_token");
		//   studentIsTypingFirebaseDatabase =  FirebaseDatabase.getInstance().getReference(firebase_table_student_istyping);
		//   coachIsTypingFirebaseDatabase =  FirebaseDatabase.getInstance().getReference(firebase_table_coach_istyping);
		msgFirebaseDatabase.addValueEventListener(valueEventListen);

	}
	ArrayList<String> token,token1;
	private ValueEventListener valueEventListen = new ValueEventListener() {
		@Override
		public void onDataChange(DataSnapshot dataSnapshot) {
			token=new ArrayList<>();
			token1=new ArrayList<>();


				for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
					// TODO: handle the post
					token.add(postSnapshot.getValue().toString());
					token1.add(postSnapshot.getKey());



			}


		}

		@Override
		public void onCancelled(DatabaseError databaseError) {

		}
	};


	public void showToken(View view) {
		mTextView.setText(FirebaseInstanceId.getInstance().getToken());
		Log.i("token", FirebaseInstanceId.getInstance().getToken());
	}

	public void subscribe(View view) {
		FirebaseMessaging.getInstance().subscribeToTopic("news");
		mTextView.setText(R.string.subscribed);
	}

	public void unsubscribe(View view) {
		FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
		mTextView.setText(R.string.unsubscribed);
	}

	public void sendToken(View view) {
		sendWithOtherThread("token");
	}

	public void sendTokens(View view) {
		sendWithOtherThread("tokens");
	}

	public void sendTopic(View view) {
		sendWithOtherThread("topic");
	}

	private void sendWithOtherThread(final String type) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				pushNotification(type);
			}
		}).start();
	}

	private void pushNotification(String type) {
		JSONObject jPayload = new JSONObject();
		JSONObject jNotification = new JSONObject();
		JSONObject jData = new JSONObject();
		try {
			jNotification.put("title", "Google I/O 2016");
			jNotification.put("body", "Firebase Cloud Messaging (App)");
			jNotification.put("sound", "default");
			jNotification.put("badge", "1");
			jNotification.put("click_action", "OPEN_ACTIVITY_1");
			jNotification.put("icon", "ic_notification");

			jData.put("picture", "http://opsbug.com/static/google-io.jpg");

			switch(type) {
				case "tokens":
					JSONArray ja = new JSONArray();
					ja.put("cQw07vB2LqM:APA91bHMEItqxI7AsRp0vt9e0JY5n3czCIzUdK7B1CN1qj9rhko1uppznJnagRH_IDTDbIJqySHlceAncEf6L5ilczwI_38OqmMBkMuoATrSBOWDFlXt78ioC18sl8ZvcU_1au57n8Tz");
					ja.put(FirebaseInstanceId.getInstance().getToken());
					jPayload.put("registration_ids", ja);
					break;
				case "topic":
					jPayload.put("to", "/topics/news");
					break;
				case "condition":
					jPayload.put("condition", "'sport' in topics || 'news' in topics");
					break;
				default:
					jPayload.put("to", FirebaseInstanceId.getInstance().getToken());
			}

			jPayload.put("priority", "high");
			jPayload.put("notification", jNotification);
			jPayload.put("data", jData);

			URL url = new URL("https://fcm.googleapis.com/fcm/send");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", AUTH_KEY);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			// Send FCM message content.
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(jPayload.toString().getBytes());

			// Read FCM response.
			InputStream inputStream = conn.getInputStream();
			final String resp = convertStreamToString(inputStream);

			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					mTextView.setText(resp);
				}
			});
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	private String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next().replace(",", ",\n") : "";
	}
}