package com.example.drawings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.drawings.CommandManager;
import com.example.drawings.DrawingPath;

public class DrawingSurface extends SurfaceView implements SurfaceHolder.Callback {
    private Boolean _run;
    protected DrawThread thread;
    private Bitmap mBitmap;
    public boolean isDrawing = true;
    public DrawingPath previewPath;

    private CommandManager commandManager;

    public DrawingSurface(Context context, AttributeSet attrs) {
        super(context, attrs);

        getHolder().addCallback(this);


        commandManager = new CommandManager();
    }

    @SuppressLint("HandlerLeak")
	private Handler previewDoneHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            isDrawing = false;
        }
    };

    class DrawThread extends  Thread{
        private SurfaceHolder mSurfaceHolder;


        public DrawThread(SurfaceHolder surfaceHolder){
            mSurfaceHolder = surfaceHolder;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            try {
				Thread.sleep(250, 0);
			} catch (InterruptedException e) {}

            while (_run){
                if(isDrawing == true){
                    try{
                        if(mBitmap == null){
                            mBitmap =  Bitmap.createBitmap (1, 1, Bitmap.Config.ARGB_8888);
                            mBitmap.eraseColor(android.graphics.Color.GREEN);
                        }
                        final Canvas c = new Canvas (mBitmap);

                        canvas = mSurfaceHolder.lockCanvas(null);
                        
                        if (canvas == null)
                        	continue;
                        
                        c.drawColor(0xFFFFFFFF, PorterDuff.Mode.CLEAR);
                        canvas.drawColor(0xFFFFFFFF, PorterDuff.Mode.CLEAR);

                        commandManager.executeAll(c, previewDoneHandler);
                        previewPath.draw(c);
                        
                        canvas.drawBitmap(mBitmap, 0, 0, null);
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    } catch(Exception e) {}
                }

            }
        }
    }


    public void addDrawingPath (DrawingPath drawingPath){
        commandManager.addCommand(drawingPath);
    }

    public boolean hasMoreRedo(){
        return commandManager.hasMoreRedo();
    }

    public void redo(){
        isDrawing = true;
        commandManager.redo();
    }

    public void undo(){
        isDrawing = true;
        commandManager.undo();
    }

    public boolean hasMoreUndo(){
        return commandManager.hasMoreUndo();
    }

    public Bitmap getBitmap(){
        return mBitmap;
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width,  int height) {
        mBitmap =  Bitmap.createBitmap (width, height, Bitmap.Config.ARGB_8888);
        mBitmap.eraseColor(android.graphics.Color.GREEN);
    }


    public void surfaceCreated(SurfaceHolder holder) {
    	if (thread == null || !thread.isAlive()) {
    		thread = new DrawThread(getHolder());
    		thread.setRunning(true);
        	thread.start();
    	}
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {}
        }
    }
}

