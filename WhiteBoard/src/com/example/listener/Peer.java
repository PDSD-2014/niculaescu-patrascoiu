package com.example.listener;

import java.io.IOException;
import java.nio.ByteBuffer;
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
			channel.read(buffer);
			whiteBoard.auxDraw(buffer.asFloatBuffer().array());
		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public void sendPath(ByteBuffer buffer) {
		try {
			sock.write(buffer);
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
