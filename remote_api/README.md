# Installation:

## method 1 - without Docker

```
python3 -m venv env
source env/bin/activate
pip install -r requirements.txt

export FLASK_APP=server
```

#### if more than one server instance is required do everything below more than once each time with different export bank

```
export bank=Santander

flask init-db
```

## method 2 - with Docker

#### run in directory with Dockerfile 

```
docker build -t api-example:1.0 .
```

# Usage

## method 1 - without Docker
```
source env/bin/activate
export FLASK_APP=server
```

#### if you want to recreate database run
```
flask init-db
```

#### if more than one server instance is required do everything below more than once each time with different export bank and with different port

```
export bank=Santander
flask run --port=5000
```

#### then open localhost:port in browser for each one

## method 2 - with Docker
```
docker-compose -f docker_compose.yaml up
```
#### this will create n-independant instances of example api server, open localhost:port in browser for each one
#### if one of the ports is taken please change docker_compose.yaml port mapping, default ports are 5000 for santander and 5001 for mbank

<br>

#### or run as many as you want individually, change port mappings and env variables as required
``` 
docker run -d -p <your_port>:5000 -e bank=Santander api-example:1.0
```

# API:

- GET api/statements/updates
    - query parameters:
        - start-time - YYYYMMDDHHMMSS formatted string, only updates added later or at that moment will be returned (required)
        - end-time   - YYYYMMDDHHMMSS formatted string, only updates added before or at that moment will be returned
    - response:
        ```
            {
                "updates": [
                    {
                    "statement_id": <int>,
                    "extension": <String>,
                    "upload_time": "2022-01-14 23:41:41.807859" <YYYY-MM-DD HH:MM:SS.s+>
                    },
                    ...
                ]
            }
        ```
    - example: 
        - GET /api/statements/updates?start-time=20220114235600&end-time=20220115014600
        ```
        {
            "updates": [
                {
                "statement_id": 1,
                "extension": "py",
                "upload_time": "2022-01-14 23:57:34.766158"
                },
                {
                "statement_id": 2,
                "extension": "csv",
                "upload_time": "2022-01-15 00:04:47.749948"
                }
            ]
        }
        ```


- GET /api/statements/{statement_id}
    - response - raw file data, client should assert validity
    - example:
        - GET /api/statements/2
        - response will contain binary data from file with statement_id=2