package org.graphstream.geography.test;

import java.util.Random;

import org.graphstream.geography.Point;
import org.graphstream.geography.index.SpatialIndex;

public class Test_SpatialIndex {

	public static void main(String[] args) {

		// Instantiate the spatial index.
		
		SpatialIndex index = new SpatialIndex();

		// Add a lot of randomly placed points.
		
		Random rnd = new Random(12345);
		
		for(int i = 0; i < 1000; ++i) {
			Point p = new Point(i + "");
			p.setPosition(rnd.nextDouble() * 100, rnd.nextDouble() * 100);
			index.add(p);
		}
		
		// Add specific points.
		
		Point a = new Point("A");
		a.setPosition(0, 0);
		index.add(a);

		Point b = new Point("B");
		b.setPosition(0.00001, 0.00001);
		index.add(b);

		Point c = new Point("C");
		c.setPosition(1, 1);
		index.add(c);

		//
		
		System.out.println(index.size());
		
		System.out.println(index.getElementsAt(0, 0));
	}

}
