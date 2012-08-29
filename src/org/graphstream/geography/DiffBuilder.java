package org.graphstream.geography;

public abstract class DiffBuilder {

	protected GeoSource source;
	
	public DiffBuilder(GeoSource source) {
	
		this.source = source;
	}
	
	abstract public ElementState diff(Element element, ElementState previousDiff, Integer previousDate, Object o);

}
