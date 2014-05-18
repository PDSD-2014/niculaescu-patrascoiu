package com.example.drawings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.drawings.DrawingPath;
import com.example.drawings.DrawingSurface;
import com.example.listener.Listener;
import com.example.whiteboard.R;
import com.example.brush.*;
import com.example.colorPicker.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DrawingActivity extends Activity implements View.OnTouchListener, ColorPickerDialog.OnColorChangedListener{
    private DrawingSurface drawingSurface;
    private DrawingPath currentDrawingPath;
    private Paint currentPaint;
	private static final String COLOR_PREFERENCE_KEY = "color";
    
    public DrawingActivity() throws IOException {
    	Listener.getListener().setDrawingActivity(this);
    }
    
    private ArrayList<Float> coords;

    private Button redoBtn;
    private Button undoBtn;

    private Brush currentBrush;

    private File APP_FILE_PATH = new File(Environment.getExternalStorageDirectory().getPath() + "/TutorialForAndroidDrawings");

	final String[] names = {"Line", "Circle", "Disc", "Square"};
	final int[] images = {R.drawable.line, R.drawable.circle, R.drawable.line, R.drawable.line};
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawing_activity);

        setCurrentPaint();
        currentBrush = new PenBrush();
        
        drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
        drawingSurface.setOnTouchListener(this);
        drawingSurface.previewPath = new DrawingPath();
        drawingSurface.previewPath.path = new Path();
        drawingSurface.previewPath.paint = getPreviewPaint();


        redoBtn = (Button) findViewById(R.id.redoBtn);
        undoBtn = (Button) findViewById(R.id.undoBtn);
        
        Spinner s = (Spinner) findViewById(R.id.brushMenu);
    	s.setAdapter(new BrushSpinnerAdapter(this, R.layout.brush_descr, names));
    	s.setOnItemSelectedListener(new OnItemSelectedListener() {
    	    @Override
    	    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    	        switch(position) {
    	        	case 0:
    	        		currentBrush = new PenBrush();
    	        		break;
    	        	case 1:
    	        		currentBrush = new CircleBrush();
    	        		break;
    	        	case 2:
    	        		currentBrush = new DiscBrush();
    	        		break;
    	        	case 3:
    	        		currentBrush = new SquareBrush();
    	        		break;
    	        }
    	    }

			@Override
			public void onNothingSelected(AdapterView<?> parent) {}
    	});

        redoBtn.setEnabled(false);
        undoBtn.setEnabled(false);
        
        Button btn = (Button) findViewById(R.id.colorPick);
    	btn.setOnClickListener(new View.OnClickListener() {
    	    @Override
    	    public void onClick(View v) {
    	        int color = PreferenceManager.getDefaultSharedPreferences(
    	                DrawingActivity.this).getInt(COLOR_PREFERENCE_KEY,
    	                Color.WHITE);
    	        new ColorPickerDialog(DrawingActivity.this, DrawingActivity.this,
    	                color).show();
    	    }
    	});
        
    }

    private void setCurrentPaint(){
        currentPaint = new Paint();
        currentPaint.setDither(true);
        currentPaint.setColor(0xFFFFFF00);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setStrokeJoin(Paint.Join.ROUND);
        currentPaint.setStrokeCap(Paint.Cap.ROUND);
        currentPaint.setStrokeWidth(3);

    }

    private Paint getPreviewPaint(){
        final Paint previewPaint = new Paint();
        previewPaint.setColor(0xFFC1C1C1);
        previewPaint.setStyle(Paint.Style.STROKE);
        previewPaint.setStrokeJoin(Paint.Join.ROUND);
        previewPaint.setStrokeCap(Paint.Cap.ROUND);
        previewPaint.setStrokeWidth(3);
        return previewPaint;
    }



 
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
            drawingSurface.isDrawing = true;

            coords = new ArrayList<Float>();
            currentDrawingPath = new DrawingPath();
            currentDrawingPath.paint = currentPaint;
            currentDrawingPath.path = new Path();
            currentBrush.mouseDown(currentDrawingPath.path, motionEvent.getX(), motionEvent.getY());
            currentBrush.mouseDown(drawingSurface.previewPath.path, motionEvent.getX(), motionEvent.getY());
            coords.add(motionEvent.getX());
            coords.add(motionEvent.getY());

            
        }else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){
            drawingSurface.isDrawing = true;
            currentBrush.mouseMove(currentDrawingPath.path, motionEvent.getX(), motionEvent.getY() );
            currentBrush.mouseMove(drawingSurface.previewPath.path, motionEvent.getX(), motionEvent.getY());
            coords.add(motionEvent.getX());
            coords.add(motionEvent.getY());


        }else if(motionEvent.getAction() == MotionEvent.ACTION_UP){


            currentBrush.mouseUp(drawingSurface.previewPath.path, motionEvent.getX(), motionEvent.getY());
            drawingSurface.previewPath.path = new Path();
            drawingSurface.addDrawingPath(currentDrawingPath);

            currentBrush.mouseUp(currentDrawingPath.path, motionEvent.getX(), motionEvent.getY() );
            
            coords.add(motionEvent.getX());
            coords.add(motionEvent.getY());
            
            /* Send the new drawing to the other connected devices. */
            transmitPath(coords);

            undoBtn.setEnabled(true);
            redoBtn.setEnabled(false);

        }

        return true;
    }
    
    /**
     * Send the data to all other devices.
     */
    private void transmitPath(ArrayList<Float> coords) {
        ByteBuffer buffer = ByteBuffer.allocate(4 + 4 * coords.size());

        /* Build a byte buffer with the data. */
        buffer.putFloat(currentDrawingPath.paint.getColor());
        for (float value : coords) {
            buffer.putFloat(value + 10);
        }
		
        Listener.getListener().sendPath(buffer);
	}

	public void auxDraw(float[] fs) {
		
    	DrawingPath path = new DrawingPath();
    	Paint paint = new Paint();

        paint.setDither(true);
        paint.setColor((int)fs[0]);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        
    	path.path = new Path();
    	path.paint = paint;
    
    	Brush br = new PenBrush();
    	br.mouseDown(path.path, fs[1], fs[2] + 10);
    	for (int i = 1; i < fs.length; i += 2)
    		br.mouseMove(path.path, fs[i], fs[i + 1] + 10);
        drawingSurface.addDrawingPath(path);
    }
	
	@Override
	public void colorChanged(int color) {
	PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
	        COLOR_PREFERENCE_KEY, color).commit();
		currentPaint = new Paint();
	    currentPaint.setDither(true);
	    currentPaint.setColor(color);
	    currentPaint.setStyle(Paint.Style.STROKE);
	    currentPaint.setStrokeJoin(Paint.Join.ROUND);
	    currentPaint.setStrokeCap(Paint.Cap.ROUND);
	    currentPaint.setStrokeWidth(3);
	}

    @SuppressLint("HandlerLeak")
	public void onClick(View view){
        switch (view.getId()){
	        case R.id.colorPick:
	            currentPaint = new Paint();
	            currentPaint.setDither(true);
	            currentPaint.setColor(0xFFFF0000);
	            currentPaint.setStyle(Paint.Style.STROKE);
	            currentPaint.setStrokeJoin(Paint.Join.ROUND);
	            currentPaint.setStrokeCap(Paint.Cap.ROUND);
	            currentPaint.setStrokeWidth(3);
	            break;

            case R.id.undoBtn:
                drawingSurface.undo();
                if( drawingSurface.hasMoreUndo() == false ){
                    undoBtn.setEnabled( false );
                }
                redoBtn.setEnabled( true );
                break;

            case R.id.redoBtn:
                drawingSurface.redo();
                if( drawingSurface.hasMoreRedo() == false ){
                    redoBtn.setEnabled( false );
                }

                undoBtn.setEnabled( true );
                break;
            /*case R.id.saveBtn:
                final Activity currentActivity  = this;
                Handler saveHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
                        alertDialog.setTitle("Saved 1");
                        alertDialog.setMessage("Your drawing had been saved :)");
                        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                        alertDialog.show();
                    }
                } ;
               new ExportBitmapToFile(this, saveHandler, drawingSurface.getBitmap()).execute();
            break;
            case R.id.circleBtn:
                currentBrush = new CircleBrush();
            break;
            case R.id.pathBtn:
                currentBrush = new PenBrush();
            break;*/
        }
    }


    private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> {
        private Handler mHandler;
        private Bitmap nBitmap;

        public ExportBitmapToFile(Context context,Handler handler,Bitmap bitmap) {
            nBitmap = bitmap;
            mHandler = handler;
        }

        @Override
        protected Boolean doInBackground(Intent... arg0) {
            try {
                if (!APP_FILE_PATH.exists()) {
                    APP_FILE_PATH.mkdirs();
                }

                final FileOutputStream out = new FileOutputStream(new File(APP_FILE_PATH + "/myAwesomeDrawing.png"));
                nBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                out.flush();
                out.close();
                return true;
            }catch (Exception e) {
                e.printStackTrace();
            }
            //mHandler.post(completeRunnable);
            return false;
        }


        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            if ( bool ){
                mHandler.sendEmptyMessage(1);
            }
        }
    }
    
    public class BrushSpinnerAdapter extends ArrayAdapter<String> {

        public BrushSpinnerAdapter(Context context, int textViewResourceId, String[] objects) {
            super(context, textViewResourceId, objects);
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row=inflater.inflate(R.layout.brush_descr, parent, false);
            TextView label=(TextView)row.findViewById(R.id.brName);
            label.setText(names[position]);

            ImageView icon=(ImageView)row.findViewById(R.id.brImage);
            icon.setImageResource(images[position]);

            return row;
        }
     }
}
