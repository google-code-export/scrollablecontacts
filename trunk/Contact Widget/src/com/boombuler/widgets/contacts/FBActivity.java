package com.boombuler.widgets.contacts;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.boombuler.widgets.contacts.SessionEvents.AuthListener;
import com.boombuler.widgets.contacts.SessionEvents.LogoutListener;
import com.boombuler.widgets.contacts.LoginButton;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;
import com.facebook.android.Util;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class FBActivity extends Activity{
    /** Called when the activity is first created. */
	// Your Facebook Application ID must be set before running this example
    // See http://www.facebook.com/developers/createapp.php
    
	public static final String APP_ID = "4b90947a7e082e542d9f22dc07035d5f";
    
    private static final String[] PERMISSIONS =
        new String[] {"publish_stream", "read_stream", "offline_access"};
    private LoginButton mLoginButton;
    private Button mRequestButton;
    private Button mGetPicturesBtn;
        
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
       	 
       	 char[] inputBuffer = new char[1000]; 
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
        mRequestButton = (Button) findViewById(R.id.requestButton);
        mGetPicturesBtn = (Button) findViewById(R.id.postButton);
        
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
        
        mGetPicturesBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
            	String myText = "";
            	FileInputStream fis = null;            
            	            	
            	try {
					fis = openFileInput("settings.dat");
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	String line;
            	
            	DataInputStream dis = new DataInputStream(fis);
            	try {
					while((line=dis.readLine())!=null)
						Log.v("Outp", line);
					//JSONObject json = line;
	                //final String name = json.getString("id"); 
					myText = myText+line;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	
            	            	
            	String photo_url_str = "http://graph.facebook.com/"/*+FaceBookID+*/+"/picture";
            	Bitmap mIcon_val;
            	ImageView imgView; 
            	URL newurl;
				try {
					newurl = new URL(photo_url_str);
					try {
						mIcon_val = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
						imgView = (ImageView)findViewById(R.id.image1);
		            	imgView.setImageBitmap(mIcon_val);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
	            	
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
            	
            	
            	} 
        	});
        
        mGetPicturesBtn.setVisibility(mFacebook.isSessionValid() ?
                View.VISIBLE : 
                View.INVISIBLE);
    }
    
    public class SampleAuthListener implements AuthListener {
        
        public void onAuthSucceed() {
        	Toast.makeText(FBActivity.this,"You have logged in! ", Toast.LENGTH_SHORT);
            mRequestButton.setVisibility(View.VISIBLE);
            
        }

        public void onAuthFail(String error) {
            Toast.makeText(FBActivity.this,"Login Failed: " + error, Toast.LENGTH_SHORT);
        }
    }
    
    public class SampleLogoutListener implements LogoutListener {
        public void onLogoutBegin() {
            Toast.makeText(FBActivity.this,"Logging out...", Toast.LENGTH_SHORT);
        }
        
        public void onLogoutFinish() {
        	Toast.makeText(FBActivity.this,"You have logged out! ", Toast.LENGTH_SHORT);
            mRequestButton.setVisibility(View.INVISIBLE);
            mGetPicturesBtn.setVisibility(View.INVISIBLE);
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
				mGetPicturesBtn.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
					Log.w("Facebook-Example", "JSON Error in response");
				}
        }
				
    }
    
}