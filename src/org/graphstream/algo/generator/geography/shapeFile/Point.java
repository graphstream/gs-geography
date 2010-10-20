package org.graphstream.algo.generator.geography.shapeFile;

import java.util.ArrayList;
import java.util.HashMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.miv.pherd.Particle;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * A point pertaining to one or more features.
 * 
 * As the main purpose of this code is to fusion points so that several features will share the
 * intersection points, a point contains a location and potentially several sets of attributes, one
 * for each feature it aggregates.
 */
public class Point extends Particle
{
// Attribute
	
	/**
	 * The offset at which a location is considered aligned with this particle.
	 */
	public static final float OFFSET = 0.001f;
	
	/**
	 * Various sets of attributes. There is only one set if this point represents only one feature
	 * or part of one feature. As soon as a point as been merged, there may be several attributes
	 * in this list. The attribute sets are indexed by the feature id they come from.
	 */
	public HashMap<String,AttributeSet> attributes = new HashMap<String,AttributeSet>();
	
	/**
	 * Set of edges connected to this point.
	 */
	public ArrayList<Edge> polylines = new ArrayList<Edge>();
	
	/**
	 * The graph node assigned to this point, if any.
	 */
	public Node node;
	
// Construction
	
	/**
	 * New intersection at (x,y).
	 * @param id The node identifier.
	 * @param x The abscissa. 
	 * @param y The ordinate.
	 */
	public Point( Object id, float x, float y )
	{
		super( id, x, y, 0 );
	}
	
	/**
	 * New point from the given feature, using as coordinates the first point of the feature if it
	 * contains several ones. The attributes of the feature are put in an attribute set of the
	 * point.
	 * @param feature The feature to represent.
	 */
	public Point( SimpleFeature feature )
	{
		this( feature, 0 );
	}
	
	/**
	 * Like {@link #Point(SimpleFeature)} but specify with pair of coordinates of the feature to use
	 * if it has several ones.
	 * @param feature The feature to represent.
	 * @param cooIndex The index of the pair of coordinates to use for this point location.
	 */
	public Point( SimpleFeature feature, int cooIndex )
	{
		this( feature, cooIndex, null );
	}
	
	/**
	 * Like {@link #Point(SimpleFeature,int)} but specify a filter for attributes of the feature
	 * to store in the point.
	 * @param feature The feature to represent.
	 * @param cooIndex The index of the pair of coordinates to use for this point location.
	 * @param filter The attribute filter to select witch feature attribute to store in the point.  
	 */
	public Point( SimpleFeature feature, int cooIndex, AttributeFilter filter )
	{
		super( getFeatureId( feature, cooIndex ) );
		
		Coordinate[] coos = ((Geometry)feature.getDefaultGeometry()).getCoordinates();
		
		if( cooIndex < 0 )
			cooIndex = ( coos.length + cooIndex );
	
		initPos( (float)coos[cooIndex].x, (float)coos[cooIndex].y, 0 );
		attributes.put( getId().toString(), new AttributeSet( feature, filter ) );
		moved = true;
	}
	
// Access
	
	protected static String getFeatureId( SimpleFeature feature, int cooIndex )
	{
		String idx = "";
		
		if( cooIndex < 0 )
		{
			idx = "_";
			cooIndex = -cooIndex;
		}
		
		return String.format( "%s_%s%d", feature.getID(), idx, cooIndex  );
	}
	
	/**
	 * The collection of attribute sets. Each set of attributes is indexed by the id of the
	 * feature they come from if this point aggregates several feature.
	 * @return The set of attribute sets.
	 */
	public HashMap<String,AttributeSet> getAttributes()
	{
		return attributes;
	}
	
	/**
	 * True if one of the attribute sets contains the given attribute.
	 * @param attribute The attribute to test.
	 * @return False if no attribute set contains the attribute.
	 */
	public boolean hasAttribute( String attribute )
	{
		for( AttributeSet attribs: attributes.values() )
		{
			if( attribs.containsKey( attribute ) )
				return true;
		}
		
		return false;
	}
	
	/**
	 * Return all the values for the given attribute.
	 * @param name The attribute name.
	 * @return The set of values for the given attribute.
	 */
	public ArrayList<Object> getValuesForAttribute( String name )
	{
		ArrayList<Object> values = new ArrayList<Object>();
		
		for( AttributeSet attribs: attributes.values() )
		{
			Object o = attribs.get( name );
			
			if( o != null )
				values.add( o );
		}
		
		return values;
	}
	
	/**
	 * True if the given point (x,y) is considered aligned with this particle.
	 * @param x The point abscissa.
	 * @param y The point ordinate.
	 * @return True if aligned with (x,y).
	 */
	public boolean isAt( float x, float y )
	{
		float dx = pos.x - x;
		float dy = pos.y - y;
		
		if( dx < 0 ) dx = -dx;
		if( dy < 0 ) dy = -dy;
		
		if( dx < OFFSET && dy < OFFSET )
			return true;
		
		return false;
	}
	
	/**
	 * The graph node assigned to this point if any.
	 * @return The node representing this point in the graph or null.
	 */
	public Node getNode()
	{
		return node;
	}

// Command
	
	@Override
    public void move( int time )
    {
	    // Nop, features do not move.
    }	
	
	/**
	 * Merge the attributes of the other point with this one.
	 * @param other The point to merge to this one.
	 */
	public void merge( Point other )
	{
		if( other != this )
		{
			for( String key: other.attributes.keySet() )
			{
/*				AttributeSet old = */attributes.put( key, other.attributes.get( key ) );
//				if( old != null )
//					System.err.printf( "While merging attributes : the attribute set for %s already exists!!%n", key );
			}
		}
	}
	
	public void bindToPolyLine( Edge edge )
	{
		polylines.add( edge );
	}
	
	/**
	 * Assign a node to this point.
	 * @param node The node to add.
	 */
	public void bindToNode( Node node )
	{
		assert this.node == null : "double node binding on a point";
		
		if( this.node == null )
		{
			this.node = node;
		}
	}

	@Override
    public void inserted()
    {
	    // TODO Auto-generated method stub
	    
    }

	@Override
    public void removed()
    {
	    // TODO Auto-generated method stub
	    
    }
}