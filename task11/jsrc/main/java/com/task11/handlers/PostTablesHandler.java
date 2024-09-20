package com.task11.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.data.OrderedTable;
import com.task11.data.SimpleResponse;
import com.task11.data.TableResponse;

public class PostTablesHandler extends BaseHandler {

    private final String tablesTable;
    private final Table table;

    public PostTablesHandler(ObjectMapper objectMapper) {
        super(objectMapper);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        var dynamoDB = new DynamoDB(client);
        tablesTable = System.getenv("tables_table");
        table = dynamoDB.getTable(tablesTable);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Start adding Tables entry: " + tablesTable);

            OrderedTable orderedTable = fromJson(requestEvent.getBody(), OrderedTable.class);
            logger.log("Found request Tables entry: " + orderedTable);

            Item item = new Item()
                    .withPrimaryKey("id", Integer.toString(orderedTable.id()))
                    .withInt("number", orderedTable.number())
                    .withInt("places", orderedTable.places())
                    .withBoolean("isVip", orderedTable.isVip());

            if (orderedTable.minOrder() != null) {
                item.withInt("minOrder", orderedTable.minOrder());
            }
            logger.log("Item constructed");

            table.putItem(item);

            logger.log("Item saved");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new TableResponse(orderedTable.id())));
        } catch (Exception ex) {
            logger.log("Get tables error: " + ex);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Get tables error: " + ex)));
        }
    }
}
