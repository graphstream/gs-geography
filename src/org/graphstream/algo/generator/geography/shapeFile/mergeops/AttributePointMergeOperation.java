package org.graphstream.algo.generator.geography.shapeFile.mergeops;

import org.graphstream.algo.generator.geography.shapeFile.Point;

public class AttributePointMergeOperation extends PointMergeOperation
{
	public String attributeMatch;
	
	public AttributePointMergeOperation( String attribute, PointMergeOperation.PointOperation mergeOperation )
	{
		super( mergeOperation );
		this.attributeMatch = attribute;
	}

	@Override
	public boolean matches( Point oldOne, Point newOne )
	{
		return attributeMatches( attributeMatch, oldOne, newOne );
	}
}