package eu.arrowhead.legacy.sr.driver;

import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.SystemResponseDTO;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.legacy.common.LegacySystemRegistrationProperties;
import eu.arrowhead.legacy.common.model.LegacyArrowheadSystem;
import eu.arrowhead.legacy.common.model.LegacyServiceRegistryEntry;

@Service
public class LegacyServiceRegistryDriver {

	//=================================================================================================
	// members
	
	private static final String REMOVE_URI = "/remove";
	
	@Autowired
	private HttpService httpService;
	
	@Autowired
	private LegacySystemRegistrationProperties systemRegistrationProperties;

	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String echo() {
		final String scheme = sslEnabled ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final UriComponents uri = Utilities.createURI(scheme, systemRegistrationProperties.getServiceRegistryAddress(), systemRegistrationProperties.getServiceRegistryPort(), 
													  CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.ECHO_URI);
		final ResponseEntity<String> response = httpService.sendRequest(uri, HttpMethod.GET, String.class);
		
		return response.getBody();
	}

	//-------------------------------------------------------------------------------------------------
	public void removeServiceRegistry413(final String serviceDefinition, final String providerName, final String providerAddress, final int providerPort) {
		final UriComponents uri = createUnregisterUri(serviceDefinition, providerName, providerAddress, providerPort);
		
		httpService.sendRequest(uri, HttpMethod.DELETE, Void.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	public void removeServiceRegistry412(final LegacyServiceRegistryEntry entry) {
		final LegacyArrowheadSystem system = entry.getProvider();
		final UriComponents uri = createUnregisterUri(entry.getProvidedService().getServiceDefinition(), system.getSystemName().toLowerCase(), system.getAddress().toLowerCase(), system.getPort());
		
		httpService.sendRequest(uri, HttpMethod.DELETE, Void.class);
	}
	
	//-------------------------------------------------------------------------------------------------
	public ServiceRegistryResponseDTO getServiceRegistryEntry(final LegacyServiceRegistryEntry entry) {
		if (entry == null || entry.getProvidedService() == null || Utilities.isEmpty(entry.getProvidedService().getServiceDefinition())) {
			throw new BadPayloadException("Service definition requirement is null or blank" , HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICE_REGISTRY_URI + REMOVE_URI);
		}
		
		checkArrowheadSystem(entry.getProvider(), CommonConstants.SERVICE_REGISTRY_URI + REMOVE_URI);
		
		final UriComponents uri = createQueryUri();
		final ServiceQueryFormDTO form = new ServiceQueryFormDTO.Builder(entry.getProvidedService().getServiceDefinition()).build();
		final ResponseEntity<ServiceQueryResultDTO> response = httpService.sendRequest(uri, HttpMethod.POST, ServiceQueryResultDTO.class, form);
		final ServiceQueryResultDTO resultDTO = response.getBody();
		
		final LegacyArrowheadSystem arrowheadSystem = entry.getProvider();
		for (final ServiceRegistryResponseDTO candidate : resultDTO.getServiceQueryData()) {
			final SystemResponseDTO provider = candidate.getProvider();
			if (provider.getSystemName().equalsIgnoreCase(arrowheadSystem.getSystemName()) && provider.getAddress().equalsIgnoreCase(arrowheadSystem.getAddress()) &&
				provider.getPort() == arrowheadSystem.getPort().intValue()) {
				return candidate;
			}
		}
		
		return null;
	}

	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private UriComponents createUnregisterUri(final String serviceDefinition, final String providerName, final String address, final int port) {
		final String scheme = sslEnabled ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final String unregisterUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI;
		final MultiValueMap<String,String> queryMap = new LinkedMultiValueMap<>(4);
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME, List.of(providerName.toLowerCase()));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS, List.of(address));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT, List.of(String.valueOf(port)));
		queryMap.put(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION, List.of(serviceDefinition));
		
		return Utilities.createURI(scheme, systemRegistrationProperties.getServiceRegistryAddress(), systemRegistrationProperties.getServiceRegistryPort(), queryMap, unregisterUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private UriComponents createQueryUri() {
		final String scheme = sslEnabled ? CommonConstants.HTTPS : CommonConstants.HTTP;
		final String queryUriStr = CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI;
		
		return Utilities.createURI(scheme, systemRegistrationProperties.getServiceRegistryAddress(), systemRegistrationProperties.getServiceRegistryPort(), queryUriStr);
	}
	
	//-------------------------------------------------------------------------------------------------
	private void checkArrowheadSystem(final LegacyArrowheadSystem system, final String origin) {
		if (system == null) {
			throw new BadPayloadException("Provider is null.", HttpStatus.SC_BAD_REQUEST, origin);
		} 
		
		if (Utilities.isEmpty(system.getSystemName())) {
			throw new BadPayloadException("System name is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (Utilities.isEmpty(system.getAddress())) {
			throw new BadPayloadException("System address is null or blank.", HttpStatus.SC_BAD_REQUEST, origin);
		}
		
		if (system.getPort() == null) {
			throw new BadPayloadException("System port is null.", HttpStatus.SC_BAD_REQUEST, origin);
		}
	}
}