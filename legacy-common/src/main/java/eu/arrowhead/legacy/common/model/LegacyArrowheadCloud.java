package eu.arrowhead.legacy.common.model;

import java.io.Serializable;

public class LegacyArrowheadCloud implements Serializable {

	//=================================================================================================
	// members

	private static final long serialVersionUID = 952183829875922276L;
	
	private Long id;
	private String operator;
	private String cloudName;
	private String address;
	private Integer port;
	private String gatekeeperServiceURI;
	private String authenticationInfo;
	private Boolean secure = false;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public String getOperator() { return operator; }
	public String getCloudName() { return cloudName; }
	public String getAddress() { return address; }
	public Integer getPort() { return port; }
	public String getGatekeeperServiceURI() { return gatekeeperServiceURI; }
	public String getAuthenticationInfo() { return authenticationInfo; }
	public Boolean getSecure() { return secure; }
	
	//-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
	public void setOperator(final String operator) { this.operator = operator; }
	public void setCloudName(final String cloudName) { this.cloudName = cloudName; }
	public void setAddress(final String address) { this.address = address; }
	public void setPort(final Integer port) { this.port = port; }
	public void setGatekeeperServiceURI(final String gatekeeperServiceURI) { this.gatekeeperServiceURI = gatekeeperServiceURI; }
	public void setAuthenticationInfo(final String authenticationInfo) { this.authenticationInfo = authenticationInfo; }
	public void setSecure(final Boolean secure) { this.secure = secure; }
}