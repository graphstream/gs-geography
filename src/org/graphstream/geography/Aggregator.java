package org.graphstream.geography;

import java.util.ArrayList;

import org.graphstream.geography.ElementShape.Type;

public abstract class Aggregator {

	protected GeoSource source;
	
	protected Aggregate aggregate;

	public Aggregator(GeoSource source) {

		this.source = source;

		this.aggregate = new Aggregate();
	}

	public Aggregate read(GeoSource source, boolean onlyReadId) {

		ArrayList<FileDescriptor> fileDescriptors = source.getFileDescriptors();

		for(FileDescriptor fileDescriptor : fileDescriptors) {

			open(fileDescriptor);

			traverse(fileDescriptor, onlyReadId);

			close(fileDescriptor);
		}

		return this.aggregate;
	}

	abstract protected void open(FileDescriptor fileDescriptor);

	abstract protected void traverse(FileDescriptor fileDescriptor, boolean onlyReadId);

	abstract protected void close(FileDescriptor fileDescriptor);

	protected void aggregate(Object o, boolean onlyReadId) {

		String id = getFeatureId(o);
		Integer date = 0; // TODO TODO TODO TODO TODO
		Object object = onlyReadId ? id : o;
		
		this.aggregate.add(id, date, object);
	}

	// Abstract

	public abstract String getFeatureId(Object o);

	public abstract boolean isPoint(Object o);

	public abstract boolean isLine(Object o);

	public abstract boolean isPolygon(Object o);

	public boolean isOfType(Object o, ElementShape.Type type) {

		if(type == ElementShape.Type.POINT)
			return isPoint(o);

		if(type == ElementShape.Type.LINE)
			return isLine(o);

		if(type == ElementShape.Type.POLYGON)
			return isPolygon(o);

		return false;
	}
	
	public ElementShape.Type getType(Object o) {
		
		if(isPoint(o))
			return Type.POINT;
		
		if(isLine(o))
			return Type.LINE;
		
		if(isPolygon(o))
			return Type.POLYGON;
		
		return null;
	}

	public abstract boolean hasKey(Object o, String key);

	public abstract boolean hasKeyValue(Object o, String key, Object value);
}
