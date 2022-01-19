FROM python:3.8-alpine

COPY ./requirements.txt .

RUN pip install -r requirements.txt

EXPOSE 5000/tcp

ENV FLASK_APP=server
ENV DOCKER=1

COPY ./app /home/app

WORKDIR /home/app

RUN rm -rf env

RUN flask init-db

CMD ["flask", "run", "--host=0.0.0.0"]