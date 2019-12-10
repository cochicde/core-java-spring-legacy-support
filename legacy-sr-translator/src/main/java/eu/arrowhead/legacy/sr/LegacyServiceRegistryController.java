package eu.arrowhead.legacy.sr;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryResultDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.common.exception.ArrowheadException;
import eu.arrowhead.common.exception.BadPayloadException;
import eu.arrowhead.legacy.common.model.LegacyModelConverter;
import eu.arrowhead.legacy.common.model.LegacyServiceQueryFrom;
import eu.arrowhead.legacy.common.model.LegacyServiceQueryResult;
import eu.arrowhead.legacy.common.model.LegacyServiceRegistryEntry;
import eu.arrowhead.legacy.sr.driver.LegacyServiceRegistryDriver;

@RestController
@RequestMapping(CommonConstants.SERVICE_REGISTRY_URI)
public class LegacyServiceRegistryController {
	
	//=================================================================================================
	// members
	
	private static final String REMOVE_URI = "/remove";
	private static final String REGISTER_INPUT_JSON_KEY_SERVICE_DEFINITION = "serviceDefinition";
	
	@Autowired
	private LegacyServiceRegistryDriver legacyDriver;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "The REAL Service Registry says: " + legacyDriver.echo();
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> registerService(@RequestBody final Map<String,Object> request) {
		final String requestStr = Utilities.toJson(request);
		
		if (request.containsKey(REGISTER_INPUT_JSON_KEY_SERVICE_DEFINITION)) {
			return registerService413(requestStr);
		} else {
			return registerService412(requestStr);
		}
	}

	//-------------------------------------------------------------------------------------------------
	@PutMapping(path = REMOVE_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> removeService(@RequestBody final LegacyServiceRegistryEntry entry) {
		final ServiceRegistryResponseDTO dbEntry = legacyDriver.getServiceRegistryEntry(entry);
		if (dbEntry == null) {
			return new ResponseEntity<LegacyServiceRegistryEntry>(entry, HttpStatus.NO_CONTENT);
		}
		
		legacyDriver.removeServiceRegistry412(entry);
		
		return new ResponseEntity<LegacyServiceRegistryEntry>(LegacyModelConverter.convertServiceRegistryResponseDTOToLegacyServiceRegistryEntry(dbEntry), HttpStatus.OK);
	}
	
	//-------------------------------------------------------------------------------------------------
	@DeleteMapping(path = CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_URI)
	public void unregisterService(@RequestParam(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_SERVICE_DEFINITION) final String serviceDefinition,
								  @RequestParam(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_SYSTEM_NAME) final String providerName,
								  @RequestParam(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_ADDRESS) final String providerAddress,
								  @RequestParam(CommonConstants.OP_SERVICE_REGISTRY_UNREGISTER_REQUEST_PARAM_PROVIDER_PORT) final int providerPort) {
		legacyDriver.removeServiceRegistry413(serviceDefinition, providerName, providerAddress, providerPort);
	}
	
	//-------------------------------------------------------------------------------------------------
	@PostMapping(path = CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public ServiceQueryResultDTO queryRegistry413(@RequestBody final ServiceQueryFormDTO form) {
		return legacyDriver.queryRegistry413(form);
	}
	
	//-------------------------------------------------------------------------------------------------
	@PutMapping(path = CommonConstants.OP_SERVICE_REGISTRY_QUERY_URI, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody public LegacyServiceQueryResult queryRegistry412(@RequestBody final LegacyServiceQueryFrom form) {
		return legacyDriver.queryRegistry412(form);
	}
	
	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<?> registerService412(final String requestStr) {
		LegacyServiceRegistryEntry request;
		try {
			request = Utilities.fromJson(requestStr, LegacyServiceRegistryEntry.class);
		} catch (final ArrowheadException ex) {
			throw new BadPayloadException("Invalid input JSON.", org.apache.http.HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI);
		}
		
		return legacyDriver.registerService412(request);
	}

	//-------------------------------------------------------------------------------------------------
	private ResponseEntity<?> registerService413(final String requestStr) {
		ServiceRegistryRequestDTO request;
		try {
			request = Utilities.fromJson(requestStr, ServiceRegistryRequestDTO.class);
		} catch (final ArrowheadException ex) {
			throw new BadPayloadException("Invalid input JSON.", org.apache.http.HttpStatus.SC_BAD_REQUEST, CommonConstants.SERVICE_REGISTRY_URI + CommonConstants.OP_SERVICE_REGISTRY_REGISTER_URI);
		}
		
		return legacyDriver.registerService413(request);
	}
}