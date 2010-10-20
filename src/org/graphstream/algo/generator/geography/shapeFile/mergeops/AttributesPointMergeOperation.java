package org.graphstream.algo.generator.geography.shapeFile.mergeops;

import java.util.ArrayList;
import java.util.Collection;

import org.graphstream.algo.generator.geography.shapeFile.Point;

public class AttributesPointMergeOperation extends PointMergeOperation
{
	public ArrayList<String> attributesMatch = new ArrayList<String>();
	
	public AttributesPointMergeOperation( Collection<String> attributes, PointMergeOperation.PointOperation mergeOperation )
	{
		super( mergeOperation );
		this.attributesMatch.addAll( attributes );
	}
	
	@Override
	public boolean matches( Point oldOne, Point newOne )
	{
		for( String attribute: attributesMatch )
		{
			if( attributeMatches( attribute, oldOne, newOne ) )
				return true;
		}
		
		return false;
	}	
}