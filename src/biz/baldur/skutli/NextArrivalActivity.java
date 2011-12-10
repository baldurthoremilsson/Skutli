package biz.baldur.skutli;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import biz.baldur.skutli.R;
import biz.baldur.skutli.model.BusStop;
import biz.baldur.skutli.model.Direction;
import biz.baldur.skutli.model.Route;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class NextArrivalActivity extends Activity {
	public static DataStore dataStore;
	
	private static final Map<Integer, String> weekdays = new HashMap<Integer, String>();
	private ArrayAdapter<BusStop> arrayAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.next_arrival);

		String routeID = getIntent().getExtras().getString("route");
		Route route = dataStore.getRoute(routeID);
		
		final int routeColor = route.getColor();

		TextView tvRouteName = (TextView) findViewById(R.id.routeName);
		tvRouteName.setText(route.getName());
		tvRouteName.setBackgroundColor(route.getColor());
		
		ListView lvStops = (ListView) findViewById(R.id.stopList);
		final Calendar currentTime = Calendar.getInstance();
		
		arrayAdapter = new ArrayAdapter<BusStop>(this, R.layout.next_arrival_item) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				BusStop stop = getItem(position);
				if(stop.getName().equals("")) {
					View view = getLayoutInflater().inflate(R.layout.next_arrival_separator, null); 
					view.setBackgroundColor(routeColor);
					return view;
				}
				
				LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.next_arrival_item, null);
				TextView tv;

				tv = (TextView) l.findViewById(R.id.stopName);
				tv.setText(stop.getName());
				
				tv = (TextView) l.findViewById(R.id.time);
				tv.setText(formatTime(stop.getNextArrival(currentTime), currentTime));
				
				return l;
			}
		};
		
		lvStops.setAdapter(arrayAdapter);
		
		int iDirection = 0;
		for(Direction direction: route.getDirections()) {
			if(iDirection != 0)
				arrayAdapter.add(new BusStop(""));
			for(BusStop stop: direction.getBusStops()) {
				arrayAdapter.add(stop);
			}
			iDirection++;
		}
	}
	
	private Map<Integer, String> getWeekdays() {
		if(weekdays.size() == 0) {
			Log.v("BUSME", "adding weekdays");
			weekdays.put(Calendar.SUNDAY, "sun");
			weekdays.put(Calendar.MONDAY, "mán");
			weekdays.put(Calendar.TUESDAY, "þri");
			weekdays.put(Calendar.WEDNESDAY, "mið");
			weekdays.put(Calendar.THURSDAY, "fim");
			weekdays.put(Calendar.FRIDAY, "fös");
			weekdays.put(Calendar.SATURDAY, "lau");
		}
		return weekdays;
	}
	
	private String formatTime(int delta, Calendar currentTime) {
		if(delta < 60) {
			return delta + "mín";
		}

		Calendar newTime = (Calendar) currentTime.clone();
		newTime.add(Calendar.MINUTE, delta);
		int hour = newTime.get(Calendar.HOUR_OF_DAY);
		int minute = newTime.get(Calendar.MINUTE);
		String value;
		
		if(minute < 10)
			value =  hour + ":0" + minute;
		else
			value = hour + ":" + minute;
		
		if(delta < 60 * 24) {
			return value;
		}
		
		return value + " " + getWeekdays().get(newTime.get(Calendar.DAY_OF_WEEK));
	}
}
