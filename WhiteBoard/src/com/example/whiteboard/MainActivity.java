package com.example.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.drawings.DrawingActivity;
import com.example.network.NDS;

public class MainActivity extends Activity
{
	
	private NDS service = new NDS();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        service = new NDS();
        service.initializeDiscoveryListener();
        service.initializeResolveListener();
        service.initializeRegistrationListener();
        service.registerService(this, 30000);
        setContentView(R.layout.drawing_activity);
        Intent drawIntent = new Intent(this, DrawingActivity.class);   
        startActivity( drawIntent);
    }
	
	@Override 
	public void onPause() {
		//service.unregister();
		super.onPause();
	}
	
	@Override 
	public void onResume() {
		//service.registerService(this, 30000);
		super.onResume();
	}
	
	@Override
	public void onDestroy() {
		service.unregister();
		super.onDestroy();
	}
	
}
