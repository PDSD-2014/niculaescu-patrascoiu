package com.example.listener;

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

import android.util.Log;

import com.example.drawings.DrawingActivity;

/** 
 * Class that implements the listening part of the network component
 */
public class Listener implements Runnable {

	private final String TAG = "Listener";
	private Selector selector;
	private ServerSocketChannel listener;
	private ExecutorService pool;
	private ReentrantLock selectorLock;
	
	private DrawingActivity whiteBoard;
	private ArrayList<Peer> peers = new ArrayList<Peer>();
	
	public Listener(DrawingActivity whiteBoard) throws IOException {
		this.whiteBoard = whiteBoard;
		selector = Selector.open();
		selectorLock = new ReentrantLock();
		
		/* Open listener and register it with the selector*/
		listener = ServerSocketChannel.open();
		listener.socket().bind(new InetSocketAddress("127.0.0.1", 30000));
		listener.configureBlocking(false);
		listener.register(selector, SelectionKey.OP_ACCEPT, null);
		
		pool = Executors.newFixedThreadPool(2);
		pool.submit(this);
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
						Peer p = new Peer(whiteBoard, newConn);
						peers.add(p);
						newConn.register(selector, SelectionKey.OP_READ, p);
						selector.wakeup();
					} else 	if (key.isReadable() || key.isWritable()) {
						/* Data has arrived */
						key.channel().register(selector, 0, key.attachment());
						pool.submit(new Runnable() {
							@Override
							public void run() {
								Peer peer = (Peer) key.attachment();
								peer.drawFigure((SocketChannel) key.channel());
								try {
									if (key.channel().isOpen()) {
										key.channel().register(selector, SelectionKey.OP_READ, peer);
										selector.wakeup();
									}
								} catch (ClosedChannelException e) {
									e.printStackTrace();
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

	public void sendPath(ByteBuffer buffer) {
		
		for(Peer p : peers) {
			p.sendPath(buffer);
		}
		
	}

	public void newConnection(String address, int port) {
		try {
			SocketChannel socket = SocketChannel.open();
			socket.connect(new InetSocketAddress(address, port));
			Peer peer = new Peer(whiteBoard, socket);
			socket.configureBlocking(false);
			registerSocket(socket, peer);
		} catch(Exception e) {
			Log.e(TAG, "Error", e);
		}
		
	}
}
