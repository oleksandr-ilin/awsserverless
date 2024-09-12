# task06

High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task06 diagram

![task06](pics/task06_diagram.png)

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


## Creating the resources

```
syndicate generate lambda --name audit_producer --runtime java

syndicate generate meta dynamodb \
    --resource_name Configuration \
    --hash_key_name key \
    --hash_key_type S \
    --read_capacity 1 \
    --write_capacity 1 

syndicate generate meta dynamodb \
    --resource_name Audit \
    --hash_key_name id \
    --hash_key_type S \
    --read_capacity 1 \
    --write_capacity 1 


```