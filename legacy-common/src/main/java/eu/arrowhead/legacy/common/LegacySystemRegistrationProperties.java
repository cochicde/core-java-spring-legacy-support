package eu.arrowhead.legacy.common;

import java.util.ServiceConfigurationError;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;

@Component
public class LegacySystemRegistrationProperties {

	//=================================================================================================
	// members
	
	@Value(CommonConstants.$SERVICE_REGISTRY_ADDRESS_WD)
	private String serviceRegistryAddress;
	
	@Value(CommonConstants.$SERVICE_REGISTRY_PORT_WD)
	private int serviceRegistryPort;

	@Value(LegacyCommonConstants.$SERVER_ADDRESS)
	private String systemAddress;
	
	@Value(LegacyCommonConstants.$SERVER_PORT)
	private int systemPort;
	
	@Value(LegacyCommonConstants.$DOMAIN_NAME)
	private String systemDomainName;
	
	@Value(LegacyCommonConstants.$DOMAIN_PORT)
	private int systemDomainPort;

	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public String getServiceRegistryAddress() { return serviceRegistryAddress; }
	public int getServiceRegistryPort() { return serviceRegistryPort; }
	
	//-------------------------------------------------------------------------------------------------
	public String getSystemDomainName() { 
		if (Utilities.isEmpty(systemDomainName)) {
			return Utilities.isEmpty(systemAddress) ? CommonConstants.LOCALHOST : systemAddress;
		}
		
		return systemDomainName;
	}
	
	//-------------------------------------------------------------------------------------------------
	public int getSystemDomainPort() {
		if (systemDomainPort > 0) {
			return systemDomainPort;
		}
		
		if (systemPort > 0) {
			return systemPort;
		}
		
		throw new ServiceConfigurationError("Please specify a " + LegacyCommonConstants.SERVER_PORT + " in the application.properties file.");
	}
}