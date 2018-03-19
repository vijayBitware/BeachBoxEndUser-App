package com.beachbox.beachbox.Config;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
	
	private  Context context;
	private static ConnectionDetector instance = null;

	/*private ConnectionDetector(Context context) {
		// Exists only to defeat instantiation.
		this.context = context;
	}*/
	
	public ConnectionDetector(Context context){
		this.context = context;
	}

	/*public static ConnectionDetector getInstance() {
		if(instance == null) {
			instance = new ConnectionDetector(context);
		}
		return instance;
	}*/

	public boolean isConnectingToInternet(){
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		  if (connectivity != null) 
		  {
			  NetworkInfo[] info = connectivity.getAllNetworkInfo();
			  if (info != null) 
				  for (int i = 0; i < info.length; i++) 
					  if (info[i].getState() == NetworkInfo.State.CONNECTED)
					  {
						  return true;
					  }

		  }
		  return false;
	}


}
