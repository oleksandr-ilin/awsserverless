# task10

High level project overview - business value it brings, non-detailed technical overview.

### Notice
All the technical details described below are actual for the particular
version, or a range of versions of the software.
### Actual for versions: 1.0.0

## task10 diagram

![task10](pics/task10_diagram.png)

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

## Testing


```shell

curl -X POST https://api-id.execute-api.eu-central-1.amazonaws.com/api/signup -d "{\"firstName\": \"Bob\", \"lastName\": \"Lars\", \"email\": \"bob@email.com\", \"password\": \"bogoMips1999@\" }"

curl -X POST https://api-id.execute-api.eu-central-1.amazonaws.com/api/signin -d "{ \"email\": \"bob@email.com\", \"password\": \"bogoMips1999@\" }"

curl -X POST https://api-id.execute-api.eu-central-1.amazonaws.com/api/tables -d "{ \"id\": 1, \"number\": 88, \"places\": 2, \"isVip\": true }" -H "Authorization: Bearer put-token-here"
curl -X POST https://api-id.execute-api.eu-central-1.amazonaws.com/api/tables -d "{ \"id\": 2, \"number\": 72, \"places\": 5, \"isVip\": true, \"minOrder\": 155 }" -H "Authorization: Bearer put-token-here"



curl https://api-id.execute-api.eu-central-1.amazonaws.com/api/tables -H "Authorization: Bearer put-token-here"

curl https://api-id.execute-api.eu-central-1.amazonaws.com/api/tables/1 -H "Authorization: Bearer put-token-here"
curl https://api-id.execute-api.eu-central-1.amazonaws.com/api/tables/2 -H "Authorization: Bearer put-token-here"
curl https://api-id.execute-api.eu-central-1.amazonaws.com/api/tables/3 -H "Authorization: Bearer put-token-here"


curl https://api-id.execute-api.eu-central-1.amazonaws.com/api/reservations -H "Authorization: Bearer put-token-here"

curl -X POST https://api-id.execute-api.eu-central-1.amazonaws.com/api/reservations -d "{\"tableNumber\": 88, \"clientName\": \"Lobster Crab\", \"phoneNumber\": \"949-123-4343\", \"date\": \"2024-09-29\", \"slotTimeStart\": \"13:00\", \"slotTimeEnd\": \"15:00\" }" -H "Authorization: Bearer put-token-here"


```

