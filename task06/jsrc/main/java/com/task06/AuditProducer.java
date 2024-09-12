package com.task06;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemUtils;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.syndicate.deployment.annotations.environment.EnvironmentVariable;
import com.syndicate.deployment.annotations.environment.EnvironmentVariables;
import com.syndicate.deployment.annotations.events.DynamoDbTriggerEventSource;
import com.syndicate.deployment.annotations.lambda.LambdaHandler;
import com.syndicate.deployment.annotations.resources.DependsOn;
import com.syndicate.deployment.model.DeploymentRuntime;
import com.syndicate.deployment.model.ResourceType;
import com.syndicate.deployment.model.RetentionSetting;


import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.amazonaws.services.dynamodbv2.model.OperationType.INSERT;
import static com.amazonaws.services.dynamodbv2.model.OperationType.MODIFY;

@LambdaHandler(
        lambdaName = "audit_producer",
        roleName = "audit_producer-role",
        runtime = DeploymentRuntime.JAVA21,
        logsExpiration = RetentionSetting.SYNDICATE_ALIASES_SPECIFIED
)
@DynamoDbTriggerEventSource(targetTable = "Configuration", batchSize = 1)
@DependsOn(name = "Configuration", resourceType = ResourceType.DYNAMODB_TABLE)
@EnvironmentVariables(value = {
        @EnvironmentVariable(key = "target_table", value = "${target_table}")
})
public class AuditProducer implements RequestHandler<DynamodbEvent, Void> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC);
    private final String tableAudit;
    private final Table table;

    public AuditProducer() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        var dynamoDB = new DynamoDB(client);
        tableAudit = System.getenv("target_table");
        table = dynamoDB.getTable(tableAudit);
    }

    @Override
    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {

        LambdaLogger logger = context.getLogger();
        logger.log("Start configuration change handling. tableAudit: " + tableAudit);

        for (DynamodbEvent.DynamodbStreamRecord record : dynamodbEvent.getRecords()) {
            if ("INSERT".equals(record.getEventName())) {
                createAddAudit(logger, ItemUtils.toSimpleMapValue(record.getDynamodb()
                        .getNewImage()));
            } else if ("MODIFY".equals(record.getEventName())) {
                createModifyAudit(logger, ItemUtils.toSimpleMapValue(record.getDynamodb()
                                .getOldImage()),
                        ItemUtils.toSimpleMapValue(record.getDynamodb()
                                .getNewImage())
                );
            } else {
                logger.log("Unknown DB event type: " + record.getEventName());
            }
        }

        return null;
    }


    private void createAddAudit(LambdaLogger logger, Map<String, Object> newValueMap) {
        String id = (String) newValueMap.get("key");
        String value = Optional.ofNullable(newValueMap.get("value")).map(Object::toString).orElse("");

        logger.log("INSERT key:" + id + " Value: " + value);

        Item item = new Item()
                .withPrimaryKey("id", UUID.randomUUID().toString())
                .withString("itemKey", id)
                .withString("modificationTime", formatter.format(Instant.now()))
                .withMap("newValue", Map.of("key", id, "value", value ));

        table.putItem(item);
    }

    private void createModifyAudit(LambdaLogger logger, Map<String, Object> oldValueMap, Map<String, Object> newValueMap) {
        String id = (String) newValueMap.get("key");
        String newValue = Optional.ofNullable(newValueMap.get("value")).map(Object::toString).orElse("");
        String oldValue = Optional.ofNullable(oldValueMap.get("value")).map(Object::toString).orElse("");


        logger.log("MODIFY key:" + id + " Old Value: " + oldValue  + " New Value: " + newValue );

        Item item = new Item()
                .withPrimaryKey("id", UUID.randomUUID().toString())
                .withString("itemKey", id)
                .withString("modificationTime", formatter.format(Instant.now()))
                .withString("updatedAttribute", "value")
                .withString("oldValue", oldValue)
                .withString("newValue", newValue);

        table.putItem(item);

    }
}
