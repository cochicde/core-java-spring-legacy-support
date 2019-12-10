package eu.arrowhead.legacy.common.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.legacy.common.LegacyCommonConstants;

public class LegacyModelConverter {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static LegacyServiceRegistryEntry convertServiceRegistryResponseDTOToLegacyServiceRegistryEntry(final ServiceRegistryResponseDTO dto) {
		final LegacyArrowheadService providedService = new LegacyArrowheadService();
		providedService.setId(dto.getServiceDefinition().getId());
		providedService.setServiceDefinition(dto.getServiceDefinition().getServiceDefinition());
		providedService.setInterfaces(Set.of(dto.getMetadata().get(LegacyCommonConstants.KEY_LEGACY_INTERFACE)));
		providedService.setServiceMetadata(dto.getMetadata());
		providedService.getServiceMetadata().put(LegacyCommonConstants.KEY_SECURITY, dto.getSecure().name());
		providedService.getServiceMetadata().remove(LegacyCommonConstants.KEY_LEGACY_INTERFACE);
		providedService.getServiceMetadata().remove(LegacyCommonConstants.KEY_ARROWHEAD_VERSION);
		
		final LegacyArrowheadSystem provider = new LegacyArrowheadSystem();
		provider.setId(dto.getProvider().getId());
		provider.setSystemName(dto.getProvider().getSystemName());
		provider.setAddress(dto.getProvider().getAddress());
		provider.setPort(dto.getProvider().getPort());
		provider.setAuthenticationInfo(dto.getProvider().getAuthenticationInfo());
		
		final LegacyServiceRegistryEntry entry = new LegacyServiceRegistryEntry();
		entry.setId(dto.getId());
		entry.setProvidedService(providedService);
		entry.setProvider(provider);
		entry.setServiceUri(dto.getServiceUri());
		entry.setUdp(false);
		entry.setVersion(dto.getVersion());
		entry.setEndOfValidity(Utilities.isEmpty(dto.getEndOfValidity()) ? null : convertUTCSTringToLocalDateTime(dto.getEndOfValidity()));
		
		return entry;
	}
	
	//-------------------------------------------------------------------------------------------------
	public static ServiceRegistryRequestDTO convertLegacyServiceRegistryEntryToServiceRegistryRequestDTO(final LegacyServiceRegistryEntry entry) {
		final SystemRequestDTO provider = new SystemRequestDTO();
		provider.setSystemName(entry.getProvider().getSystemName());
		provider.setAddress(entry.getProvider().getAddress());
		provider.setPort(entry.getProvider().getPort());
		provider.setAuthenticationInfo(entry.getProvider().getAuthenticationInfo());
		
		if (entry.getProvidedService().getServiceMetadata() == null) {
			entry.getProvidedService().setServiceMetadata(new HashMap<>());
		} 
		entry.getProvidedService().getServiceMetadata().put(LegacyCommonConstants.KEY_ARROWHEAD_VERSION, LegacyCommonConstants.ARROWHEAD_VERSION_VALUE_412);
		
		final ServiceRegistryRequestDTO dto = new ServiceRegistryRequestDTO();
		dto.setServiceDefinition(entry.getProvidedService().getServiceDefinition());
		dto.setProviderSystem(provider);
		dto.setServiceUri(entry.getServiceUri());
		dto.setVersion(entry.getVersion());
		dto.setSecure(calculateSecurityType(entry.getProvidedService().getServiceMetadata()));
		entry.getProvidedService().getServiceMetadata().remove(LegacyCommonConstants.KEY_SECURITY);
		entry.getProvidedService().getServiceMetadata().put(LegacyCommonConstants.KEY_LEGACY_INTERFACE, entry.getProvidedService().getInterfaces().iterator().next());
		dto.setInterfaces(List.of(LegacyCommonConstants.DEFAULT_INTERFACE));
		dto.setMetadata(entry.getProvidedService().getServiceMetadata());
		dto.setEndOfValidity(entry.getEndOfValidity() == null ? null : convertEndOfValidityToUTCString(entry.getEndOfValidity()));
		
		return dto;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private static LocalDateTime convertUTCSTringToLocalDateTime(final String utcTime) {
		final ZonedDateTime zonedDateTime = Utilities.parseUTCStringToLocalZonedDateTime(utcTime);
		return zonedDateTime.toLocalDateTime(); 
	}
	
	//-------------------------------------------------------------------------------------------------
	private static String convertEndOfValidityToUTCString(final LocalDateTime localDateTime) {
		ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
		return Utilities.convertZonedDateTimeToUTCString(zonedDateTime);
	}
	
	//-------------------------------------------------------------------------------------------------
	private static ServiceSecurityType calculateSecurityType(final Map<String,String> metadata) {
		if (metadata == null) {
			return ServiceSecurityType.NOT_SECURE;
		}
		
		if (metadata.containsKey(LegacyCommonConstants.KEY_SECURITY)) {
			return metadata.get(LegacyCommonConstants.KEY_SECURITY).equalsIgnoreCase(LegacyCommonConstants.SECURITY_VALUE_TOKEN) ? ServiceSecurityType.TOKEN : ServiceSecurityType.CERTIFICATE;
		} else {
			return ServiceSecurityType.NOT_SECURE;
		}
	}

	//-------------------------------------------------------------------------------------------------
	private LegacyModelConverter() {
		throw new UnsupportedOperationException();
	}
}