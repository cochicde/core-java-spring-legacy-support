package eu.arrowhead.legacy.common.model;

import java.io.Serializable;

public class LegacyServiceQueryFrom implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -1270050559095164515L;
	
	private LegacyArrowheadService service;
	private boolean pingProviders = false;
	private boolean metadataSearch = false;
	private Integer version;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public LegacyArrowheadService getService() { return service; }
	public boolean isPingProviders() { return pingProviders; }
	public boolean isMetadataSearch() { return metadataSearch; }
	public Integer getVersion() { return version; }
	
	//-------------------------------------------------------------------------------------------------
	public void setService(final LegacyArrowheadService service) { this.service = service; }
	public void setPingProviders(final boolean pingProviders) { this.pingProviders = pingProviders; }
	public void setMetadataSearch(final boolean metadataSearch) { this.metadataSearch = metadataSearch; }
	public void setVersion(final Integer version) { this.version = version; }
}