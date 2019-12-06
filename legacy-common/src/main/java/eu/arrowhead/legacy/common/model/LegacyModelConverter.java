package eu.arrowhead.legacy.common.model;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.stream.Collectors;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.legacy.common.LegacyCommonConstants;

public class LegacyModelConverter {
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public static LegacyServiceRegistryEntry convertServiceRegistryResponseDTOToLegacyServiceRegistryEntry(final ServiceRegistryResponseDTO dto) {
		final LegacyServiceRegistryEntry entry = new LegacyServiceRegistryEntry();
		
		final LegacyArrowheadService providedService = new LegacyArrowheadService();
		providedService.setId(dto.getServiceDefinition().getId());
		providedService.setServiceDefinition(dto.getServiceDefinition().getServiceDefinition());
		providedService.setInterfaces(dto.getInterfaces().stream().map(e -> e.getInterfaceName()).collect(Collectors.toSet()));
		providedService.setServiceMetadata(dto.getMetadata());
		providedService.getServiceMetadata().put(LegacyCommonConstants.KEY_SECURITY, dto.getSecure().name());
		
		final LegacyArrowheadSystem provider = new LegacyArrowheadSystem();
		provider.setId(dto.getProvider().getId());
		provider.setSystemName(dto.getProvider().getSystemName());
		provider.setAddress(dto.getProvider().getAddress());
		provider.setPort(dto.getProvider().getPort());
		provider.setAuthenticationInfo(dto.getProvider().getAuthenticationInfo());
		
		entry.setId(dto.getId());
		entry.setProvidedService(providedService);
		entry.setProvider(provider);
		entry.setServiceUri(dto.getServiceUri());
		entry.setUdp(false);
		entry.setVersion(dto.getVersion());
		entry.setEndOfValidity(Utilities.isEmpty(dto.getEndOfValidity()) ? null : convertUTCSTringToLocalDateTime(dto.getEndOfValidity()));
		
		return entry;
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private static LocalDateTime convertUTCSTringToLocalDateTime(final String utcTime) {
		final ZonedDateTime zonedDateTime = Utilities.parseUTCStringToLocalZonedDateTime(utcTime);
		return zonedDateTime.toLocalDateTime(); 
	}

	//-------------------------------------------------------------------------------------------------
	private LegacyModelConverter() {
		throw new UnsupportedOperationException();
	}
}