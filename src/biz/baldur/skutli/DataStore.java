package biz.baldur.skutli;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.os.Handler;
import biz.baldur.skutli.R;
import biz.baldur.skutli.model.BusStop;
import biz.baldur.skutli.model.Direction;
import biz.baldur.skutli.model.Route;
import biz.baldur.skutli.model.Schedule;

public class DataStore implements Serializable {
	private static final long serialVersionUID = 2450934055814595508L;
	
	private Map<String, Route> routeMap;
	private List<Route> routeList;
	private boolean populated;
	
	public DataStore() {
		this.routeMap = new HashMap<String, Route>();
		this.routeList = new ArrayList<Route>();
		this.populated = false;
	}
	
	public boolean isPopulated() {
		return populated;
	}
	
	private String getHttpData(String uri) {
	    HttpClient client = new DefaultHttpClient();
	    HttpGet request = new HttpGet();
        try {
			request.setURI(new URI(uri));
		} catch (URISyntaxException e) {
			return null;
		}
        try {
			return client.execute(request, new BasicResponseHandler());
		} catch (ClientProtocolException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	public void populate(String baseUri, Handler callback, Resources resources) {
		this.routeMap.clear();
		this.routeList.clear();
		this.populated = false;
		try {
			String overviewJSON = getHttpData(baseUri + "routes");
			if(overviewJSON == null)
				return;
			
			JSONObject rows = new JSONObject(overviewJSON);
			JSONArray routesObjects = rows.getJSONArray("rows");
			JSONObject routeObject;
			String routeId;
			String routeName;
			Route route;
			
			callback.sendMessage(callback.obtainMessage(SkutliActivity.PROGRESS_MAX, routesObjects.length(), 0));
			for(int i = 0; i < routesObjects.length(); i++) {
				routeObject = routesObjects.getJSONObject(i);
				routeId = routeObject.getString("id");
				routeName = routeObject.getJSONObject("value").getString("name");
				route = getRouteInfo(baseUri, routeId, resources);
				routeMap.put(routeName, route);
				routeList.add(route);
				callback.sendMessage(callback.obtainMessage(SkutliActivity.PROGRESS_UPDATE, routeName));
			}
		} catch (JSONException e) {
		}
		this.populated = true;
	}
	
	private Route getRouteInfo(String baseUri, String id, Resources resources) throws JSONException {
		Route route;
		
		String jData = getHttpData(baseUri + id);
		JSONObject jRoute = new JSONObject(jData);
		String name = jRoute.getString("name");
		String sColor = jRoute.getString("color");
		
		int color = resources.getColor(R.color.white);
		if(sColor.equals("red"))
			color = resources.getColor(R.color.bus_red);
		else if(sColor.equals("green"))
			color = resources.getColor(R.color.bus_green);
		else if(sColor.equals("blue"))
			color = resources.getColor(R.color.bus_blue);
		
		route = new Route(name, color);
		
		JSONArray jDirections = jRoute.getJSONArray("directions");
		JSONObject jDirection;
		JSONArray jTimetables;
		JSONObject jTimetable;
		JSONArray jDays;
		JSONArray jStops;
		JSONObject jStop;
		JSONArray jTimes;
		String sTime;
		int iTime;
		Direction direction;
		String day;
		String stopName;
		BusStop busStop;
		Map<String, BusStop> busStops;
		Schedule schedule;
		for(int i = 0; i < jDirections.length(); i++) {
			direction = new Direction();
			
			jDirection = jDirections.getJSONObject(i);
			jTimetables = jDirection.getJSONArray("timetable");
			
			busStops = new HashMap<String, BusStop>();
			
			for(int j = 0; j < jTimetables.length(); j++) {
				jTimetable = jTimetables.getJSONObject(j);
				jDays = jTimetable.getJSONArray("days");
				jStops = jTimetable.getJSONArray("stops");
				for(int k=0; k < jStops.length(); k++) {
					jStop = jStops.getJSONObject(k);
					stopName = jStop.getString("stop");
					busStop = busStops.get(stopName);
					if(busStop == null) {
						busStop = new BusStop(stopName);
						busStops.put(stopName, busStop);
						direction.addBusStop(busStop);
					}
					jTimes = jStop.getJSONArray("times");
					schedule = new Schedule();
					for(int l = 0; l < jTimes.length(); l++) {
						sTime = jTimes.getString(l);
						if(sTime.contains("..."))
							continue;
						iTime = timeToInt(sTime);
						schedule.addArrival(iTime);
					}
					for(int l = 0; l < jDays.length(); l++) {
						int weekday;
						day = jDays.getString(l);
						if(day.equals("sun"))
							weekday = Calendar.SUNDAY;
						else if(day.equals("mon"))
							weekday = Calendar.MONDAY;
						else if(day.equals("tue"))
							weekday = Calendar.TUESDAY;
						else if(day.equals("wed"))
							weekday = Calendar.WEDNESDAY;
						else if(day.equals("thu"))
							weekday = Calendar.THURSDAY;
						else if(day.equals("fri"))
							weekday = Calendar.FRIDAY;
						else if(day.equals("sat"))
							weekday = Calendar.SATURDAY;
						else
							continue;
						busStop.putSchedule(weekday, schedule);
					}
				}
			}
			route.addDirection(direction);
		}
		
		return route;
	}
	
	public int timeToInt(String sTime) {
		int time = 0;
		time += Integer.valueOf(sTime.substring(0, 2)) * 60;
		time += Integer.valueOf(sTime.substring(3, 5));
		return time;
	}
	
	public Route[] getRoutes() {
		return routeList.toArray(new Route[routeList.size()]);
	}
	
	public Route getRoute(String sRoute) {
		return routeMap.get(sRoute);
	}
}
