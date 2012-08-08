package org.graphstream.geography;

import java.util.ArrayList;
import java.util.Iterator;

import org.graphstream.geography.Element;
import org.graphstream.geography.SpatialIndex;

/**
 * The most basic form of spatial index: a dumb list of geometric elements.
 * 
 * TODO implement a quadtree-like structure later.
 * 
 * @author Merwan Achibet
 */
public class BasicSpatialIndex implements SpatialIndex {

	private ArrayList<Element> elements;
	
	public BasicSpatialIndex() {
	
		this.elements = new ArrayList<Element>();
	}
	
	@Override
	public void add(Element element) {

		this.elements.add(element);
	}

	@Override
	public ArrayList<Element> getElementsAt(double x, double y) {
		
		ArrayList<Element> selection = new ArrayList<Element>();
		
		for(Element element : this.elements)
			if(element.at(x, y))
				selection.add(element);
		
		return selection;
	}

	@Override
	public Iterator<Element> iterator() {
		
		return this.elements.iterator();
	}

	/**
	@Override
	public Iterator<Element> iterator() {
		
		return new BasicSpatialIndexIterator(this);
	}

	private class BasicSpatialIndexIterator implements Iterator<Element> {

		public BasicSpatialIndexIterator(BasicSpatialIndex index) {
			
		}
		
		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Element next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
	}
	*/
	
}
