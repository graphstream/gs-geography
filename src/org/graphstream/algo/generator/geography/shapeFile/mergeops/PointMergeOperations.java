package org.graphstream.algo.generator.geography.shapeFile.mergeops;

import java.util.ArrayList;
import java.util.Collection;

import org.graphstream.algo.generator.geography.shapeFile.Point;
import org.graphstream.algo.generator.geography.shapeFile.SpatialIndex;

/**
 * Ordered set of merge operation to apply to points of the spatial index that are at the same
 * location.
 *
 * @author Antoine Dutot
 */
public class PointMergeOperations
{
	/**
	 * The ordered set of merge operations.
	 */
	protected ArrayList<PointMergeOperation> ops = new ArrayList<PointMergeOperation>();
	
	/**
	 * New empty set of operations.
	 */
	public PointMergeOperations()
	{
	}
	
	/**
	 * New ordered set of operation with a first operation.
	 * @param firstOperation The initial operation to add to the set.
	 */
	public PointMergeOperations( PointMergeOperation firstOperation )
	{
		addOperation( firstOperation );
	}
	
	/**
	 * Add a merge operation in the set.
	 * @param operation The operation to add.
	 */
	public void addOperation( PointMergeOperation operation )
	{
		ops.add( operation );
	}
	
	/**
	 * The ordered set of operations.
	 * @return A collection of operations, in order.
	 */
	public Collection<PointMergeOperation> getOperations()
	{
		return ops;
	}
	
	/**
	 * The number of merge operations.
	 * @return The operations count.
	 */
	public int getOperationCount()
	{
		return ops.size();
	}
	
	/**
	 * The i-th merge operation.
	 * @param i The operation index.
	 * @return The operation at index i.
	 */
	public PointMergeOperation getOperation( int i )
	{
		return ops.get( i );
	}
	
	/**
	 * Look if the given point is at the same location than others in the spatial index. If this is
	 * the case, look if one ore more merge-operations can be applied on it. If this is the case
	 * the one of the old or new point is merged an the other may disappear from the spatial index.
	 * Several merges can be done. Merge operations are ordered and the a merge operation can assume
	 * the points it compare contain the attributes merged from previous operations. At the end of
	 * operations, if the obtained point is not in the index yet (the newOne you given was not in
	 * the index), it is added.
	 * @param newOne The new point to check.
	 * @return The point with all other points merged, this can be an entirely new point so the
	 *         reference passed in parameter newOne may not be the same as the one that is output,
	 *         also the reference to newOne may have disappeared if it was merged with another.
	 */
	public Point mergePoint( Point newOne, SpatialIndex index )
	{
		// For all operations in order :

		for( PointMergeOperation op: ops )
		{
			ArrayList<Point> points = index.searchForPointsAt( newOne.getPosition().x, newOne.getPosition().y );
			
//			System.err.printf( "Merging point %s to %d points%n", newOne.getId(),  points != null ? points.size() : 0 );
			
			// For all points at this position.

			if( points == null )
				break;			// There are no other points, there will be no merge, and
								// Therefore nothing more to do.
			
//			if( points != null )
			{
				for( Point p: points )
				{
//					System.err.printf( "    %s == %s ... ", p.getId(), newOne.getId() );
					
					if( op.matches( p, newOne ) )
					{
//						System.err.printf( "Yes%n" );
						newOne = op.apply( p, newOne, index );
					}
//					else System.err.printf( "No%n" );
				}
			}
		}
		
		if( ! index.contains( newOne ) )
			index.add( newOne );
		
		return newOne;
	}
}