package com.task11.handlers;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.task11.data.Reservation;
import com.task11.data.ReservationResponse;
import com.task11.data.SimpleResponse;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class PostReservationsHandler extends BaseHandler {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

    private final String reservationsTableName;
    private final Table reservationsTable;
    private final AmazonDynamoDB client;

    public PostReservationsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
        client = AmazonDynamoDBClientBuilder.standard().build();
        var dynamoDB = new DynamoDB(client);
        reservationsTableName = System.getenv("reservations_table");
        reservationsTable = dynamoDB.getTable(reservationsTableName);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent requestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Start adding Reservations Table entry: " + reservationsTableName);

            Reservation reservation = fromJson(requestEvent.getBody(), Reservation.class);
            logger.log("Found request Reservation entry: " + reservation);
            validate(logger, reservation);
            logger.log("Validation OK ");

            String reservationId = UUID.randomUUID().toString();
            Item item = new Item()
                    .withPrimaryKey("id", reservationId)
                    .withInt("tableNumber", reservation.tableNumber())
                    .withString("clientName", reservation.clientName())
                    .withString("phoneNumber", reservation.phoneNumber())
                    .withString("date", reservation.date())
                    .withString("slotTimeStart", reservation.slotTimeStart())
                    .withString("slotTimeEnd", reservation.slotTimeEnd());

            logger.log("Item constructed");

            reservationsTable.putItem(item);

            logger.log("Item saved");

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new ReservationResponse(reservationId)));

        } catch (Exception ex) {
            logger.log("Can't add the reservation: " + ex);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Can't add the reservation: " + ex)));
        }
    }

    private void validate(LambdaLogger logger, Reservation reservation) {
        logger.log("Validating for table: " + reservation.tableNumber());
        validateTableExists(logger, reservation.tableNumber());
        validateNoTimeOverlap(logger, reservation.tableNumber(), reservation.slotTimeStart(), reservation.slotTimeEnd());
    }

    private void validateTableExists(LambdaLogger logger, int tableNumber) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tableNumber", new AttributeValue().withN(tableNumber + ""));

        var scanRequest = new ScanRequest()
                .withTableName(System.getenv("tables_table"))
                .withFilterExpression("#number = :tableNumber")
                .withExpressionAttributeValues(expressionAttributeValues)
                .withProjectionExpression("id")
                .withExpressionAttributeNames(Map.of("#number", "number"));

        ScanResult items = client.scan(scanRequest);

        logger.log("Checking table exists: " + logger);

        if (items.getCount() == 0) {
            throw new RuntimeException("No such table: " + tableNumber);
        }
    }

    private void validateNoTimeOverlap(LambdaLogger logger, int tableNumber, String slotTimeStart, String slotTimeEnd) {
        logger.log("Checking timeslot " + slotTimeStart + "-" + slotTimeEnd);

        LocalTime slotStart = LocalTime.parse(slotTimeStart, formatter);
        LocalTime slotEnd = LocalTime.parse(slotTimeEnd, formatter);


        Map<String, Object> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":tableNumber", tableNumber);

        ItemCollection<ScanOutcome> items = reservationsTable.scan("tableNumber = :tableNumber",
                "slotTimeStart, slotTimeEnd",
                null,
                expressionAttributeValues);

        logger.log("Checking overlapping" + items);

        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            String existedSlotTimeStart = item.getString("slotTimeStart");
            String existedSlotTimeEnd = item.getString("slotTimeEnd");

            logger.log("Found timeslot " + existedSlotTimeStart + "-" + existedSlotTimeEnd);

            LocalTime start = LocalTime.parse(existedSlotTimeStart, formatter);
            LocalTime end = LocalTime.parse(existedSlotTimeEnd, formatter);

            if ((slotStart.isBefore(end) && slotEnd.isAfter(start))) {
                throw new RuntimeException("The time slot %s-%s for %d table is overlapping existed reservation %s-%s"
                        .formatted(slotTimeStart, slotTimeEnd, tableNumber, existedSlotTimeStart, existedSlotTimeEnd));
            }
        }
    }
}
