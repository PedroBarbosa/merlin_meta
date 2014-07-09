package pt.uminho.sysbio.merlin.utilities;

import java.io.Serializable;
import java.util.Comparator;

public class PairComparator<T> implements Comparator<Pair<String,T>>, Serializable {

	private static final long serialVersionUID = 1039418059532611238L;

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Pair<String,T> obj1, Pair<String,T> obj2){

		String objS1  = obj1.getA();

		String objS2  =  obj2.getA();

		T objCount1 =  obj1.getB();

		T objCount2 = obj2.getB();

		if(objS1.trim().contains("hypothetical"))
			return 1;

		if(objS2.trim().contains("hypothetical"))
			return -1;

		if(objS1 == objS2)
		{
			return 0;
		}
		else
		{
			if(objCount1 instanceof java.lang.Double)
			{
				if( (Double) objCount1 > (Double) objCount2 )
				{
					return -1;
				}
				else
					if( (Double) objCount1 < (Double) objCount2 )
					{
						return 1;
					}
			}

			if(objCount1 instanceof java.lang.Integer)
			{
				if( (Integer) objCount1 > (Integer) objCount2 )
				{
					return -1;
				}
				else
					if( (Integer) objCount1 < (Integer) objCount2 )
					{
						return 1;
					}
			}
		}
		return 0;

	}
}
