package com.task09;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.*;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task09.data.Forecast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.*;

@LambdaHandler(
        lambdaName = "processor",
        roleName = "processor-role",
        layers = {"sdk-layer"},
        runtime = DeploymentRuntime.JAVA21,
        tracingMode = TracingMode.Active,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DependsOn(name = "${target_table}", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
@LambdaLayer(
        layerName = "sdk-layer",
        libraries = {"lib/commons-lang3-3.14.0.jar", "lib/gson-2.10.1.jar"},
        runtime = DeploymentRuntime.JAVA21,
        artifactExtension = ArtifactExtension.ZIP
)
@LambdaUrlConfig(
        authType = AuthType.NONE,
        invokeMode = InvokeMode.BUFFERED
)
public class Processor implements RequestHandler<Object, String> {

    private final String tableWeather;
    private final Table table;

    public Processor() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        var dynamoDB = new DynamoDB(client);
        tableWeather = System.getenv("target_table");
        table = dynamoDB.getTable(tableWeather);
    }

    public String handleRequest(Object httpRequest, Context context) {
        LambdaLogger log = context.getLogger();
        try {
            log.log("Querying for forecast");
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            Forecast fc = load(gson, log);
            log.log("Loaded forecast: " + fc);
            persistForecast(log, fc);
            return gson.toJson(fc);

        } catch (Exception ex) {
            log.log("INTERNAL ERROR: " + ex.toString());
            return "{ \"errorMessage\": \"%s\"}".formatted(ex);
        }
    }

    private void persistForecast(LambdaLogger log, Forecast fc) {

        log.log("Persisting forecast");

        var hourly = Map.of(
                "temperature_2m", Arrays.stream(Optional.ofNullable(fc.hourly().temperature2m()).orElse(new double[]{})).mapToObj(Double::valueOf).toList(),
                "time", Arrays.stream(Optional.ofNullable(fc.hourly().time()).orElse(new String[]{})).toList());
        var hourlyUnits = Map.of(
                "temperature_2m", Optional.ofNullable(fc.hourlyUnits().temperature2m()).map(Object::toString).orElse(""),
                "time", Optional.ofNullable(fc.hourlyUnits().time()).orElse(""));

        log.log("Maps are set");
        var forecastMap = Map.of("elevation", fc.elevation(),
                "generationtime_ms", fc.generationtimeMs(),
                "hourly", hourly,
                "hourly_units", hourlyUnits,
                "latitude", fc.latitude(),
                "longitude", fc.longitude(),
                "timezone", fc.timezone(),
                "timezone_abbreviation", fc.timezoneAbbreviation(),
                "utc_offset_seconds", fc.utcOffsetSeconds()
        );
        log.log("Forecast Map provisioned" + forecastMap);

        Item item = new Item()
                .withPrimaryKey("id", UUID.randomUUID().toString())
                .with("forecast", forecastMap);
        log.log("Persisting to table:" + tableWeather + " table object: " + tableWeather);
        table.putItem(item);
        log.log("Successfully added into: " + tableWeather);

    }

    private Forecast load(Gson gson, LambdaLogger log) throws IOException, InterruptedException, URISyntaxException {
        String urlString = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
        // Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        // Create an HttpRequest instance
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(urlString))
                .GET()
                .build();

        // Send the request and get the response
        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {

            try (JsonReader reader = new JsonReader(new InputStreamReader(response.body()))) {

                return gson.fromJson(reader, Forecast.class);
            }

        } else {
            throw new RuntimeException("GET request failed. Response Code: " + response.statusCode());
        }
    }
}
