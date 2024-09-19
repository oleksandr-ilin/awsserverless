package com.task10.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.SignUp;
import com.task10.data.SimpleResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;

public class PostSignUpHandler extends CognitoAwareHandler {
    public PostSignUpHandler(ObjectMapper objectMapper, CognitoIdentityProviderClient cognitoClient) {
        super(objectMapper, cognitoClient);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            SignUp signUp = fromJson(requestEvent.getBody(), SignUp.class);

            logger.log("signUp request for: " + signUp);
            // sign up
            String userId = cognitoSignUp(signUp)
                    .user().attributes().stream()
                    .filter(attr -> attr.name().equals("sub"))
                    .map(AttributeType::value)
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("Sub not found."));

            logger.log("signUp request get userId: " + userId);

            // confirm sign up
            String idToken = confirmSignUp(signUp)
                    .authenticationResult()
                    .idToken();

            logger.log("signUp request get idToken: " + idToken);


            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new SimpleResponse("User has been successfully signed up: " + signUp.email())));
        } catch (Exception e) {
            logger.log("SignUp Error: " + e);

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Sign up error: " + e)));
        }
    }
}
