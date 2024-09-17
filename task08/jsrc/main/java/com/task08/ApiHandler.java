package com.task08;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.lambda.LambdaLayer;
import com.syndicate.deployment.annotations.lambda.LambdaUrlConfig;
import com.syndicate.deployment.model.ArtifactExtension;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.RetentionSetting;
import com.syndicate.deployment.model.lambda.url.AuthType;
import com.syndicate.deployment.model.lambda.url.InvokeMode;
import com.task08.data.Forecast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@LambdaHandler(
        lambdaName = "api_handler",
        layers = {"sdk-layer"},
        roleName = "api_handler-role",
        runtime = DeploymentRuntime.JAVA21,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
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
public class ApiHandler implements RequestHandler<Object, String> {

    public String handleRequest(Object httpRequest, Context context) {
        LambdaLogger log = context.getLogger();
        try {
            log.log("Querying for forecast");
            Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .create();

            Forecast fc = load(gson, log);

            return gson.toJson(fc);

        } catch (Exception ex) {
            log.log("INTERNAL ERROR: " + ex.toString());
            return "{ \"errorMessage\": \"%s\"}".formatted(ex);
        }
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
