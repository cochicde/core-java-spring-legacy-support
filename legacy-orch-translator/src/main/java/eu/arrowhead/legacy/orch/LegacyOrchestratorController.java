package eu.arrowhead.legacy.orch;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.legacy.common.LegacyCommonConstants;
import eu.arrowhead.legacy.common.model.LegacyServiceRequestForm;
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
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = CommonConstants.OP_ORCH_PROCESS, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ResponseEntity<?> orchestrationProcess(@RequestBody final Map<String,Object> request) {
		final String requestStr = Utilities.toJson(request);
		
		if (request.containsKey(LegacyCommonConstants.ORCHESTRATION_INPUT_JSON_KEY_SERVICE_DEFINITION_REQUIREMENT)) {
			return orchestrationProcess413(requestStr);
		} else {
			return orchestrationProcess412(requestStr);
		}
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<?> orchestrationProcess413(final String requestStr) {
		OrchestrationFormRequestDTO request;
		try {
			request = Utilities.fromJson(requestStr, OrchestrationFormRequestDTO.class);
		} catch (final ArrowheadException ex) {
			throw new BadPayloadException("Invalid input JSON.", org.apache.http.HttpStatus.SC_BAD_REQUEST, CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS);
		}
		return legacyDriver.proceedOrchestration413(request);
	}
	
	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<?> orchestrationProcess412(final String requestStr) {
		LegacyServiceRequestForm request;
		try {
			request = Utilities.fromJson(requestStr, LegacyServiceRequestForm.class);
		} catch (final ArrowheadException ex) {
			throw new BadPayloadException("Invalid input JSON.", org.apache.http.HttpStatus.SC_BAD_REQUEST, CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS);
		}
		return legacyDriver.proceedOrchestration412(request);
	}	
}