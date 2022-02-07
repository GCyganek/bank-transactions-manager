DROP TABLE IF EXISTS uploads;

CREATE TABLE uploads (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    upload_time TIMESTAMP NOT NULL,
    local_path TEXT NOT NULL --UNIQUE easier testing
);