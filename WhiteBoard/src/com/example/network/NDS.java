package com.example.network;

import java.net.InetAddress;

import android.annotation.SuppressLint;
import android.content.*;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.net.nsd.NsdManager.ResolveListener;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.RegistrationListener;
import android.util.Log;

@SuppressLint("NewApi")
public class NDS {

	protected static final String TAG = "NDS";
	protected static final String SERVICE_TYPE = "_http._tcp.";
	private final String SERVICE_NAME = "WhiteBoard";
	private RegistrationListener mRegistrationListener;
	private NsdManager mNsdManager;
	private String mServiceName;
	private DiscoveryListener mDiscoveryListener;
	private ResolveListener mResolveListener;
	private boolean registered = false;
	private static NDS service;
	
	private NDS() {}
	
	public static NDS getService() {
		if (service == null) {
			service = new NDS();
	        service.initializeDiscoveryListener();
	        service.initializeResolveListener();
	        service.initializeRegistrationListener();
		}
		return service;
	}
	
	public void initializeRegistrationListener() {
	    mRegistrationListener = new NsdManager.RegistrationListener() {

			@Override
	        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
	            // Save the service name.  Android may have changed it in order to
	            // resolve a conflict, so update the name you initially requested
	            // with the name Android actually used.
	            mServiceName = NsdServiceInfo.getServiceName();
	            Log.d(TAG, "Registered service with name: " + mServiceName);
	            discoverServices();
	        }

	        @Override
	        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
	            Log.w(TAG, "Service registration failed with code " + errorCode);
	        }

	        @Override
	        public void onServiceUnregistered(NsdServiceInfo arg0) {
	        	Log.d(TAG, "Unregistered service with name: " + mServiceName);
	        }

	        @Override
	        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
	        	Log.w(TAG, "Service unregistration failed with code " + errorCode);
	        }

	    };
	}
	
	public void registerService(Context context, int port) {
	    NsdServiceInfo serviceInfo  = new NsdServiceInfo();
	    serviceInfo.setServiceName(SERVICE_NAME);
	    serviceInfo.setServiceType(SERVICE_TYPE);
	    serviceInfo.setPort(port);
	   
	    mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

	    mNsdManager.registerService(
	            serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
	    
	    registered = true;
	}
	
	public void discoverServices() {
	    mNsdManager.discoverServices(
	            SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
	}
	
    public void unregister() {
    	if (registered) {
	    	mNsdManager.unregisterService(mRegistrationListener);
	    	registered = false;
    	}
    }
	
	public void initializeResolveListener() {
	    mResolveListener = new NsdManager.ResolveListener() {

	        private NsdServiceInfo mService;

			@Override
	        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
	            Log.d(TAG, "Resolve failed " + errorCode);
	        }

	        @Override
	        public void onServiceResolved(NsdServiceInfo serviceInfo) {

	            if (serviceInfo.getServiceName().equals(mServiceName)) {
	                Log.d(TAG, "Same IP.");
	                return;
	            }
	            
	            mService = serviceInfo;
	            int port = mService.getPort();
	            InetAddress host = mService.getHost();
	            Log.d(TAG, "Have " + host + ":" + port);
	            Listener.getListener().newConnection(host.getHostAddress(), port);
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
	                // connecting to.
	                Log.d(TAG, "Same machine: " + mServiceName);
	            } else if (service.getServiceName().contains(SERVICE_NAME)){
	                mNsdManager.resolveService(service, mResolveListener);
	            }
	        }

	        @Override
	        public void onServiceLost(NsdServiceInfo service) {
	            // When the network service is no longer available.
	            // Internal bookkeeping code goes here.
	            Log.d(TAG, "service lost: " + service);
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
