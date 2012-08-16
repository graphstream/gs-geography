package org.graphstream.geography.index;

import org.graphstream.geography.Element;
import org.miv.pherd.Particle;

public class SpatialIndexPoint extends Particle {

	protected Element referencedElement;
	
	public SpatialIndexPoint(Element element, String id, double x, double y) {
		super(id, x, y, 0);
		
		this.referencedElement = element;
	}
	
	public Element getReferencedElement() {
		
		return this.referencedElement;
	}
	
	public boolean isAt(double x, double y, double offset) {

		return Math.sqrt(Math.pow(this.pos.x - x, 2) + Math.pow(this.pos.y - y, 2)) < offset;
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
