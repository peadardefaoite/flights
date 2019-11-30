package pw.peterwhite.flights.helpers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * This class is necessary for the JSON Serializing for the API Response into the LocalDateTime format of
 * 'YYYY-MM-DDTHH:MM' eg '2019-12-11T12:34'. Otherwise the JSON serializer will marshall LocalDateTime fields into JSON
 * rather than a String format as above.
 *
 * This is only used in test code.
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime> {
    public JsonElement serialize(LocalDateTime dateTime, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(dateTime.format(DateTimeFormatter.ISO_DATE_TIME));
    }
}