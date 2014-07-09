package pt.uminho.sysbio.merlin.utilities;

import java.io.Serializable;

public class Pair<A,B> implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5828859486104168730L;
	private A a;
	private B b;
	
	/**
	 * @param a
	 * @param b
	 */
	public Pair(A a, B b){
		this.a = a;
		this.b = b;
	}

	/**
	 * @return
	 */
	public A getA() { 
		
		return a;
	}
	
	/**
	 * @param a
	 */
	public void setA(A a) {
		
		this.a = a;
	}
	
	/**
	 * @return
	 */
	public B getB() {
		
		return b;
	}
	
	/**
	 * @param b
	 */
	public void setB(B b) {
		
		this.b = b;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		
		return "Pair [" + (a != null ? "a=" + a + ", " : "")
				+ (b != null ? "b=" + b : "") + "]";
	}
}