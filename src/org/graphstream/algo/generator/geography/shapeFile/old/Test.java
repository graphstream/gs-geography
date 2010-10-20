package org.miv.graphstream.geotools.old;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.ServiceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Geometry;

public class Test
{
	public static void main( String[] args )
		throws Exception
	{
	    File file = promptShapeFile( args );

	    try
	    {
	    	// Connection parameters.
	    	
	        Map<String,Serializable> connectParameters =
	        	new HashMap<String,Serializable>();

	        connectParameters.put( "url", file.toURI().toURL() );
	        connectParameters.put( "create spatial index", true );
	        DataStore dataStore = DataStoreFinder.getDataStore( connectParameters );

	        // We are now connected.

	        ServiceInfo info = dataStore.getInfo();
	        
	        System.out.printf( "Info: %n" );
	        System.out.printf( "    description : %s%n", info.getDescription() );
	        System.out.printf( "    title :       %s%n", info.getTitle() );
	        
	        String[] typeNames = dataStore.getTypeNames();
	        String typeName = typeNames[0];

	        System.out.printf( "Reading content `%s'%n", typeName  );

	        FeatureSource<SimpleFeatureType, SimpleFeature>     featureSource;
	        FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
	        FeatureIterator<SimpleFeature>                      iterator;

	        featureSource = dataStore.getFeatureSource( typeName );
	        collection    = featureSource.getFeatures();
	        iterator      = collection.features();

	        double totalLength = 0;

	        try
	        {
	            while( iterator.hasNext() )
	            {
	                SimpleFeature feature = iterator.next();

	                Geometry geometry = (Geometry) feature.getDefaultGeometry();
	                
//	                Coordinate[] coos = geometry.getCoordinates();
	                
	                totalLength += geometry.getLength();
	                
	                printFeature( feature );
	            }
	        }
	        finally
	        {
	        	if( iterator != null )
	                iterator.close();
	        }

	        System.out.println( "Total Length " + totalLength );

	    }
	    catch( Exception ex )
	    {
	        ex.printStackTrace();
	        System.exit( 1 );
	    }

	    System.exit( 0 );
	}
	
	protected static void printFeature( SimpleFeature feature )
	{
		System.out.printf( "Feature %s:%n", feature.getID() );
		Collection<Property> props = feature.getProperties();
		
		for( Property p: props )
		{
			System.out.printf( "    %s = %s%n", p.getName().toString(), p.getValue() );
		}
	}

	private static File promptShapeFile(String[] args)
			throws FileNotFoundException {
		File file;
		if (args.length == 0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setDialogTitle("Open Shapefile for Reprojection");
			chooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory() || f.getPath().endsWith("shp")
							|| f.getPath().endsWith("SHP");
				}

				@Override
				public String getDescription() {
					return "Shapefiles";
				}
			});
			int returnVal = chooser.showOpenDialog(null);

			if (returnVal != JFileChooser.APPROVE_OPTION) {
				System.exit(0);
			}
			file = chooser.getSelectedFile();

			System.out.println("You chose to open this file: " + file.getName());
		} else {
			file = new File(args[0]);
		}
		if (!file.exists()) {
			throw new FileNotFoundException(file.getAbsolutePath());
		}
		return file;
	}
}