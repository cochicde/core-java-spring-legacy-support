package eu.arrowhead.legacy.sr.driver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.legacy.common.LegacySystemRegistrationProperties;

@Service
public class LegacyServiceRegistryDriver {

	//=================================================================================================
	// members
	
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
}