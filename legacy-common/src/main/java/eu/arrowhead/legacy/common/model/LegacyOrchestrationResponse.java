package eu.arrowhead.legacy.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LegacyOrchestrationResponse implements Serializable {

	//=================================================================================================
	// members
	
	private static final long serialVersionUID = 8291962211918396525L;
	
	private List<LegacyOrchestrationForm> response = new ArrayList<>();

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public List<LegacyOrchestrationForm> getResponse() { return response; }

	//-------------------------------------------------------------------------------------------------
	public void setResponse(final List<LegacyOrchestrationForm> response) { this.response = response; }
	
}
