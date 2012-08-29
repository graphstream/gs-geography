package org.graphstream.geography;

public abstract class ElementShape {

	public static enum Type {
		POINT, LINE, POLYGON
	};
	
	protected Element element;
	
	protected Type type;
	
	public ElementShape(Element element) {
		
		this.element = element;
	}
	
	public ElementShape diff() {
		
		return this; // TODO
	}
	
	public String getElementId() {
		
		return this.element.getId();
	}
	
	/**
	 * Check if the element has a specific geometric type.
	 * 
	 * @param type
	 *            The geometric type.
	 * @return True if the element has the same type, false otherwise.
	 */
	public boolean isType(Type type) {

		if(type == Type.POINT)
			return isPoint();

		if(type == Type.LINE)
			return isLine();

		if(type == Type.POLYGON)
			return isPolygon();

		return false;
	}

	/**
	 * Check if the element is a point.
	 * 
	 * @return True if the element is a point, false otherwise.
	 */
	public boolean isPoint() {

		return this.type == Type.POINT;
	}

	/**
	 * Check if the element is a line.
	 * 
	 * @return True if the element is a line, false otherwise.
	 */
	public boolean isLine() {

		return this.type == Type.LINE;
	}

	/**
	 * Check if the element is a polygon.
	 * 
	 * @return True if the element is a polygon, false otherwise.
	 */
	public boolean isPolygon() {

		return this.type == Type.POLYGON;
	}

}
