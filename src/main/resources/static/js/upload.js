//change label after upload file/folder
document.getElementById('file_input').addEventListener("change", function (e) {
    document.querySelector("[for=" + e.target.id + "]").innerHTML = e.target.files[0].name;
})

document.getElementById('folder_input').addEventListener("change", function (e) {
    document.querySelector("[for=" + e.target.id + "]").innerHTML =
        e.target.files[0].webkitRelativePath.split('/')[0];
})

//file upload
document.getElementById('file_upload').addEventListener("submit", function (e) {
    e.preventDefault();
    handleFiles(document.getElementById('file_input').files);
})

//folder upload
document.getElementById('folder_upload').addEventListener("submit", function (e) {
    e.preventDefault();
    handleFolder(document.getElementById('folder_input').files);
})

//create folder
document.getElementById('create_folder').addEventListener("submit", function (e) {
    e.preventDefault();

    let folderName = document.getElementById('create_input').value;
    const parentFolder = getParentFolder();

    sendRequest('/folder/create?folderName=' + folderName + '&parentFolder=' + parentFolder);
})

document.getElementById('create_input').addEventListener('click', function (e) {
    e.preventDefault();

    const input = document.getElementById('create_input');
    const label = document.querySelector("[for=" + e.target.id + "]");

    label.setAttribute('hidden', true);
    input.setAttribute('type', 'text');
    input.style.width = '110px';

    input.addEventListener('blur', function () {
        label.removeAttribute('hidden');
        if(input.value) {
            label.innerHTML = input.value;
        }
        input.setAttribute('hidden', true);
        input.style.width = '';
    });
})


function handleFolder(files) {
    const formData = new FormData();

    const parentFolder = getParentFolder();
    formData.append('parentFolder', parentFolder);

    [...files].forEach(file => {
        formData.append('folder', file);
    });

    sendFolder(formData);
}

function handleFiles(files) {
    [...files].forEach(file => {
        sendFile(file);
    });
}

function sendFolder(formData) {
    fetch('/folder/upload', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json().then(data => ({
            status: response.status,
            body: data

        })))
        .then(({status, body}) => {
            if (status !== 200) {
                displayErrorModal({status, message: body.message})
            }
        })
}

function sendFile(file) {
    let formData = new FormData();
    formData.append('file', file);

    let parentFolder = getParentFolder();
    formData.append('folderName', parentFolder);

    fetch('/files', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            let data = response.json();

            if (!response.ok) {
                displayErrorModal({status: data.status, message: data.message})
            } else {
                return data;
            }
        })
}

function getParentFolder() {
    const parts = getPath('path').split('/')
        .filter(folder => folder !== '');

    const parentFolder = parts[parts.length - 1];
    return parentFolder ? parentFolder + "/" : "";
}

function getPath(name) {
    name = new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)').exec(location.search);
    if (name)
        return decodeURIComponent(name[1]);
}