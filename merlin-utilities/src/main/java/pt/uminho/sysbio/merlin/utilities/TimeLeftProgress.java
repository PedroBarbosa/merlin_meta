package pt.uminho.sysbio.merlin.utilities;

/**
 * @author ODias
 *
 */
public class TimeLeftProgress extends OperationProgress{
	
	private String time;

	/**
	 * 
	 */
	public TimeLeftProgress() {
		
		super();
		this.time=new String();
	}
	
	/**
	 * @return
	 */
	public String getTime() {
		
		return time;
	}

	/**
	 * @param differTime
	 * @param pos
	 * @param max
	 */
	public void setTime(long differTime, int pos, int max) {
		
		String timeLeft = new String();
		
		if(pos!=0) {
			
			float seconds = differTime/(float)1000;
			float eachSeconds = seconds/(float)pos;
			float timeL = eachSeconds*(max-pos); 
			int minutes = (int)timeL/60;
			this.setProgress(((float)pos/(float)max));
			int percentage = Math.round(((float)pos/(float)max)*100);
			
			
			if(minutes>59) {
				
				minutes = minutes % 60;
			}
			int hour = (int) timeL/3600 ;
//			System.out.println("pos :"+(float)pos);
//			System.out.println("max :"+(float)max+"\n");
//			System.out.println("progress :"+((float)pos/(float)max)+"\n");
//			System.out.println("progress :"+((float)pos/(float)max)*100+"\n");
//			System.out.println("timeRunSeconds :"+seconds);
//			System.out.println("eachSeconds :"+eachSeconds);
//			System.out.println("timeL :"+timeL);
//			System.out.println("minutes :"+minutes);
//			System.out.println("hour :"+hour);
			//System.out.println(this.getProgress());
			
			if(minutes==0 && hour==0) {
				
				timeLeft = timeLeft.concat("Time Left : <1m");
			}
			else {
				
				timeLeft = timeLeft.concat(percentage+"%   Time Left : "+hour+"h "+minutes+"m");
			}
		}
		else {
			
			timeLeft = timeLeft.concat("Time Left : oo");
		}
		this.time=timeLeft;
	}
	
//	public static void main(String[] arg) {
//		
//		System.out.println((float)2/(float)13);
//	}
}
