package org.graphstream.geography;

import java.util.ArrayList;

/**
 * 
 * @author Merwan Achibet
 */
public interface SpatialIndex extends Iterable<Element> {

	public void add(Element element);
	
	public int size();
	
	public ArrayList<Element> getElementsAt(double x, double y);
}
