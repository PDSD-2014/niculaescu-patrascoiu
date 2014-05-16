package com.example.listener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.SocketChannel;

import android.util.Log;

import com.example.drawings.DrawingActivity;

public class Peer {

	DrawingActivity whiteBoard;
	SocketChannel sock;
	
	public Peer(DrawingActivity whiteBoard, SocketChannel sock) {
		this.whiteBoard = whiteBoard;
		this.sock = sock;
	}

	public void drawFigure(SocketChannel channel) {
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		try {
			Log.d("Peer", "read " + channel.read(buffer));
			buffer.flip();
			float[] coords = new float[buffer.limit() / 4];
			buffer.asFloatBuffer().get(coords);
			whiteBoard.auxDraw(coords);
		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException e1) {
				Log.d("Peer", "Error", e1);
			}
			Log.d("Peer", "Error", e);
		} catch (Exception e) {
			Log.d("Peer", "Error", e);
		}
	}

	public void sendPath(ByteBuffer buffer) {
		try {
			Log.d("aux", "wrote " + sock.write(buffer));
		} catch (IOException e) {
			Log.e("Peer", "Error", e);
			try {
				sock.close();
			} catch (IOException e1) {
				Log.e("Peer", "Error", e1);
			}
		}
		
	}
}
