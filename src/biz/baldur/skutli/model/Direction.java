package biz.baldur.skutli.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Direction implements Serializable {
	private static final long serialVersionUID = 6388947285227916217L;

	List<BusStop> busStops;
	
	public Direction() {
		busStops = new ArrayList<BusStop>();
	}
	
	public final List<BusStop> getBusStops() {
		return busStops;
	}
	
	public void addBusStop(BusStop busStop) {
		this.busStops.add(busStop);
	}
}
