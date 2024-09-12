package com.task05.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task05.model.CreatedEventResponse;
import com.task05.model.EventRequest;

import java.time.Instant;
import java.util.UUID;

public class PostEventsHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private final String tableName;

    private final DynamoDB dynamoDB;
    private final ObjectMapper objectMapper;

    public PostEventsHandler(ObjectMapper objectMapper, String tableName) {
        this.objectMapper = objectMapper;
        this.tableName = tableName;

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        this.dynamoDB = new DynamoDB(client);
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent request, Context context) {
        LambdaLogger logger = context.getLogger();
        logger.log("Start /events handling: " + tableName);
        String payload = request.getBody();
        try {
            logger.log("payload:" + payload);

            EventRequest eventRequest = objectMapper.readValue(payload, EventRequest.class);

            Item item = new Item()
                    .withPrimaryKey("id", UUID.randomUUID().toString())
                    .withInt("principalId", eventRequest.principalId())
                    .withString("createdAt", Instant.now().toString())
                    .withMap("body", eventRequest.content());

            Table table = dynamoDB.getTable("cmtr-2139af1e-Events-test");
            table.putItem(item);

            logger.log("Event saved:");

            var response = new CreatedEventResponse(201, item.asMap());

            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(201)
                    .withBody(objectMapper.writeValueAsString(response))
                    .build();

        } catch (JsonProcessingException e) {
            logger.log("Error occurred: " + e.toString());
            throw new RuntimeException(e);
        } finally {
            logger.log("The event processed");
        }
    }
}
