package eu.arrowhead.legacy.orch;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;

import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.core.CoreSystemService;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.legacy.common.LegacyAppInitListener;
import eu.arrowhead.legacy.common.LegacyCommonConstants;

@Component
public class LegacyOrchestratorAppInitListener extends LegacyAppInitListener {

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected String getSystemName() {
		return "Legacy Orchestrator Translator";
	}

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {
		logger.debug("customInit started");
		
		@SuppressWarnings("unchecked")
		final Map<String, Object> context = event.getApplicationContext().getBean(CommonConstants.ARROWHEAD_CONTEXT, Map.class);
		final String scheme = sslProperties.isSslEnabled() ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final UriComponents srQueryUri = createSRQueryUri(scheme);
		final SystemResponseDTO orchestrator = getOrchestrationCoreSystemDTO(srQueryUri, scheme);
		context.put(LegacyCommonConstants.ORCHESTRATOR_ORCHESTRATION_URI, Utilities.createURI(scheme, orchestrator.getAddress(), orchestrator.getPort(), CoreSystemService.ORCHESTRATION_SERVICE.getServiceUri()));
		unregisterOrchestratorFromSR(orchestrator, scheme);
		registerLegacyOrchestratorTranslator(scheme);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createSRQueryUri(final String scheme) {
		logger.debug("createSRQueryUri started...");
				
		final String srQueryUriString = CommonConstants.SERVICE_REGISTRY_URI + LegacyCommonConstants.OP_SERVICE_REGISTRY_QUERY_URI;		
		return Utilities.createURI(scheme, systemRegistrationProperties.getServiceRegistryAddress(), systemRegistrationProperties.getServiceRegistryPort(),	srQueryUriString);
	}
	
	//-------------------------------------------------------------------------------------------------
	private SystemResponseDTO getOrchestrationCoreSystemDTO(final UriComponents srQueryUri, final String scheme) {
		logger.debug("getOrchestrationCoreSystemDTO started...");
		
		final ServiceQueryFormDTO serviceQueryFormDTO = new ServiceQueryFormDTO.Builder(CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition()).build();
		final ResponseEntity<ServiceQueryResultDTO> serviceQueryResult = httpService.sendRequest(srQueryUri, HttpMethod.POST, ServiceQueryResultDTO.class, serviceQueryFormDTO);
		final List<ServiceRegistryResponseDTO> serviceQueryData = serviceQueryResult.getBody().getServiceQueryData();
		if (serviceQueryData == null || serviceQueryData.isEmpty()) {
			throw new ServiceConfigurationError("Orcestrator Core System not found");
		}
		return serviceQueryData.iterator().next().getProvider();
	}
	
	//-------------------------------------------------------------------------------------------------
	private void unregisterOrchestratorFromSR(final SystemResponseDTO orchestrator, final String scheme) {
		logger.debug("unregisterOrchestratorFromSR started...");
		
		final String srUnregisterUriSrting = CommonConstants.SERVICE_REGISTRY_URI + LegacyCommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
		final String serviceDefinition = CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition();
		final String providerName = orchestrator.getSystemName();
		final String providerAddress = orchestrator.getAddress();
		final int providerPort= orchestrator.getPort();
		
		String[] queryParam = {CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, serviceDefinition,
							   CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, providerName,
							   CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS, providerAddress,
							   CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT, String.valueOf(providerPort)};
		
		final UriComponents srUnregisterUri = Utilities.createURI(scheme, systemRegistrationProperties.getServiceRegistryAddress(), systemRegistrationProperties.getServiceRegistryPort(), srUnregisterUriSrting,
																  queryParam);
		httpService.sendRequest(srUnregisterUri, HttpMethod.DELETE, Void.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void registerLegacyOrchestratorTranslator(final String scheme) {
		logger.debug("unregisterOrchestratorFromSR started...");
		
		final String srRegisterUriSrting = CommonConstants.SERVICE_REGISTRY_URI + LegacyCommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI;
		final UriComponents srRegisterUri = Utilities.createURI(scheme, systemRegistrationProperties.getServiceRegistryAddress(), systemRegistrationProperties.getServiceRegistryPort(), srRegisterUriSrting);
		
		final SystemRequestDTO systemRequestDTO = new SystemRequestDTO();
		systemRequestDTO.setSystemName(CoreSystem.ORCHESTRATOR.name().toLowerCase());
		systemRequestDTO.setAddress(systemRegistrationProperties.getSystemDomainName());
		systemRequestDTO.setPort(systemRegistrationProperties.getSystemDomainPort());
		if (sslProperties.isSslEnabled()) {
			systemRequestDTO.setAuthenticationInfo(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
		}
		
		final ServiceRegistryRequestDTO serviceRegistryRequestDTO = new ServiceRegistryRequestDTO();
		serviceRegistryRequestDTO.setProviderSystem(systemRequestDTO);
		serviceRegistryRequestDTO.setServiceDefinition(CoreSystemService.ORCHESTRATION_SERVICE.getServiceDefinition());
		serviceRegistryRequestDTO.setServiceUri(CoreSystemService.ORCHESTRATION_SERVICE.getServiceUri());
		serviceRegistryRequestDTO.setInterfaces(sslProperties.isSslEnabled() ? List.of(CommonConstants.HTTP_SECURE_JSON) : List.of(CommonConstants.HTTP_INSECURE_JSON));
		serviceRegistryRequestDTO.setSecure(sslProperties.isSslEnabled() ? ServiceSecurityType.CERTIFICATE : ServiceSecurityType.NOT_SECURE);
		
		httpService.sendRequest(srRegisterUri, HttpMethod.POST, ServiceRegistryResponseDTO.class, serviceRegistryRequestDTO);
	}
}
