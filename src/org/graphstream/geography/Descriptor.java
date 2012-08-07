package org.graphstream.geography;

/**
 * Definition of some elements that the user wants to keep in the output graph.
 * 
 * The heart of a descriptor is its matches method. If an object matches the
 * inner definition then it is kept for the next step of the import. The
 * attributes of the element are then filtered using the attribute filter
 * provided via the constructor.
 * 
 * Descriptors are in charge of converting the kept elements into GraphStream
 * geometric elements. As such, the implementation of a descriptor needs to be
 * capable of determining the type of a feature (point, line or polygon) and to
 * instantiate new Points, Lines and Polygons from the input format of the
 * considered objects by means of the isXXX() and newXXX() methods.
 * 
 * @author Merwan Achibet
 */
public abstract class Descriptor {

	/**
	 * ID of the described class of elements.
	 */
	protected String category;

	/**
	 * Filter for the attributes of described element.
	 */
	protected AttributeFilter filter;

	/**
	 * Instantiate a descriptor.
	 * 
	 * @param category
	 *            The name of the category of features described.
	 * @param filter
	 *            The attribute filter used on matching features.
	 */
	public Descriptor(String category, AttributeFilter filter) {

		this.category = category;
		this.filter = filter;
	}

	public String getCategory() {

		return new String(this.category);
	}

	/**
	 * Give the considered feature in the GraphStream geometric format.
	 * 
	 * @param o
	 *            The feature to convert.
	 * @return
	 */
	public Element newElement(Object o) {

		if(isPoint(o))
			return newPoint(o);
		else if(isLine(o))
			return newLine(o);
		
		System.err.println("oops");
		return null;
	}

	// Abstract

	/**
	 * Check if the supplied feature conforms to the inner definition of the
	 * descriptor.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature should be categorized by the descriptor.
	 */
	public abstract boolean matches(Object o);

	/**
	 * Check if the supplied feature is a point.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a point, false otherwise (line or
	 *         polygon).
	 */
	public abstract boolean isPoint(Object o);

	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	public abstract Point newPoint(Object o);

	/**
	 * Check if the supplied feature is a line.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a line, false otherwise (point or
	 *         polygon).
	 */
	public abstract boolean isLine(Object o);

	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	public abstract Line newLine(Object o);

}
