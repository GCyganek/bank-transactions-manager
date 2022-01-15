import os
import click
from . import db
from . import api
from .uploaded_file import UploadedFile
from flask import Flask, render_template, request
from flask import current_app as app


def init_app():
    app = Flask(__name__, instance_relative_config=True)

    bank_type = os.environ.get('bank') or "Santander"
    db_name = f'{bank_type}_db.sqlite' if os.environ.get('DOCKER') is None else 'db.sqlite'

    app.config.from_mapping(
        DATABASE=os.path.join(app.instance_path, db_name),
        BANK=bank_type
    )

    try:
        os.makedirs(app.instance_path)
    except OSError:
        pass

    @app.cli.command('init-db')
    def init_db_command():
        db.init_db()
        click.echo(f'Database createad at: {app.config["DATABASE"]}')

    app.cli.add_command(init_db_command)

    app.teardown_appcontext(db.close_db)

    app.register_blueprint(api.bp)

    return app


app = init_app()


@app.route('/', methods=['GET', 'POST'])
def upload_file():
    file_form_key = "statement"

    if request.method == 'GET':
        return render_template('index.html', file_form_key=file_form_key, bankType=app.config['BANK'])
    else:
        if file_form_key not in request.files:
            return f"expected {file_form_key}", 404

        try:
            UploadedFile.persist_file(request.files[file_form_key])
        except FileExistsError:
            return f'file {request.files[file_form_key].filename} already exists', 400

        return f'file {request.files[file_form_key].filename} saved successfully'
