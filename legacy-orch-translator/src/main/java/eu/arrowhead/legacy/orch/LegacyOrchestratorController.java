package eu.arrowhead.legacy.orch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.legacy.orch.driver.LegacyOrchestratorDriver;

@RestController
@RequestMapping(CommonConstants.ORCHESTRATOR_URI)
public class LegacyOrchestratorController {
	
	//=================================================================================================
	// members
	
	@Autowired
	private LegacyOrchestratorDriver legacyDriver;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "The REAL Orchestrator says: " + legacyDriver.echo();
	}
}