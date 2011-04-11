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

import org.graphstream.algo.generator.geography.shapeFile.Point;
import org.graphstream.algo.generator.geography.shapeFile.SpatialIndex;

/**
 * Specify what to do when a point (for the point or polyline shapes) to be inserted is exactly
 * at the same location than another existing point. 
 */
public abstract class PointMergeOperation
{
	/**
	 * What to do with one of the points if two points can be merged.
	 * <ul>
	 * 		<li>NOTHING do not touch the spatial index.</li>
	 * 		<li>DELETE_NEW removes (or do not insert) the new point (the one considered for merging)
	 *		    in the spatial index.</li>
	 * 		<li>DELETE_OLD removes the old point from the spatial index. Be extra careful, removing
	 *		    an existing point may breaks some already constructed topology. Ensure you use
	 *			this operation only if it applies to point features, not to polylines or
	 *		    polygons.</li>
	 * </ul>
	 */
	public enum PointOperation { DELETE_OLD, DELETE_NEW, KEEP_OLD, KEEP_NEW, NOTHING };

	/**
	 * The merge operation to accomplish on one of the merged points.
	 */
	public PointOperation mergeOperation = PointOperation.NOTHING;

	/**
	 * Base class to create merge operations.
	 * @param mergeOperation What to do with the remaining point if the merge occurred ? This
	 *        specify if the old point or new point is removed from the spatial index.
	 */
	public PointMergeOperation( PointOperation mergeOperation )
	{
		this.mergeOperation = mergeOperation;
	}
	
	/**
	 * Test if an existing point matches a another one (they are considered
	 * already at the same position, and the spatial test is not made).
	 * @param oldOne The existing point.
	 * @param newOne The new point.
	 * @return True if they can be merged.
	 */
	public abstract boolean matches( Point oldOne, Point newOne );
	
	/**
	 * Apply the merge operation considering the merge matched. This operation may remove one of
	 * the points from the spatial index if it is not yet in it. This never add a point in the
	 * index.
	 * @param oldOne The old point.
	 * @param newOne The new point.
	 * @return The merge of the old and new point.
	 */
	public Point apply( Point oldOne, Point newOne, SpatialIndex index )
	{
		switch( mergeOperation )
		{
			case DELETE_NEW:
//				System.err.printf( "        Del New (%s)%n", newOne.getId() );
				oldOne.merge( newOne );
				index.remove( newOne );
				return oldOne;
			case DELETE_OLD:
//				System.err.printf( "        Del Old (%s)%n", oldOne.getId() );
				newOne.merge( oldOne );
				index.remove( oldOne );
				return newOne;
			case KEEP_NEW:
//				System.err.printf( "        Keep New (%s)%n", newOne.getId() );
				oldOne.merge( newOne );
				return oldOne;
			case KEEP_OLD:
//				System.err.printf( "        Keep Old (%s)%n", oldOne.getId() );
				newOne.merge( oldOne );
				return newOne;
			default:
			case NOTHING:
//				System.err.printf( "        Do Nop%n" );
				return newOne;
		}
	}
	
	/**
	 * If the old point has a value for the given attribute, and new point also
	 * has a value for this attribute, return true if the two values match.
	 * @param attribute The attribute to test.
	 * @param oldOne See if this point has this attribute.
	 * @param newOne See if this point has this attribute.
	 * @return True if the two points attributes exist and have the same value.
	 */
	protected boolean attributeMatches( String attribute, Point oldOne, Point newOne )
	{
		ArrayList<Object> valuesOld = oldOne.getValuesForAttribute( attribute );
		ArrayList<Object> valuesNew = newOne.getValuesForAttribute( attribute );
		
		if( valuesOld.size() > 0 && valuesNew.size() > 0 )
		{
			for( Object valueOld: valuesOld )
			{
				for( Object valueNew: valuesNew )
				{
					if( valueOld != null && valueNew != null )
					{
						if( valueOld.equals( valueNew ) )
						{
							return true;
						}
					}
				}
			}
		}
		
		return false;
	}

	/**
	 * True if the given point has the given attribute.
	 * @param attribute The attribute to test.
	 * @param point The point to test.
	 * @return False if the attribute is not in the point.
	 */
	protected boolean hasAttribute( String attribute, Point point )
	{
		return point.hasAttribute( attribute );
	}
}