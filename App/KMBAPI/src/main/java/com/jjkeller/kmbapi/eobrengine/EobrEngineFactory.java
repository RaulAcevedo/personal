package com.jjkeller.kmbapi.eobrengine;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.eobrengine.eobrreader.BTGenI;
import com.jjkeller.kmbapi.eobrengine.eobrreader.BTGenII;
import com.jjkeller.kmbapi.featuretoggle.IFeatureToggleService;
import com.jjkeller.kmbapi.geotabengine.GeotabEngine;
import com.jjkeller.kmbapi.kmbeobr.Constants;
import com.jjkeller.kmbapi.kmbeobr.Thresholds;
import com.jjkeller.kmbapi.proxydata.CompanyConfigSettings;

public class EobrEngineFactory {

	private static GeotabEngine _geotabEngine = null;

	public static IEobrEngine ForGeneration(int generation, Context context) {
		switch(generation) {
			case 1:
				return ForBTGenI();
			case 2:
				return ForBTGenII();
			case Constants.GENERATION_GEOTAB: {
				IFeatureToggleService featureToggleService = GlobalState.getInstance().getFeatureService();
				CompanyConfigSettings settings = GlobalState.getInstance().getCompanyConfigSettings(context);
				Thresholds thresholds = settings.toThresholds(featureToggleService.getIsEldMandateEnabled());

				return ForGeotab(context, thresholds);
			}
		}

		return null;
	}

	public static IEobrEngine ForBTGenI(){
		return new BTGenI();
	}

	public static IEobrEngine ForBTGenII() {
		return new BTGenII();
	}

	public static IEobrEngine ForGeotab(Context context, Thresholds thresholds) {
		if(_geotabEngine == null)
			_geotabEngine = new GeotabEngine(context, thresholds);

		return _geotabEngine;
	}

	public static int DetermineGenerationFor(IEobrEngineBluetooth reader){
		int returnVal = 0;
		
		Class klass = reader.getClass();
		if(klass == BTGenI.class)
			returnVal = Constants.GENERATION_GEN_I;
		else if(klass == BTGenII.class)
			returnVal = Constants.GENERATION_GEN_II;
		else if(klass == GeotabEngine.class)
			returnVal = Constants.GENERATION_GEOTAB;
		
		return returnVal;
	}
}
