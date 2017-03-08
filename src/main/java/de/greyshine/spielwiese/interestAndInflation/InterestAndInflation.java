package de.greyshine.spielwiese.interestAndInflation;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InterestAndInflation {
	
	private static final BigDecimal BD_100 = new BigDecimal("100");
	private static final BigDecimal BD_m1 = new BigDecimal("-1");
	private static final MathContext MC = new MathContext( 10 , RoundingMode.HALF_UP);
	
	private final List<Entry> entries = new ArrayList<>();
	
	private int accuracy = 2;
	
	private InterestProvider interestProvider;
	private InflationProvider inflationProvider = new InflationProvider() {};
	private ValueProvider valueProvider = new ValueProvider() {};
	
	public InterestAndInflation start( double inValue ) {
		return this.start( inValue, 0, 0 );
	};
	
	public InterestAndInflation start( double inValue, double inInterest ) {
		return this.start( inValue, inInterest, 0 );
	};
	
	public InterestAndInflation start( double inValue, double inInterestPercent, double inInflationPercent ) {
		
		final Entry e = new Entry();
		e.value = BigDecimal.valueOf( inValue );
		entries.add( e );
		
		interestProvider = new InterestProvider() {
			@Override
			public double get(int inIndex) {
				return inInterestPercent;
			}
		};
		
		inflationProvider = new InflationProvider() {
			@Override
			public Double get(int inIndex) {
				return inInflationPercent;
			}
		};
		
		return refresh();
	};
	
	public InterestAndInflation inflationProvider(InflationProvider inInflationProvider) {
		inflationProvider = inInflationProvider;
		return this;
	}
	
	public InterestAndInflation valueProvider( ValueProvider inValueProvider ) {
		valueProvider = inValueProvider == null ? new ValueProvider(){} : inValueProvider;
		return this;
	}
	
	public InterestAndInflation refresh() {
		
		entries.get(0).refresh();
		
		for (int i = 1, l=entries.size(); i < l; i++) {
			
			final Entry e = entries.get(i);
			final Entry p = entries.get(i-1);
			
			e.refresh();
			
			e.value = e.value.add( p.interestAbsolute );
			e.value = e.value.subtract( p.inflationAbsolute );
		}
		
		return this;
	}
	
	private String format(BigDecimal inBd) {
		inBd = inBd == null ? BigDecimal.ZERO : inBd;
		return inBd.setScale(accuracy, RoundingMode.HALF_UP).toPlainString();
	}
	
	public class Entry {
		
		private BigDecimal value = BigDecimal.ZERO;
		private BigDecimal interestPercent;
		private BigDecimal interestAbsolute = BigDecimal.ZERO;
		private BigDecimal inflationPercent;
		private BigDecimal inflationAbsolute = BigDecimal.ZERO;

		public int getIndex() {
			return entries.indexOf(this);
		}

		public void refresh() {
			
			final int index = entries.indexOf( this ); 
			
			interestPercent = BigDecimal.valueOf( interestProvider.get( index ) );
			
			final Double theInflationOverwrite = inflationProvider.get(index);
			inflationPercent = theInflationOverwrite == null ? inflationPercent : BigDecimal.valueOf( theInflationOverwrite );
			
			if ( index != 0 ) {
				
				final Entry p = entries.get( index-1 );
				
				value = BigDecimal.valueOf( valueProvider.get(index, p.value.doubleValue()) );
				
				if ( interestPercent == null ) {
					interestPercent = p.interestPercent;
				}
				
				if ( inflationPercent == null ) {
					inflationPercent = p.inflationPercent;
				}
			}
			
			if ( inflationPercent != null && BigDecimal.ZERO.compareTo( inflationPercent ) != 0 ) {
				inflationAbsolute = value.multiply( inflationPercent.divide( BD_100, MC ) , MC );	
			} else {
				inflationAbsolute = BigDecimal.ZERO;
			}
			
			if ( interestPercent != null && BigDecimal.ZERO.compareTo( interestPercent ) != 0 ) {
				interestAbsolute = value.multiply( interestPercent.divide( BD_100, MC ) , MC );
			} else {
				interestAbsolute = BigDecimal.ZERO;
			}
		}

		@Override
		public String toString() {
			return "interval="+ getIndex() +"; value="+ format( value ) +", interest="+ format( interestPercent ) +"%/"+ format( interestAbsolute ) +", inflation="+ format( inflationPercent ) +"%/"+ format( inflationAbsolute.multiply( BD_m1 ) );
		}
	}
	
	private Entry getCreateIndex(int inIndex) {
		
		while( entries.size() <= inIndex ) {
			entries.add( new Entry() );
		}
		
		return entries.get( inIndex );
	}
	
	public Entry getInterval(int inIndex) {
		
		if ( inIndex < 0 ) { throw new IllegalArgumentException("interval must be >= 0"); }
		
		final Entry e = getCreateIndex( inIndex );
		
		refresh();
		
		return e;
	}
	
	public Entry setIntervalInterest(int inIndex, Double inInterestPercent, Double inInflationPercent ) {
		
		final Entry e = getInterval(inIndex);
		
		e.interestPercent = inInterestPercent == null ? null : BigDecimal.valueOf( inInterestPercent );
		e.inflationPercent = inInflationPercent == null ? null : BigDecimal.valueOf( inInflationPercent );
		
		refresh();
		
		return e;
	}
	
	public int getEntriesSize() {
		return entries.size();
	}

	public void handle(int inMaxIndex, Consumer<Entry> inConsumer) {
		
		if ( inMaxIndex < 0 || inConsumer == null ) {
			return;
		}
		
		getInterval( inMaxIndex );
		
		entries.stream().filter( (e)->{ return e.getIndex() <= inMaxIndex; } ).forEach( inConsumer );
	}
	
	public interface InterestProvider {
		double get(int inIndex);
	}
	
	public interface InflationProvider {
		default Double get(int inIndex) { return null; }
	}

	public interface ValueProvider {
		default double get(int inIndex, double inValue) { return inValue; }
	}
	
}
