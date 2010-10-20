package org.graphstream.algo.generator.geography.shapeFile;

/**
 * Tell the direction of an edge considering its end-points and attribute set.
 *
 * @author Antoine Dutot
 */
public interface EdgeDirecter
{
	enum Direction { FROM_TO, TO_FROM, UNDIRECTED };
	
	Direction edgeDirection( Point from, Point to, AttributeSet attributes );
}