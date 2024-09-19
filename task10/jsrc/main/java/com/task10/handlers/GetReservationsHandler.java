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
import com.task10.data.Reservation;
import com.task10.data.ReservationResponse;
import com.task10.data.ReservationsResponse;
import com.task10.data.SimpleResponse;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class GetReservationsHandler extends BaseHandler {

    private final String reservationsTable;

    public GetReservationsHandler(ObjectMapper objectMapper) {
        super(objectMapper);
        reservationsTable = System.getenv("reservations_table");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            logger.log("Start getting Reservations entries: " + reservationsTable);

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
            ScanRequest scanRequest = new ScanRequest()
                    .withTableName(reservationsTable);

            ScanResult result = client.scan(scanRequest);
            logger.log("Scan reservations found: " + result.getCount());

            List<Reservation> resultReservations = new ArrayList<>();
            for (Map<String, AttributeValue> item : result.getItems()) {

                var reservation = new Reservation(
                        Integer.valueOf(item.get("tableNumber").getN()),
                        item.get("clientName").getS(),
                        item.get("phoneNumber").getS(),
                        item.get("date").getS(),
                        item.get("slotTimeStart").getS(),
                        item.get("slotTimeEnd").getS()
                );

                resultReservations.add(reservation);

                logger.log("Item added: " + toJson(reservation));

            }

            logger.log("Success getting Reservations ");
            resultReservations.sort(Comparator.comparingInt(Reservation::tableNumber));

            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withBody(toJson(new ReservationsResponse(resultReservations)));
        } catch (Exception ex) {
            logger.log("Get reservations error: " + ex);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody(toJson(new SimpleResponse("Get reservations error: " + ex)));
        }
    }
}
