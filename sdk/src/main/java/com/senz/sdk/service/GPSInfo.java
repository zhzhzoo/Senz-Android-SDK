package com.senz.sdk.service;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import java.util.concurrent.TimeUnit;
import com.senz.sdk.utils.L;

public class GPSInfo {
	public static final String TAG = GPSInfo.class.getSimpleName();
	
	private LocationManager locationManager;
	private String provider;
	private GPSInfoListener GPSListener;
	
	public GPSInfo(Context ctx) {
        if (ctx == null) {
            L.d("yes");
        }
		locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);
		provider = selectProvider();
	}
	
	public void start(GPSInfoListener ltn) {
		GPSListener = ltn;
		notifyAbout(locationManager.getLastKnownLocation(provider));
        // update once per 1min and 200m
		locationManager.requestLocationUpdates(provider, TimeUnit.MINUTES.toMillis(1), 100, locationListener);
	}
	
	public void end() {
		locationManager.removeUpdates(locationListener);
	}
	
	private String selectProvider() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return locationManager.getBestProvider(criteria, true);
	}
	
	private final LocationListener locationListener = new LocationListener () {
		
		@Override
		public void onLocationChanged(Location location) {
			notifyAbout(location);
		}
		
		@Override
		public void onProviderDisabled(String provider) {
		}
		
		@Override
		public void onProviderEnabled(String provider) {
		}
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};
	
	private void notifyAbout(Location location) {
		GPSListener.onGPSInfoChanged(location);
	}

    public interface GPSInfoListener {
        public void onGPSInfoChanged(Location location);
    }
}
