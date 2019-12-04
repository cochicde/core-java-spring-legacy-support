package eu.arrowhead.legacy.orch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import eu.arrowhead.common.CommonConstants;

@SpringBootApplication
@ComponentScan(CommonConstants.BASE_PACKAGE)
public class LegacyOrchestratorMain {
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	public static void main(final String[] args) {
		SpringApplication.run(LegacyOrchestratorMain.class, args);
	}
}