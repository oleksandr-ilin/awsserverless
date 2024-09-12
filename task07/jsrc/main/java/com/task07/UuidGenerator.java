package com.task07;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.RuleEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;
import com.task07.models.UuidIdList;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@LambdaHandler(
        lambdaName = "uuid_generator",
        roleName = "uuid_generator-role",
        runtime = DeploymentRuntime.JAVA21,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@RuleEventSource(targetRule = "uuid_trigger")
@DependsOn(name = "uuid_trigger", resourceType = ResourceType.CLOUDWATCH_RULE)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "region", value = "${region}"),
        @EnvironmentVariable(key = "target_bucket", value = "${target_bucket}")
})
public class UuidGenerator implements RequestHandler<Object, Void> {

    public static final int CHUNK_SIZE = 10;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);

    private final AmazonS3 s3Client;
    private final ObjectMapper objectMapper;
    private final String bucketName;

    public UuidGenerator() {
        objectMapper = new ObjectMapper();
        this.s3Client = AmazonS3Client.builder().withRegion(System.getenv("region")).build();
        this.bucketName = System.getenv("target_bucket");
    }

    public Void handleRequest(Object request, Context context) {

        LambdaLogger logger = context.getLogger();
        try {
            var now = Instant.now();
            var objectName = formatter.format(now);
            logger.log("Start UUID generation on S3 scheduled at: " + now + " into object: " + objectName);

            var uuidIdList = new UuidIdList(
                    IntStream.range(0, CHUNK_SIZE).mapToObj(v -> UUID.randomUUID().toString()).toList());

            s3Client.putObject(bucketName, objectName,
                    convertObjectToJson(uuidIdList));

            logger.log("Generation on S3 has been done");

        } catch (Throwable ex) {
            logger.log("Error: " + ex.toString());
        }
        return null;
    }

    private String convertObjectToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Object cannot be converted to JSON: " + object);
        }
    }
}
