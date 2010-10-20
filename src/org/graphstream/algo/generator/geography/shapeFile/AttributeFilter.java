package org.graphstream.algo.generator.geography.shapeFile;

import java.util.HashSet;

/**
 * Specify which attributes are kept or filtered when importing or exporting data.
 * 
 * @author Antoine Dutot
 */
public class AttributeFilter
{
// Attribute
	
	/**
	 * Keep or filter the specified attributes ?.
	 */
	public static enum Mode { KEEP, FILTER };
	
	/**
	 * The filtering mode.
	 */
	protected Mode mode;
	
	/**
	 * The set of attributes to keep or filter.
	 */
	protected HashSet<String> attributes = new HashSet<String>();
	
// Constructor
	
	/**
	 * New filter with the given mode.
	 * @param keep If true, the set represent the set of attributes to preserve and store, else
	 * this set is the set of attributes to filter and ignore.
	 */
	public AttributeFilter( boolean keep )
	{
		if( keep )
		     mode = Mode.KEEP;
		else mode = Mode.FILTER;
	}
	
	/**
	 * New filter with the given mode.
	 * @param mode The mode (one of Mode.KEEP or Mode.FILTER). 
	 */
	public AttributeFilter( Mode mode )
	{
		this.mode = mode;
	}
	
// Access
	
	/**
	 * True if the attribute must not be considered.
	 * @param attribute The attribute to test.
	 * @return False if the attribute must be stored.
	 */
	public boolean isFiltered( String attribute )
	{
		if( mode == Mode.KEEP )
		     return( ! attributes.contains( attribute ) );
		else return(   attributes.contains( attribute ) );
	}
	
	/**
	 * True if the attribute must not be considered.
	 * @param attribute The attribute to test.
	 * @return False if the attribute must be stored.
	 */
	public boolean isKept( String attribute )
	{
		if( mode == Mode.KEEP )
		     return(   attributes.contains( attribute ) );
		else return( ! attributes.contains( attribute ) );
	}

	/**
	 * True if the set of attributes defines the attributes to conserve and store.
	 * @return False if the set of attributes define what must be ignored.
	 */
	public boolean isKeepMode()
	{
		return( mode == Mode.KEEP );
	}

	/**
	 * True if the set of attributes defines the attributes to filter.
	 * @return False if the set of attributes define what must be kept.
	 */
	public boolean isFilterMode()
	{
		return( mode == Mode.FILTER );
	}
	
// Command

	/**
	 * Add an attribute in the set of kept or filtered attributes.
	 * @param attribute The attribute to add to the set.
	 */
	public void add( String attribute )
	{
		attributes.add( attribute );
	}
	
	/**
	 * Remove and attribute of the set of kept or filtered attributes.
	 * @param attribute The attribute to remove from the set.
	 */
	public void remove( String attribute )
	{
		attributes.remove( attribute );
	}
}