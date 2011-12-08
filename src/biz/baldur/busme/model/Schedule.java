package biz.baldur.busme.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Schedule implements Serializable {
	private static final long serialVersionUID = -2830024555325395514L;
	
	ArrayList<Integer> arrivals;
	
	public Schedule() {
		this.arrivals = new ArrayList<Integer>();
	}
	
	public void addArrival(int time) {
		this.arrivals.add(time);
	}
	
	public int nextArrival(int currTime) {
		for(Integer arrival: arrivals) {
			if(currTime < arrival)
				return arrival;
		}
		return -1;
	}
}
