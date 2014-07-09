package pt.uminho.sysbio.common.bioapis.externalAPI;

import java.io.Serializable;

public interface IExternalRef extends Serializable{
	
	ExternalRefSource getExternalRefSource();
	
	String getId();
	String getName();
	String getURLLink();
	Boolean hasMiriamCode();
	String getMiriamCode();
	
}
