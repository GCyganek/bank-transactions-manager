# Usage:

## method 1 - without Docker

```
python3 -m venv env
source env/bin/activate
pip install flask

export FLASK_APP=server
```

#### if more than one server instance is require do everything below more than once
#### each time with different export bank and with different port

```
export bank=Santander 

flask init-db

flask run --port=5000
```

## method 2 - with Docker

#### Run these commands in directory containing Dockerfile
#### if one of the ports is taken please change docker_compose.yaml port mapping

```
docker build -t api-example:1.0 .
docker-compose -f docker_compose.yaml up
```



#### this will create n-independant instances of example api server
#### open localhost:port in browser for each one
