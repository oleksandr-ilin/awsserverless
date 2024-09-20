package com.task11.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.data.SignIn;
import com.task11.data.SignInResponse;
import com.task11.data.SimpleResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public class PostSignInHandler extends CognitoAwareHandler {
    public PostSignInHandler(ObjectMapper objectMapper, CognitoIdentityProviderClient cognitoClient) {
        super(objectMapper, cognitoClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {

            SignIn signIn = fromJson(requestEvent.getBody(), SignIn.class);

            logger.log("SignIn request for: " + signIn.email());


            String accessToken = cognitoSignIn(signIn.email(), signIn.password())
                    .authenticationResult()
                    .idToken();

            logger.log("SignIn request receives the token: " + accessToken);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new SignInResponse(accessToken)));
        } catch (Exception e) {
            logger.log("SignIn Error: " + e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Sign in error: " + e)));
        }
    }
}
