package com.example.drawings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.drawings.DrawingPath;
import com.example.drawings.DrawingSurface;
import com.example.listener.Listener;
import com.example.whiteboard.R;
import com.example.brush.Brush;
import com.example.brush.CircleBrush;
import com.example.brush.PenBrush;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class DrawingActivity extends Activity implements View.OnTouchListener{
    private DrawingSurface drawingSurface;
    private DrawingPath currentDrawingPath;
    private Paint currentPaint;
    
    private Listener listener;
    
    public DrawingActivity() throws IOException {
    	Log.d("MAIN", "here1");
        listener = new Listener(this);
        //listener.newConnection("127.0.0.1", 30000);
        Log.d("MAIN", "here");
    }
    
    private ArrayList<Float> coords;

    private Button redoBtn;
    private Button undoBtn;

    private Brush currentBrush;

    private File APP_FILE_PATH = new File("/sdcard/TutorialForAndroidDrawings");

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

        redoBtn.setEnabled(false);
        undoBtn.setEnabled(false);
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
            
            transmitPath(coords);
    		
    		/*try {
    			auxDraw(coords.toArray(new float[coords.size()]));
    		} catch (Exception e) {
    			e.printStackTrace();
    		}*/

            undoBtn.setEnabled(true);
            redoBtn.setEnabled(false);

        }

        return true;
    }
    
    private void transmitPath(ArrayList<Float> coords) {
        ByteBuffer buffer = ByteBuffer.allocate(4 * coords.size());

        for (float value : coords) {
            buffer.putFloat(value + 10);
        }
		
        listener.sendPath(buffer);
	}

	public void auxDraw(float[] fs) {
    	
    	DrawingPath path = new DrawingPath();
    	Paint paint = new Paint();
    	
        paint.setDither(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(3);
        
    	path.path = new Path();
    	path.paint = paint;
    
    	Brush br = new PenBrush();
    	br.mouseDown(path.path, fs[0], fs[1] + 10);
    	for (int i = 0; i < fs.length; i += 2)
    		br.mouseMove(path.path, fs[i], fs[i + 1] + 10);

        drawingSurface.addDrawingPath(path);
    }


    public void onClick(View view){
        switch (view.getId()){
            case R.id.colorRedBtn:
                currentPaint = new Paint();
                currentPaint.setDither(true);
                currentPaint.setColor(0xFFFF0000);
                currentPaint.setStyle(Paint.Style.STROKE);
                currentPaint.setStrokeJoin(Paint.Join.ROUND);
                currentPaint.setStrokeCap(Paint.Cap.ROUND);
                currentPaint.setStrokeWidth(3);
            break;
            case R.id.colorBlueBtn:
                currentPaint = new Paint();
                currentPaint.setDither(true);
                currentPaint.setColor(0xFF00FF00);
                currentPaint.setStyle(Paint.Style.STROKE);
                currentPaint.setStrokeJoin(Paint.Join.ROUND);
                currentPaint.setStrokeCap(Paint.Cap.ROUND);
                currentPaint.setStrokeWidth(3);
            break;
            case R.id.colorGreenBtn:
                currentPaint = new Paint();
                currentPaint.setDither(true);
                currentPaint.setColor(0xFF0000FF);
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
            case R.id.saveBtn:
                final Activity currentActivity  = this;
                Handler saveHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        final AlertDialog alertDialog = new AlertDialog.Builder(currentActivity).create();
                        alertDialog.setTitle("Saved 1");
                        alertDialog.setMessage("Your drawing had been saved :)");
                        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                return;
                            }
                        });
                        alertDialog.show();
                    }
                } ;
               new ExportBitmapToFile(this,saveHandler, drawingSurface.getBitmap()).execute();
            break;
            case R.id.circleBtn:
                currentBrush = new CircleBrush();
            break;
            case R.id.pathBtn:
                currentBrush = new PenBrush();
            break;
        }
    }


    private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> {
        private Context mContext;
        private Handler mHandler;
        private Bitmap nBitmap;

        public ExportBitmapToFile(Context context,Handler handler,Bitmap bitmap) {
            mContext = context;
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
}