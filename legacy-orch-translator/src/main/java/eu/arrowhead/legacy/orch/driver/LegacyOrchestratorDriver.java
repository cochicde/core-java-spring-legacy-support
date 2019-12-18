package eu.arrowhead.legacy.orch.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceInterfaceResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.legacy.common.LegacyCommonConstants;
import eu.arrowhead.legacy.common.model.LegacyModelConverter;
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
		if (request.getOrchestrationFlags().getOrDefault(Flag.TRIGGER_INTER_CLOUD, false)) {
			throw new BadPayloadException("Translator does not support orchestration with flag 'TRIGGER_INTER_CLOUD=true'", HttpStatus.SC_BAD_REQUEST, origin);
		}	
		if (request.getOrchestrationFlags().getOrDefault(Flag.ENABLE_INTER_CLOUD, false)) {
			request.getOrchestrationFlags().put(Flag.ENABLE_INTER_CLOUD, false);
			logger.debug("Orchestration flag 'ENABLE_INTER_CLOUD=true' is not supported and was changed to false");
		}		
		
		final boolean originalMatchmakingFlag = request.getOrchestrationFlags().getOrDefault(Flag.MATCHMAKING, false);
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
		final String origin = CommonConstants.ORCHESTRATOR_URI + CommonConstants.OP_ORCH_PROCESS;
		if (request.getRequesterSystem() == null) {
			throw new BadPayloadException("requesterSystem cannot be null", HttpStatus.SC_BAD_REQUEST, origin);
		}
		if (request.getOrchestrationFlags().getOrDefault(CommonConstants.ORCHESTRATON_FLAG_TRIGGER_INTER_CLOUD, false)) {
			throw new BadPayloadException("Translator does not support orchestration with flag 'TRIGGER_INTER_CLOUD=true'", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		final Set<String> requestedInterfaces = request.getRequestedService().getInterfaces();		
		final OrchestrationFormRequestDTO orchestrationRequest = LegacyModelConverter.convertLegacyServiceRequestFormToOrchestrationFormRequestDTO(request);
		if (orchestrationRequest.getOrchestrationFlags().getOrDefault(Flag.ENABLE_INTER_CLOUD, false)) {
			orchestrationRequest.getOrchestrationFlags().put(Flag.ENABLE_INTER_CLOUD, false);
			logger.debug("Orchestration flag 'ENABLE_INTER_CLOUD=true' is not supported and was changed to false");
		}		
		final boolean originalMatchmakingFlag = orchestrationRequest.getOrchestrationFlags().getOrDefault(Flag.MATCHMAKING, false);
		orchestrationRequest.getOrchestrationFlags().put(Flag.MATCHMAKING, false);
		
		final UriComponents uri = (UriComponents) arrowheadContext.get(LegacyCommonConstants.ORCHESTRATOR_ORCHESTRATION_URI);		
		final ResponseEntity<OrchestrationResponseDTO> response = httpService.sendRequest(uri, HttpMethod.POST, OrchestrationResponseDTO.class, request);
		final OrchestrationResponseDTO dto = response.getBody();
		
		//Filter on originally requested interface
		if (requestedInterfaces != null && !requestedInterfaces.isEmpty()) {
			final List<OrchestrationResultDTO> providersWithProperInterface = new ArrayList<>();
			for (final OrchestrationResultDTO result : dto.getResponse()) {
				if (result.getMetadata() != null
						&& result.getMetadata().containsKey(LegacyCommonConstants.KEY_ARROWHEAD_VERSION)
						&& result.getMetadata().get(LegacyCommonConstants.KEY_ARROWHEAD_VERSION).equalsIgnoreCase(LegacyCommonConstants.ARROWHEAD_VERSION_VALUE_412)) {
					
					//Arrowhead v4.1.2 compliant provider
					if (result.getMetadata().containsKey(LegacyCommonConstants.KEY_LEGACY_INTERFACE)
							&& requestedInterfaces.contains(result.getMetadata().get(LegacyCommonConstants.KEY_LEGACY_INTERFACE))) {
						providersWithProperInterface.add(result);
					}
					
				} else {
					
					//Arrowhead v4.1.3 compliant provider 
					for (final ServiceInterfaceResponseDTO interfaceDTO : result.getInterfaces()) {
						if (requestedInterfaces.contains(interfaceDTO.getInterfaceName())) {
							providersWithProperInterface.add(result);
						}
					}
					
				}
			}
			dto.setResponse(providersWithProperInterface);			
		}
		
		if (originalMatchmakingFlag && dto.getResponse().size() > 1) {
			dto.setResponse(List.of(dto.getResponse().iterator().next()));
		}
		
		final Entry<String, String> tokenWorkaround = tokenWorkaround();
		final String legacyToken = tokenWorkaround.getKey();
		final String legacySignature = tokenWorkaround.getValue();
		
		return new ResponseEntity<>(LegacyModelConverter.convertOrchestrationResponseDTOtoLegacyOrchestrationResponse(dto, legacyToken, legacySignature), org.springframework.http.HttpStatus.OK);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	public Entry<String, String> tokenWorkaround() {
		return null; //TODO
	}

}
