package eu.arrowhead.legacy.common.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LegacyArrowheadService implements Serializable {
	
	//=================================================================================================
	// members

	private static final long serialVersionUID = 4195657722095471161L;
	
	private Long id;
    private String serviceDefinition;
    private Set<String> interfaces = new HashSet<>();
    private Map<String,String> serviceMetadata = new HashMap<>();
    
    //=================================================================================================
	// methods
    
    //-------------------------------------------------------------------------------------------------
    public LegacyArrowheadService() {}
    
    //-------------------------------------------------------------------------------------------------
    public LegacyArrowheadService(final Long id, final String serviceDefinition, final Set<String> interfaces, final Map<String, String> serviceMetadata) {
    	this.id = id;
    	this.serviceDefinition = serviceDefinition;
    	this.interfaces = interfaces;
    	this.serviceMetadata = serviceMetadata;
    }
    
    //-------------------------------------------------------------------------------------------------
	public Long getId() { return id; }
	public String getServiceDefinition() { return serviceDefinition; }
    public Set<String> getInterfaces() { return interfaces; }
    public Map<String,String> getServiceMetadata() { return serviceMetadata; }
    
    //-------------------------------------------------------------------------------------------------
	public void setId(final Long id) { this.id = id; }
    public void setServiceDefinition(final String serviceDefinition) { this.serviceDefinition = serviceDefinition; }
    public void setInterfaces(final Set<String> interfaces) { this.interfaces = interfaces; }
    public void setServiceMetadata(final Map<String,String> serviceMetadata) { this.serviceMetadata = serviceMetadata; }
}