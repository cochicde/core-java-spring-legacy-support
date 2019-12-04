package eu.arrowhead.legacy.sr;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import eu.arrowhead.common.CommonConstants;

@RestController
@RequestMapping(CommonConstants.SERVICE_REGISTRY_URI)
public class LegacyServiceRegistryController {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = CommonConstants.ECHO_URI)
	public String echoService() {
		return "LEGACY Service Registry got it!";
	}
}