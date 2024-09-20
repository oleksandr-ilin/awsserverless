package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.data.SimpleResponse;

public class RouteNotImplementedHandler extends BaseHandler {

    public RouteNotImplementedHandler(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();

        logger.log("Root not implemented: " + requestEvent);

        return new APIGatewayProxyResponseEvent()
                .withStatusCode(501)
                .withBody(toJson(new SimpleResponse("Handler for the %s method on the %s path is not implemented."
                        .formatted(requestEvent.getHttpMethod(), requestEvent.getPath()))));
    }

}