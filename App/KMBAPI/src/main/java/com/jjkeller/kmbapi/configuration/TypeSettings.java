package com.jjkeller.kmbapi.configuration;

public class TypeSettings {

    private String _type = "";
    public TypeSettings(String type)
    {
		setType(type);
	}
	/// <summary>
    /// This is the string used by System.Reflection to instantiate types
    /// The format of this string is the following:
    /// type="typename, assembly".   
    /// For example: type="JJKA.Project.ClassName, JJKA.Project"
    /// </summary>
    public String getType()
    {
        return _type;
    }
    public void setType(String value)
    {
    	_type = value;
        String[] tokens = value.split(",");
        if (tokens.length >= 2)
        {
            _typeName = tokens[0].trim();
            _assemblyName = tokens[1].trim();
        }
    }

    private String _typeName;
    /// <summary>
    /// The type that will be instantiated.
    /// </summary>
    public String getTypeName()
    {
        return _typeName;
    }
    public void setTypeName(String value)
    {
    	_typeName = value;
    }

    private String _assemblyName;
    /// <summary>
    /// The assembly that holds the type to be instantiated
    /// </summary>
    public String getAssemblyName()
    {
        return _assemblyName;
    }
    public void setAssemblyName(String value)
    {
    	_assemblyName = value;
    }
}
