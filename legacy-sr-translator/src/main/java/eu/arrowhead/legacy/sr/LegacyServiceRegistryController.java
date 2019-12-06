package eu.arrowhead.legacy.sr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.ServiceRegistryResponseDTO;
import eu.arrowhead.legacy.common.model.LegacyModelConverter;
import eu.arrowhead.legacy.common.model.LegacyServiceRegistryEntry;
import eu.arrowhead.legacy.sr.driver.LegacyServiceRegistryDriver;

@RestController
@RequestMapping(CommonConstants.SERVICE_REGISTRY_URI)
public class LegacyServiceRegistryController {
	
	//=================================================================================================
	// members
	
	private static final String REMOVE_URI = "/remove";
	
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
}