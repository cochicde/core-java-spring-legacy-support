package eu.arrowhead.legacy.sr.security;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.AuthException;
import eu.arrowhead.legacy.common.security.CoreSystemAccessControlFilter;

@Component
@ConditionalOnProperty(name = CommonConstants.SERVER_SSL_ENABLED, matchIfMissing = true) 
public class LegacySRAccessControlFilter extends CoreSystemAccessControlFilter {
	
	//=================================================================================================
	// members
	
	private static final String REGISTER_INPUT_JSON_KEY_SERVICE_DEFINITION = "serviceDefinition";
	private static final String REGISTRY_INPUT_JSON_KEY_PROVIDER_SYSTEM = "providerSystem";
	private static final String REGISTRY_INPUT_JSON_KEY_PROVIDER = "provider";
	private static final String REGISTRY_INPUT_JSON_KEY_PROVIDER_SYSTEM_NAME = "systemName";
	
	private static final String REMOVE_URI = "/remove";
	
	private static final CoreSystem[] allowedCoreSystemsForQuery = { CoreSystem.ORCHESTRATOR, CoreSystem.GATEKEEPER, CoreSystem.CERTIFICATE_AUTHORITY, CoreSystem.EVENT_HANDLER,
																	 CoreSystem.AUTHORIZATION };
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void checkClientAuthorized(final String clientCN, final String method, final String requestTarget, final String requestJSON, final Map<String,String[]> queryParams) {
		super.checkClientAuthorized(clientCN, method, requestTarget, requestJSON, queryParams);
		
		final String cloudCN = getServerCloudCN();
		
		if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI) || requestTarget.endsWith(REMOVE_URI)) {
			// A provider system can only register its own services!
			checkProviderAccessToRegister(clientCN, requestJSON, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI)) {
			// A provider system can only unregister its own services!
			checkProviderAccessToDeregister(clientCN, queryParams, requestTarget);
		} else if (requestTarget.endsWith(CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI)) {
			if (isClientACoreSystem(clientCN, cloudCN)) {
				// Only dedicated core systems can use this service without limitation but every core system can query info about its own services
				checkIfClientAnAllowedCoreSystemOrQueryingOwnSystems(clientCN, cloudCN, requestJSON, requestTarget); 
			} else {
				// Public core system services are allowed to query directly by the local systems
				checkIfRequestedServiceIsAPublicCoreSystemService(requestJSON);
			}			
		} 
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings("unchecked")
	private void checkProviderAccessToRegister(final String clientCN, final String requestJSON, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		final Map<String,Object> requestBody = Utilities.fromJson(requestJSON, Map.class);
		final String providerName = acquireProviderName(requestBody);
		if (Utilities.isEmpty(providerName)) {
			log.debug("Provider name is not set in the body when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!providerName.equalsIgnoreCase(clientName) && !providerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name(" + providerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}

	//-------------------------------------------------------------------------------------------------
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String acquireProviderName(final Map<String,Object> requestBody) {
		if (requestBody.containsKey(REGISTER_INPUT_JSON_KEY_SERVICE_DEFINITION)) {
			final Map<String,Object> system = (Map) requestBody.get(REGISTRY_INPUT_JSON_KEY_PROVIDER_SYSTEM);
			return system != null ? (String) system.get(REGISTRY_INPUT_JSON_KEY_PROVIDER_SYSTEM_NAME) : null;
		} else {
			final Map<String,Object> system = (Map) requestBody.get(REGISTRY_INPUT_JSON_KEY_PROVIDER);
			return system != null ? (String) system.get(REGISTRY_INPUT_JSON_KEY_PROVIDER_SYSTEM_NAME) : null;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private void checkProviderAccessToDeregister(final String clientCN, final Map<String,String[]> queryParams, final String requestTarget) {
		final String clientName = getClientNameFromCN(clientCN);
		
		final String providerName = queryParams.getOrDefault(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, new String[] { "" })[0];
		if (Utilities.isEmpty(providerName)) {
			log.debug("Provider name is not set in the query parameters when use {}", requestTarget);
			return; // we can't continue the check and the endpoint will throw BadPayloadException
		}
		
		if (!providerName.equalsIgnoreCase(clientName) && !providerName.replaceAll("_", "").equalsIgnoreCase(clientName)) {
			log.debug("Provider system name and certificate common name do not match! Registering denied!");
			throw new AuthException("Provider system name(" + providerName + ") and certificate common name (" + clientCN + ") do not match!", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkIfRequestedServiceIsAPublicCoreSystemService(final String requestJSON) {
		final ServiceQueryFormDTO requestBody = Utilities.fromJson(requestJSON, ServiceQueryFormDTO.class);
		
		if (Utilities.isEmpty(requestBody.getServiceDefinitionRequirement())) {
			throw new AuthException("Service is not defined.", HttpStatus.UNAUTHORIZED.value());
		}
		
		for (final CoreSystemService service : CommonConstants.PUBLIC_CORE_SYSTEM_SERVICES) {
			if (service.getServiceDefinition().equalsIgnoreCase(requestBody.getServiceDefinitionRequirement().trim())) {
				return;
			}
		}
		
		throw new AuthException("Only public core system services are allowed to query directly. Requested service (" + requestBody.getServiceDefinitionRequirement() + ") is not!",
								HttpStatus.UNAUTHORIZED.value());
	}
	
	//-------------------------------------------------------------------------------------------------
	private boolean isClientACoreSystem(final String clientCN, final String cloudCN) {
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			final String coreSystemCN = coreSystem.name().toLowerCase() + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return true;
			}
		}		
		return false;
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkIfClientAnAllowedCoreSystemOrQueryingOwnSystems(final String clientCN, final String cloudCN, final String requestJSON, final String requestTarget) {
		final boolean firstCheck = checkIfClientIsAnAllowedCoreSystemNoException(clientCN, cloudCN, allowedCoreSystemsForQuery, requestTarget);
		
		if (!firstCheck) { // no privileged core system
			final CoreSystem coreSystem = getClientCoreSystem(clientCN, cloudCN);
			
			if (coreSystem != null) {
				final ServiceQueryFormDTO requestBody = Utilities.fromJson(requestJSON, ServiceQueryFormDTO.class);
				
				if (Utilities.isEmpty(requestBody.getServiceDefinitionRequirement())) {
					throw new AuthException("Service is not defined.", HttpStatus.UNAUTHORIZED.value());
				}
				
				for (final CoreSystemService service : coreSystem.getServices()) {
					if (service.getServiceDefinition().equalsIgnoreCase(requestBody.getServiceDefinitionRequirement().trim())) {
						return;
					}
				}
			}
			
			throw new AuthException("This core system only query data about its own services.", HttpStatus.UNAUTHORIZED.value());
		}
	}
	
	//-------------------------------------------------------------------------------------------------
	private CoreSystem getClientCoreSystem(final String clientCN, final String cloudCN) {
		for (final CoreSystem coreSystem : CoreSystem.values()) {
			final String coreSystemCN = coreSystem.name().toLowerCase() + "." + cloudCN;
			if (clientCN.equalsIgnoreCase(coreSystemCN)) {
				return coreSystem;
			}
		}		
		
		return null;
	}
	
	//-------------------------------------------------------------------------------------------------
	private String getClientNameFromCN(final String clientCN) {
		return clientCN.split("\\.", 2)[0];
	}
}