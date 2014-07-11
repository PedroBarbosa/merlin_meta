package utilities;

import java.util.List;
public class BlastScorer {


	private Double s1;
	private List<Integer> s2;
	private Double alpha, beta;
	private Double s;
	private int maxRank, minimumNumberofHits;

	/**
	 * @param s1
	 * @param s2
	 * @param maxRank
	 * @param alpha
	 * @param beta
	 */
	public BlastScorer(Double s1, List<Integer> s2, int maxRank, Double alpha, Double beta) {
		
		this.s1=s1;
		this.s2=s2;
		this.alpha = alpha;
		this.beta = beta;
		this.setMaxRank(maxRank);
		this.setS(this.getAlpha() * this.getS1() +(1-this.getAlpha())*taxonomyAverage());
	}

	/**
	 * 
	 */
	public BlastScorer(){}

	/**
	 * @return the s1
	 */
	public Double getS1() {
		return s1;
	}

	/**
	 * @param s1 the s1 to set
	 */
	public void setS1(Double s1) {
		this.s1 = s1;
	}

	/**
	 * @return the s2
	 */
	public List<Integer> getS2() {
		return s2;
	}

	/**
	 * @param s2 the s2 to set
	 */
	public void setS2(List<Integer> s2, int maxRank) {
		
		this.s2 = s2;
		this.setMaxRank(maxRank);
	}

	/**
	 * @return the alpha
	 */
	public Double getAlpha() {
		
		return alpha;
	}

	/**
	 * @param d the alpha to set
	 */
	public void setAlpha(Double d) {
		
		this.alpha = d;
	}

	/**
	 * @return the s
	 */
	public Double getS() {
		
		if(this.getS1()>0) {
			System.out.println("taxaver" + taxonomyAverage());
			double b = this.getAlpha() + this.getS1();
			System.out.println(b);
			this.setS(this.getAlpha() * this.getS1() +(1-this.getAlpha())*taxonomyAverage());	
		}
		else {
			
			this.setS(1.0);
		}
		return s;
	}
	
	/**
	 * @return the s in metagenomics projects
	 */
	public Double getSMeta() {
		
		if(this.getS1()>0) {

			this.setS(this.getAlpha() * this.getS1() +(1-this.getAlpha())*0.0);	
		}
		else {
			
			this.setS(1.0);
		}
		return s;
	}

	/**
	 * @param s the s to set
	 */
	public void setS(Double s) {
		
		this.s = s;
	}

	/**
	 * @param numberOfOrganisms
	 * @return
	 */
	public double taxonomyAverage() {
		
		List<Integer> taxScore;
		double r=0;
		
		if(this.minimumNumberofHits>=this.getS2().size()) {
			
			taxScore = this.getS2();
		}
		else {
			
			taxScore = this.getS2().subList(0, this.minimumNumberofHits);
			
			for(int i = this.minimumNumberofHits; i<this.getS2().size(); i++) {
				
				for(int j = 0; j<taxScore.size(); j++) {
					
					if(taxScore.get(j) < this.getS2().get(i)) {
						
						taxScore.set(j, this.getS2().get(i));
						j = taxScore.size();
					}
				}
			}
		}

		for(int rank:taxScore) {
			r+=rank;
		}
		
		//o penalty aplicado ao ranking maximo vai baixar o divisor, por isso o ranking vai ser mais alto...
		//r=(r/this.getMaxRank())/(numberOfOrganisms*(1-(numberOfOrganisms-ranks.size())*penaltyCost));
		
		//r=(r*(1-(numberOfOrganisms-ranks.size())*this.beta)/(numberOfOrganisms*this.getMaxRank()));
		System.out.println("R" + r);
		System.out.println(this.minimumNumberofHits);
		System.out.println(taxScore.size());
		
		r=(r*(1-(this.minimumNumberofHits - taxScore.size()) * this.beta)/(taxScore.size()*this.getMaxTaxonomy()));
		System.out.println(r);
		return r;
	}

	/**
	 * @param maxRank the maxRank to set
	 */
	public void setMaxRank(int maxRank) {
		this.maxRank = maxRank;
	}

	/**
	 * @return the maxRank
	 */
	public int getMaxTaxonomy() {
		return maxRank;
	}

	/**
	 * @return the beta
	 */
	public Double getBeta() {
		return beta;
	}

	/**
	 * @param beta the beta to set
	 */
	public void setBeta(Double beta) {
		this.beta = beta;
	}

	/**
	 * @return the minimumNumberofHits
	 */
	public int getMinimumNumberofHits() {
		return minimumNumberofHits;
	}

	/**
	 * @param minimumNumberofHits the minimumNumberofHits to set
	 */
	public void setMinimumNumberofHits(int minimumNumberofHits) {
		this.minimumNumberofHits = minimumNumberofHits;
	}

}
