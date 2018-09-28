package com.jjkeller.kmbapi.eldmandate;

import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.employeelogeldevents.Enums;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by rab5795 on 4/21/2016.
 */
public class EventDataChecksumHelper {
    private static final Map<Character, Integer> charToDecimal;
    static {
        Map<Character, Integer> map = new HashMap<Character, Integer>();
        map.put('1', 1);
        map.put('2', 2);
        map.put('3', 3);
        map.put('4', 4);
        map.put('5', 5);
        map.put('6', 6);
        map.put('7', 7);
        map.put('8', 8);
        map.put('9', 9);
        map.put('A', 17);
        map.put('B', 18);
        map.put('C', 19);
        map.put('D', 20);
        map.put('E', 21);
        map.put('F', 22);
        map.put('G', 23);
        map.put('H', 24);
        map.put('I', 25);
        map.put('J', 26);
        map.put('K', 27);
        map.put('L', 28);
        map.put('M', 29);
        map.put('N', 30);
        map.put('O', 31);
        map.put('P', 32);
        map.put('Q', 33);
        map.put('R', 34);
        map.put('S', 35);
        map.put('T', 36);
        map.put('U', 37);
        map.put('V', 38);
        map.put('W', 39);
        map.put('X', 40);
        map.put('Y', 41);
        map.put('Z', 42);
        map.put('a', 49);
        map.put('b', 50);
        map.put('c', 51);
        map.put('d', 52);
        map.put('e', 53);
        map.put('f', 54);
        map.put('g', 55);
        map.put('h', 56);
        map.put('i', 57);
        map.put('j', 58);
        map.put('k', 59);
        map.put('l', 60);
        map.put('m', 61);
        map.put('n', 62);
        map.put('o', 63);
        map.put('p', 64);
        map.put('q', 65);
        map.put('r', 66);
        map.put('s', 67);
        map.put('t', 68);
        map.put('u', 69);
        map.put('v', 70);
        map.put('w', 71);
        map.put('x', 72);
        map.put('y', 73);
        map.put('z', 74);
        charToDecimal = Collections.unmodifiableMap(map);
    }

    public static String EventDataChecksum(EmployeeLogEldEvent employeeLogEvent, String username)
    {
        String eventDataCheckString = GetEventDataCheckString(employeeLogEvent, username);
        int sumOFAllProperties = SumNumericPropertyCharacters(eventDataCheckString);

        int bottom8Bits = sumOFAllProperties & 0xFF;

        int shifted = ((bottom8Bits << 3) & 0xFF) | (bottom8Bits >>> (8 - 3));

        int xOred = shifted ^ 0xC3;

        String value = Integer.toHexString(xOred);

        if(value.length() == 1){
            value = "0" + value;
        }

        return value;
    }

    //Mandate format HHMMSS <HH> between 00 and 23, <MM> and <SS> must be between 00 and 59
    private static String ConvertDateToEventTime(Date eventDate, boolean isUtc) {
        DateFormat formatTime = new SimpleDateFormat("HHmmss");

        if (isUtc) {
            TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
            formatTime.setTimeZone(utcTimeZone);
        }

        return formatTime.format(eventDate);
    }

    //Mandate format <MMDDYY>
    private static String ConvertDateToEventDate(Date eventDate, boolean isUtc)
    {
        DateFormat formatDate = new SimpleDateFormat("MMddyy");

        if (isUtc) {
            TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
            formatDate.setTimeZone(utcTimeZone);
        }

        return formatDate.format(eventDate);
    }

    private static int CharacterToDecimalMapper(char character) {
        Character c = new Character(character);
        int value = 0;
        if(charToDecimal.containsKey(c))
        {
            value = charToDecimal.get(c);
        }
        return value;
    }

    private static int SumNumericPropertyCharacters(String properyValue)
    {
        int sumValue = 0;
        char[] properyValueArray = properyValue.toCharArray();
        for(char ch : properyValueArray)
        {
            sumValue += CharacterToDecimalMapper(ch);
        }
        return sumValue;
    }

    private static String GetFormattedLatOrLong(Double source, Enums.EmployeeLogEldEventType eventType, int eventCode)
    {
        int precision = 2;
        if (eventType == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown && eventCode == 2
                || eventType == Enums.EmployeeLogEldEventType.EnginePowerUpPowerDown && eventCode == 4
                || eventType == Enums.EmployeeLogEldEventType.IntermediateLog && eventCode == 2)
            precision = 1;

        String format = "#0.0";
        if (precision == 2) format += "0";

        DecimalFormat decimalFormat = new DecimalFormat(format);
        return decimalFormat.format(source);
    }

    private static String GetEventDataCheckString(EmployeeLogEldEvent employeeLogEldEvent, String username){
        String eventString = "";
        boolean hasParentLog = (employeeLogEldEvent.getLogKey() != null && employeeLogEldEvent.getLogKey() != -1);

        eventString += Integer.toString(employeeLogEldEvent.getEventType().getValue());
        eventString += "," + Integer.toString(employeeLogEldEvent.getEventCode());

        eventString += ",";
        if(employeeLogEldEvent.getEventDateTime() != null) {
            eventString += ConvertDateToEventDate(employeeLogEldEvent.getEventDateTime(), !hasParentLog); // employee's timezone
        }

        eventString += ",";
        if(employeeLogEldEvent.getEventDateTime() != null) {
            eventString += ConvertDateToEventTime(employeeLogEldEvent.getEventDateTime(), !hasParentLog); // employee's timezone
        }

        eventString += ",";
        if(employeeLogEldEvent.getAccumulatedVehicleMiles() != null){
            eventString += Integer.toString(employeeLogEldEvent.getAccumulatedVehicleMiles());
        }

        eventString += ",";
        if(employeeLogEldEvent.getEngineHours() != null) {
            DecimalFormat decimalFormat = new DecimalFormat("#.0");
            eventString += decimalFormat.format(employeeLogEldEvent.getEngineHours());
        }

        eventString += ",";
        if(employeeLogEldEvent.getLatitude() != null) {
            eventString += GetFormattedLatOrLong(employeeLogEldEvent.getLatitude(), employeeLogEldEvent.getEventType(), employeeLogEldEvent.getEventCode());
        }

        eventString += ",";
        if(employeeLogEldEvent.getLongitude() != null) {
            eventString += GetFormattedLatOrLong(employeeLogEldEvent.getLongitude(), employeeLogEldEvent.getEventType(), employeeLogEldEvent.getEventCode());
        }

        eventString += ",";
        if(employeeLogEldEvent.getTractorNumber() != null) {
            eventString += employeeLogEldEvent.getTractorNumber();
        }

        eventString += ",";
        if (employeeLogEldEvent.getLogKey() != null && employeeLogEldEvent.getLogKey() != -1)
            eventString += username;

        return eventString;
    }
}
