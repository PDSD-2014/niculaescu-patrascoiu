package com.example.whiteboard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.drawings.DrawingActivity;
import com.example.network.Listener;
import com.example.network.NDS;

public class MainActivity extends Activity
{	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_activity);
        Intent drawIntent = new Intent(this, DrawingActivity.class);   
        startActivity( drawIntent);
    }
}
