from flask import Blueprint, request, send_file
from .uploaded_file import UploadedFile, convert_querystr_time_to_local

bp = Blueprint('api', __name__, url_prefix='/api')


@bp.route('/statements/<int:statement_id>', methods=['GET'])
def get_statement_with_id(statement_id):
    file = UploadedFile.find_file_by_id(statement_id)

    if file is None:
        return f'Failed to find statement with id: {statement_id}', 404

    return send_file(file.get_path())


@bp.route('/statements/updates', methods=['GET'])
def get_updated_statements():
    start = request.args.get('start-time')
    end = request.args.get('end-time')

    if start is None:
        return 'Expected interval begin timestamp', 400

    try:
        start_dt = convert_querystr_time_to_local(start)
        end_dt = convert_querystr_time_to_local(
            end) if end is not None else None

        updates = []

        for file in UploadedFile.get_files_between(start_dt, end_dt):
            updates.append({
                'statement_id': file.get_id(),
                'upload_time': str(file.get_upload_time()),
                'extension': file.get_extension()
            })

        return {
            'updates': updates
        }
    except ValueError:
        return 'invalid query params', 400
