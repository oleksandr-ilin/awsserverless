package com.task10.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task10.data.OrderedTable;
import com.task10.data.SimpleResponse;
import com.task10.data.TablesResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GetTablesHandler extends BaseHandler {

    private final String tablesTable;

    public GetTablesHandler(ObjectMapper objectMapper) {
        super(objectMapper);
        tablesTable = System.getenv("tables_table");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Start getting Tables entries: " + tablesTable);

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(tablesTable);

            ScanResult result = client.scan(scanRequest);
            logger.log("Scan tables found: " + result.getCount());

            List<OrderedTable> resultTables = new ArrayList<>();
            for (Map<String, AttributeValue> item : result.getItems()) {

                //Get tables error: java.lang.NullPointerException:
                // Cannot invoke "java.lang.Boolean.booleanValue()"
                // because the return value of "com.amazonaws.services.dynamodbv2.model.AttributeValue.isNULL()" is null
                var resultTable = new OrderedTable(
                        Integer.valueOf(item.get("id").getS()),
                        Integer.valueOf(item.get("number").getN()),
                        Integer.valueOf(item.get("places").getN()),
                        item.get("isVip").getBOOL(),
                        item.get("minOrder") == null ? null : Integer.valueOf(item.get("minOrder").getN()));

                resultTables.add(resultTable);

                logger.log("Item added: " + toJson(resultTables));

            }

            logger.log("Success getting tables ");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new TablesResponse(resultTables)));
        } catch (Exception ex) {
            logger.log("Get tables error: " + ex);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Get tables error: " + ex)));
        }
    }
}
