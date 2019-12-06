package eu.arrowhead.legacy.common.model;

import java.io.Serializable;

public class LegacyArrowheadSystem implements Serializable {

    //=================================================================================================
	// members

	private static final long serialVersionUID = -1341488629592560969L;
	
	private Long id;
    private String systemName;
    private String address;
    private Integer port;
    private String authenticationInfo;
    
    //=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public String getSystemName() { return systemName; }
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setSystemName(final String systemName) { this.systemName = systemName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
}