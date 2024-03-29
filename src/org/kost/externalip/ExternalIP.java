package org.kost.externalip;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
// import android.util.Log;
import android.content.Intent;
import android.content.SharedPreferences;

public class ExternalIP extends Activity implements OnClickListener {
	private EditText ip;
	private EditText aip;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ip = (EditText) findViewById(R.id.extip);
        aip = (EditText) findViewById(R.id.androidip);

        Button btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(this);

        Button btnPrefs = (Button)findViewById(R.id.btnPreferences);
        btnPrefs.setOnClickListener(this);

        Button btnCopyExtIP = (Button)findViewById(R.id.btnExtIP);
        btnCopyExtIP.setOnClickListener(this);
        
        Button btnCopyIntfIP = (Button)findViewById(R.id.btnIntfIP);
        btnCopyIntfIP.setOnClickListener(this);
        
        updateIP();
    }
    
    private void updateIP() {
    	getCurrentIP();
    	dispAndroidIP();
    	
    	Toast.makeText(getApplicationContext(), "IP refreshed",Toast.LENGTH_SHORT).show();
    }
    
    private void copyExtIP() {
    	android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE); 
    	clipboard.setText(ip.getText());
    	
    	Toast.makeText(getApplicationContext(), 
    			getString(R.string.toast_copied)+" "+ip.getText()+" "+getString(R.string.toast_to_clipboard), 
    			Toast.LENGTH_SHORT).show();   
    }

    private void copyIntfIP() {
    	android.text.ClipboardManager clipboard = (android.text.ClipboardManager)getSystemService(CLIPBOARD_SERVICE); 
    	clipboard.setText(aip.getText());
    	
    	Toast.makeText(getApplicationContext(), 
    			getString(R.string.toast_copied)+" "+aip.getText()+" "+getString(R.string.toast_to_clipboard), 
    			Toast.LENGTH_SHORT).show();   
    }
    
    public void onClick(View v) {
    	switch (v.getId()) {
    	   case R.id.btnPreferences:
    	      Intent intent = new Intent(this,PrefsActivity.class);
    	      startActivity(intent);
    	      break;
    	 
    	   case R.id.btnRefresh:
    	      updateIP();
    	      break;
    	      
    	   case R.id.btnExtIP:
    		   copyExtIP();
    		   break;

    	   case R.id.btnIntfIP:
    		   copyIntfIP();
    		   break;    	 
    	   }
    }
    
	private String getAndroidIP () {
    	    try {
                String interfaces="";
    	        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
    	            NetworkInterface intf = en.nextElement();
    	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
    	                InetAddress inetAddress = enumIpAddr.nextElement();
    	                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
    	                    interfaces=interfaces+inetAddress.getHostAddress().toString()+"\n";
    	                }
    	            }
    	        }
                return(interfaces);
    	    } catch (SocketException ex) {
    	        // Log.i("externalip", ex.toString());
    	    }
    	    return null;
    }
    
    private void dispAndroidIP () {
    	String andIP;
    	aip.setText(getString(R.string.info_please_wait));
    	andIP = getAndroidIP();
    	if (andIP==null) {
    		aip.setText(getString(R.string.info_error));
    	} else {
    		aip.setText(andIP);
    	}
    }
    
    private void getCurrentIP () {
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String useurl;
    	String remoteurl = prefs.getString("remoteurl", "");
    	if (remoteurl == "") {
    		useurl = prefs.getString("remoteurllist","http://wtfismyip.com/text"); 
    	} else {
    		useurl = remoteurl;
    	}
    	
        ip.setText(getString(R.string.info_please_wait));  
        try {
        	HttpClient httpclient = new DefaultHttpClient();
        	HttpGet httpget = new HttpGet(useurl);
        	HttpResponse response;
        	
        	response = httpclient.execute(httpget);
        	
        	//Log.i("externalip",response.getStatusLine().toString());
        	
        	HttpEntity entity = response.getEntity();
        	if (entity != null) {
        		long len = entity.getContentLength();
        		if (len != -1 && len < 1024) {
        			String str=EntityUtils.toString(entity);
        			//Log.i("externalip",str);
                    ip.setText(str);
        		} else {
        			ip.setText(getString(R.string.info_response_long));
        			//debug
        			//ip.setText("Response too long or error: "+EntityUtils.toString(entity));
        			//Log.i("externalip",EntityUtils.toString(entity));
        		}            
        	} else {
        		ip.setText(getString(R.string.info_error)+response.getStatusLine().toString());
        	}
            
        }
        catch (Exception e)
        {
            ip.setText(getString(R.string.info_error));
        }

    }
    
}