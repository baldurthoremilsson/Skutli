package biz.baldur.busme.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Route implements Serializable {
	private static final long serialVersionUID = -7276576781938638958L;
	
	String name;
	int color;
	List<Direction> directions;
	
	public Route(String name, int color) {
		this.name = name;
		this.color = color;
		this.directions = new ArrayList<Direction>();
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public void addDirection(Direction direction) {
		this.directions.add(direction);
	}
	
	public final List<Direction> getDirections() {
		return this.directions;
	}
}
