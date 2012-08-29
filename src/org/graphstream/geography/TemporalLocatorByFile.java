package org.graphstream.geography;

public class TemporalLocatorByFile extends TemporalLocator {

	public TemporalLocatorByFile(GeoSource source) {
		super(source);
	}
	
	@Override
	public Integer date(Object o) {
	
		String currentFileName = this.source.getAggregator().getCurrentFileName();
		
		int fileIndex = this.source.getFileNames().indexOf(currentFileName);
		
		return fileIndex;
	}

}
