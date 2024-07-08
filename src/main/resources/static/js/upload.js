//file upload
document.getElementById('file_upload').addEventListener("submit", function (e) {
    e.preventDefault();

    let fileInput = document.getElementById('file_input');
    const files = fileInput.files;
    handleFiles(files);
})

//folder upload
document.getElementById('folder_upload').addEventListener("submit", function (e) {
    e.preventDefault();

    let folderInput = document.getElementById('folder_input');
    const files = folderInput.files;
    handleFolder(files);
})


function handleFolder(files) {
    let parentFolder = getParentFolder();

    const formData = new FormData();
    formData.append('parentFolder', parentFolder + '/');

    for(let file of files) {
        formData.append('folder', file);
    }
    sendFolder(formData);
}

function handleFiles(files) {
    [...files].forEach(file => {
        sendFiles(file);
    });
}

function sendFolder(formData) {
    fetch('/folder/upload', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => console.log('Success:' + data.toString()))
        .catch(error => console.error('Error:', error));
}

function sendFiles(file) {
    let formData = new FormData();
    formData.append('file', file);
    formData.append('folderName', getParentFolder() + '/');

    fetch('/files', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => console.log('Success:' + data.toString()))
        .catch(error => console.error('Error:', error));
}

function getParentFolder() {
    let parts = getPath('path')
        .split('/')
        .filter(folder => folder!== '');

    return parts[parts.length - 1];
}

function getPath(name) {
    name = new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)').exec(location.search);
    if (name)
        return decodeURIComponent(name[1]);
}