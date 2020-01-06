package eu.arrowhead.legacy.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.arrowhead.common.dto.shared.OrchestratorWarnings;

public class LegacyOrchestrationForm implements Serializable {
	
	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 2329612597220767765L;
	
	private LegacyArrowheadService service;
	private LegacyArrowheadSystem provider;
	private String serviceURI;
	private String instruction;
	private String authorizationToken;
	private String signature;
	private List<OrchestratorWarnings> warnings = new ArrayList<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public LegacyArrowheadService getService() { return service; }
	public LegacyArrowheadSystem getProvider() { return provider; }
	public String getServiceURI() { return serviceURI; }
	public String getInstruction() { return instruction; }
	public String getAuthorizationToken() { return authorizationToken; }
	public String getSignature() { return signature; }
	public List<OrchestratorWarnings> getWarnings() { return warnings; }

	//-------------------------------------------------------------------------------------------------
	public void setService(final LegacyArrowheadService service) { this.service = service; }
	public void setProvider(final LegacyArrowheadSystem provider) { this.provider = provider; }
	public void setServiceURI(final String serviceURI) { this.serviceURI = serviceURI; }
	public void setInstruction(final String instruction) { this.instruction = instruction; }
	public void setAuthorizationToken(final String authorizationToken) { this.authorizationToken = authorizationToken; }
	public void setSignature(final String signature) { this.signature = signature; }
	public void setWarnings(final List<OrchestratorWarnings> warnings) { this.warnings = warnings; }
}