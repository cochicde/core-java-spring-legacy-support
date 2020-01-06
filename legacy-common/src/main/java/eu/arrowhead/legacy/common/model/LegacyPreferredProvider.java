package eu.arrowhead.legacy.common.model;

public class LegacyPreferredProvider {
	
	//=================================================================================================
	// members
	
	private LegacyArrowheadSystem providerSystem;
	private LegacyArrowheadCloud providerCloud;
	
	//=================================================================================================
	// methods
	
	//-------------------------------------------------------------------------------------------------
	public LegacyPreferredProvider() {}
	
	//-------------------------------------------------------------------------------------------------
	public LegacyPreferredProvider(final LegacyArrowheadSystem providerSystem, final LegacyArrowheadCloud providerCloud) {
		this.providerSystem = providerSystem;
		this.providerCloud = providerCloud;
	}

	//-------------------------------------------------------------------------------------------------
	public LegacyArrowheadSystem getProviderSystem() { return providerSystem; }
	public LegacyArrowheadCloud getProviderCloud() { return providerCloud; }

	//-------------------------------------------------------------------------------------------------
	public void setProviderSystem(final LegacyArrowheadSystem providerSystem) { this.providerSystem = providerSystem; }
	public void setProviderCloud(final LegacyArrowheadCloud providerCloud) { this.providerCloud = providerCloud; }
}