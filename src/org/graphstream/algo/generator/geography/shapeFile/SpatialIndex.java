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
package org.graphstream.algo.generator.geography.shapeFile;

import java.util.ArrayList;
import java.util.Iterator;

import org.miv.pherd.Particle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.Cell;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.QuadtreeCellSpace;

/**
 * A set of points of the shape file (and only points) in a quad-tree for fast fetching knowing
 * only a position.
 * 
 * @author Antoine Dutot
 */
public class SpatialIndex
{
// Attribute
	
	/**
	 * The spatial index.
	 */
	protected ParticleBox pbox;
	
	/**
	 * Number of points in one cell.
	 */
	protected int nodesPerCell = 5;
	
	/**
	 * The number of features read since the last reorganisation of the spatial index. 
	 */
	int count = 0;
	
	/**
	 * The maximum number of features to insert in the spatial index before reoranising it.
	 */
	int MAXSTEPS = 100;

// Construction
	
	/**
	 * New spatial index in 2D.
	 */
	public SpatialIndex()
	{
		CellSpace space;
		
		space = new QuadtreeCellSpace( new Anchor( -1, -1, -0.01f ), new Anchor( 1, 1, 0.01f ) );
		pbox  = new ParticleBox( nodesPerCell, space, new BarycenterCellData() );
		
		pbox.getNTree().setDepthMax( 15 );
	}
	
	/**
	 * True if the index contains the given point.
	 * @param point The point to test.
	 * @return True of the index already contains the point.
	 */
	public boolean contains( Point point )
	{
		return( pbox.getParticle( point.getId() ) == point );
	}
	
	/**
	 * Search an intersection at (x,y).
	 * @param x The abscissa.
	 * @param y The ordinate.
	 * @return The set of points at (x,y) or null if nothing is found.
	 */
	public ArrayList<Point> searchForPointsAt( float x, float y )
	{
		Cell cell = pbox.getNTree().getRootCell();
		
		if( ! cell.contains( x, y, 0 ) )
			System.err.printf( "*** (%f,%f,0) not in (%s | %s)%n", x, y, cell.getSpace().getLoAnchor(), cell.getSpace().getHiAnchor() );
		
		return searchInCell( cell, x, y, 0 );
	}
	
	/**
	 * Recursively search for points around (close from) a given point.
	 * @param cell The cell (and recursively sub-cells) to search for the point.
	 * @param x The abscissa of the point to search.
	 * @param y The ordinate of the point to search.
	 * @return The set of points close to the given point or null if nothing is found.
	 */
	protected ArrayList<Point> searchInCell( Cell cell, float x, float y, int level )
	{
		if( cell.isLeaf() )
		{
			Iterator<? extends Particle> particles = cell.getParticles();
			ArrayList<Point> points = null;

			while( particles.hasNext() )
			{
				Point i = (Point) particles.next();
				
				if( i.isAt( x, y ) )
				{
					if( points == null )
						points = new ArrayList<Point>();
					
					points.add( i );
				}
			}
/*			if( points != null )
			{
				printLevel( level );
				System.err.printf( "found %d of %d (level %d) contains=%b%n", points != null ? points.size() : 0, cell.getPopulation(), level, cell.contains(x,y,0) );
			}
*/			
			return points;
		}
		else
		{
			int div = cell.getSpace().getDivisions();
			
//			printLevel( level );
//			System.err.printf( "level %d%n", level );
			for( int i=0; i<div; i++ )
			{
				Cell sub = cell.getSub( i );
/*
				ArrayList<Point> res = searchInCell( sub, x, y, level+1 );

				if( res != null )
				{
					printLevel( level );
					System.err.printf( "level %d cell %d = %b res=%d contains=%b%n", level, i, sub.contains( x, y, 0 ), res != null ? res.size() : 0, sub.contains( x, y,0 ) );
					//if(  sub.contains( x, y, 0 ) )
					{
						boolean r  = cell.contains( x, y, 0 );
						boolean c0 = cell.getSub(0).contains( x, y, 0 );
						boolean c1 = cell.getSub(1).contains( x, y, 0 );
						boolean c2 = cell.getSub(2).contains( x, y, 0 );
						boolean c3 = cell.getSub(3).contains( x, y, 0 );
						
						printLevel( level );
						System.err.printf( "    super contains=%b  c0=%b c1=%b c2=%b c3=%b%n", r, c0, c1, c2, c3 );
						
						if( r && ( (!c0)&&(!c1)&&(!c2)&&(!c3) ) )
						{
							printLevel(level);
							System.err.printf( "    SUPER MERDE (%f, %f)%n", x, y );
							
							printLevel(level);
							System.err.printf( "    root = %s%n", cell.getSpace().toString() );
							printLevel(level);
							System.err.printf( "        c0 = %s%n", cell.getSub(0).getSpace().toString() );
							printLevel(level);
							System.err.printf( "        c1 = %s%n", cell.getSub(1).getSpace().toString() );
							printLevel(level);
							System.err.printf( "        c2 = %s%n", cell.getSub(2).getSpace().toString() );
							printLevel(level);
							System.err.printf( "        c3 = %s%n", cell.getSub(3).getSpace().toString() );
							cell.getSub(0).getSpace().contains2( x, y, 0 );
							cell.getSub(1).getSpace().contains2( x, y, 0 );
							cell.getSub(2).getSpace().contains2( x, y, 0 );
							cell.getSub(3).getSpace().contains2( x, y, 0 );
						}
					}
					return res;
				}
*/
				if( sub.contains( x, y, 0 ) )
					return searchInCell( sub, x, y, level+1 );
			}

//			System.err.printf( "        NOT FOUND at %d for (%f, %f)%n", level, x, y );
			
			return null;
		}	
	}
	
	protected void printLevel( int level )
	{
		for( int i=0; i<level; i++ ) System.err.printf( "    " );
	}
	
	/**
	 * Register a point in the index.
	 * @param point The point to add.
	 */
	public void add( Point point )
	{
		pbox.addParticle( point );
		
		count++;
		
		if( count > MAXSTEPS )
		{
			reorganize();
			count = 0;
		}
	}
	
	/**
	 * Unregister a point from the index.
	 * @param point The point to remove.
	 */
	public void remove( Point point )
	{
		pbox.removeParticle( point.getId() );
	}
	
	/**
	 * Force a reorganisation the spatial index to speed up later accesses. This is only needed if
	 * a lot of points have been added. This is however done automatically from times to times when
	 * {@link #add(Point)} is called so it should not be needed. 
	 */
	public void reorganize()
	{
		pbox.step();
	}
}