package org.graphstream.algo.generator.geography.shapeFile;

import java.util.Collection;
import java.util.HashMap;

import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Represents a set of attributes for a feature.
 * 
 * A point can contain several such sets, one for each feature it aggregates.
 */
public class AttributeSet extends HashMap<String,Object>
{
    private static final long serialVersionUID = 1L;

    /**
     * Create the set of attributes from a given feature.
     * @param feature The feature to explore.
     */
    public AttributeSet( SimpleFeature feature )
    {
		Collection<Property> props = feature.getProperties();
		
		if( props.size() > 0 )
		{
			for( Property p: props )
				put( p.getName().toString(), p.getValue() );
		}
    }
    
    /**
     * Create the set of attributes from a given feature and the given filter.
     * @param feature The feature to explore.
     * @param filter Filter witch attribute to keep.
     */
    public AttributeSet( SimpleFeature feature, AttributeFilter filter )
    {
    	Collection<Property> props = feature.getProperties();
    	
    	if( props.size() > 0 )
    	{
    		for( Property p: props )
    		{
    			String key = p.getName().toString();
    			
    			if( filter != null )
    			{
    				if( filter.isKept( key ) )
    					put( key, p.getValue() );
    			}
    			else
    			{
    				put( key, p.getValue() );
    			}
    		}
    	}
    }
    
    /**
     * True if the attribute has a value in this set.
     * @param attribute The attribute to test.
     * @return False if there is no value for this attribute in this set.
     */
    public boolean hasAttribute( String attribute )
    {
    	return( get( attribute ) != null );
    }
    
    /**
     * If the searched attribute is a number returns its value. If the attribute is not present or
     * is not a number, return 0. 
     * @param attribute The attribute to test.
     * @return The value of the attribute, be careful, returns 0 if the attribute does not exist or
     *  is not a number.
     */
    public double getNumericAttribute( String attribute )
    {
    	Object value = get( attribute );
    	
    	if( value != null && value instanceof Number )
    		return ((Number)value).doubleValue();
    	
    	return 0;
    }
    
    /**
     * If the searched attribute is a string, returns its value, else return null.
     * @param attribute The attribute to search.
     * @return The string value if any, else null.
     */
    public String getStringAttribute( String attribute )
    {
    	Object value = get( attribute );
    	
    	if( value instanceof String )
    		return (String) value;
    	
    	return null;
    }
}