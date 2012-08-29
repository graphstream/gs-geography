package org.graphstream.geography;

import java.util.ArrayList;

import org.graphstream.geography.ElementShape.Type;

public abstract class Aggregator {

	/**
	 * 
	 */
	protected GeoSource source;
	
	/**
	 * 
	 */
	protected Aggregate aggregate; // XXX same aggregate?
	
	/**
	 * 
	 */
	protected String currentFileName;

	/**
	 * 
	 * @param source
	 */
	public Aggregator(GeoSource source) {

		this.source = source;

		this.aggregate = new Aggregate();
	}

	/**
	 * 
	 * @param source
	 * @param onlyReadId
	 * @return
	 */
	public Aggregate read(GeoSource source, boolean onlyReadId) {

		ArrayList<FileDescriptor> fileDescriptors = source.getFileDescriptors();

		for(FileDescriptor fileDescriptor : fileDescriptors) {

			this.currentFileName = fileDescriptor.getFileName();
			
			open(fileDescriptor);

			traverse(fileDescriptor, onlyReadId);

			close(fileDescriptor);
		}

		return this.aggregate;
	}

	/**
	 * 
	 * @param fileDescriptor
	 */
	abstract protected void open(FileDescriptor fileDescriptor);

	/**
	 * 
	 * @param fileDescriptor
	 * @param onlyReadId
	 */
	abstract protected void traverse(FileDescriptor fileDescriptor, boolean onlyReadId);

	/**
	 * 
	 * @param fileDescriptor
	 */
	abstract protected void close(FileDescriptor fileDescriptor);

	/**
	 * 
	 * @param o
	 * @param onlyReadId
	 */
	protected void aggregate(Object o, Integer date, boolean onlyReadId) {

		String id = getFeatureId(o);

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
	
	public abstract Object getAttributeValue(Object o, String key);
	
	public String getCurrentFileName() {
		
		return this.currentFileName;
	}
	
}
