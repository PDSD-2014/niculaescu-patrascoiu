package com.example.listener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import android.content.*;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.util.Log;

public class NDS {

	protected static final String TAG = "Listener";
	protected static final Object SERVICE_TYPE = "";
	private ServerSocket mServerSocket;
	private int mLocalPort;
	private RegistrationListener mRegistrationListener;
	private NsdManager mNsdManager;
	private String mServiceName;
	private DiscoveryListener mDiscoveryListener;
	private ResolveListener mResolveListener;

	public void initializeServerSocket() throws IOException {
	    // Initialize a server socket on the next available port.
	    mServerSocket = new ServerSocket(0);

	    // Store the chosen port.
	    mLocalPort =  mServerSocket.getLocalPort();
	}
	
	public void initializeRegistrationListener() {
	    mRegistrationListener = new NsdManager.RegistrationListener() {

			@Override
	        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
	            // Save the service name.  Android may have changed it in order to
	            // resolve a conflict, so update the name you initially requested
	            // with the name Android actually used.
	            mServiceName = NsdServiceInfo.getServiceName();
	        }

	        @Override
	        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
	            // Registration failed!  Put debugging code here to determine why.
	        }

	        @Override
	        public void onServiceUnregistered(NsdServiceInfo arg0) {
	            // Service has been unregistered.  This only happens when you call
	            // NsdManager.unregisterService() and pass in this listener.
	        }

	        @Override
	        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
	            // Unregistration failed.  Put debugging code here to determine why.
	        }

	    };
	}
	
	public void registerService(Context context, int port) {
	    NsdServiceInfo serviceInfo  = new NsdServiceInfo();
	    serviceInfo.setServiceName("NsdChat");
	    serviceInfo.setServiceType("_http._tcp.");
	    serviceInfo.setPort(port);
	   
	    mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

	    mNsdManager.registerService(
	            serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
	}
	
	public void initializeResolveListener() {
	    mResolveListener = new NsdManager.ResolveListener() {

	        private NsdServiceInfo mService;

			@Override
	        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
	            // Called when the resolve fails.  Use the error code to debug.
	            Log.e(TAG, "Resolve failed" + errorCode);
	        }

	        @Override
	        public void onServiceResolved(NsdServiceInfo serviceInfo) {
	            Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

	            if (serviceInfo.getServiceName().equals(mServiceName)) {
	                Log.d(TAG, "Same IP.");
	                return;
	            }
	            mService = serviceInfo;
	            int port = mService.getPort();
	            InetAddress host = mService.getHost();
	            Log.d(TAG, "Have " + host + ":" + port);
	        }
	    };
	}
	
	public void initializeDiscoveryListener() {

	    // Instantiate a new DiscoveryListener
	    mDiscoveryListener = new NsdManager.DiscoveryListener() {

	        //  Called as soon as service discovery begins.
	        @Override
	        public void onDiscoveryStarted(String regType) {
	            Log.d(TAG, "Service discovery started");
	        }

	        @Override
	        public void onServiceFound(NsdServiceInfo service) {
	            // A service was found!  Do something with it.
	            Log.d(TAG, "Service discovery success" + service);
	            if (!service.getServiceType().equals(SERVICE_TYPE)) {
	                // Service type is the string containing the protocol and
	                // transport layer for this service.
	                Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
	            } else if (service.getServiceName().equals(mServiceName)) {
	                // The name of the service tells the user what they'd be
	                // connecting to. It could be "Bob's Chat App".
	                Log.d(TAG, "Same machine: " + mServiceName);
	            } else if (service.getServiceName().contains("NsdChat")){
	                mNsdManager.resolveService(service, mResolveListener);
	            }
	        }

	        @Override
	        public void onServiceLost(NsdServiceInfo service) {
	            // When the network service is no longer available.
	            // Internal bookkeeping code goes here.
	            Log.e(TAG, "service lost" + service);
	        }

	        @Override
	        public void onDiscoveryStopped(String serviceType) {
	            Log.i(TAG, "Discovery stopped: " + serviceType);
	        }

	        @Override
	        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
	            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
	            mNsdManager.stopServiceDiscovery(this);
	        }

	        @Override
	        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
	            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
	            mNsdManager.stopServiceDiscovery(this);
	        }
	    };
	}
}
