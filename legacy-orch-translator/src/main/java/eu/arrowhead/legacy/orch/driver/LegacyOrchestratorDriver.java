package eu.arrowhead.legacy.orch.driver;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.legacy.common.LegacyCommonConstants;
import eu.arrowhead.legacy.common.model.LegacyOrchestrationResponse;
import eu.arrowhead.legacy.common.model.LegacyServiceRequestForm;

@Service
public class LegacyOrchestratorDriver {
	
	//=================================================================================================
	// members
	
	@Autowired
	private HttpService httpService;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	private final Logger logger = LogManager.getLogger(LegacyOrchestratorDriver.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String echo() {
		final String scheme = sslEnabled ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final UriComponents uri = (UriComponents) arrowheadContext.get(LegacyCommonConstants.ORCHESTRATOR_ORCHESTRATION_URI);
		final String address = uri.getHost();
		final int port = uri.getPort();
		final UriComponents uriEcho = Utilities.createURI(scheme, address, port, CommonConstants.ORCHESTRATOR_URI + CommonConstants.ECHO_URI);
		final ResponseEntity<String> response = httpService.sendRequest(uriEcho, HttpMethod.GET, String.class);
		
		return response.getBody();
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<OrchestrationResponseDTO> proceedOrchestration413(final OrchestrationFormRequestDTO request) {
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS;
		if (request.getOrchestrationFlags().get(Flag.TRIGGER_INTER_CLOUD)) {
			throw new BadPayloadException("Translator does not support orchestration with flag 'TRIGGER_INTER_CLOUD=true'", HttpStatus.SC_BAD_REQUEST, origin);
		}	
		if (request.getOrchestrationFlags().get(Flag.ENABLE_INTER_CLOUD)) {
			request.getOrchestrationFlags().put(Flag.ENABLE_INTER_CLOUD, false);
			logger.debug("Orchestration flag 'ENABLE_INTER_CLOUD=true' is not supported and was changed to false");
		}		
		
		final boolean originalMatchmakingFlag = request.getOrchestrationFlags().get(Flag.MATCHMAKING);
		request.getOrchestrationFlags().put(Flag.MATCHMAKING, false);
		
		final UriComponents uri = (UriComponents) arrowheadContext.get(LegacyCommonConstants.ORCHESTRATOR_ORCHESTRATION_URI);		
		final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(uri, HttpMethod.POST, OrchestrationResponseDTO.class, request);
		final OrchestrationResponseDTO dto = response.getBody();
		
		if (originalMatchmakingFlag && dto.getResponse().size() > 1) {
			dto.setResponse(List.of(dto.getResponse().iterator().next()));
		}
		
		return new ResponseEntity<>(dto, org.springframework.http.HttpStatus.OK);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ResponseEntity<LegacyOrchestrationResponse> proceedOrchestration412(final LegacyServiceRequestForm request) {
		return null; //TODO
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	public void tokenWorkaround() {
		//TODO
	}

}
