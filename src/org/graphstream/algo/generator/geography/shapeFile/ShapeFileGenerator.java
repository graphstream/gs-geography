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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.graphstream.algo.generator.geography.shapeFile.mergeops.PointMergeOperations;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Graph generator using shape files as input.
 * 
 * <p>
 * This generator reads one or more shape files, accumulating data, until it can effectively
 * export nodes and edges to build a graph from the features of the input shape files. The main
 * goal of this generator is to extract a road graph from the features. 
 * </p>
 * 
 * <p>
 * This generator only considers point features and polyline features. Polygons are not handled
 * at all since they almost never model streets.
 * </p>
 * 
 * <p>
 * To correctly work, this generator needs to be able to detect the intersection points of
 * polylines. The most basic way to do this is to consider each polyline end-point and merge them
 * to all other polyline end-points at the same position. However this can lead to more
 * intersections points than needed. Shape files ensure that each time a line crosses another there
 * is a point. This can happen at end-points of polylines, but this does not necessarily means that
 * there is an intersection at this point. The polylines can in fact be at two distinct Z levels
 * (for example when there is a bridge or a tunnel). Therefore, most of the time you have to
 * provide "merge operations".  
 * </p>
 * 
 * <p>
 * Merge operations are set of operations to apply to newly added points (point-features or the
 * end-points of a polyline) when they are at the same position than a previously existing point.
 * </p>
 * 
 * TODO add a better description of the quite-complicated point-merging process used to extract
 * topological informations from the shape data base.
 * TODO a feature filter, very similar to the pointMergeOperations that avoid to store an input
 * feature and discard it as soon as read. This should allow to reduce memory usage a lot for
 * non interesting feature : can be done using the PointMergeOperations ??
 * 
 * @author Antoine Dutot
 */
public class ShapeFileGenerator //extends BaseGenerator
{
// Attribute
	
	/**
	 * Iterator on the current shape file.
	 */
	protected FeatureIterator<SimpleFeature> iterator;
	
	/**
	 * The spatial index to retrieve intersection points.
	 */
	protected SpatialIndex index = new SpatialIndex();
	
	/**
	 * The graph.
	 */
	protected Graph graph;

	/**
	 * Used to create unique identifiers for each edge.
	 */
	protected int edgeAllocator = 0;
	
	/**
	 * Current set of merge operations.
	 */
	protected PointMergeOperations mergeOps;
	
	/**
	 * Merge multiple edges between two nodes (often the sign of multiple denomination of the 
	 * same street).
	 */
	protected boolean mergeDuplicateEdge = false;
	
	/**
	 * Store the shape (coordinates) of the edge in an attribute.
	 */
	protected boolean addEdgeShapeAttribute = true;
	
	/**
	 * Attribute filter for input point features.
	 */
	protected AttributeFilter pointAttributeFilter;
	
	/**
	 * Attribute filter for output nodes.
	 */
	protected AttributeFilter nodeAttributeFilter;
	
	/**
	 * Attribute filter for output edges.
	 */
	protected AttributeFilter edgeAttributeFilter;
	
	/**
	 * How to determine the edge direction.
	 */
	protected EdgeDirecter edgeDirecter = new DefaultEdgeDirecter();

	/**
	 * If true, store the edge length computed by GeoTools as an attribute of each edge. 
	 */
	protected boolean addEdgeLengths = true;
	
// Attribute
	
	/**
	 * Are duplicate edges merged ?.
	 */
	public boolean getMergeDuplicateEdge()
	{
		return mergeDuplicateEdge;
	}
	
// Command

	/**
	 * Set the current merge operations applied to newly added points when they are at the same
	 * position than a previously existing point. This is applied for point-features and the start
	 * and end points of polyline-features. 
	 * @param mergeOps The set of merge operations to apply to added points that are at the same
	 * position than a previously existing point.
	 */
	public void setMergeOperations( PointMergeOperations mergeOps )
	{
		this.mergeOps = mergeOps;
	}
	
	/**
	 * Set the filter for attributes exported on nodes of the graph. The resulting attribute set
	 * does not affect point merging.
	 * @param filter The filter.
	 */
	public void setNodeAttributeFilter( AttributeFilter filter )
	{
		nodeAttributeFilter = filter;
	}
	
	/**
	 * Set the filter for attributes exported on edges of the graph. The resulting attribute set
	 * does not affect point merging.
	 * @param filter The filter.
	 */
	public void setEdgeAttributeFilter( AttributeFilter filter )
	{
		edgeAttributeFilter = filter;
	}
	
	/**
	 * Set the filter for attribute on point-feature read on input. This affects the available
	 * attributes when doing point merging. However this can greatly improve memory consumption
	 * as some points have to be kept in memory with all their attributes.
	 * @param filter The filter.
	 */
	public void setPointAttributeFilter( AttributeFilter filter )
	{
		pointAttributeFilter = filter;
	}
	
	/**
	 * Set the class that give the direction of edge based on the attributes of the features they
	 * correspond to.
	 * @param directer The edge directer.
	 */
	public void setEdgeDirecter( EdgeDirecter directer )
	{
		edgeDirecter = directer;
	}
	
	/**
	 * If true, duplicate edges (edges between to points already joined by another edge) will not
	 * be added to the graph.
	 * @param on If true, delete duplicate edges.
	 */
	public void setMergeDuplicateEdge( boolean on )
	{
		mergeDuplicateEdge = on;
	}
	
	/**
	 * If true add an attribute "length" to each edge generated with the length computed by GeoTools
	 * for the feature.
	 * @param on If true, add the "length" attribute (default on).
	 */
	public void setAddEdgeLengths( boolean on )
	{
		addEdgeLengths = on;
	}
	
	public void begin( Graph graph, String filename )
    {
		if( iterator == null )
		{
			this.graph = graph;
			
			try
			{
				Map<String,Serializable> connectParameters = new HashMap<String,Serializable>();
	
				File file = new File( filename );
				
				connectParameters.put( "url", file.toURI().toURL() );
				connectParameters.put( "create spatial index", true );
	
				DataStore dataStore = DataStoreFinder.getDataStore( connectParameters );
				String[]  typeNames = dataStore.getTypeNames();
				String    typeName  = typeNames[0];
	
				FeatureSource<SimpleFeatureType, SimpleFeature>     featureSource;
				FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
	
				featureSource = dataStore.getFeatureSource( typeName );
				collection    = featureSource.getFeatures();
				iterator      = collection.features();
			}
			catch( IOException e )
			{
				throw new RuntimeException( "I/O error : " + e.getMessage() );
			}
		}
    }

	public boolean nextElement()
    {
		if( iterator != null )
		{
			if( iterator.hasNext() )
			{
				addFeature( iterator.next(), mergeOps );
				return true;
			}
		}
		
	    return false;
    }

	public void end()
    {
		if( iterator != null )
		{
			index.reorganize();
			iterator.close();
			iterator = null;
		}
    }
	
	/**
	 * Release any accumulated data from previous begin()/end() cycles.
	 */
	public void release()
	{
		if( iterator == null )
		{
			index         = new SpatialIndex();
			edgeAllocator = 0;
			mergeOps      = null;
			
			System.gc();
		}
	}
	
	/**
	 * Add a feature as a point or polyline and eventually produce nodes and edge equipped with the
	 * attributes of the feature. If the feature shares points with another node, they are merged
	 * so that a topology is created.
	 * @param feature The feature to examine.
	 */
	protected void addFeature( SimpleFeature feature, PointMergeOperations mergeOps )
	{
        if( isPoint( feature ) )
        {
        	addPoint( feature, mergeOps );
        }
        else if( isPolyline( feature ) )
        {
        	addPolyline( feature, mergeOps );
        }
        else
        {
        	// We do not handle polygons.
        	System.err.printf( "Unknown feature type (%s)%n", feature.getType().getGeometryDescriptor().getType().getBinding() );
        }
	}
	
	/**
	 * True if the feature is a point.
	 * @param feature The feature to test.
	 * @return True if the feature is a point shape.
	 */
	protected boolean isPoint( SimpleFeature feature )
	{
		// A better way to do this ?
		return( feature.getType().getGeometryDescriptor().getType().getBinding() == com.vividsolutions.jts.geom.Point.class );
	}
	
	/**
	 * True if the feature is a polyline.
	 * @param feature The feature to test.
	 * @return True if the feature is a polyline shape.
	 */
	protected boolean isPolyline( SimpleFeature feature )
	{
		// A better way to do this ?
		Class<?> binding = feature.getType().getGeometryDescriptor().getType().getBinding();
		
		return( ( binding == com.vividsolutions.jts.geom.MultiLineString.class )
		     || ( binding == com.vividsolutions.jts.geom.LineString.class ) );
	}
	
	/**
	 * Add a point and eventually merge it with another existing point.
	 * @param feature The point feature to add as a (eventually merged) point in the spatial index.
	 * @param mergeOps The merge operations to use if another point is at the same position
	 */
	protected void addPoint( SimpleFeature feature, PointMergeOperations mergeOps )
	{
		mergePoint( feature, 0 );
	}
	
	/**
	 * Add a polyline, and its two end-points and merge these end-points to existing points if
	 * needed. This operation eventually creates nodes and edges in the graph.  
	 * @param feature The polyline feature to add. 
	 * @param mergeOps The merge operations to use if other points are at the same position than the
	 *        two end-points of the polyline.
	 */
	protected void addPolyline( SimpleFeature feature, PointMergeOperations mergeOps )
	{
		Point  point1 = mergePoint( feature,  0 );
		Point  point2 = mergePoint( feature, -1 );
		String id1    = point1.getId().toString();
		String id2    = point2.getId().toString();
		String fid    = feature.getID();
	
		Node node1 = graph.getNode( id1 );
		Node node2 = graph.getNode( id2 );
		Edge edge  = null;
		
		if( mergeDuplicateEdge && node1 != null && node2 != null )
		{
			edge = node1.getEdgeToward( node2.getId() );
			
			if( edge == null )
				edge = node1.getEdgeFrom( node2.getId() );
		}

		if( node1 == null )
		{
			node1 = graph.addNode( id1 );
			addNodeAttributes( node1, fid, point1 );
		}

		if( node2 == null )
		{
			node2 = graph.addNode( id2 );
			addNodeAttributes( node2, fid, point2 );
		}
	
		if( edge == null )
		{
			AttributeSet           attr  = new AttributeSet( feature );
			EdgeDirecter.Direction dir   = edgeDirecter.edgeDirection( point1, point2, attr );
			boolean                isDir = true;

			if( dir == EdgeDirecter.Direction.TO_FROM )
			{
				String id = id2;
				id2 = id1;
				id1 = id;
			}
			else if( dir == EdgeDirecter.Direction.UNDIRECTED )
			{
				isDir = false;
			}
			
			edge = graph.addEdge( fid, id1, id2, isDir );
			
			if( addEdgeLengths ) {
				edge.addAttribute( "length",
						((Geometry)feature.getDefaultGeometry()).getLength() );
			}
		
			mergeAttributes( edge, attr, edgeAttributeFilter, feature );
		}
	}

	/**
	 * Try to see of the new point discovered in a feature can be merged with another.
	 * @param feature The feature for which a point must be added.
	 * @param pointIndex Which coordinate of the feature to use to create the point (if it has
	 *        several coordinates), 0 means the first coordinate, -1 means the last coordinate.
	 * @return The point resulting from the merge, or a new point if no merge occurred.
	 */
	protected Point mergePoint( SimpleFeature feature, int pointIndex )
	{
		if( mergeOps != null && mergeOps.getOperationCount() > 0 )
		{
			// Eventually merge the new point with one or more existing points.
			// This operation may modify the spatial index.
			
			Point point = new Point( feature, pointIndex, pointAttributeFilter ); 
			point = mergeOps.mergePoint( point, index );
			return point;
		}
		else
		{
			// Create a new point.
			
			Point point = new Point( feature, pointIndex, pointAttributeFilter );
			index.add( point );
			return point;
		}
	}
	
	/**
	 * Add the attributes of the given point to the given node and add the "xyz" position attribute.
	 * @param node The node to modify.
	 * @param fid The feature identifier.
	 * @param point The corresponding point.
	 */
	protected void addNodeAttributes( Node node, String fid, Point point )
	{
		HashMap<String,AttributeSet> attributes = point.getAttributes();
		
		float x = point.getPosition().x;
		float y = point.getPosition().y;
		
		node.setAttribute( "xyz", x, y, 0 );
		
		for( String key: attributes.keySet() )
		{
			if( ! key.equals( fid ) )
			{
				HashMap<String,Object> attrs = attributes.get( key );

				for( String k: attrs.keySet() )
				{
					if( nodeAttributeFilter != null )
					{
						if( nodeAttributeFilter.isKept( k ) )
							node.addAttribute( k, attrs.get( k ) );
					}
					else
					{
						node.addAttribute( k, attrs.get( k ) );
					}
				}
			}
		}
	}
	
	/**
	 * Add all the attributes of the given feature to the given edge.
	 * @param element The graph element to change.
	 * @param attr The attribute set to merge in the element.
	 */
	protected void mergeAttributes( Element element, AttributeSet attr, AttributeFilter filter, SimpleFeature feature )
	{
		if( attr.size() > 0 )
		{
			for( String key: attr.keySet() )
			{
				if( filter != null )
				{
					if( filter.isKept( key ) )
						element.setAttribute( key, attr.get( key ) );
				}
				else
				{
					element.setAttribute( key, attr.get( key ) );
				}
			}
		}
		
//		if( addEdgeShapeAttribute && element instanceof Edge )
//			addEdgeShapeStyle( (Edge)element, feature );
	}
	
	/**
	 * Store the coordinates of the edge shape in the style attribute of the edge.
	 * @param edge The edge to modify.
	 * @param feature The feature that gives the coordinates.
	 */
	protected void addEdgeShapeStyle( Edge edge, SimpleFeature feature )
	{
		Coordinate[]  coos  = ((Geometry)feature.getDefaultGeometry()).getCoordinates();
		StringBuilder style = new StringBuilder();

		int n = coos.length - 1;
	
		if( (n-1) > 0 )
		{
			style.append(  "edge-shape:"  );
			
			for( int i=1; i<n; i++ )
			{
				style.append( '(' );
				style.append( coos[i].x );
				style.append( ',' );
				style.append( coos[i].y );
				if( i == (n-1) )
				     style.append( ",0);" );
				else style.append( ",0)," );
			}
			
			edge.addAttribute( "style", style.toString() );
		}
	}
	
	/**
	 * Store the shape of the edge as an attribute in the edge.
	 * @param edge The edge.
	 * @param coos The coordinates.
	 */
	protected void addEdgeShape( Edge edge, Coordinate coos[] )
	{
		int n = coos.length;

		if( n > 2 )
		{
			n--;
			Object xyz[] = new Object[n*3];
			
			for( int i=1; i<n; ++i )
			{
				int j = (i-1)*3;
				
				xyz[j+0] = (float) coos[i].x;
				xyz[j+1] = (float) coos[i].y;
				xyz[j+2] = (float) 0;
			}
			
			edge.setAttribute( "xyz", xyz );
		}
	}
	
	/**
	 * By default all edges will be undirected.
	 */
	public static class DefaultEdgeDirecter implements EdgeDirecter
	{
		public Direction edgeDirection( Point from, Point to, AttributeSet attributes )
        {
	        return Direction.UNDIRECTED;
        }
	}
}