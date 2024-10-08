package com.exe201.ilink.model.exception;

import com.exe201.ilink.Util.DateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseBuilder {

    public static ResponseEntity<Object> responseBuilderWithData(HttpStatus httpStatus, String message, Object responseObject) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("http_status", httpStatus.value());
        response.put("time_stamp", DateUtil.formatTimestamp(new Date()));
        response.put("message", message);
        response.put("data", responseObject);

        return new ResponseEntity<>(response, httpStatus);

    }

    public static ResponseEntity<Object> responseBuilder(HttpStatus httpStatus, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("http_status", httpStatus.value());
        response.put("time_stamp", DateUtil.formatTimestamp(new Date()));
        response.put("message", message);
        return new ResponseEntity<>(response, httpStatus);
    }


}
