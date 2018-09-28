package com.jjkeller.kmbapi.calcengine;

public class Enums
{
	public enum DutyStatusEnum
	{
		OFF, ON, DRV, SLP, OFFWLLST
	}
	
	public enum RuleTypeEnum
	{
		Both, CDOnly, USOnly
	}
	
	public enum RuleSetTypeEnum
	{
		Null, US60Hour, US70Hour, Canadian_Cycle1, Canadian_Cycle2, Florida_7Day, Florida_8Day, Wisconsin_7Day, Wisconsin_8Day, Texas, USOilField, TexasOilField, USMotionPicture_7Day, USMotionPicture_8Day, Alaska_7Day, Alaska_8Day, USConstruction_7Day, USConstruction_8Day, California_MP_80
	}
	
	public enum CanadaDeferralTypeEnum
	{
		None, DayOne, DayTwo
	}
	
	public enum SearchDirectionTypeEnum
	{
		Forward, Backward, Both
	}

	public enum ReconcileChangeRequestedEldEventsEnum {
		ORIGINAL, ACCEPT_PREVIEW, ACCEPT_DATABASE, REJECT_DATABASE
	}
}
