package com.example.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.drawings.DrawingActivity;

/** 
 * Class that implements the listening part of the network component
 */
public class Listener implements Runnable {

	private final static String TAG = "Listener";
	private Selector selector;
	private ServerSocketChannel listener;
	private ExecutorService pool;
	private ReentrantLock selectorLock;
	
	private static Listener instance;
	
	private DrawingActivity whiteBoard;
	private ArrayList<Peer> peers = new ArrayList<Peer>();
	
	private Listener() throws IOException {
		selector = Selector.open();
		selectorLock = new ReentrantLock();
		
		/* Open listener and register it with the selector. Listen on all interfaces. */
		listener = ServerSocketChannel.open();

		listener.socket().bind(new InetSocketAddress("0.0.0.0", 0));
		listener.configureBlocking(false);
		listener.register(selector, SelectionKey.OP_ACCEPT, null);
		
		pool = Executors.newFixedThreadPool(2);
		pool.submit(this);
	}
	
	public static Listener getListener() {
		if (instance == null) {
			try {
				instance = new Listener();
			} catch (Exception e) {
				Log.d(TAG, "Error", e);
			}
		}
		return instance;
	}
	
	public int getPort() {
		return getListener().listener.socket().getLocalPort();
	}
	
	public void setDrawingActivity(DrawingActivity whiteBoard) {
		this.whiteBoard = whiteBoard;
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				/* Ensure the selector is not being modified */
				selectorLock.lock();
				selectorLock.unlock();

				selector.select();
				/* Iterate over the events */
				for (Iterator<SelectionKey> it = selector.selectedKeys().iterator(); it.hasNext(); ) {
					// get current event and REMOVE it from the list!!!
					final SelectionKey key = it.next();
					it.remove();
					if (key.isAcceptable()) {
						/* Create a new connection for sending/receiving information */
						ServerSocketChannel listener= (ServerSocketChannel)key.channel();
						SocketChannel newConn = listener.accept();
						newConn.configureBlocking(false);
						Peer p = new Peer(this, newConn);
						peers.add(p);
						Log.d(TAG, "New Connection");
						newConn.register(selector, SelectionKey.OP_READ, p);
						selector.wakeup();
					} else 	if (key.isReadable() || key.isWritable()) {
						/* Data has arrived */
						key.channel().register(selector, 0, key.attachment());
						pool.submit(new Runnable() {
							@Override
							public void run() {
								Peer peer = (Peer) key.attachment();
								peer.executeCommand((SocketChannel) key.channel());
								try {
									if (key.channel().isOpen()) {
										key.channel().register(selector, SelectionKey.OP_READ, peer);
										selector.wakeup();
									}
								} catch (ClosedChannelException e) {
									Log.e(TAG, "", e);
								}
							}
						});
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "Error", e);
			}
		}
	}
	
	public Selector getSelector() {
		return selector;
	}

	/* Register a socket for selection, with the corresponding Transfer object */
	public void registerSocket(SocketChannel socket, Peer peer) {
		try {
			selectorLock.lock();
			selector.wakeup();
			socket.register(selector, SelectionKey.OP_READ, peer);
		} catch (ClosedChannelException e) {
			Log.e(TAG, "Error", e);
			try {
				socket.close();
			} catch (IOException e1) {
				Log.e(TAG, "Error", e1);
			}
		} finally {
			selectorLock.unlock();
		}
	}

	public void sendCommand(ByteBuffer buffer) {
		new SendCommand().execute(peers, buffer);
	}

	public void newConnection(String address, int port) {
		new NewConnection().execute(this, address, port, peers);
	}

	public DrawingActivity getWhiteBoard() {
		return whiteBoard;
	}

	public void removePeer(Peer peer) {
		peers.remove(peer);
	}
}

class NewConnection extends AsyncTask<Object, Listener, Void> {
    
	protected Void doInBackground(Object ... params) {
		try {
			Listener listener = (Listener) params[0];
			SocketChannel socket = SocketChannel.open();
			ArrayList<Peer> peers = (ArrayList<Peer>) params[3];
			socket.connect(new InetSocketAddress((String)params[1], (Integer)params[2]));
			Peer peer = new Peer(listener, socket);
			socket.configureBlocking(false);
			listener.registerSocket(socket, peer);
			peers.add(peer);
		} catch(Exception e) {
			Log.e("NewConn", "Error", e);
		}
		return null;
    }
}

class SendCommand extends AsyncTask<Object, Void, Void> {

	@SuppressWarnings("unchecked")
	@Override
	protected Void doInBackground(Object... params) {
		ArrayList<Peer> peers = (ArrayList<Peer>) params[0];
		ByteBuffer buffer = (ByteBuffer) params[1];
		for(Peer p : peers) {
			p.sendCommand(buffer);
		}
		return null;
	}
	
}

