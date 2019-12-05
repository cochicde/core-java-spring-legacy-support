package eu.arrowhead.legacy.sr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.legacy.sr.driver.LegacyServiceRegistryDriver;

@RestController
@RequestMapping(CommonConstants.SERVICE_REGISTRY_URI)
public class LegacyServiceRegistryController {
	
	//=================================================================================================
	// members
	
	@Autowired
	private LegacyServiceRegistryDriver legacyDriver;

	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "The REAL Service Registry says: " + legacyDriver.echo();
	}
}