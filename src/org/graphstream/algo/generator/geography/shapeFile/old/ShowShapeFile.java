package org.miv.graphstream.geotools.old;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.media.opengl.GL;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.ServiceInfo;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.miv.glutil.Buffer;
import org.miv.glutil.BufferListener;
import org.miv.glutil.SwingBuffer;
import org.miv.glutil.geom.Cube;
import org.graphstream.io.file.GraphSink;
import org.graphstream.io.file.GraphSinkDGS;
import org.graphstream.ui.geom.Point3;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class ShowShapeFile extends JFrame implements BufferListener, ActionListener, KeyListener,MouseWheelListener
{
// Attributes

    private static final long serialVersionUID = 1L;

    protected SwingBuffer buffer;
	
    protected int steps = 0;
    
    protected Timer timer;
    
	protected float near = 4f;
	
	protected float radius = 27f, theta = 0f, phi = 0f;
	
	protected Point3 camera = new Point3( 0, 0, 0 );
	
	protected Point3 lookAt = new Point3( 0, 4, -15 ) ;
	
	protected Cube env = new Cube( 35500 );
	
	protected Color background = Color.WHITE;

	protected float fogColor[] = { 0.8f, 0.8f, 0.8f, 1.0f };
	
	protected float color1[] = { 0.9f, 0f, 0f, 0.8f };
	
	protected float color2[] = { 0f, 0.9f, 0f, 0.8f };
	
	protected float cubeColor[] = { 0.1f, 0.1f, 0.1f, 0.5f };

	protected float lineWidth = 1f;

	protected boolean autoRotate = false;
	
	protected ArrayList<Road> roads = new ArrayList<Road>();
	
	protected ArrayList<Intersection> inter = new ArrayList<Intersection>();

// Attributes
	
	protected String URL;
	
	protected File input;
	
	protected boolean modePrecision = false;
	
	protected boolean modeIntersect = false;
	
	public static HashSet<String> usedProperties;
	
	static
	{
		usedProperties = new HashSet<String>();
		
		usedProperties.add( "ID_TR_SIGU" );
		usedProperties.add( "ID_VO_DEB" );
		usedProperties.add( "ID_VO_FIN" );
		usedProperties.add( "NIV_AGREG" );
		usedProperties.add( "LIMITE_COM" );
		usedProperties.add( "POSIT_SOL" );		// "au sol"
		usedProperties.add( "HIERARCHIE" );		// "régionale", "locale", "annexe", "principale"
		usedProperties.add( "SENS_UNIQ" );
		usedProperties.add( "SENS_CIRC" );		// "double sens"
		usedProperties.add( "CHAUS_SEPA" );
		usedProperties.add( "NATURE" );			// voie de circulation
		usedProperties.add( "BAND_CYCL" );
		usedProperties.add( "TYPVOIE" );		// "rond-point", "voie piétonne", "voie de circulation", "piste cyclable", "piste cyclable sur troittoir", "contre allée"
		usedProperties.add( "LIANT" );
		usedProperties.add( "LIBVOIE" );
		usedProperties.add( "SHAPE_LEN" );
		usedProperties.add( "ET_ID" );
		usedProperties.add( "CapDebFin" );
		usedProperties.add( "FluxDebFin" );
		usedProperties.add( "RestDebFin" );
		usedProperties.add( "CapFinDeb" );
		usedProperties.add( "FluxFinDeb" );
		usedProperties.add( "RestFinDeb" );
	}
	
// Construct
	
    public static void main( String args[] )
    {
    	try
    	{
    		new ShowShapeFile( args );
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}
    }
    
    public ShowShapeFile( String args[] )
    	throws IOException
    {
    	buffer = new SwingBuffer( this, "Attracteur", 600, 600, Buffer.OutputMode.CANVAS, false );
    	timer  = new Timer( 40, this );
    	input  = promptShapeFile( args );

    	initShapeFile();
    	computeIntersections( 0.5f );
    	timer.start();
    	
    	setSphericCoordinates( radius, theta, phi );   	
    	add( (Component) buffer.getComponent(), BorderLayout.CENTER );
    	addKeyListener( this );
    	((Component)buffer.getComponent()).addKeyListener( this );
    	((Component)buffer.getComponent()).addMouseWheelListener( this );
    	setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    	setSize( new Dimension( 800, 600 ) );
    	setVisible( true );
    }

// Access
	
// Commands
    
	protected File promptShapeFile( String[] args ) throws MalformedURLException
	{
		File file;

		if( args.length == 0 )
		{
			JFileChooser chooser = new JFileChooser();
			
			chooser.setDialogTitle( "Open a shape file" );
			chooser.setFileFilter( new FileFilter() {
				@Override public boolean accept( File f ) { return f.isDirectory() || f.getPath().endsWith("shp") || f.getPath().endsWith("SHP"); }
				@Override public String getDescription() { return "Shapefiles"; }
			} );

			int returnVal = chooser.showOpenDialog( this );

			if( returnVal != JFileChooser.APPROVE_OPTION )
			{
				System.exit( 0 );
			}

			file = chooser.getSelectedFile();
			URL  = file.toURI().toURL().toString();
		}
		else
		{
			URL  = args[0];
			file = new File( args[0] );
		}

		if( ! file.exists() )
		{
			System.err.printf( "cannot find file \"%s\"%n", file.getAbsolutePath() );
			System.exit( 1 );
		}

		return file;
	}
	
	protected void initShapeFile() throws IOException
	{
        Map<String,Serializable> connectParameters = new HashMap<String,Serializable>();

        connectParameters.put( "url", input.toURI().toURL() );
        connectParameters.put( "create spatial index", true );

        DataStore   dataStore = DataStoreFinder.getDataStore( connectParameters );
        ServiceInfo infos     = dataStore.getInfo();
        
        System.out.printf( "Reading shape file `%s':%n", input.toURI().toURL() );
        System.out.printf( "    description : %s%n", infos.getDescription() );
        System.out.printf( "    title :       %s%n", infos.getTitle() );
        
        String[] typeNames = dataStore.getTypeNames();
        String   typeName  = typeNames[0];

        System.out.printf( "    reading content `%s'%n", typeName  );

        FeatureSource<SimpleFeatureType, SimpleFeature>     featureSource;
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection;
        FeatureIterator<SimpleFeature>                      iterator;

        featureSource = dataStore.getFeatureSource( typeName );
        collection    = featureSource.getFeatures();
        iterator      = collection.features();

        try
        {
            while( iterator.hasNext() )
            {
                SimpleFeature feature  = iterator.next();
                Geometry      geometry = (Geometry) feature.getDefaultGeometry();
                Coordinate[]  coos     = geometry.getCoordinates();
                
                roads.add( new Road( coos, feature, geometry ) );
            }
        }
        finally
        {
        	if( iterator != null )
                iterator.close();
        }
        
        System.out.printf( "    OK%n" );
	}
	
	protected void computeIntersections( float offset )
	{
		// Yes, O(n²), and what ? :-)
		
		System.out.printf( "Intersections ...%n" );
		long t1 = System.currentTimeMillis();
		int i = 0;
		
		for( Road road: roads )
		{
			if( i%500 == 0 )
				System.out.printf( "    %03d%%%n", (int)((100f/roads.size())*i) );
			road.intersections( offset, roads, inter );
			i++;
		}
		
		removeIntersectionDoubles( offset );
		
		long t2 = System.currentTimeMillis();
		System.out.printf( "OK (%f seconds)%n", ( t2 - t1 ) / 1000f );
	}
	
	protected void removeIntersectionDoubles( float offset )
	{
		// Yes O(n²) and what ?!?
		
		int n = inter.size();
		int m = n;
		
		System.out.printf( "Removing double intersections :%n" );
		
		for( int i=0; i<n; i++ )
		{
			if( i%500 == 0 )
				System.out.printf( "    %03d%%%n", (int)((100f/n)*i) );

			if( inter.get( i ) != null )
			{
				Intersection i1 = inter.get( i );
				
				for( int j=i+1; j<n; j++ )
				{
					if( inter.get( j ) != null )
					{
						Intersection i2 = inter.get( j );
						
						if( alignedOn( i1.position, i2.position, offset ) )
						{
							// Merge i2 into i1.

							i2.references( i1 );
							
							// Remove i2.
							
							m--;
							inter.set( j, null );
						}
					}
				}
			}
		}
		
		System.out.printf( "    Removed %d double intersections.%n", (n-m) );
		
		ArrayList<Intersection> inter2 = new ArrayList<Intersection>();
		
		for( Intersection i: inter )
		{
			if( i != null )
				inter2.add( i );
		}
		
		inter.clear();
		inter = inter2;
		
		// Add Missing intersections
		
		for( Road road: roads )
		{
			if( road.point0IsInter == null )
			{
				Intersection i = new Intersection( road.points[0], road ); 
				inter.add( i );
				road.point0IsInter = i;
			}
			if( road.point1IsInter == null )
			{
				Intersection i = new Intersection( road.points[road.points.length-1], road ); 
				inter.add( i );
				road.point1IsInter = i;
			}
			
			if( road.point0IsInter.ref != null ) road.point0IsInter = road.point0IsInter.ref;
			if( road.point1IsInter.ref != null ) road.point1IsInter = road.point1IsInter.ref;
		}

		System.out.printf( "    There are %d intersections and %d roads.%n", inter.size(), roads.size() );
	}

	protected HashMap<String,Object> attrs = new HashMap<String,Object>();
	
	public void outputGraph() throws IOException
	{
		System.out.printf( "Output of the graph ...%n" );
		long t1 = System.currentTimeMillis();
		
		GraphWriter out = new GraphWriterDGS();
		
		int    pos    = URL.lastIndexOf( '.' );
		String output = URL;
		
		if( pos > 0 ) output = output.substring( 0, pos );
		
		pos = URL.lastIndexOf( '/' );
		
		if( pos >= 0 ) output = output.substring( pos + 1 );
		
		out.begin( String.format( "%s.dgs", output ), output );
		
		for( Intersection i: inter )
		{
			attrs.clear();
			attrs.put( "x", i.position.x );
			attrs.put( "y", i.position.y );
			out.addNode( i.id, attrs );
		}
		
		int idgen = 0;
		
		for( Road road: roads )
		{
			String id0 = road.point0IsInter.id;
			String id1 = road.point1IsInter.id;

			out.addEdge( String.format( "%s_%s_%d", id0, id1, idgen++ ),
				id0,
				id1,
				false,
				road.properties );
		}
		
		out.end();
		
		long t2 = System.currentTimeMillis();
		System.out.printf( " OK -> %s.dgs (%f seconds)%n", output, (t2-t1)/1000f );
	}
    
// Command
    
    public void display()
    {
    	buffer.display();
/*    	
    	if( autoRotate )
    	{
    		theta += 0.001f;
    		
    		if( theta > (2*Math.PI) )
    			theta = 0;
    		
			setSphericCoordinates( radius, theta, phi );	    		
    	}
*/    }
    
	float maxx = Float.MIN_VALUE, maxy = Float.MIN_VALUE, maxz = Float.MIN_VALUE;
	float minx = Float.MAX_VALUE, miny = Float.MAX_VALUE, minz = Float.MAX_VALUE;

    protected void computeInitialRadius()
    {
    	maxx = Float.MIN_VALUE; maxy = Float.MIN_VALUE; maxz = Float.MIN_VALUE;
    	minx = Float.MAX_VALUE; miny = Float.MAX_VALUE; minz = Float.MAX_VALUE;
    	
    	for( Road road: roads )
    	{
    		if( road.x0 < minx ) minx = road.x0;
    		if( road.y0 < miny ) miny = road.y0;
    		if( road.z0 < minz ) minz = road.z0;
    		if( road.x1 < minx ) minx = road.x1;
    		if( road.y1 < miny ) miny = road.y1;
    		if( road.z1 < minz ) minz = road.z1;

    		if( road.x0 > maxx ) maxx = road.x0;
    		if( road.y0 > maxy ) maxy = road.y0;
    		if( road.z0 > maxz ) maxz = road.z0;
    		if( road.x1 > maxx ) maxx = road.x1;
    		if( road.y1 > maxy ) maxy = road.y1;
    		if( road.z1 > maxz ) maxz = road.z1;
    	}
    	
    	//System.err.printf( "min( %f %f %f )  max( %f %f %f )%n", minx, miny, minz, maxx, maxy, maxz );
    	
    	float dx = maxx - minx;
    	float dy = maxy - miny;
    	float dz = maxz - minz;
    	
    	lookAt.x = minx + ( dx / 2 );
    	lookAt.y = miny + ( dy / 2 );
    	lookAt.z = minz + ( dz / 2 );
    	
    	float diag = (float)Math.sqrt( dx*dx + dy*dy + dz*dz ); 
    	
    	radius = diag / 1.7f;
    	theta  = lookAt.x; 
    	phi    = lookAt.y;
    	
    	setSphericCoordinates( radius, theta, phi );
    }
    
    protected void pushPerspView()
    {
    	GL  gl  = buffer.getGl();
//    	GLU glu = buffer.getGlu();
    	
    	float ratio = ((float)buffer.getHeight()/(float)buffer.getWidth());  
    	
    	gl.glClear( GL.GL_COLOR_BUFFER_BIT );
    	gl.glPushMatrix();
    	gl.glMatrixMode( GL.GL_PROJECTION );
    	gl.glLoadIdentity();
    	gl.glFrustum( -near, near, -near*ratio, near*ratio, near, 1000000 );
    	gl.glMatrixMode( GL.GL_MODELVIEW );
    	gl.glLoadIdentity();
    	/*
    	glu.gluLookAt(
    		camera.x, camera.y, camera.z,
    		lookAt.x, lookAt.y, lookAt.z, 0, 1, 0 );
    	*/
    	gl.glTranslatef( -lookAt.x, -lookAt.y, -radius );
    }
    
    protected void popPerspView()
    {
    	GL gl = buffer.getGl();
    	
    	gl.glPopMatrix();
    }
    
    protected void pushPixelView()
    {
    	GL gl = buffer.getGl();

    	int w = getWidth();
    	int h = getHeight();
    	
    	gl.glPushMatrix();
    	gl.glMatrixMode( GL.GL_PROJECTION );
    	gl.glLoadIdentity();
    	gl.glMatrixMode( GL.GL_MODELVIEW );
    	gl.glLoadIdentity();
    	gl.glOrtho( 0, w, 0, h, -10, 10 );
    }
    
    protected void popPixelView()
    {
    	GL gl = buffer.getGl();
    	
    	gl.glPopMatrix();
    }

    protected void displayEnv()
    {
    	GL gl = buffer.getGl();
    	
    	gl.glPolygonMode( GL.GL_FRONT_AND_BACK, GL.GL_LINE );
    	gl.glColor4f( cubeColor[0], cubeColor[1], cubeColor[2], cubeColor[3] );
		gl.glEnable( GL.GL_LINE_SMOOTH );
		
		
		gl.glColor3f( 1, 0, 0 );
		gl.glBegin( GL.GL_LINE_LOOP );
			gl.glVertex3f( minx, miny, 0 );
			gl.glVertex3f( maxx, miny, 0 );
			gl.glVertex3f( maxx, maxy, 0 );
			gl.glVertex3f( minx, maxy, 0 );
		gl.glEnd();
		
/*		gl.glPushMatrix();
		gl.glTranslatef( lookAt.x, lookAt.y, lookAt.z );
		gl.glLineWidth( 1 );
    	env.display( gl );
   		gl.glPopMatrix();
*/		gl.glDisable( GL.GL_LINE_SMOOTH );
    }
    
    protected void displayMap()
    {
    	GL gl = buffer.getGl();
		
    	gl.glEnable( GL.GL_LINE_SMOOTH );
		gl.glLineWidth( lineWidth );
    	
    	if( modePrecision == false )
    	{
    		gl.glBegin( GL.GL_LINES );
    			gl.glColor3f( 0, 0, 0 );
    			for( Road road: roads )
    			{
    				int n = road.points.length;
    				
    				gl.glVertex3f( road.points[0].x,   road.points[0].y,   road.points[0].z );
    				gl.glVertex3f( road.points[n-1].x, road.points[n-1].y, road.points[n-1].z );
    			}
    		gl.glEnd();
    	}
    	else
    	{
			for( Road road: roads )
			{
				gl.glBegin( GL.GL_LINE_STRIP );
    			gl.glColor3f( 0, 0, 0 );
				for( int i=0; i<road.points.length; i++ )
					gl.glVertex3f( road.points[i].x, road.points[i].y, road.points[i].z );
				gl.glEnd();
			}
			
			if( modeIntersect )
			{
				for( Road road: roads )
				{
					gl.glPointSize( 2 );
					gl.glBegin( GL.GL_POINTS );
	    			gl.glColor4f( 0, 1, 0, 0.5f );
					for( int i=0; i<road.points.length; i++ )
						gl.glVertex3f( road.points[i].x, road.points[i].y, road.points[i].z );
					gl.glEnd();
				}
			}
    	}

    	if( modeIntersect )
    	{
	    	gl.glPointSize( 4 );
	    	gl.glEnable( GL.GL_POINT_SMOOTH );
	    	gl.glBegin( GL.GL_POINTS );
	    	gl.glColor3f( 1, 0, 0 );
	    	for( Intersection i: inter )
	    	{
	    		Point3 p = i.position;
	    		gl.glVertex3f( p.x, p.y, p.z );
	    	}
	    	gl.glEnd();
    	}
    }    
    
   	public void setSphericCoordinates( float rayon, double theta, double phi )
	{
   		// Conversion in degrees if needed :
   		//		theta = (float) ( theta * (Math.PI/180f) );
   		//		phi   = (float) ( phi   * (Math.PI/180f) );
		
		camera.x = lookAt.x + (float) ( Math.cos( phi ) * Math.sin( theta ) * rayon ); 
		camera.y = lookAt.y + (float) ( Math.sin( phi ) * rayon ); 
		camera.z = lookAt.z + (float) ( Math.cos( phi ) * Math.cos( theta ) * rayon ); 
	}
    
// Buffer listener
    
	public void close( Buffer buffer )
    {
    }

	public void display( Buffer buffer )
    {
    	pushPerspView();
    	displayEnv();
    	displayMap();
    	popPerspView();
//    	pushPixelView();
//    	displayXT();
//    	popPixelView();
    }

	public void init( Buffer buffer )
    {
		GL gl = buffer.getGl();
		
		gl.glClearColor( background.getRed()/255f, background.getGreen()/255f, background.getBlue()/255f, 0 );
		gl.glClearDepth( 255f );
		gl.glEnable( GL.GL_BLEND );
		gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
		gl.glEnable( GL.GL_POINT_SMOOTH );
/*		
		gl.glEnable( GL.GL_FOG );

		gl.glFogi( GL.GL_FOG_MODE, GL.GL_LINEAR );
		gl.glFogfv( GL.GL_FOG_COLOR, fogColor, 0 );
		gl.glFogf( GL.GL_FOG_DENSITY, 0.35f );
		gl.glHint( GL.GL_FOG_HINT, GL.GL_DONT_CARE );
		gl.glFogf( GL.GL_FOG_START, radius + 1 );
		gl.glFogf( GL.GL_FOG_END, radius + 10 );
*/
		computeInitialRadius();		
    }

	public void key( Buffer buffer, int key, char unicode, boolean pressed )
    {
    }

	public void keyTyped( Buffer buffer, char unicode, int modifiers )
    {
    }

	public void mouse( Buffer buffer, int x, int y, int button )
    {
    }

	public void reshape( Buffer buffer, int x, int y, int width, int height )
    {
    }

	public void actionPerformed( ActionEvent e )
    {
		display();
    }
	
	public void keyPressed( KeyEvent e )
    {
		if( e.getKeyCode() == KeyEvent.VK_RIGHT )
		{
			lookAt.x += 100f;
			setSphericCoordinates( radius, theta, phi );
		}
		else if( e.getKeyCode() == KeyEvent.VK_LEFT )
		{
			lookAt.x -= 100f;
			setSphericCoordinates( radius, theta, phi );
		}
		if( e.getKeyCode() == KeyEvent.VK_UP )
		{
			lookAt.y += 100f;
			setSphericCoordinates( radius, theta, phi );
		}
		else if( e.getKeyCode() == KeyEvent.VK_DOWN )
		{
			lookAt.y -= 100f;
			setSphericCoordinates( radius, theta, phi );
		}
		else if( e.getKeyCode() == KeyEvent.VK_PAGE_UP )
		{
			radius -= 100f;
			setSphericCoordinates( radius, theta, phi );
		}
		else if( e.getKeyCode() == KeyEvent.VK_PAGE_DOWN )
		{
			radius += 100f;
			setSphericCoordinates( radius, theta, phi );
		}
		else if( e.getKeyCode() == KeyEvent.VK_R )
		{
			computeInitialRadius();
		}
		else if( e.getKeyCode() == KeyEvent.VK_P )
		{
			modePrecision = ! modePrecision;
		}
		else if( e.getKeyCode() == KeyEvent.VK_I )
		{
			modeIntersect = ! modeIntersect;
		}
		else if( e.getKeyCode() == KeyEvent.VK_Q )
		{
			System.exit( 0 );
		}
		else if( e.getKeyCode() == KeyEvent.VK_O )
		{
			try
            {
	            outputGraph();
            }
            catch( IOException e1 )
            {
	            e1.printStackTrace();
            }
		}
    }

	public void keyReleased( KeyEvent e )
    {
    }

	public void keyTyped( KeyEvent e )
    {
	    
    }

	public void mouseWheelMoved( MouseWheelEvent e )
    {
		float s = e.getWheelRotation();
		
		//theta += (0.1f * s);
		radius += ( 0.1 * s );
		setSphericCoordinates( radius, theta, phi );
    }
	
	public boolean alignedOn( Point3 src, Point3 trg, float offset )
	{
		float dx = Math.abs( src.x - trg.x );
		float dy = Math.abs( src.y - trg.y );
		
		return( dx <= offset && dy <= offset  );
	}
	
// Nested classes
	
	class Road
	{
		public float x0, y0, z0;
		
		public float x1, y1, z1;
		
		public Point3 points[];
		
		public HashMap<String,Object> properties;
		
		public Intersection point0IsInter = null;

		public Intersection point1IsInter = null;
		
		public Road( Coordinate coos[], SimpleFeature feature, Geometry geometry )
		{
			int n = coos.length;
			
            x0 = (float) coos[0].x;
            y0 = (float) coos[0].y;
            z0 = 0f;//(float) coos[0].z;
            x1 = (float) coos[n-1].x;
            y1 = (float) coos[n-1].y;
            z1 = 0f;//(float) coos[n-1].z;
            
            float tmp;
            
            if( x0 > x1 ) { tmp=x0; x0=x1; x1=tmp; }
            if( y0 > y1 ) { tmp=y0; y0=y1; y1=tmp; }
            if( z0 > z1 ) { tmp=z0; z0=z1; z1=tmp; }
            
            points = new Point3[n];
            
            for( int i=0; i<n; i++ )
            	points[i] = new Point3( (float)coos[i].x, (float)coos[i].y, 0f/*(float)coos[i].z*/ );

            readProperties( feature );
		}
		
		protected void readProperties( SimpleFeature feature )
		{
			Collection<Property> props = feature.getProperties();
			
			if( props.size() > 0 )
			{
				properties = new HashMap<String,Object>();
			
//				System.err.printf( "Feature :%n" );
				
				for( Property p: props )
				{
				//	if( usedProperties.contains( p.getName().toString() ) )
					{
//						System.err.printf( "    [%s] \t = %s%n", p.getName(), p.getValue() );
						properties.put( p.getName().toString(), p.getValue() );
					}
				}
			}
		}
		
		public float getLength()
		{
			return points[0].distance( points[points.length-1] );
		}
		
		public void intersections( float offset, ArrayList<Road> roads, ArrayList<Intersection> inter )
		{
			for( Road other: roads )
			{
				if( other != this )
				{
					if( other.bboxIntersects( this, offset ) )
					{
						/*
						 * Compute the intersections only on end-points.
						 */
						if( alignedOn( points[0], other.points[0], offset ) )
						{
							Intersection i = new Intersection( points[0], other.points[0], this, other ); 
							inter.add( i );
							point0IsInter = i;
							other.point0IsInter = i;
						}
						
						if( alignedOn( points[0], other.points[other.points.length-1], offset ) )
						{
							Intersection i = new Intersection( points[0], other.points[other.points.length-1], this, other );
							inter.add( i );
							point0IsInter = i;
							other.point1IsInter = i;
						}
						
						if( alignedOn( points[points.length-1], other.points[0], offset ) )
						{
							Intersection i = new Intersection( points[points.length-1], other.points[0], this, other ); 
							inter.add( i );
							point1IsInter = i;
							other.point0IsInter = i;
						}
						
						if( alignedOn( points[points.length-1], other.points[other.points.length-1], 0 ) )
						{
							Intersection i = new  Intersection( points[points.length-1], other.points[other.points.length-1], this, other ); 
							inter.add( i );
							point1IsInter = i;
							other.point1IsInter = i;
						}
						/*
						 * Compute the intersections on all segments.
						 * 
						for( Point3 pSrc: points )
						{
							for( Point3 pTrg: other.points )
							{
								if( alignedOn( pSrc, pTrg, offset ) )
								{
									// TODO check some intersection points are not the same !
									
									inter.add( new Intersection( pSrc, pTrg, this, other ) );
								}
							}
						}
						*/
					}
				}
			}
		}
		
		public boolean bboxIntersects( Road other, float offset )
		{
			//        |-------|
			// |---|  |       |  |---|
			//      |-++|     | 
			//        | |+++| |
			//        |     |++-|
			//        |       |
			//        |-------|
			
			if( other.x1+offset < x0-offset || other.x0+offset > x1-offset
			 || other.y1+offset < y0-offset || other.y0+offset > y1-offset )
				return false;
			
			return true;
		}
	}
	
	public static int idGenerator = 0;

	class Intersection
	{
		public String id;
		
		public Point3 position;
		
		public HashSet<Road> roads;
		
		public Intersection ref = null;
		
		public Intersection( Point3 p, Road r )
		{
			id = String.format( "%d", idGenerator++ );
			position = new Point3( p );
			roads = new HashSet<Road>();
			roads.add( r );
		}
		
		public Intersection( Point3 src, Point3 trg, Road srcR, Road trgR )
		{
			id = String.format( "%d", idGenerator++ );
			position = new Point3( 
					src.x + ( ( src.x - trg.x ) / 2 ),
					src.y + ( ( src.y - trg.y ) / 2 ),
					0
				);
			roads = new HashSet<Road>();
			roads.add( srcR );
			roads.add( trgR );
		}
		
		public void references( Intersection other )
		{
			other.roads.addAll( roads );
			roads.clear();
			position = null;
			id = null;
			ref = other;
		}
	}
}