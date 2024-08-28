package com.task02;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.Architecture;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;

import java.util.Map;
import java.util.function.Function;

@LambdaHandler(
        lambdaName = "hello_world",
        roleName = "hello_world-role",
        layers = {"sdk-layer"},
        runtime = DeploymentRuntime.JAVA11,
        architecture = Architecture.ARM64,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@LambdaLayer(
        layerName = "sdk-layer",
        libraries = {"lib/commons-lang3-3.14.0.jar", "lib/gson-2.10.1.jar"},
        runtime = DeploymentRuntime.JAVA11,
        architectures = {Architecture.ARM64},
        artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class HelloWorld implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final int SC_OK = 200;
    private static final int SC_BAD_REQUEST = 400;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<String, String> responseHeaders = Map.of("Content-Type", "application/json");
    private final Map<RouteKey, Function<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse>> routeHandlers = Map.of(
            new RouteKey("GET", "/hello"), this::handleGetHello
    );

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent requestEvent, Context context) {
        RouteKey routeKey = new RouteKey(getMethod(requestEvent), getPath(requestEvent));
        return routeHandlers.getOrDefault(routeKey, this::notFoundResponse).apply(requestEvent);
    }

    private APIGatewayV2HTTPResponse handleGetHello(APIGatewayV2HTTPEvent requestEvent) {
        return buildResponse(new Body(SC_OK, "Hello from Lambda"));
    }

    private APIGatewayV2HTTPResponse notFoundResponse(APIGatewayV2HTTPEvent requestEvent) {
        return buildResponse(new Body(SC_BAD_REQUEST, String.format("Bad request syntax or unsupported method. Request path: %s. HTTP method: %s",
                getPath(requestEvent),
                getMethod(requestEvent)
        )));
    }

    private APIGatewayV2HTTPResponse buildResponse(Body body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(body.statusCode)
                .withHeaders(responseHeaders)
                .withBody(gson.toJson(body))
                .build();
    }

    private String getMethod(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getMethod();
    }

    private String getPath(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getPath();
    }

    private class RouteKey {
        public String method;
        public String path;

        public RouteKey(String method, String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RouteKey) {
                RouteKey rk = (RouteKey) obj;
                return method.equals(rk.method) && path.equals(rk.path);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }
    }

    private class Body {
        public int statusCode;
        public String message;

        public Body(int statusCode, String message) {
            this.statusCode = statusCode;
            this.message = message;
        }
    }
}
