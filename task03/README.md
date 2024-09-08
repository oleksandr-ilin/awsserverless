# task03

High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task03 diagram

![task03](pics/task03_diagram.png)

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

## Generate the project

### Java project
```shell
syndicate generate lambda --name hello_world --runtime java
```

### API Meta

```shell
syndicate generate meta api_gateway \
    --resource_name task3_api \
    --deploy_stage api \
    --minimum_compression_size 0 
    
syndicate generate meta api_gateway_resource \
    --api_name task3_api \
    --path /hello \
    --enable_cors false     

syndicate generate meta api_gateway_resource_method	\
    --api_name task3_api \
    --path /hello \
    --method GET \
    --integration_type lambda \
    --lambda_name hello_world \
    --authorization_type NONE \
    --api_key_required false 
```
