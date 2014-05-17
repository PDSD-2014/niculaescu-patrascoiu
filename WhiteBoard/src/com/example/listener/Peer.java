package com.example.listener;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import android.util.Log;

public class Peer {

	Listener listener;
	SocketChannel sock;
	
	public Peer(Listener list, SocketChannel sock) {
		this.listener = list;
		this.sock = sock;
	}

	public void drawFigure(SocketChannel channel) {
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		try {
			int bytesRead = channel.read(buffer);
			Log.d("Peer", "read " + bytesRead);
			
			/* Close socket and remove peer in case it was closed on other end. */
			if (bytesRead <= 0) {
				channel.close();
				listener.removePeer(this);
				return;
			}

			buffer.flip();
			float[] coords = new float[buffer.limit() / 4];
			buffer.asFloatBuffer().get(coords);
			listener.getWhiteBoard().auxDraw(coords);
		} catch (IOException e) {
			try {
				channel.close();
				listener.removePeer(this);
			} catch (IOException e1) {
				Log.d("Peer", "Error", e1);
			}
			Log.e("Peer", "Error", e);
		}
	}

	/** 
	 * Send the drawing to the other peer.
	 */
	public void sendPath(ByteBuffer buffer) {
		try {
			buffer.flip();
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
