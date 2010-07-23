package com.boombuler.widgets.contacts;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boombuler.widgets.contacts.SessionEvents.AuthListener;
import com.boombuler.widgets.contacts.SessionEvents.LogoutListener;
import com.boombuler.widgets.contacts.LoginButton;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class FBActivity extends Activity{
    /** Called when the activity is first created. */
	// Your Facebook Application ID must be set before running this example
    // See http://www.facebook.com/developers/createapp.php
    
	public static final String APP_ID = "4b90947a7e082e542d9f22dc07035d5f";
    
    private static final String[] PERMISSIONS =
        new String[] {"publish_stream", "read_stream", "offline_access"};
    private LoginButton mLoginButton;
    private TextView mText;
    private Button mRequestButton;
    private Button mPostButton;
    private Button mDeleteButton;
    
    private Facebook mFacebook;
    private AsyncFacebookRunner mAsyncRunner;

    

 // Save settings 
      public void WriteSettings(Context context, String data){ 
     	 FileOutputStream fOut = null; 
     	 OutputStreamWriter osw = null;
     	 
     	 try{
     	  fOut = openFileOutput("settings.dat",MODE_PRIVATE);       
           osw = new OutputStreamWriter(fOut); 
           osw.write(data); 
           osw.flush(); 
           //Toast.makeText(context, "Settings saved",Toast.LENGTH_SHORT).show();
           } 
           catch (Exception e) {       
           e.printStackTrace(); 
           //Toast.makeText(context, "Settings not saved",Toast.LENGTH_SHORT).show();
           } 
           finally { 
              try { 
                     osw.close(); 
                     fOut.close(); 
                     } catch (IOException e) { 
                     e.printStackTrace(); 
                     } 
           } 
      }
      

   // Read settings 
        public String ReadSettings(Context context){ 
       	 FileInputStream fIn = null; 
       	 InputStreamReader isr = null;
       	 
       	 char[] inputBuffer = new char[255]; 
       	 String data = null;
       	 
       	 try{
       	  fIn = openFileInput("settings.dat");       
             isr = new InputStreamReader(fIn); 
             isr.read(inputBuffer); 
             data = new String(inputBuffer);
             //Toast.makeText(context, "Settings read",Toast.LENGTH_SHORT).show();
             } 
             catch (Exception e) {       
             e.printStackTrace(); 
             //Toast.makeText(context, "Settings not read",Toast.LENGTH_SHORT).show();
             } 
             finally { 
                try { 
                       isr.close(); 
                       fIn.close(); 
                       } catch (IOException e) { 
                       e.printStackTrace(); 
                       } 
             }
   		return data; 
        }
      
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (APP_ID == null) {
            Util.showAlert(this, "Warning", "Facebook Applicaton ID must be " +
                    "specified before running this example: see Example.java");
        }
        
        setContentView(R.layout.facebook);
        mLoginButton = (LoginButton) findViewById(R.id.login);
        mText = (TextView) FBActivity.this.findViewById(R.id.txt);
        mRequestButton = (Button) findViewById(R.id.requestButton);
        mPostButton = (Button) findViewById(R.id.postButton);
        mDeleteButton = (Button) findViewById(R.id.deletePostButton);
        
       	mFacebook = new Facebook();
       	mAsyncRunner = new AsyncFacebookRunner(mFacebook);
       
        SessionStore.restore(mFacebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
        mLoginButton.init(mFacebook, PERMISSIONS);
        
        mRequestButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	mAsyncRunner.request("me/friends", new SampleRequestListener());
            }
        });
        mRequestButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE :
                View.INVISIBLE);
        
        mPostButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mFacebook.dialog(FBActivity.this, "stream.publish", 
                        new SampleDialogListener());          
            }
        });
        
        mPostButton.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE : 
                View.INVISIBLE);
    }
    
    public class SampleAuthListener implements AuthListener {
        
        public void onAuthSucceed() {
            mText.setText("You have logged in! ");
            mRequestButton.setVisibility(View.VISIBLE);
            mPostButton.setVisibility(View.VISIBLE);
        }

        public void onAuthFail(String error) {
            mText.setText("Login Failed: " + error);
        }
    }
    
    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            mText.setText("Logging out...");
        }
        
        public void onLogoutFinish() {
            mText.setText("You have logged out! ");
            mRequestButton.setVisibility(View.INVISIBLE);
            mPostButton.setVisibility(View.INVISIBLE);
        }
    }
    
    public class SampleRequestListener extends BaseRequestListener {

        public void onComplete(final String response) {
        	String datalist = "";
        	try {
                // process the response here: executed in background thread
                Log.d("Facebook-Example", "Response: " + response.toString());
                JSONArray data = new JSONObject(response.toString()).getJSONArray("data");
				for (int i = 0; i < data.length(); i++) {
                JSONObject curData = data.getJSONObject(i);
                datalist = curData + "\n" + datalist;
                Log.d("Facebook-Example", "Response: " + curData.toString());
                }
				WriteSettings(FBActivity.this, datalist);
                
                
                // then post the processed result back to the UI thread
                // if we do not do this, an runtime exception will be generated
                // e.g. "CalledFromWrongThreadException: Only the original 
                // thread that created a view hierarchy can touch its views."
                FBActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                
                    }
                });
				} catch (JSONException e) {
					Log.w("Facebook-Example", "JSON Error in response");
				}
        }
				
    }
    
    public class WallPostRequestListener extends BaseRequestListener {
        
        public void onComplete(final String response) {
            Log.d("Facebook-Example", "Got response: " + response);
            String message = "<empty>";
            try {
                JSONObject json = Util.parseJson(response);
                message = json.getString("message");
            } catch (JSONException e) {
                Log.w("Facebook-Example", "JSON Error in response");
            } catch (FacebookError e) {
                Log.w("Facebook-Example", "Facebook Error: " + e.getMessage());
            }
            final String text = "Your Wall Post: " + message;
            FBActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    mText.setText(text);
                }
            });
        }
    }
    
    public class WallPostDeleteListener extends BaseRequestListener {
        
        public void onComplete(final String response) {
            if (response.equals("true")) {
                Log.d("Facebook-Example", "Successfully deleted wall post");
                FBActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        mDeleteButton.setVisibility(View.INVISIBLE);
                        mText.setText("Deleted Wall Post");
                    }
                });
            } else {
                Log.d("Facebook-Example", "Could not delete wall post");
            }
        }
    }
    
    public class SampleDialogListener extends BaseDialogListener {

        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                Log.d("Facebook-Example", "Dialog Success! post_id=" + postId);
                mAsyncRunner.request(postId, new WallPostRequestListener());
                mDeleteButton.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        mAsyncRunner.request(postId, new Bundle(), "DELETE", 
                                new WallPostDeleteListener());
                    }
                });
                mDeleteButton.setVisibility(View.VISIBLE);
            } else {
                Log.d("Facebook-Example", "No wall post made");
            }
        }
    }
}