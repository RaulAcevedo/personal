package com.jjkeller.kmbapi.proxydata;

import com.jjkeller.kmbapi.enums.InspectionDefectType;
import com.jjkeller.kmbapi.enums.InspectionTypeEnum;

import java.util.Date;

public class VehicleInspection extends ProxyBase {

	///////////////////////////////////////////////////////////////////////////////////////
	// private members
	///////////////////////////////////////////////////////////////////////////////////////
	private String eobrSerialNumber;
	private String tractorNumber;
	private String trailerNumber;
	private Date inspectionTimestamp;
	private boolean isConditionSatisfactory = true;
	private boolean areDefectsCorrected = false;
	private boolean areCorrectionsNotNeeded = false;
	private String reviewedByName;
	private Date reviewedByDate;
	private String notes;
	private boolean isPoweredUnit = true;
	private int createdByUserKey;
	private InspectionTypeEnum inspectionType = new InspectionTypeEnum(InspectionTypeEnum.NULL);
	private String certifiedByName;
	private Date certifiedByDate;
	//private boolean isSubmitted;
	private Date submitTimestamp;
	private String reviewedByEmployeeId;
	private DefectList defectList = new DefectList();
	private float odometerReading = 0.0F;
	
	///////////////////////////////////////////////////////////////////////////////////////
	// public get/set methods
	///////////////////////////////////////////////////////////////////////////////////////
	public String getSerialNumber()
	{
		return this.eobrSerialNumber;
	}
	public void setSerialNumber(String serialNumber)
	{
		this.eobrSerialNumber = serialNumber;
	}

	public String getTractorNumber()
	{
		return this.tractorNumber;
	}
	public void setTractorNumber(String tractorNumber)
	{
		this.tractorNumber = tractorNumber;
	}
	
	public String getTrailerNumber()
	{
		return this.trailerNumber;
	}
	public void setTrailerNumber(String trailerNumber)
	{
		this.trailerNumber = trailerNumber;
	}
	
	public Date getInspectionTimeStamp()
	{
		return this.inspectionTimestamp;
	}
	public void setInspectionTimeStamp(Date inspectionTimestamp)
	{
		this.inspectionTimestamp = inspectionTimestamp;
	}
	
	public void setInspectionOdometer(float inspectionOdometer)
	{
		this.odometerReading = inspectionOdometer;
	}
	public float getInspectionOdometer()
	{
		return this.odometerReading;
	}
	
	public boolean getIsConditionSatisfactory() {
		return isConditionSatisfactory;
	}
	public void setIsConditionSatisfactory(boolean isConditionSatisfactory) {
		this.isConditionSatisfactory = isConditionSatisfactory;
	}
	
	public boolean getAreDefectsCorrected() {
		return areDefectsCorrected;
	}
	public void setAreDefectsCorrected(boolean areDefectsCorrected) {
		this.areDefectsCorrected = areDefectsCorrected;
	}
	
	public boolean getAreCorrectionsNotNeeded() {
		return areCorrectionsNotNeeded;
	}
	public void setAreCorrectionsNotNeeded(boolean areCorrectionsNotNeeded) {
		this.areCorrectionsNotNeeded = areCorrectionsNotNeeded;
	}
	
	public String getReviewedByName(){
		return reviewedByName;
	}
	public void setReviewedByName(String reviewedByName)
	{
		this.reviewedByName = reviewedByName;
	}
	
	public Date getReviewedByDate()
	{
		return this.reviewedByDate;
	}
	public void setReviewedByDate(Date reviewedByDate)
	{
		this.reviewedByDate = reviewedByDate;
	}
	
	public String getNotes(){
		return notes;
	}
	public void setNotes(String notes)
	{
		this.notes = notes;
	}
	
	public boolean getIsPoweredUnit() {
		return isPoweredUnit;
	}
	public void setIsPoweredUnit(boolean isPoweredUnit) {
		this.isPoweredUnit = isPoweredUnit;
	}

	public int getCreatedByUserKey()
	{
		return createdByUserKey;
	}
	public void setCreatedByUserKey(int createByUserKey)
	{
		this.createdByUserKey = createByUserKey;
	}

	public InspectionTypeEnum getInspectionTypeEnum()
	{
		return this.inspectionType;
	}
	public void setInspectionTypeEnum(InspectionTypeEnum inspectionType)
	{
		this.inspectionType = inspectionType;
	}
	
	public String getCertifiedByName()
	{
		return this.certifiedByName;
	}
	public void setCertifiedByName(String certifiedByName)
	{
		this.certifiedByName = certifiedByName;
	}
	
	public Date getCertifiedByDate()
	{
		return this.certifiedByDate;
	}
	public void setCertifiedByDate(Date certifiedByDate)
	{
		this.certifiedByDate = certifiedByDate;
	}
	
	//public boolean getIsSubmitted()
	//{
	//	return this.isSubmitted;
	//}
	//public void setIsSubmitted(boolean isSubmitted)
	//{
	//	this.isSubmitted = isSubmitted;
	//}
	
	public Date getSubmitTimestamp()
	{
		return this.submitTimestamp;
	}
	public void setSubmitTimestamp(Date submitTimestamp)
	{
		this.submitTimestamp = submitTimestamp;
	}
	
	public String getReviewedByEmployeeId()
	{
		return this.reviewedByEmployeeId;
	}
	public void setReviewedByEmployeeId(String reviewedByEmployeeId)
	{
		this.reviewedByEmployeeId = reviewedByEmployeeId;
	}
	
	public DefectList getDefectList()
	{
		return this.defectList;
	}
	public void setDefectList(DefectList defectList)
	{
		this.defectList = defectList;
	}
	
	
	public void RemoveAllDefects(){
        if (this.getDefectList() != null)
            this.getDefectList().ClearAllDefects();		
	}
	
	public void AddDefect(InspectionDefectType defect){
        if (this.getDefectList() == null)
            this.setDefectList(new DefectList());
        this.getDefectList().Add(defect);	
	}
	
	public void RemoveDefect(InspectionDefectType defect){
        if (this.getDefectList() == null)
            this.setDefectList(new DefectList());
        this.getDefectList().Remove(defect);		
	}
	
	public boolean ContainsDefect(InspectionDefectType defect){
		boolean contains = false;
		if (this.getDefectList() != null)
            contains = this.getDefectList().Contains(defect);
		return contains;
	}
	
	public int[] GetSerializableDefectList() {
		if (this.getDefectList() == null) return null;
        return this.getDefectList().GetSerializableDefectList();
	}
	public void PutSerializableDefectList(int[] defectList) {		
		if (this.getDefectList() == null)
            this.setDefectList(new DefectList());
        this.getDefectList().PutSerializableDefectList(defectList);
	}
}
