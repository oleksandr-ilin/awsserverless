package com.task05.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task05.model.SimpleResponse;
import com.task05.utils.HttpUtils;

public class RouteNotImplementedHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final ObjectMapper objectMapper;

    public RouteNotImplementedHandler(ObjectMapper objectMapper) {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {

        try {
            LambdaLogger logger = context.getLogger();
            logger.log("Not /events request");
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(501)
                    .withBody(objectMapper.writeValueAsString(
                            new SimpleResponse("Handler for the %s method on the %s path is not implemented."
                                    .formatted(HttpUtils.getMethod(requestEvent), HttpUtils.getPath(requestEvent))))
                    )
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
