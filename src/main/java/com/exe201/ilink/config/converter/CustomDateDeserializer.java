package com.exe201.ilink.config.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException {
        String dateAsString = jsonParser.getText();
        try {
            // Xử lý định dạng và bỏ phần múi giờ, mili giây
            return dateFormat.parse(dateAsString.replace("T", " ").split("\\.")[0]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
