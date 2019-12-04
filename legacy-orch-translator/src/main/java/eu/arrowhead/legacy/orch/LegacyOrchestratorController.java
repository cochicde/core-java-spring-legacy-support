package eu.arrowhead.legacy.orch;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;

@RestController
@RequestMapping(CommonConstants.ORCHESTRATOR_URI)
public class LegacyOrchestratorController {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "LEGACY Orchestrator got it!";
	}
}