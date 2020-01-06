package eu.arrowhead.legacy.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LegacyServiceRequestForm implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = -3227265323710091052L;
	
	private LegacyArrowheadSystem requesterSystem;
	private LegacyArrowheadCloud requesterCloud;
	private LegacyArrowheadService requestedService;
	private Map<String,Boolean> orchestrationFlags = new HashMap<>();
	private List<LegacyPreferredProvider> preferredProviders = new ArrayList<>();
	private Map<String,String> requestedQoS = new HashMap<>();
	private Map<String,String> commands = new HashMap<>();
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public LegacyArrowheadSystem getRequesterSystem() { return requesterSystem; }
	public LegacyArrowheadCloud getRequesterCloud() { return requesterCloud; }
	public LegacyArrowheadService getRequestedService() { return requestedService; }
	public Map<String,Boolean> getOrchestrationFlags() { return orchestrationFlags; }
	public List<LegacyPreferredProvider> getPreferredProviders() { return preferredProviders; }
	public Map<String,String> getRequestedQoS() { return requestedQoS; }
	public Map<String,String> getCommands() { return commands; }
	
	//-------------------------------------------------------------------------------------------------
	public void setRequesterSystem(final LegacyArrowheadSystem requesterSystem) { this.requesterSystem = requesterSystem; }
	public void setRequesterCloud(final LegacyArrowheadCloud requesterCloud) { this.requesterCloud = requesterCloud; }
	public void setRequestedService(final LegacyArrowheadService requestedService) { this.requestedService = requestedService; }
	public void setOrchestrationFlags(final Map<String,Boolean> orchestrationFlags) { this.orchestrationFlags = orchestrationFlags; }
	public void setPreferredProviders(final List<LegacyPreferredProvider> preferredProviders) { this.preferredProviders = preferredProviders; }
	public void setRequestedQoS(final Map<String,String> requestedQoS) { this.requestedQoS = requestedQoS; }
	public void setCommands(final Map<String,String> commands) { this.commands = commands; }	
}