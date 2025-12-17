package com.t4app.t4syncwave.conection.model;


import android.util.Log;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class ApiError {
    private boolean status;
    private String message;
    private String simpleMsg;
    private Map<String, List<String>> detailedMsg;
    private Map<String, List<String>> errors;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSimpleMsg() {
        return simpleMsg;
    }

    public Map<String, List<String>> getDetailedMsg() {
        return detailedMsg;
    }

    public Map<String, List<String>> getErrors() {
        return errors;
    }

    public static class ApiErrorDeserializer implements JsonDeserializer<ApiError> {
        @Override
        public ApiError deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            ApiError error = new ApiError();
            JsonObject obj = json.getAsJsonObject();

            if (obj.has("status")) {
                error.status = obj.get("status").getAsBoolean();
            } else if (obj.has("success")) {
                error.status = obj.get("success").getAsBoolean();
            }

            if (obj.has("message") && obj.get("message").isJsonPrimitive()) {
                error.message = obj.get("message").getAsString();
            }

            if (obj.has("msg")) {
                JsonElement msgElement = obj.get("msg");
                if (msgElement.isJsonPrimitive()) {
                    error.simpleMsg = msgElement.getAsString();
                } else if (msgElement.isJsonObject()) {
                    Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
                    error.detailedMsg = context.deserialize(msgElement, mapType);
                }
            }

            if (obj.has("error")) {
                JsonElement msgElement = obj.get("error");
                if (msgElement.isJsonPrimitive()) {
                    error.simpleMsg = msgElement.getAsString();
                } else if (msgElement.isJsonObject()) {
                    Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
                    error.detailedMsg = context.deserialize(msgElement, mapType);
                }
            }

            if (obj.has("errors") && obj.get("errors").isJsonObject()) {
                Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
                error.errors = context.deserialize(obj.get("errors"), mapType);
            }

            if (obj.has("data") && obj.get("data").isJsonObject()) {
                Log.d("TAG_TEST_ERROR", "deserialize:  HAS DATA ");
                JsonObject objData = obj.get("data").getAsJsonObject();
                if (objData.has("errors") && objData.get("errors").isJsonObject()) {
                    Type mapType = new TypeToken<Map<String, List<String>>>() {}.getType();
                    error.errors = context.deserialize(objData.get("errors"), mapType);
                }
            }

            return error;
        }
    }
}

