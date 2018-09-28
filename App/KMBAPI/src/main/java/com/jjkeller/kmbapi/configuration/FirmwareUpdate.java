package com.jjkeller.kmbapi.configuration;

import com.google.gson.annotations.SerializedName;

import java.text.NumberFormat;
import java.text.ParseException;

public class FirmwareUpdate {

	//NOTE: The app uses Gson to deserialize JSON into this class.  It populates it using field names - the JSON needs to match the field names here.
	@SerializedName("autoInstall")
    private boolean autoInstall = false;
    /// <summary>
    /// This property controls where the firmware update is automatically applied
    /// at system startup time.
    /// </summary>
    //[XmlAttribute("autoinstall")]
    public boolean getAutoInstall()
    {
        return autoInstall;
    }
    public void setAutoInstall(boolean value)
    {
    	autoInstall = value;
    }

    @SerializedName("version")
    private String version = null;
    /// <summary>
    /// This property is the version number of the firmware update.
    /// It's important that this property exactly match what gets
    /// returned from the EOBR API method TechnicianIF_GetEOBRRevisions for the
    /// 'mainFirmwareRevision'.
    /// </summary>
    //[XmlAttribute("version")]
    public String getVersion()
    {
        return version;
    }
    public void setVersion(String value)
    {
    	version = value;
	}

    @SerializedName("updateBootLoader")
    private boolean updateBootLoader = false;
    /// <summary>
    /// This property controls where the firmware includes an update to the 
    /// bootloader code.
    /// </summary>
    //[XmlAttribute("updateLoader")]
    public boolean getUpdateBootLoader()
    {
        return updateBootLoader;
    }
    public void setUpdateBootLoader(boolean value)
    {
    	updateBootLoader = value;
    }

    @SerializedName("readHistoryFirst")
    private boolean readHistoryFirst = false;
    /// <summary>
    /// This property controls where the KMBHistoryReader is used before
    /// the firmware is updated
    /// </summary>
    //[XmlAttribute("readHistory")]
    public boolean getReadHistoryFirst()
    {
        return readHistoryFirst;
    }
    public void setReadHistoryFirst(boolean value)
    {
    	readHistoryFirst = value;
    }

	@SerializedName("forceUpdate")
	private boolean forceUpdate = false;
	/// <summary>
	/// This property controls where the firmware will be forced to download
	/// with a version check
	/// </summary>
	//[XmlAttribute("forceUpdate")]
	public boolean getForceUpdate()
	{
		return forceUpdate;
	}
	public void setForceUpdate(boolean value)
	{
		forceUpdate = value;
	}

    @SerializedName("preventOverwrite")
    private boolean preventOverwrite = false;
    /// <summary>
    /// This property controls whether or not this version of firmware can be overwritten
	/// by the default version.
    /// </summary>
    //[XmlAttribute("preventOverwrite")]
    public boolean getPreventOverwrite()
    {
        return preventOverwrite;
    }
    public void setPreventOverwrite(boolean value)
    {
		preventOverwrite = value;
    }

	@SerializedName("defaultVersion")
	private boolean defaultVersion = false;
	/// <summary>
	/// Whether or not KMB should consider this version of firmware the default
	/// </summary>
	//[XmlAttribute("defaultVersion")]
	public boolean getDefaultVersion()
	{
		return defaultVersion;
	}
	public void setDefaultVersion(boolean value)
	{
		defaultVersion = value;
	}

	@SerializedName("imageFileName")
	private String imageFileName = null;
	/// <summary>
	/// This property controls where the firmware will be forced to download
	/// with a version check
	/// </summary>
	//[XmlAttribute("imageFileName")]
	public String getImageFileName()
	{
		return imageFileName;
	}
	public void setImageFileName(String value)
	{
		imageFileName = value;
	}

    @SerializedName("generation")
    private int generation;
	public int getGeneration() {
		return generation;
	}
	public void setGeneration(int generation) {
		this.generation = generation;
	}
    
	@SerializedName("maker")
	private String maker;
	public String getMaker() {
		return maker;
	}
	public void setMaker(String maker) {
		this.maker = maker;
	}
	
	@SerializedName("firmwarePatchId")
	private long firmwarePatchId;
	public long getFirmwarePatchId() {
		return firmwarePatchId;
	}
	public void setFirmwarePatchId(long firmwarePatchId) {
		this.firmwarePatchId = firmwarePatchId;
	}
	
	/***
	 * This is intended to be the currently installed firmware version of the ELD
	 */
	private String installedVersion;
	public String getInstalledVersion() {
		return installedVersion;
	}
	public void setInstalledVersion(String value) {
		this.installedVersion = value;
	}

	/***
	 * This is intended to signify whether firmware upgrade is conditional
	 */
	private boolean isConditional;
	public boolean getIsConditional() {
		return isConditional;
	}
	public void setIsConditional(boolean value) {
		this.isConditional = value;
	}

	/***
	 * This is intended to be the currently installed firmware version of the ELD
	 */
	public double getInstalledVersionNumber() {
		double versionNumber = 0.0;
		
		if(installedVersion == null || installedVersion.length() == 0)
			return 0.0;
		else{
			
			try{
				// attempt to take the version number string and convert to a double
				NumberFormat nf = NumberFormat.getNumberInstance();
				versionNumber = nf.parse(installedVersion).doubleValue();
			}
			catch(ParseException ex){
				// ignore exception
			}			
		}
		return versionNumber;
	}

	/***
	 * Answer whether this firmware configuration supports large packet download for firmware update
	 * @return
	 */
	public boolean getSupportsLargeBlockDownload(){
		boolean supportLargePacketDownload = false;
		
		if(this.getGeneration() == 2 && this.getMaker() != null)
		{
			// JJKA Gen 2 ELD supports large packet download starting with FW 6.70
			if(this.getMaker().equalsIgnoreCase("jjka"))
				supportLargePacketDownload = this.getInstalledVersionNumber() >= 6.70;

			// NWF BTE supports large packet download starting with FW PatchId 3551111064 (2.22)		
			else if(this.getMaker().equalsIgnoreCase("networkfleet"))
				supportLargePacketDownload = this.getInstalledVersionNumber() >= 3551111064.0;
		}
		
		return supportLargePacketDownload;
	}
}
