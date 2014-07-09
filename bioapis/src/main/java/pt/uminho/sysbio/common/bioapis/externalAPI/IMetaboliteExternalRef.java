package pt.uminho.sysbio.common.bioapis.externalAPI;

import java.io.Serializable;

public interface IMetaboliteExternalRef extends IExternalRef, Serializable{
	
	String getSmiles();
	Integer getCharge();
	String getFormula();
	String getMass();
	String getInchikey();

}
