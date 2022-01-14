FROM python:3.8-alpine

EXPOSE 5000/tcp

COPY ./app /home/app

ENV FLASK_APP=server
ENV DOCKER=1

WORKDIR /home/app

RUN rm -rf env

RUN pip install flask

RUN flask init-db

CMD ["flask", "run", "--host=0.0.0.0"]