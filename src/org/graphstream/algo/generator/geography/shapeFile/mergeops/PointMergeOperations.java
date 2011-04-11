/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
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