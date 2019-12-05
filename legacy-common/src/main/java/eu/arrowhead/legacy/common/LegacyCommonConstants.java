package eu.arrowhead.legacy.common;

public class LegacyCommonConstants {

	//=================================================================================================
	// members
	
	public static final String SERVER_ADDRESS = "server.address";
	public static final String $SERVER_ADDRESS = "${" + SERVER_ADDRESS + "}";
	public static final String SERVER_PORT = "server.port";
	public static final String $SERVER_PORT = "${" + SERVER_PORT + "}";
	public static final String DOMAIN_NAME = "domain.name";
	public static final String $DOMAIN_NAME = "${" + DOMAIN_NAME + ":}";
	public static final String DOMAIN_PORT = "domain.port";
	public static final String $DOMAIN_PORT = "${" + DOMAIN_PORT + ":0}";
	
	public static final long CONVERSION_MILLISECOND_TO_SECOND = 1000;

	//=================================================================================================
	// assistant methods
	
	//-------------------------------------------------------------------------------------------------
	private LegacyCommonConstants() {
		throw new UnsupportedOperationException();
	}
}