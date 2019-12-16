package eu.arrowhead.legacy.orch.driver;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.http.HttpService;
import eu.arrowhead.legacy.common.LegacyCommonConstants;

@Service
public class LegacyOrchestratorDriver {
	
	//=================================================================================================
	// members
	
	@Autowired
	private HttpService httpService;
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public String echo() {
		final String scheme = sslEnabled ? CommonConstants.HTTPS : CommonConstants.HTTP;
		UriComponents uri = (UriComponents) arrowheadContext.get(LegacyCommonConstants.ORCHESTRATOR_ORCHESTRATION_URI);
		String address = uri.getHost();
		int port = uri.getPort();
		final UriComponents uriEcho = Utilities.createURI(scheme, address, port, CommonConstants.ORCHESTRATOR_URI + CommonConstants.ECHO_URI);
		final ResponseEntity<String> response = httpService.sendRequest(uriEcho, HttpMethod.GET, String.class);
		
		return response.getBody();
	}

}
