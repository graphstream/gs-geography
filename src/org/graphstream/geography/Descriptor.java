package org.graphstream.geography;

public abstract class Descriptor {
	
	/**
	 * ID of the described class of elements.
	 */
	protected String category;
	
	/**
	 * Filter for the attributes of described element.
	 */
	protected AttributeFilter filter;
	
	public Descriptor(String category, AttributeFilter filter) {
		
		this.category = category;
		this.filter = filter;
	}
	
	public String getCategory() {
		
		return new String(this.category);
	}
	
	public Element newElement(Object o) {
		
		if(isPoint(o))
			return newPoint(o);
		
		System.err.println("oops");
		return null;
	}
	
	// Abstract
	
	public abstract boolean matches(Object o);

	public abstract boolean isPoint(Object o);
	
	public abstract Point newPoint(Object o);

}
