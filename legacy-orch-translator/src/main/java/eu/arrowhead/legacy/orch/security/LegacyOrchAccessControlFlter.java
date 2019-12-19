package eu.arrowhead.legacy.orch.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.OrchestrationFlags;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.legacy.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class LegacyOrchAccessControlFlter extends CoreSystemAccessControlFilter {

	@Override
	protected void checkClientAuthorized(String clientCN, String method, String requestTarget, String requestJSON, Map<String, String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final OrchestrationFormRequestDTO orchestrationFormRequestDTO = Utilities.fromJson(requestJSON, OrchestrationFormRequestDTO.class);
		final OrchestrationFlags orchestrationFlags = orchestrationFormRequestDTO.getOrchestrationFlags();		
		if (orchestrationFlags.getOrDefault(CommonConstants.ORCHESTRATON_FLAG_EXTERNAL_SERVICE_REQUEST, false)) {
			throw new ArrowheadException("External service request not supported by Legacy Translator");
		} else {
			//All request from the local cloud are allowed, but requester system has to be match with the certificate
			final String clientNameFromCN = clientCN.split("\\.", 2)[0];
			String requesterSystemName = orchestrationFormRequestDTO.getRequesterSystem().getSystemName();
			if (!requesterSystemName.equalsIgnoreCase(clientNameFromCN) && !requesterSystemName.replaceAll("_", "").equalsIgnoreCase(clientNameFromCN)) {
				log.debug("Requester system name and client name from certificate do not match!");
				throw new AuthException("Requester system name(" + requesterSystemName + ") and client name from certificate (" + clientNameFromCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
			}
		}		
	}	
}
