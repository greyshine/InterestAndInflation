

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.junit.Test;

import de.greyshine.spielwiese.interestAndInflation.InterestAndInflation;
import de.greyshine.spielwiese.interestAndInflation.InterestAndInflation.InflationProvider;
import de.greyshine.spielwiese.interestAndInflation.InterestAndInflation.ValueProvider;

public class InterestAndInflationTester {
	
	
	@Test
	public void main1() {
		
		final InterestAndInflation iai = new InterestAndInflation().start( 100, 2.90570 );
		
		iai.inflationProvider( new InflationProvider() {
			
			final Random R = new Random();
			
			@Override
			public Double get(int inIndex) {
				
				int r = R.nextInt( 210 );
				
				if ( r == 0 ) { return 0d; }
				
				return BigDecimal.valueOf( r ).divide( BigDecimal.valueOf(100) , 2, RoundingMode.HALF_UP).doubleValue();
			}
		} );
		
		iai.inflationProvider( new GermanDataProvider() );
		
		iai.handle( 100, (e)->{
			System.out.println(e);
		} );
	}

	@Test
	public void main2() {
		
		final InterestAndInflation iai = new InterestAndInflation().start( 100, 2.90570 );
		
		iai.inflationProvider( new GermanDataProvider() );
		
		iai.handle( 100, (e)->{
			System.out.println(e);
		} );
	}

	@Test
	public void main3() {
		
		final InterestAndInflation iai = new InterestAndInflation().start( 0, 1.545, 1.5 );
		
		iai.valueProvider( new ValueProvider() {
			public double get(int inIndex, double inValue) {
				return inValue + 1700;
			}
		} );
		
		iai.handle( 30, (e)->{
			System.out.println(e);
		} );
	}

	
}
