package eu.arrowhead.legacy.common.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
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
		if (dto.getMetadata().containsKey(LegacyCommonConstants.KEY_LEGACY_INTERFACE)) {
			providedService.setInterfaces(Set.of(dto.getMetadata().get(LegacyCommonConstants.KEY_LEGACY_INTERFACE)));
		} else {
			providedService.setInterfaces(dto.getInterfaces().stream().map(e -> e.getInterfaceName()).collect(Collectors.toSet()));
		}
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
	
	//-------------------------------------------------------------------------------------------------
	public static ServiceQueryFormDTO convertLegacyServiceQueryFormToServiceQueryFormDTO(final LegacyServiceQueryFrom form) {
		
		final Map<String,String> metadata = form.getService().getServiceMetadata();
		final ServiceQueryFormDTO.Builder builder = new ServiceQueryFormDTO.Builder(form.getService().getServiceDefinition())
															   	   		   .version(form.getVersion())
															   	   		   .pingProviders(form.isPingProviders());
		if (metadata.containsKey(LegacyCommonConstants.KEY_SECURITY)) {
			builder.security(metadata.get(LegacyCommonConstants.KEY_SECURITY).equalsIgnoreCase(LegacyCommonConstants.SECURITY_VALUE_TOKEN) ? 
							 ServiceSecurityType.TOKEN : ServiceSecurityType.CERTIFICATE);
			metadata.remove(LegacyCommonConstants.KEY_SECURITY);
		}
		
		if (metadata.containsKey(LegacyCommonConstants.KEY_MIN_VERSION) || metadata.containsKey(LegacyCommonConstants.KEY_MAX_VERSION)) {
			final String minVersionStr = metadata.get(LegacyCommonConstants.KEY_MIN_VERSION);
			final String maxVersionStr = metadata.get(LegacyCommonConstants.KEY_MAX_VERSION);
			
			Integer minVersion = null, maxVersion = null;
			try {
				minVersion = Utilities.isEmpty(minVersionStr) ? null : Integer.parseInt(minVersionStr);
			} catch (final NumberFormatException ex) {
				// intentionally ignored
			}
			
			try {
				maxVersion = Utilities.isEmpty(maxVersionStr) ? null : Integer.parseInt(maxVersionStr);
			} catch (final NumberFormatException ex) {
				// intentionally ignored
			}

			builder.version(minVersion, maxVersion);
			metadata.remove(LegacyCommonConstants.KEY_MIN_VERSION);
			metadata.remove(LegacyCommonConstants.KEY_MAX_VERSION);
			
			if (metadata.size() > 0) {
				builder.metadata(metadata);
			}
		}
		
		return builder.build();
	}
	
	//-------------------------------------------------------------------------------------------------
	public static LegacyServiceQueryResult convertServiceQueryResultDTOToLegacyServiceQueryResult(final ServiceQueryResultDTO dto) {
		final LegacyServiceQueryResult result = new LegacyServiceQueryResult();
		final List<LegacyServiceRegistryEntry> resultList = new ArrayList<LegacyServiceRegistryEntry>(dto.getServiceQueryData().size());
		
		for (final ServiceRegistryResponseDTO srDTO : dto.getServiceQueryData()) {
			resultList.add(convertServiceRegistryResponseDTOToLegacyServiceRegistryEntry(srDTO));
		}
		
		result.setServiceQueryData(resultList);
		
		return result;
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