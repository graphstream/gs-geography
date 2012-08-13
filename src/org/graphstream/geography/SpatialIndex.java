package org.graphstream.geography;

import java.util.ArrayList;

/**
 * A spatial index used to store geographical elements.
 * 
 * Its implementations should be capable of returning elements upon spatial
 * queries in an efficient manner as this kind of process can rapidly become
 * expensive (espacially with huge geographical data sets).
 * 
 * @author Merwan Achibet
 */
public interface SpatialIndex extends Iterable<Element> {

	/**
	 * Add an element to the spatial index.
	 * 
	 * @param element
	 *            The geographical element to add.
	 */
	public void add(Element element);

	/**
	 * Count the number of stored elements.
	 * 
	 * @return The number of elements.
	 */
	public int size();

	/**
	 * Give all the elements placed at the supplied position.
	 * 
	 * @param x
	 *            The x coordinate.
	 * @param y
	 *            The y coordinate.
	 * @return A list of geographical elements.
	 */
	public ArrayList<Element> getElementsAt(double x, double y);
}
