package com.dayday.map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Dialog;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class MainTab extends FragmentActivity implements LocationListener, OnCheckedChangeListener{
	
	//Define some view related variables
	private ViewPager viewPager;
    private List<View> userResgiterviews;
    private View mainView, listView, aboutusView, blankView;
    
    private Location location;
    private GoogleMap mGoogleMap;
    private Spinner mSprPlaceType;
    
    String[] mPlaceType=null;
    String[] mPlaceTypeName=null;
 
    double mLatitude=0;
    double mLongitude=0;
    
    private ListView locationListView;
    ArrayList<String[]> locationResults = new ArrayList<String[]>();
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.maintab);
        InitViewPager();
    }

    /** Initialize the view pager layout */
    private void InitViewPager() {  
    	
        viewPager = (ViewPager) findViewById(R.id.main_pager);  
        userResgiterviews = new ArrayList<View>();
        LayoutInflater inflater = getLayoutInflater();  
        
        mainView = inflater.inflate(R.layout.main, null);  
        listView = inflater.inflate(R.layout.listview, null);
        aboutusView = inflater.inflate(R.layout.aboutus, null);
        blankView = inflater.inflate(R.layout.blank, null);
        
        initRadioButton();
        
        initMainPage();
        initListPage();
        
        userResgiterviews.add(mainView);
        userResgiterviews.add(blankView);
        userResgiterviews.add(listView);
        userResgiterviews.add(aboutusView);
        
        viewPager.setAdapter(new MyViewPagerAdapter(userResgiterviews));
		//viewPager.dispatchTouchEvent(ev);
        viewPager.getParent().requestDisallowInterceptTouchEvent(true);
        viewPager.setCurrentItem(0);
        
        
        viewPager.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				return true;
			}
		});
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
        
    }
    
    private void initListPage() {
		// TODO Auto-generated method stub
    	locationListView = (ListView) listView.findViewById(R.id.location_listview);
    	
    	//locationListView = (ListView) aboutusView.findViewById(R.id.location_listview);
	}

	private void initMainPage() {
		// TODO Auto-generated method stub
    	// Array of place types, including pharmacy shop, hospital
        mPlaceType = getResources().getStringArray(R.array.place_type);
 
        // Array of place type names
        mPlaceTypeName = getResources().getStringArray(R.array.place_type_name);
 
        // Creating an array adapter with an array of Place types
        // to populate the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, mPlaceTypeName);
 
        // Getting reference to the Spinner
        mSprPlaceType = (Spinner) mainView.findViewById(R.id.spr_place_type);
 
        // Setting adapter on Spinner to set place types
        mSprPlaceType.setAdapter(adapter);
 
        Button btnFind;
 
        // Getting reference to Find Button
        btnFind = (Button) mainView.findViewById(R.id.btn_find);
        
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
 
        if(status != ConnectionResult.SUCCESS){ // Google Play Services are not available
 
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
 
        } else { // Google Play Services are available
 
            // Getting reference to the SupportMapFragment
            SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 
            // Getting Google Map
            mGoogleMap = fragment.getMap();
            
            // Enabling MyLocation in Google Map
            mGoogleMap.setMyLocationEnabled(true);
            
            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 
            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();
 
            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);
 
            // Getting Current Location From GPS
            location = locationManager.getLastKnownLocation(provider);
 
            if(location != null){
                onLocationChanged(location);
            }
 
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
 
            // Setting click event lister for the find button
            btnFind.setOnClickListener(new OnClickListener() {
 
                @Override
                public void onClick(View v) {
 
                    int selectedPosition = mSprPlaceType.getSelectedItemPosition();
                    String type = mPlaceType[selectedPosition];
                    
                    Log.i("Type", "Place:" + type);
                    
                    //The url we are requesting google map server
                    StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
                    sb.append("location="+mLatitude+","+mLongitude);
                    sb.append("&radius=5000");
                    sb.append("&types="+type);
                    sb.append("&sensor=true");
                    sb.append("&key=AIzaSyCosFJRKlRVjqisJf0zn32eB7IXJHYw3Gc");
                    
                    Log.i("Type", sb.toString());
                    
                    // Creating a new non-ui thread task to download json data
                    PlacesTask placesTask = new PlacesTask();
 
                    // Invokes the "doInBackground()" method of the class PlaceTask
                    placesTask.execute(sb.toString());
 
                }
            });
            
        }
	}

	private void initRadioButton() {
		// TODO Auto-generated method stub
    	((RadioButton) findViewById(R.id.radio_button0))
		.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button1))
		.setOnCheckedChangeListener(this);
        ((RadioButton) findViewById(R.id.radio_button2))
		.setOnCheckedChangeListener(this);
       
	}
    
    public class MyOnPageChangeListener implements OnPageChangeListener{  
    	
        public void onPageScrollStateChanged(int arg0) {  
              
              
        }  
  
        public void onPageScrolled(int arg0, float arg1, int arg2) {  
              
              
        }  
  
        public void onPageSelected(int arg0) {  
            
            
            
        }  
          
    }
    
    public class MyViewPagerAdapter extends PagerAdapter{  
        private List<View> mListViews;  
          
        public MyViewPagerAdapter(List<View> mListViews) {  
            this.mListViews = mListViews;  
        }  
  
        @Override  
        public void destroyItem(ViewGroup container, int position, Object object)   {     
            container.removeView(mListViews.get(position));  
        }  
        
        
        
        @Override  
        public Object instantiateItem(ViewGroup container, int position) {            
             container.addView(mListViews.get(position), 0);
             
             
             return mListViews.get(position);  
        }  
  
        @Override  
        public int getCount() {           
            return  mListViews.size();  
        }  
        
        
          
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {             
            return arg0 == arg1;  
        }  
    }
    
    /** Select the page you select */
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			switch (buttonView.getId()) {
			case R.id.radio_button0:
				viewPager.setCurrentItem(0);
				break;
			case R.id.radio_button1:
				viewPager.setCurrentItem(2);
				break;
			case R.id.radio_button2:
				viewPager.setCurrentItem(3);
				break;
			
			}
		}
		
	}
	
	/** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException{
    	
    	Log.i("downloadUrl", strUrl);
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
 
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
 
            // Connecting to url
            urlConnection.connect();
 
            // Reading data from url
            iStream = urlConnection.getInputStream();
 
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
 
            StringBuffer sb  = new StringBuffer();
 
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
 
            data = sb.toString();
 
            br.close();
 
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
 
        return data;
    }
    
    /** A class, to download Google Places */
    private class PlacesTask extends AsyncTask<String, Integer, String>{
 
        String data = null;
 
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try{
            	//Log.i("PlaceTask", url[0]);
                data = downloadUrl(url[0]);
                //Log.i("PlaceTask", data);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
 
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result){
            ParserTask parserTask = new ParserTask();
 
            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParseTask
            parserTask.execute(result);
        }
 
    }
    
    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>>{
 
        JSONObject jObject;
 
        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String,String>> doInBackground(String... jsonData) {
 
            List<HashMap<String, String>> places = null;
            PlaceJSONParser placeJsonParser = new PlaceJSONParser();
 
            try{
                jObject = new JSONObject(jsonData[0]);
                
                //Log.i("ParserTask", jObject.toString());
                /** Getting the parsed data as a List construct */
                places = placeJsonParser.parse(jObject);
                //Log.i("ParserTask_Places", places.toString());
                
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return places;
        }
 
        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String,String>> list){
 
            // Clears all the existing markers
            mGoogleMap.clear();
            
            List<String> locationData = new ArrayList<String>();
 
            for(int i = 0; i < list.size(); i++){
 
                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();
 
                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);
 
                // Getting latitude of the place
                double lat = Double.parseDouble(hmPlace.get("lat"));
 
                // Getting longitude of the place
                double lng = Double.parseDouble(hmPlace.get("lng"));
 
                // Getting name
                String name = hmPlace.get("place_name");
 
                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");
 
                LatLng latLng = new LatLng(lat, lng);
 
                // Setting the position for the marker
                markerOptions.position(latLng);
 
                // Setting the title for the marker.
                //This will be displayed on taping the marker
                markerOptions.title(name + " : " + vicinity);
                
                markerOptions.describeContents();
                
                // Placing a marker on the touched position
                mGoogleMap.addMarker(markerOptions.draggable(true).anchor(0.5f, 0.5f));
                
                //Calculate the distance between two points
                Location locationA = new Location("point A");  
                
                locationA.setLatitude(mLatitude);  
                locationA.setLongitude(mLongitude);  

                Location locationB = new Location("point B");  

                locationB.setLatitude(lat);  
                locationB.setLongitude(lng);  

                float distance = locationA.distanceTo(locationB);
                
                locationData.add((i + 1) + ". " + name + "\nLocation : " + vicinity 
                		+ "\nDistance: " + (distance / 1000) + " km");
				
            }
            
            //Add the adapter to the listview
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
    				getApplicationContext(),
    				android.R.layout.simple_list_item_1, locationData);
            
			locationListView.setAdapter(arrayAdapter);
			
        }
    }
    

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
        LatLng latLng = new LatLng(mLatitude, mLongitude);
 
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(13));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	

}