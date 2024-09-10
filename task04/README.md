# task04

High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task04 diagram

![task04](pics/task04_diagram.png)

## Lambdas descriptions

### Lambda `lambda-name`
Lambda feature overview.

### Required configuration
#### Environment variables
* environment_variable_name: description

#### Trigger event
```buildoutcfg
{
    "key": "value",
    "key1": "value1",
    "key2": "value3"
}
```
* key: [Required] description of key
* key1: description of key1

#### Expected response
```buildoutcfg
{
    "status": 200,
    "message": "Operation succeeded"
}
```
---

## Deployment from scratch
1. action 1 to deploy the software
2. action 2
...

## Generationg stuff

```
syndicate generate lambda --name sqs_handler --runtime java
syndicate generate lambda --name sns_handler --runtime java


syndicate generate meta sqs_queue \
    --resource_name async_queue \
    --region eu-central-1 \
    --fifo_queue false \
    --visibility_timeout 30 \
    --delay_seconds 0 \
    --maximum_message_size 1024 \
    --message_retention_period 60 \
    --receive_message_wait_time_seconds 20 \
    --dead_letter_target_arn arn:aws:sqs:eu-central-1:859465876068:dead-letter-queue \
    --max_receive_count 2 \
    --kms_master_key_id b328b33f-51fd-44ab-bdd9-e182a015bffd \
    --kms_data_key_reuse_period_seconds 60 \
    --content_based_deduplication false 

#or    
    
syndicate generate meta sqs_queue \
    --resource_name async_queue \
    --region eu-central-1 \
    --fifo_queue false \
    --visibility_timeout 30 
   
      
syndicate generate meta sns_topic \
    --resource_name lambda_topic \
    --region eu-central-1 
    
    
syndicate generate meta iam_policy \
    --resource_name sqs-lambda-execution-policy \
    --policy_content policy.json 
```
