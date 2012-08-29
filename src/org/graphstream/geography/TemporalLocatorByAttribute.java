package org.graphstream.geography;

public class TemporalLocatorByAttribute extends TemporalLocator {

	/**
	 * 
	 */
	protected String attributeKey;
	
	/**
	 * 
	 * @param source
	 * @param attributeKey
	 */
	public TemporalLocatorByAttribute(GeoSource source, String attributeKey) {
		super(source);
		
		this.attributeKey = attributeKey;
	}
	
	@Override
	public Integer date(Object o) {
		
		String dateString = this.source.getAggregator().getAttributeValue(o, this.attributeKey).toString();
		
		if(dateString == null)
			return null;

		return Integer.parseInt(dateString);
	}

}
