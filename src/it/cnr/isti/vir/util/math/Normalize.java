package it.cnr.isti.vir.util.math;

public class Normalize {

	/**
	 * @param values
	 * 
	 * In case the sum of the values is 0.0, values[] is set to null
	 */
	public static final void l2( float[] values ) {
		float norm = Norm.l2(values);
        if ( norm > 0.0 ) {
	        for (int i=0; i<values.length; i++) {
	        	values[i] = values[i] / norm;	        	
	        }        
        } else {
        	values = null;
        }
	}
	
	public static final void sqrt( float[] values ) {
		int size = values.length;
		// Power Normalization 0.5
        for (int i=0; i<size; i++) {     
        	if ( values[i] == 0 ) values[i] = 0.0F;
        	else if ( values[i] > 0 )
        		values[i] =   (float) Math.sqrt(values[i]);
        	else 
        		values[i] = - (float) Math.sqrt(-values[i]);
        }
	}
	
	/**
	 * @param values	the array to be power 0.5 normalized
	 */
	public static final void sqrt(int[] values) {
		int size = values.length;
		// Power Normalization 0.5
        for (int i=0; i<size; i++) {     
        	if ( values[i] == 0 ) values[i] = 0;
        	else if ( values[i] > 0 )
        		values[i] =   (int) Math.sqrt(values[i]);
        	else 
        		values[i] = - (int) Math.sqrt(-values[i]);
        }
	}
	
	/**
	 * @param values	the array to be power 0.5 normalized
	 */
	public static final float[] getSQRT_float(int[] values) {
		int size = values.length;
		float[] res = new float[size];
		// Power Normalization 0.5
        for (int i=0; i<size; i++) {     
        	if ( values[i] == 0 ) res[i] = 0.0F;
        	else if ( values[i] > 0 )
        		res[i] =   (float) Math.sqrt((double) values[i]);
        	else 
        		res[i] = - (float) Math.sqrt((double) -values[i]);
        }
        return res;
	}	
	
	public static final float[] getPower_float( int[] values, double a ) {
		int size = values.length;
		float[] res = new float[size];
        for (int i=0; i<size; i++) {     
        	if ( values[i] == 0 ) res[i] = 0.0F;
        	else if ( values[i] > 0 )
        		res[i] =   (float) Math.pow(values[i], a);
        	else 
        		res[i] = - (float) Math.pow(-values[i], a);
        }
        return res;
	}
	
	public static final void power( double[] values, double a ) {
		int size = values.length;
		for (int i=0; i<size; i++) {     
        	if ( values[i] == 0 ) values[i] = 0.0F;
        	else if ( values[i] > 0 )
        		values[i] =   Math.pow( values[i], a);
        	else 
        		values[i] = - Math.pow(-values[i], a);
        }
	}	
	
	public static final void power( float[] values, double a ) {
		int size = values.length;
		for (int i=0; i<size; i++) {     
        	if ( values[i] == 0 ) values[i] = 0.0F;
        	else if ( values[i] > 0 )
        		values[i] =   (float) Math.pow(values[i], a);
        	else 
        		values[i] = - (float) Math.pow(-values[i], a);
        }
	}
	
//	public static final void power( float[] values, float a ) {
//		return;
//	}
	
	
}
