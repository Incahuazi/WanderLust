package Entity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Erik.Rans on 7/06/2017.
 */

public class Encounter {
    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public String Name;
    public String Message;
    public String LocationCity;
    public String LocationCountry;
    public String LocationLatLong;
    public EncounterPicture[] encounterPicture = new EncounterPicture[3];
    public String InsertedTimeStamp;
}


