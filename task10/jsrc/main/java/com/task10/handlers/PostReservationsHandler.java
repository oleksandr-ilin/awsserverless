package com.task10.handlers;

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
import com.task10.data.Reservation;
import com.task10.data.SimpleResponse;

public class PostReservationsHandler  extends BaseHandler {

    private final String reservationsTable;
    private final Table table;

    public PostReservationsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        var dynamoDB = new DynamoDB(client);
        reservationsTable = System.getenv("reservations_table");
        table = dynamoDB.getTable(reservationsTable);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Start adding Reservations Table entry: " + reservationsTable);

            Reservation reservation = fromJson(requestEvent.getBody(), Reservation.class);
            logger.log("Found request Reservation entry: " + reservation);

            Item item = new Item()
                    .withPrimaryKey("id", "" + reservation.tableNumber())
                    .withString("clientName", reservation.clientName())
                    .withString("phoneNumber", reservation.phoneNumber())
                    .withString("date", reservation.date())
                    .withString("slotTimeStart", reservation.slotTimeStart())
                    .withString("slotTimeEnd", reservation.slotTimeEnd());

            logger.log("Item constructed");

            table.putItem(item);

            logger.log("Item saved");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new SimpleResponse("Reservation has been added")));
        } catch (Exception ex) {
            logger.log("Get tables error: " + ex);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Get tables error: " + ex)));
        }
    }
}
