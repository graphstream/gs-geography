package org.graphstream.geography;

/**
 * Definition of some elements that the user wants to keep in the output graph.
 * 
 * The heart of a descriptor is its matching method. If an object matches its
 * inner definition then it is kept for the next step of the import. The
 * attributes of the element are also filtered using the attribute filter
 * provided via the constructor.
 * 
 * Descriptors are in charge of converting the kept elements into GraphStream
 * geometric elements (simple Points, Lines and Polygons with attributes). As
 * such, the implementation of a descriptor needs to be capable of determining
 * the type of a feature (point, line or polygon) and to instantiate new Points,
 * Lines and Polygons from the input format of the considered objects by means
 * of the isXXX() and newXXX() methods.
 * 
 * @author Merwan Achibet
 */
public abstract class Descriptor {

	/**
	 * The source using this descriptor.
	 */
	protected GeoSource source;

	/**
	 * The ID of the described class of elements.
	 */
	protected String category;

	/**
	 * Filter for the attributes of the described element.
	 */
	protected AttributeFilter filter;

	/**
	 * Instantiate a descriptor.
	 * 
	 * @param category
	 *            The name of the category of elements described.
	 * @param filter
	 *            The attribute filter used on matching features.
	 */
	public Descriptor(GeoSource source, String category, AttributeFilter filter) {

		this.source = source;
		this.category = category;
		this.filter = filter;
	}

	/**
	 * Give the name of the category of elements described by the descriptor.
	 * 
	 * @return The name of the element category.
	 */
	public String getCategory() {

		return new String(this.category);
	}

	/**
	 * Give the considered feature in the GraphStream geometric format.
	 * 
	 * @param o
	 *            The feature to convert.
	 * @return A simple geometric element.
	 */
	public Element newElement(Object o) {

		if(isPoint(o))
			return newPoint(o);
		else if(isLine(o))
			return newLine(o);

		// TODO polygons

		// TODO What happens in other cases?

		return null;
	}

	// Abstract

	/**
	 * Check if the supplied feature conforms to the inner definition of the
	 * descriptor.
	 * 
	 * @param element
	 *            The considered element.
	 * @return True if the element should be categorized by the descriptor,
	 *         false otherwise.
	 */
	public abstract boolean matches(Element element);

	/**
	 * Check if the supplied feature is a point according to the descriptor
	 * rules.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a point, false otherwise (line or
	 *         polygon).
	 */
	protected abstract boolean isPoint(Object o);

	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	protected abstract Point newPoint(Object o);

	/**
	 * Check if the supplied feature is a line according to the descriptor
	 * rules.
	 * 
	 * @param o
	 *            The considered feature.
	 * @return True if the feature is a line, false otherwise (point or
	 *         polygon).
	 */
	protected abstract boolean isLine(Object o);

	/**
	 * Give a GraphStream geometric element based on the supplied feature.
	 * 
	 * @param o
	 *            The object to convert.
	 * @return A graphStream geometric element.
	 */
	protected abstract Line newLine(Object o);

}
