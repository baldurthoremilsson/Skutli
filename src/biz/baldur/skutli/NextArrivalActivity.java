package biz.baldur.skutli;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import biz.baldur.skutli.R;
import biz.baldur.skutli.model.BusStop;
import biz.baldur.skutli.model.Direction;
import biz.baldur.skutli.model.Route;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
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
	private Map<Integer, Pair<BusStop, View>> listCache;
	private int routeColor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.next_arrival);

		listCache = new HashMap<Integer, Pair<BusStop, View>>();
		
		String routeID = getIntent().getExtras().getString("route");
		Route route = dataStore.getRoute(routeID);
		
		routeColor = route.getColor();

		TextView tvRouteName = (TextView) findViewById(R.id.routeName);
		tvRouteName.setText(route.getName());
		tvRouteName.setBackgroundColor(route.getColor());
		
		ListView lvStops = (ListView) findViewById(R.id.stopList);
		
		arrayAdapter = new ArrayAdapter<BusStop>(this, R.layout.next_arrival_item) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return listCache.get(position).second;
			}
		};
		
		lvStops.setAdapter(arrayAdapter);
		
		int iDirection = 0;
		int position = 0;
		for(Direction direction: route.getDirections()) {
			if(iDirection != 0)
				addStop(new BusStop(""), position++);
			for(BusStop stop: direction.getBusStops()) {
				addStop(stop, position++);
			}
			iDirection++;
		}
		
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				Calendar currentTime = Calendar.getInstance();
				Calendar nextTime = Calendar.getInstance();
				int nextTimeOffset;
				int firstTime;
				int secondTime;
				Set<Integer> keys = listCache.keySet();
				Pair<BusStop, View> pair;
				BusStop stop;
				LinearLayout l;
				TextView tv;
				
				for(Integer key: keys) {
					pair = listCache.get(key);
					stop = pair.first;
					if(stop.getName().equals(""))
						continue;
					
					l = (LinearLayout) pair.second;
					
					firstTime = stop.getNextArrival(currentTime);
					nextTimeOffset = firstTime + 1;
					
					nextTime.add(Calendar.MINUTE, nextTimeOffset);
					secondTime = stop.getNextArrival(nextTime) + nextTimeOffset;
					nextTime.add(Calendar.MINUTE, -nextTimeOffset);
					
					tv = (TextView) l.findViewById(R.id.firstTime);
					tv.setText(formatTime(firstTime, currentTime));
					tv.invalidate();
					
					tv = (TextView) l.findViewById(R.id.secondTime);
					tv.setText(formatTime(secondTime, currentTime));
					tv.invalidate();
				}
			}
		};
		
		Thread updateTime = new Thread(new Runnable() {
			@Override
			public void run() {
				Calendar currentTime;
				while(true) {
					handler.sendEmptyMessage(0);
					currentTime = Calendar.getInstance();
					try {
						Thread.sleep((60 - currentTime.get(Calendar.SECOND)) * 1000 + 10);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
		updateTime.start();
	}
	
	private void addStop(BusStop stop, int position) {
		View view;
		if(stop.getName().equals("")) {
			view = getLayoutInflater().inflate(R.layout.next_arrival_separator, null); 
			view.setBackgroundColor(routeColor);
		}
		else {
			LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.next_arrival_item, null);
			TextView tv = (TextView) l.findViewById(R.id.stopName);
			tv.setText(stop.getName());
			
			view = l;
		}
		listCache.put(position, new Pair<BusStop, View>(stop, view));
		arrayAdapter.add(stop);
	}
	
	private Map<Integer, String> getWeekdays() {
		if(weekdays.size() == 0) {
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
