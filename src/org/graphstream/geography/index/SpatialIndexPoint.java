package org.graphstream.geography.index;

import org.graphstream.geography.Element;
import org.miv.pherd.Particle;

/**
 * 
 * @author Merwan Achibet
 */
public class SpatialIndexPoint extends Particle {

	/**
	 * The element referenced by this point.
	 * 
	 * If the element is a point, there is only one reference to this point in
	 * the spatial index. In case of more complex elements, like lines or
	 * polygons, there can be several references to the same element.
	 */
	protected Element referencedElement;

	/**
	 * Instantiate a new reference point for a spatial index.
	 * 
	 * @param element
	 *            The element to be referenced.
	 * @param id
	 *            The ID of the reference point.
	 * @param x
	 *            the x-axis coordinate of the point.
	 * @param y
	 *            The y-axis coordinate of the point.
	 */
	public SpatialIndexPoint(Element element, String id, double x, double y) {
		super(id, x, y, 0);

		this.referencedElement = element;
	}

	/**
	 * Give the geometric element referenced by this point.
	 * 
	 * @return The referenced element.
	 */
	public Element getReferencedElement() {

		return this.referencedElement;
	}

	/**
	 * Check if the point is at the given position, with an error margin.
	 * 
	 * @param x
	 *            The x-axis coordinate.
	 * @param y
	 *            The y-axis coordinate.
	 * @param offset
	 *            The distance offset.
	 * @return True if the point is at this position, false otherwise.
	 */
	public boolean isAt(double x, double y, double offset) {

		// Check if the point is in a bounding box. It is faster that the
		// Euclidean distance and remains accurate if the distance offset is
		// small enough.

		return this.pos.x > x - offset && this.pos.x < x + offset && this.pos.y > y - offset && this.pos.y < y + offset;
	}

	// TODO ???

	@Override
	public void inserted() {
		// TODO Auto-generated method stub
	}

	@Override
	public void move(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removed() {
		// TODO Auto-generated method stub

	}

}