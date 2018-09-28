package com.jjkeller.kmbapi.wifi;

import android.annotation.TargetApi;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class AccessPointStateCompat
{
	private static final String INTERNAL_PRIVATE_KEY = "private_key";
    private static final String INTERNAL_PHASE2 = "phase2";
    private static final String INTERNAL_PASSWORD = "password";
    private static final String INTERNAL_IDENTITY = "identity";
    private static final String INTERNAL_EAP = "eap";
    private static final String INTERNAL_CLIENT_CERT = "client_cert";
    private static final String INTERNAL_CA_CERT = "ca_cert";
    private static final String INTERNAL_ANONYMOUS_IDENTITY = "anonymous_identity";
    private static final String INTERNAL_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
    private static final Method ENTERPRISE_FIELD_GET_VALUE_METHOD;
    private static final Method ENTERPRISE_FIELD_SET_VALUE_METHOD;
    
    static
    {
		Class[] wcClasses = WifiConfiguration.class.getClasses();
		Class wcEnterpriseField = null;
		for (Class wcClass : wcClasses)
		{
			if (wcClass.getName().equals(INTERNAL_ENTERPRISEFIELD_NAME))
			{
				wcEnterpriseField = wcClass;
				break;
			}
		}

		Method wcefGetValue = null;
		Method wcefSetValue = null;
		if (wcEnterpriseField != null)
		{
			for (Method m : wcEnterpriseField.getMethods())
			{
				if (m.getName().trim().equals("setValue"))
				{
					wcefSetValue = m;
				}
				else if (m.getName().trim().equals("value"))
				{
					wcefGetValue = m;
				}
			}
		}
		
		ENTERPRISE_FIELD_GET_VALUE_METHOD = wcefGetValue;
		ENTERPRISE_FIELD_SET_VALUE_METHOD = wcefSetValue;
    }
    
    /**
     * Returns true if the wifi configuration is an EAP network
     * @param wifiConfiguration
     * @return
     */
    public static boolean isEap(WifiConfiguration wifiConfiguration)
    {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			return isEapApi(wifiConfiguration);
		}
		else
		{
			return isEapCompat(wifiConfiguration);
		}
    }
    
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private static boolean isEapApi(WifiConfiguration wifiConfiguration)
    {
    	return wifiConfiguration.enterpriseConfig != null && wifiConfiguration.enterpriseConfig.getEapMethod() != WifiEnterpriseConfig.Eap.NONE;
    }
    
    private static boolean isEapCompat(WifiConfiguration wifiConfiguration)
    {
    	Object eap = getEnterpriseFieldValue(wifiConfiguration, INTERNAL_EAP);
    	return eap != null && !TextUtils.isEmpty(eap.toString());
    }
	
    /**
     * Updates the EAP password of a wifi configuration
     * @param wifiConfiguration
     * @param password
     */
	public static void setEapPassword(WifiConfiguration wifiConfiguration, String password)
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			setEapPasswordApi(wifiConfiguration, password);
		}
		else
		{
			setEapPasswordCompat(wifiConfiguration, password);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private static void setEapPasswordApi(WifiConfiguration wifiConfiguration, String password)
	{
		if (wifiConfiguration.enterpriseConfig == null)
			wifiConfiguration.enterpriseConfig = new WifiEnterpriseConfig();
		wifiConfiguration.enterpriseConfig.setPassword(password);
	}
	
	private static void setEapPasswordCompat(WifiConfiguration wifiConfiguration, String password)
	{
		try
		{
			setEnterpriseFieldValue(wifiConfiguration, INTERNAL_PASSWORD, password);
		}
		catch (Exception ex)
		{
			Log.d("UnhandledCatch", ex.getMessage(), ex);
		}
	}
	
	/**
	 * Updates all EAP fields other than password for a wifi configuration from an access point state
	 * @param state
	 * @param wifiConfiguration
	 */
	public static void updateWifiConfigurationEapFields(AccessPointState state, WifiConfiguration wifiConfiguration)
	{
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
		{
			updateWifiConfigurationEapFieldsAvailable(state, wifiConfiguration);
		}
		else
		{
			updateWifiConfigurationEapFieldsCompat(state, wifiConfiguration);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private static void updateWifiConfigurationEapFieldsAvailable(AccessPointState state, WifiConfiguration config)
	{
		if (config.enterpriseConfig == null)
			config.enterpriseConfig = new WifiEnterpriseConfig();
		
		// EAP Method
		String eapMethod = state.getEap();
		if (eapMethod != null)
		{
			if (eapMethod.equals("PEAP"))
				config.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.PEAP);
			else if (eapMethod.equals("TLS"))
				config.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TLS);
			else if (eapMethod.equals("TTLS"))
				config.enterpriseConfig.setEapMethod(WifiEnterpriseConfig.Eap.TTLS);
		}
		
		// Phase 2
		String phase2Method = state.getPhase2();
		config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.NONE);
		if (phase2Method != null)
		{
			if (phase2Method.equals("PAP"))
				config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.PAP);
			else if (phase2Method.equals("GTC"))
				config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.GTC);
			else if (phase2Method.equals("MSCHAP"))
				config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAP);
			else if (phase2Method.equals("MSCHAPV2"))
				config.enterpriseConfig.setPhase2Method(WifiEnterpriseConfig.Phase2.MSCHAPV2);
		}
		
		if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.IDENTITY)))
			config.enterpriseConfig.setIdentity(state.getEnterpriseField(AccessPointState.IDENTITY));
		else
			config.enterpriseConfig.setIdentity(null);
		
		if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.ANONYMOUS_IDENTITY)))
			config.enterpriseConfig.setAnonymousIdentity(state.getEnterpriseField(AccessPointState.ANONYMOUS_IDENTITY));
		else
			config.enterpriseConfig.setAnonymousIdentity(null);
		
		// Not supporting CLIENT_CERT
		// Not supporting CA_CERT
		// Not supporting PRIVATE_KEY
	}

	private static void updateWifiConfigurationEapFieldsCompat(AccessPointState state, WifiConfiguration wifiConfiguration)
	{
		try
		{
			setEnterpriseFieldValue(wifiConfiguration, INTERNAL_EAP, state.getEap());
			
			if (!TextUtils.isEmpty(state.getPhase2()))
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_PHASE2, AccessPointState.convertToQuotedString("auth=" + state.getPhase2()));
			else
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_PHASE2, null);
			
			if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.IDENTITY)))
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_IDENTITY, state.getEnterpriseField(AccessPointState.IDENTITY));
			else
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_IDENTITY, null);
			
			if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.ANONYMOUS_IDENTITY)))
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_ANONYMOUS_IDENTITY, state.getEnterpriseField(AccessPointState.ANONYMOUS_IDENTITY));
			else
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_ANONYMOUS_IDENTITY, null);
			
			if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.CLIENT_CERT)))
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_CLIENT_CERT, AccessPointState.convertToQuotedString(state.getEnterpriseField(AccessPointState.CLIENT_CERT)));
			else
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_CLIENT_CERT, null);
			
			if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.CA_CERT)))
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_CA_CERT, AccessPointState.convertToQuotedString(state.getEnterpriseField(AccessPointState.CA_CERT)));
			else
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_CA_CERT, null);
			
			if (!TextUtils.isEmpty(state.getEnterpriseField(AccessPointState.PRIVATE_KEY)))
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_PRIVATE_KEY, AccessPointState.convertToQuotedString(state.getEnterpriseField(AccessPointState.PRIVATE_KEY)));
			else
				setEnterpriseFieldValue(wifiConfiguration, INTERNAL_PRIVATE_KEY, null);
		}
		catch (Exception ex)
		{
			Log.d("UnhandledCatch", ex.getMessage(), ex);
		}
	}
	
	/**
	 * Sets an enterprise field of a wifi configuration based on what is supported.
	 * If the setValue method is available, it is used.
	 * Otherwise, the field is set directly.
	 * @param config
	 * @param fieldName
	 * @param value
	 * @throws Exception
	 */
	private static void setEnterpriseFieldValue(WifiConfiguration config, String fieldName, Object value) throws Exception
	{
		Field[] wcFields = WifiConfiguration.class.getFields();
		for (Field wcField : wcFields)
		{
			if (wcField.getName().equals(fieldName))
			{
				if (ENTERPRISE_FIELD_SET_VALUE_METHOD != null)
				{
					ENTERPRISE_FIELD_SET_VALUE_METHOD.invoke(wcField.get(config), value);
				}
				else
				{
					wcField.set(config, value);
				}
			}
		}
	}
	
	/**
	 * Tries to get the value of a given wifi configuration field.
	 * Returns null if it can't be found.
	 * @param config
	 * @param fieldName
	 * @return
	 */
	private static Object getEnterpriseFieldValue(WifiConfiguration config, String fieldName)
	{
		try
		{
			Field[] wcFields = WifiConfiguration.class.getFields();
			for (Field wcField : wcFields)
			{
				if (wcField.getName().equals(fieldName))
				{
					if (ENTERPRISE_FIELD_GET_VALUE_METHOD != null)
					{
						return ENTERPRISE_FIELD_GET_VALUE_METHOD.invoke(wcField.get(config));
					}
					else
					{
						return wcField.get(config);
					}
				}
			}
		}
		catch (Exception ex)
		{
			Log.d("UnhandledCatch", ex.getMessage(), ex);
		}
		return null;
	}
}
