package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public abstract class BaseHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    protected final ObjectMapper objectMapper;

    public BaseHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    protected String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(Objects.requireNonNull(object));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize the object: " + object, e);
        }
    }

    public <T> T fromJson(String json, Class<T> jsonClass) {
        try {
            return objectMapper.readValue(json, jsonClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to deserialize the class: " + json, e);
        }
    }

}
