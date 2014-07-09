package pt.uminho.sysbio.merge.databases.containers;

public class EcNumber {
	private String ecNumber;

	public EcNumber(String ecNumber) {
		super();
		this.ecNumber = ecNumber;
	}

	/**
	 * @return the ecNumber
	 */
	public String getEcNumber() {
		return ecNumber;
	}

	/**
	 * @param ecNumber the ecNumber to set
	 */
	public void setEcNumber(String ecNumber) {
		this.ecNumber = ecNumber;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "EcNumber [ecNumber=" + ecNumber + "]";
	}

}
