package eu.arrowhead.legacy.orch.driver;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.util.Base64;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.legacy.common.LegacyCommonConstants;

@Component
public class LegacyTokenGenerator {
	
	//=================================================================================================
	// members
	
	@Resource(name = CommonConstants.ARROWHEAD_CONTEXT)
	private Map<String,Object> arrowheadContext;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private final Logger logger = LogManager.getLogger(LegacyTokenGenerator.class);
	
	
	//-------------------------------------------------------------------------------------------------
	public Entry<String, String> generateLegacyToken(final String consumerCloudOperator, final String consumerCloudName, final String ConsumerSystemName, final String providerAuthInfo, 
													 final String serviceDefinition, final String serviceInterface) {
		// Cryptographic object initializations
		Security.addProvider(new BouncyCastleProvider());
	    Cipher cipher;
	    try {
	    	cipher = Cipher.getInstance("RSA/NONE/PKCS1Padding", "BC");
	    } catch (NoSuchAlgorithmException | NoSuchPaddingException | NoSuchProviderException e) {
	    	logger.fatal("Cipher.getInstance(String) throws exception, code needs to be changed!");
	    	throw new AssertionError("Cipher.getInstance(String) throws exception, code needs to be changed!", e);
	    }
	    Signature signature;
	    try {
	      signature = Signature.getInstance("SHA256withRSA", "BC");
	      signature.initSign((PrivateKey) arrowheadContext.get(LegacyCommonConstants.AUTHORIZATION_PRIVATE_KEY));
	    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
	      logger.fatal("Signature.getInstance(String) throws exception, code needs to be changed!");
	      throw new AssertionError("Signature.getInstance(String) throws exception, code needs to be changed!", e);
	    } catch (final InvalidKeyException e) {
	      logger.fatal("The private key of the Authorization module is invalid, keystore needs to be changed!");
	      throw new ServiceConfigurationError("The private key of the Authorization module is invalid, keystore needs to be changed!", e);
	    }
	    
	    // Create token
	    final PublicKey providerPublicKey = Utilities.getPublicKeyFromBase64EncodedString(providerAuthInfo);
	    final RawTokenInfo rawTokenInfo = new RawTokenInfo();
	    
	    // Set consumer info string
	    String c = ConsumerSystemName;
	    if (consumerCloudName != null && consumerCloudOperator != null) {
	    	c = c.concat(".").concat(consumerCloudName).concat(".").concat(consumerCloudOperator);
	    } else {
	    	final String ownCloudName = (String) arrowheadContext.get(LegacyCommonConstants.OWN_CLOUD_NAME);
	    	final String ownCloudOperator = (String) arrowheadContext.get(LegacyCommonConstants.OWN_CLOUD_OPERATOR);
	    	c = c.concat(".").concat(ownCloudName).concat(".").concat(ownCloudOperator);
	    }
	    rawTokenInfo.setC(c);
	    
	    // Set service info string
	    rawTokenInfo.setS(serviceInterface + "." + serviceDefinition);
	    		
		// Set the token validity duration (Translator not supports token duration!)
		rawTokenInfo.setE(0L);
		
		// There is an upper limit for the size of the token info
		String json = null;
		try {
			json = objectMapper.writer().writeValueAsString(rawTokenInfo);
			if (json.length() > 244) {
				logger.debug("Legacy ArrowheadToken exceeded the size limit");
				return null;
			}
		} catch (final JsonProcessingException e) {
			logger.debug("Legacy RawTokenInfo serialization failed");
			return null;
		}
		
		// Finally, generate the token and signature strings
		String signatureString = null;
		String tokenString = null;
		try {
			
			cipher.init(Cipher.ENCRYPT_MODE, providerPublicKey);
			final byte[] tokenBytes = cipher.doFinal(json.getBytes(StandardCharsets.UTF_8));
			signature.update(tokenBytes);
			final byte[] sigBytes = signature.sign();
			signatureString = Base64.getEncoder().encodeToString(sigBytes);
			tokenString = Base64.getEncoder().encodeToString(tokenBytes);
			
		} catch (final Exception ex) {
			logger.debug("Cipher or Signature class throws public key specific exception: " + ex.getMessage());
		}		
		
		return Map.entry(signatureString, tokenString);
	}
	
	//=================================================================================================
	// nested class
	
	public class RawTokenInfo {
		
		private String s;
		private String c;
		private Long e;
		
		public RawTokenInfo() {}

		public String getS() { return s; }
		public String getC() { return c; }
		public Long getE() { return e; }
		
		public void setS(final String s) { this.s = s; }
		public void setC(final String c) { this.c = c; }
		public void setE(final Long e) { this.e = e; }
	}
}
