import os
import sqlite3
from typing import Iterable
from .db import get_db
from pathlib import Path
from werkzeug.utils import secure_filename
from werkzeug.datastructures import FileStorage
from datetime import datetime
from pytz import timezone

UPLOAD_FOLDER = Path(__file__).absolute().parent.joinpath('uploads')

try:
    os.makedirs(UPLOAD_FOLDER)
except FileExistsError:
    pass


class UploadedFile:
    DB_TABLE_NAME = 'uploads'
    DB_PATH_COLUMN_NAME = 'local_path'
    DB_ID_COLUMN_NAME = 'id'
    DB_UPLOAD_TIME_COLUMN_NAME = 'upload_time'
    DATE_FORMAT = '%Y-%m-%d %H:%M:%S'

    def __init__(self, path: Path, id: int, upload_time: datetime = None) -> None:
        self.path = path
        self.id = id
        self.upload_time = upload_time

    def get_id(self) -> int:
        return self.id

    def get_path(self) -> Path:
        return self.path

    def get_upload_time(self) -> datetime:
        return self.upload_time

    def get_extension(self) -> str:
        ext = self.path.suffix
        if len(ext) > 0:
            ext = ext[1:]
        return ext
            

    @classmethod
    def persist_file(cls, file: FileStorage) -> 'UploadedFile':
        path = UPLOAD_FOLDER.joinpath(secure_filename(file.filename))
        db = get_db()

        try:
            timestamp = datetime.now(timezone('Europe/Warsaw'))

            cursor = db.execute(
                f'INSERT INTO {cls.DB_TABLE_NAME} ({cls.DB_PATH_COLUMN_NAME}, {cls.DB_UPLOAD_TIME_COLUMN_NAME}) VALUES (?, ?)',
                (str(path), str(timestamp))
            )
            db.commit()

            file.save(path)

            return UploadedFile(path, cursor.lastrowid, timestamp)

        except db.IntegrityError:
            raise FileExistsError

    @classmethod
    def get_files_between(cls, start_time: str, end_time: str = None) -> Iterable['UploadedFile']:
        db = get_db()

        condition = f' {cls.DB_UPLOAD_TIME_COLUMN_NAME} >= (?)'
        if end_time is not None:
            condition += f' AND {cls.DB_UPLOAD_TIME_COLUMN_NAME} <= (?)'
            condition_args = (start_time, end_time)
        else:
            condition_args = (start_time, )

        sql = f'SELECT * \
                FROM {cls.DB_TABLE_NAME} \
                WHERE {condition} \
                ORDER BY {cls.DB_UPLOAD_TIME_COLUMN_NAME}'

        return [cls._create_file_from_db(
            record) for record in db.execute(sql, condition_args)]

    @classmethod
    def find_file_by_id(cls, id: int) -> 'UploadedFile':
        db = get_db()

        record = db.execute(
            f'SELECT * FROM {cls.DB_TABLE_NAME} WHERE {cls.DB_ID_COLUMN_NAME} = (?)',
            (id, )
        ).fetchone()

        return cls._create_file_from_db(record) if record is not None else None

    @classmethod
    def _create_file_from_db(cls, record: sqlite3.Row) -> 'UploadedFile':
        path = Path(record[cls.DB_PATH_COLUMN_NAME])
        id = record[cls.DB_ID_COLUMN_NAME]
        timestamp = record[cls.DB_UPLOAD_TIME_COLUMN_NAME]

        return UploadedFile(path, id, timestamp)


def convert_querystr_time_to_local(timestamp: str) -> str:
    """YYYYMMDDHHMMSS -> YYYY-MM-DD HH:MM:SS"""
    IN_FORMAT = "%Y%m%d%H%M%S"
    OUT_FORMAT = UploadedFile.DATE_FORMAT

    return datetime.strptime(timestamp, IN_FORMAT).strftime(OUT_FORMAT)
