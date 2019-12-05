package eu.arrowhead.legacy.sr;

import org.springframework.stereotype.Component;

import eu.arrowhead.legacy.common.LegacyAppInitListener;

@Component
public class LegacyServiceRegistryAppInitListener extends LegacyAppInitListener {

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	@Override
	protected String getSystemName() {
		return "Legacy Service Registry Translator";
	}
}