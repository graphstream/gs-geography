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
package org.graphstream.algo.generator.geography.shapeFile.test;

import org.graphstream.algo.generator.geography.shapeFile.AttributeFilter;
import org.graphstream.algo.generator.geography.shapeFile.AttributeSet;
import org.graphstream.algo.generator.geography.shapeFile.EdgeDirecter;
import org.graphstream.algo.generator.geography.shapeFile.Point;
import org.graphstream.algo.generator.geography.shapeFile.ShapeFileGenerator;
import org.graphstream.algo.generator.geography.shapeFile.mergeops.PointMergeOperation;
import org.graphstream.algo.generator.geography.shapeFile.mergeops.PointMergeOperations;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.swingViewer.Viewer;

/**
 * Test the shape file generator.
 * 
 * @author Antoine Dutot
 */
public class TestShapeFileGenerator
{
	public static void main( String args[] )
	{
//		System.setProperty( "gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer" );
		new TestShapeFileGenerator();
	}
	
	public static String graphId = "Strasbourg";
	
	public TestShapeFileGenerator()
	{
		Graph              graph = new MultiGraph( graphId );
		ShapeFileGenerator gen   = new ShapeFileGenerator();
	
		Viewer viewer = graph.display( false );
		graph.addAttribute( "stylesheet", styleSheetNew );
		graph.addAttribute( "ui.quality" );
		graph.removeAttribute( "ui.antialias" );
		viewer.setCloseFramePolicy( Viewer.CloseFramePolicy.EXIT );
	
		// Setup some filtering and the point merging operations.
		
		PointMergeOperations mergeOps    = new PointMergeOperations();
		AttributeFilter      pointFilter = new AttributeFilter( AttributeFilter.Mode.KEEP );
		AttributeFilter      nodeFilter  = new AttributeFilter( AttributeFilter.Mode.KEEP );
		AttributeFilter      edgeFilter  = new AttributeFilter( AttributeFilter.Mode.KEEP );
		
		gen.setPointAttributeFilter( pointFilter );
		gen.setNodeAttributeFilter( nodeFilter );
		gen.setEdgeAttributeFilter( edgeFilter );
		
		mergeOps.addOperation( new PointMergeOperation( PointMergeOperation.PointOperation.KEEP_OLD ) {
			@Override
			public boolean matches(  Point oldOne, Point newOne ) 
			{
				return(    attributeMatches( "LINK_ID", oldOne, newOne )
					&&     hasAttribute( "Z_LEVEL", oldOne )
				    && ( ! hasAttribute( "ADDR_ST", oldOne ) )
				    && ( ! hasAttribute( "Z_LEVEL", newOne ) ) );
			}
		} );
		mergeOps.addOperation( new PointMergeOperation( PointMergeOperation.PointOperation.DELETE_NEW ) {
			@Override
            public boolean matches( Point oldOne, Point newOne )
            {
				return( attributeMatches( "Z_LEVEL", oldOne, newOne )
				    &&  hasAttribute( "ADDR_ST", oldOne )
				    &&  hasAttribute( "ADDR_ST", newOne ) );
            }
		} );

		gen.setMergeDuplicateEdge( true );
		gen.setEdgeDirecter( new EdgeDirecter(){
			public Direction edgeDirection( Point fromPoint, Point toPoint, AttributeSet attributes )
            {
				String val  = attributes.getStringAttribute( "DIR_TRAVEL" );
				
				if( val.equals( "B" ) )
					return Direction.UNDIRECTED;
				
				if( val.equals( "T" ) )
					return Direction.TO_FROM;
			
				if( val.equals( "F" ) )
					return Direction.FROM_TO;
			
				return Direction.UNDIRECTED;
            }
		} );
		
		pointFilter.add( "Z_LEVEL" );
		pointFilter.add( "ADDR_ST" );
		pointFilter.add( "LINK_ID" );		
		pointFilter.add( "ST_NAME" );
		pointFilter.add( "DIR_TRAVEL" );
		pointFilter.add( "FUNC_CLASS" );
		pointFilter.add( "SPEED_CAT" );
		pointFilter.add( "SPEED_LIM" );
		pointFilter.add( "TO_LANES" );
		pointFilter.add( "FROM_LANES" );
		pointFilter.add( "PAVED" );
		pointFilter.add( "PRIVATE" );
		pointFilter.add( "BRIDGE" );
		pointFilter.add( "TUNNEL" );
		pointFilter.add( "RAMP" );
		pointFilter.add( "TOLLWAY" );
		pointFilter.add( "FRONTAGE" );
		pointFilter.add( "CONTRACC" );
		pointFilter.add( "ROUNDABOUT" );
		pointFilter.add( "LANE_CAT" );
		
		nodeFilter.add( "Z_LEVEL" );

		edgeFilter.add( "ADDR_ST" );
		edgeFilter.add( "ST_NAME" );
		edgeFilter.add( "FUNC_CLASS" );
		edgeFilter.add( "SPEED_CAT" );
		edgeFilter.add( "SPEED_LIM" );
		edgeFilter.add( "TO_LANES" );
		edgeFilter.add( "FROM_LANES" );
		edgeFilter.add( "PAVED" );
		edgeFilter.add( "PRIVATE" );
		edgeFilter.add( "BRIDGE" );
		edgeFilter.add( "TUNNEL" );
		edgeFilter.add( "RAMP" );
		edgeFilter.add( "TOLLWAY" );
		edgeFilter.add( "FRONTAGE" );
		edgeFilter.add( "CONTRACC" );
		edgeFilter.add( "ROUNDABOUT" );
		edgeFilter.add( "LANE_CAT" );
		
		// Go and read the data.
		
		try
		{
			int count1 = 0;
			gen.begin( graph, "/home/antoine/Bureau/NavTeq/"+graphId+"/"+graphId+"_ZLevels.shp" );
			
			while( gen.nextElement() )
			{
				count1++;
				if( count1 % 100 == 0 )
					System.out.printf( " [%d]", count1 );
				if( count1 % 1000 == 0 )
					System.out.printf( "%n" );
				Thread.yield();
			}
			System.err.printf( "%n" );
			gen.end();
			
			int count2 = 0;
			gen.setMergeOperations( mergeOps );
			gen.begin( graph, "/home/antoine/Bureau/NavTeq/"+graphId+"/"+graphId+"_Streets.shp" );
			
			while( gen.nextElement() )
			{
				count2++;
				if( count2 % 100 == 0 )
					System.out.printf( " [%d]", count2 );
				if( count2 % 1000 == 0 )
					System.out.printf( "%n" );
				Thread.yield();
			}
			
			equipGraph( graph );
			
			System.out.printf( "%nOK, now writing the graph ... " );
			gen.end();
			gen.release();

			graph.write( String.format( "%s.dgs", graph.getId() ) );
			addStyle( graph );
			
			System.out.printf( "OK Finished%n" );
		}
        catch( Exception e )
        {
	        e.printStackTrace();
        }
	}
	
	protected void equipGraph( Graph graph ) 
	{
		for( Edge edge: graph.getEachEdge() )
		{
			if( edge.hasAttribute( "length" ) ) {
				double length = edge.getNumber( "length" );
				double maxCap = length;
				
				if( edge.hasLabel( "SPEED_CAT" ) ) {
					String speed_cat = (String)edge.getLabel( "SPEED_CAT" );
					double speed     = speedCatToKph( speed_cat );	// In kilometres per hour.
					double time      = ( length / 1000 ) / speed;	// In hours.
//					System.err.printf( "speed_cat=%s speed=%f length=%f time=%f(h) time=%f(mn)%n", speed_cat, speed, length, time, time*60 );
					time *= 60;	// In minutes.
					edge.setAttribute( "time", time );
				}
				else System.err.printf( "no SPEED_CAT !!%n" );
				
				maxCap /= 5;	// A car uses 5 meters.
				if( maxCap < 5 ) maxCap = 5;
//				System.err.printf( "maxCap = %f%n", maxCap );
				edge.addAttribute( "maxCap", maxCap );
			}
			else System.err.printf( "no length !!!%n" );
		}
	}
	
	protected int speedCatToKph( String cat )
	{
		int c = Integer.parseInt( cat );
		
		switch( c ) {
			case 1: return 150;
			case 2: return 130;
			case 3: return 100;
			case 4: return 90;
			case 5: return 70;
			case 6: return 50;
			case 7: return 30;
			case 8: return 11;
		}
		
		return 5;
	}
	
	protected void addStyle( Graph graph )
	{
		for( Edge edge: graph.getEachEdge() )
		{
			StringBuilder cls = new StringBuilder();
			int cnt = 0;

			if( edge.getLabel( "LANE_CAT" ).equals( "2" ) )
			{
				cls.append( "laneCat2" );
				cnt++;
			}
			else if( edge.getLabel( "LANE_CAT" ).equals( "3" ) )
			{
				cls.append( "laneCat3" );
				cnt++;
			}
			
			if( edge.getLabel( "CONTRACC" ).equals( "Y" ) )
			{
				if( cnt>0 )
					cls.append( "," );
				cls.append( "freeway" );
				cnt++;
			}
			
			if( cnt > 0 )
			{
				edge.addAttribute( "ui.class", cls.toString() );
			}
		}
	}
	
	protected static final String styleSheet = 
		"node { width: 3px; }" +
		"edge { color: #808080; arrow-length: 7px; arrow-width: 3px; }" +
		"edge.freeway  { width: 2px; border-width: 1px; border-color: red; }" +
		"edge.laneCat2 { color:#E0D040; }" +
		"edge.laneCat3 { color:#F0F060; }";
	protected static final String styleSheetNew = 
		"node { size: 3px; }" +
		"edge { fill-color: #808080; arrow-size: 3px, 3px; }" +
		"edge.freeway  { size: 2px; stroke-width: 1px; stroke-color: red; shadow-mode: plain; shadow-color: red; shadow-width: 1px; shadow-offset: 0px, 0px; }" +
		"edge.laneCat2 { fill-color:#E0D040; }" +
		"edge.laneCat3 { fill-color:#F0F060; }";
}