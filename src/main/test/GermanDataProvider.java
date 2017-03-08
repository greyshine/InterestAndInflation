import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.greyshine.spielwiese.interestAndInflation.InterestAndInflation.InflationProvider;

public class GermanDataProvider implements InflationProvider {

	private static final Map<Integer,Double> values = new HashMap<>();
	
	static {
	
		try {
		
			BufferedReader br = new BufferedReader( new InputStreamReader( Thread.currentThread().getContextClassLoader().getResourceAsStream( "germanInflationData.txt" ) , "UTF-8") );
			
			while( br.ready() ) {
				
				final String l = br.readLine().trim();
				if ( l.startsWith( "#" )|| l.isEmpty() ) { continue; }
				
				values.put( Integer.parseInt( l.substring( 16 , 20) ), Double.parseDouble( l.substring( 20 , 27 ).trim().replace(',', '.') ) );
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new InstantiationError();
		}
	}

	private Iterator<Integer> iter = null;
	
	@Override
	public Double get(int inIndex) {
		
		if ( iter == null || !iter.hasNext() ) {
			
			final List<Integer> years = new ArrayList<>( values.keySet() );
			Collections.sort( years );
			iter = years.iterator();
		}
		
		return values.get( iter.next() );
	}
	
}
