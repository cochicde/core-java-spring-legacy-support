package eu.arrowhead.legacy.orch.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.legacy.common.LegacyCommonConstants;
import eu.arrowhead.legacy.common.model.LegacyServiceRequestForm;
import eu.arrowhead.legacy.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class LegacyOrchAccessControlFlter extends CoreSystemAccessControlFilter {
	
	@Autowired
	private ObjectMapper objectMapper;

	@Override
	protected void checkClientAuthorized(String clientCN, String method, String requestTarget, String requestJSON, Map<String, String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		if (requestTarget.contains(CommonConstants.ECHO_URI)) {
			//All request from the local cloud are allowed
		} else {
			
			boolean isExtarnalServiceRequest = false;
			String requesterSystemName = "";
			
			try {
				@SuppressWarnings("unchecked")
				Map<String, String> requestMap = objectMapper.readValue(requestJSON, Map.class);
				if (requestMap.containsKey(LegacyCommonConstants.ORCHESTRATION_INPUT_JSON_KEY_SERVICE_DEFINITION_REQUIREMENT)) {
					final OrchestrationFormRequestDTO orchestrationFormRequestDTO = Utilities.fromJson(requestJSON, OrchestrationFormRequestDTO.class);
					isExtarnalServiceRequest = orchestrationFormRequestDTO.getOrchestrationFlags().getOrDefault(CommonConstants.ORCHESTRATON_FLAG_EXTERNAL_SERVICE_REQUEST, false);
					requesterSystemName = orchestrationFormRequestDTO.getRequesterSystem().getSystemName();
				} else {
					final LegacyServiceRequestForm legacyServiceRequestForm = Utilities.fromJson(requestJSON, LegacyServiceRequestForm.class);
					isExtarnalServiceRequest = legacyServiceRequestForm.getOrchestrationFlags().getOrDefault(CommonConstants.ORCHESTRATON_FLAG_EXTERNAL_SERVICE_REQUEST, false);
					requesterSystemName = legacyServiceRequestForm.getRequesterSystem().getSystemName();
				}
			} catch (final IOException ex) {
				throw new BadPayloadException("Invalid input JSON.");
			}		
			
			if (isExtarnalServiceRequest) {
				throw new ArrowheadException("External service request not supported by Legacy Translator");
			} else {
				//All request from the local cloud are allowed, but requester system has to be match with the certificate
				final String clientNameFromCN = clientCN.split("\\.", 2)[0];
				if (!requesterSystemName.equalsIgnoreCase(clientNameFromCN) && !requesterSystemName.replaceAll("_", "").equalsIgnoreCase(clientNameFromCN)) {
					log.debug("Requester system name and client name from certificate do not match!");
					throw new AuthException("Requester system name(" + requesterSystemName + ") and client name from certificate (" + clientNameFromCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
				}
			}		
		}		
	}	
}
