const dropzone = document.querySelector('#dropzone');
const statusBox = document.querySelector('#status');
const UPLOAD_URI = '/';

dropzone.addEventListener('dragover', e => {
    e.stopPropagation();
    e.preventDefault();
});

dropzone.addEventListener('drop', e => {
    e.stopPropagation();
    e.preventDefault();

    const files = [...e.dataTransfer.files];
    files.forEach(sendFile);
});


function sendFile(file) {
    const fd = new FormData();
    fd.append(FILE_FORM_KEY, file);

    appendStatusMessage(`sending file "${file.name}"`);

    fetch(UPLOAD_URI, {
        method: 'POST',
        body: fd
    })
        .then(resp => {
            appendStatusMessage(`upload finished for "${file.name}"; status = ${resp.status}`);
            console.log(resp);
        })
        .catch(err => {
            appendStatusMessage(`upload failed for "${file.name}"; error = ${err}`)
            console.error(err);
        }
        );
}


function appendStatusMessage(msg) {
    const p = document.createElement('p');
    p.textContent = msg;
    statusBox.appendChild(p);
}