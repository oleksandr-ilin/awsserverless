package com.task05.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;

public class HttpUtils {

    public static String getMethod(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getMethod();
    }

    public static String getPath(APIGatewayV2HTTPEvent requestEvent) {
        return requestEvent.getRequestContext().getHttp().getPath();
    }
}
