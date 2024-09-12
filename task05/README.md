# task05

High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task05 diagram

![task05](pics/task05_diagram.png)

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

## Generating the stuff

```
syndicate generate meta dynamodb \
    --resource_name Events \
    --hash_key_name id \
    --hash_key_type S \
    --sort_key_name principalId \
    --sort_key_type S \
    --read_capacity 1 \
    --write_capacity 1 
    
    
    
syndicate generate meta api_gateway \
    --resource_name task5_api \
    --deploy_stage api \
    --minimum_compression_size 0 
    
syndicate generate meta api_gateway_resource \
    --api_name task5_api \
    --path /events \
    --enable_cors false     

syndicate generate meta api_gateway_resource_method	\
    --api_name task5_api \
    --path /events \
    --method POST \
    --integration_type lambda \
    --lambda_name api_handler \
    --authorization_type NONE \
    --api_key_required false 
    
```

Use `enable_proxy: true`