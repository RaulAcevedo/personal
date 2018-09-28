package com.jjkeller.kmbapi.enums;

import android.content.Context;
import android.widget.ArrayAdapter;

public abstract class EnumBase {
    protected int value;

    public EnumBase(int value)
    {
    	setValue(value);
    }

    public int getValue() { return value; }
    
	public void setValue(int value) throws IndexOutOfBoundsException
	{
		throw new IndexOutOfBoundsException("Enum index out of bounds");
	}

    @Override
    public String toString() {
        return "EnumBase{" +
                "value=" + value +
                '}';
    }

    public abstract String toDMOEnum();
	
    protected abstract int getArrayId();

	public String getString(Context ctx)
	{
    	String[] array = ctx.getResources().getStringArray(getArrayId());
    	return array[value];
	}

	@Override
	public boolean equals(Object o) {
		if(o == this) return true;
		if(o == null) return false;
		if(getClass() != o.getClass()) return false;
		
		EnumBase other = (EnumBase)o;
		return this.value == other.value;
	}
	
	@Override
	public int hashCode() {
		return Integer.valueOf(value).hashCode();
	}
}
