package com.task10.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.OrderedTable;
import com.task10.data.SimpleResponse;

public class GetTablesByTableIdHandler extends BaseHandler {

    private final String tablesTable;
    private final Table table;

    public GetTablesByTableIdHandler(ObjectMapper objectMapper) {
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


            var path = requestEvent.getPath();
            var tableIdStr = path.substring(path.lastIndexOf("/") + 1);

            logger.log("Requesting Table by ID: " + tableIdStr);

            GetItemSpec spec = new GetItemSpec()
                    .withPrimaryKey("id", tableIdStr);

            Item item = table.getItem(spec);
            if (item == null) {
                logger.log("Unable to find the item: " + tableIdStr);

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(400)
                        .withBody(toJson(new SimpleResponse("Unable to find table: " + tableIdStr)));
            } else {
                logger.log("Item received: " + item.toJSONPretty());

                var orderTable = new OrderedTable(Integer.valueOf(item.getString("id")),
                        item.getInt("number"),
                        item.getInt("places"),
                        item.getBoolean("isVip"),
                        item.isPresent("minOrder") ? item.getInt("minOrder") : null
                );

                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(200)
                        .withBody(toJson(orderTable));

            }
        } catch (Exception ex) {
            logger.log("Get tables error: " + ex);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Get tables error: " + ex)));
        }
    }

}
