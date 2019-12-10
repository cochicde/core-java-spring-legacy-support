package eu.arrowhead.legacy.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LegacyServiceQueryResult implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = -789460953047495749L;
	
	private List<LegacyServiceRegistryEntry> serviceQueryData = new ArrayList<>();
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public List<LegacyServiceRegistryEntry> getServiceQueryData() { return serviceQueryData; }

	//-------------------------------------------------------------------------------------------------
	public void setServiceQueryData(final List<LegacyServiceRegistryEntry> serviceQueryData) { this.serviceQueryData = serviceQueryData; }
}