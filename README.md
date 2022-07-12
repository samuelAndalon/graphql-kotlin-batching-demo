# graphql-kotlin-v6-demo

## APQ

First Request without query and with sha256Hash
```shell
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'Content-Type: application/json' \
  --data '{
	"variables": {
		"id": 2
	},
	"extensions": {
    "persistedQuery": {
      "version": 1,
      "sha256Hash": "8e6247c8567059f28db2851068942c288a350017db67e1e15ad41304635b6f3f"
    }
  }
}'
```

Second Request with query and with sha256Hash
```hash
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'Content-Type: application/json' \
  --data '{
	"operationName": "getUser",
	"query": "query getUser($id: Int!) { user(id: $id) { id name lastName } }",
	"variables": {
		"id": 1
	},
	"extensions": {
    "persistedQuery": {
      "version": 1,
      "sha256Hash": "8e6247c8567059f28db2851068942c288a350017db67e1e15ad41304635b6f3f"
    }
  }
}'
```


### Batching
```bash
curl --request POST \
  --url http://localhost:8080/graphql \
  --header 'Content-Type: application/json' \
  --data '[
	{
		"operationName": "getUser",
		"query": "query getUser($id: Int!) { user(id: $id) { id name lastName } }",
		"variables": {
			"id": 1
		}
	},
	{
		"operationName": "getUser",
		"query": "query getUser($id: Int!) { user(id: $id) { id name lastName } }",
		"variables": {
			"id": 2
		}
	}
]'
```
