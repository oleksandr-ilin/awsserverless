package com.task05;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task05.handlers.PostEventsHandler;
import com.task05.handlers.RouteNotImplementedHandler;
import com.task05.model.RouteKey;

import java.util.Map;

@LambdaHandler(
        lambdaName = "api_handler",
        roleName = "api_handler-role",
        runtime = DeploymentRuntime.JAVA21,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "table", value = "${target_table}")})
public class ApiHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final Map<RouteKey, RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>> handlersByRouteKey;
    private final RouteNotImplementedHandler routeNotImplementedHandler;

    public ApiHandler() {
        var objectMapper = new ObjectMapper();
        this.handlersByRouteKey = initHandlers(objectMapper);
        this.routeNotImplementedHandler = new RouteNotImplementedHandler(objectMapper);
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Got request: " + request);
        logger.log("Table env var: " + System.getenv("table"));
        return getHandler(request).handleRequest(request, context);
    }


    private RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> getHandler(APIGatewayV2HTTPEvent requestEvent) {
        return handlersByRouteKey.getOrDefault(getRouteKey(requestEvent), routeNotImplementedHandler);
    }

    private RouteKey getRouteKey(APIGatewayV2HTTPEvent requestEvent) {
        //return new RouteKey(HttpUtils.getMethod(requestEvent), HttpUtils.getPath(requestEvent));
        //Workaround for now routeKey and rawPath null values. We have only one endpoint.
        return new RouteKey("POST","/events");
    }

    private Map<RouteKey, RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>> initHandlers(ObjectMapper objectMapper) {
        return Map.of(new RouteKey("POST", "/events"), new PostEventsHandler(objectMapper, System.getenv("table")));
    }

}
