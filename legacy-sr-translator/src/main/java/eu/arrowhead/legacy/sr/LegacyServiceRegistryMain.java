package eu.arrowhead.legacy.sr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@ComponentScan(CommonConstants.BASE_PACKAGE)
public class LegacyServiceRegistryMain {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		SpringApplication.run(LegacyServiceRegistryMain.class, args);
	}
}