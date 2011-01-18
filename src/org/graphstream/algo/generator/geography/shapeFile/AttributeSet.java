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