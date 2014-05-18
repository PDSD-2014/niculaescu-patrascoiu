package com.example.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import android.util.Log;
import android.widget.Toast;

public class Peer {

	Listener listener;
	SocketChannel sock;
	
	public Peer(Listener list, SocketChannel sock) {
		this.listener = list;
		this.sock = sock;
	}

	public void executeCommand(SocketChannel channel) {
		ByteBuffer buffer = ByteBuffer.allocate(4096);
		try {
			int bytesRead = channel.read(buffer);
			Log.d("Peer", "read " + bytesRead);
			
			/* Close socket and remove peer in case it was closed on other end. */
			if (bytesRead <= 0) {
				channel.close();
				listener.removePeer(this);
				listener.getWhiteBoard().displayMessage("Peer Disconnected");
				return;
			}

			buffer.flip();
			float[] data = new float[buffer.limit() / 4];
			buffer.asFloatBuffer().get(data);
			listener.getWhiteBoard().processRemoteCommand(data);
		} catch (IOException e) {
			try {
				channel.close();
				listener.removePeer(this);
				listener.getWhiteBoard().displayMessage("Peer Disconnected");
			} catch (IOException e1) {
				Log.d("Peer", "Error", e1);
			}
			Log.e("Peer", "Error", e);
		} catch (Exception e) {
			Log.e("Peer", "Error", e);
		}
	}

	/** 
	 * Send the drawing to the other peer.
	 */
	public void sendCommand(ByteBuffer buffer) {
		try {
			buffer.flip();
			int bytesWrote = sock.write(buffer);
			Log.d("aux", "wrote " + bytesWrote);
		} catch (IOException e) {
			Log.e("Peer", "Error", e);
			try {
				listener.removePeer(this);
				sock.close();
			} catch (IOException e1) {
				Log.e("Peer", "Error", e1);
			}
		}
		
	}
}
