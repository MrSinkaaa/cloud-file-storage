const dropzone = document.getElementById('drop-zone');
const content = document.getElementById('card-zone');

['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
    document.body.addEventListener(eventName, preventDefaults, false);
});

function preventDefaults(e) {
    e.preventDefault();
    e.stopPropagation();
}


document.body.addEventListener('dragenter', showDropzone, false);
document.body.addEventListener('dragleave', hideDropzone, false);
dropzone.addEventListener('dragenter', highlightDropzone, false);
dropzone.addEventListener('dragover', highlightDropzone, false);
dropzone.addEventListener('dragleave', unhighlightDropzone, false);
dropzone.addEventListener('drop', handleDrop, false);

function showDropzone(e) {
    dropzone.style.display = 'flex';
    content.style.display = 'none';
}

function hideDropzone(e) {
    if (e.clientX === 0 && e.clientY === 0) {
        dropzone.style.display = 'none';
        content.style.display = 'flex';
    }
}

function highlightDropzone() {
    dropzone.classList.add('hover');
}

function unhighlightDropzone() {
    dropzone.classList.remove('hover');
}

function handleDrop(e) {
    let dt = e.dataTransfer;
    let files = dt.files;

    handleFiles(files);
    dropzone.style.display = 'none';
    content.style.display = 'flex';
    dropzone.classList.remove('hover');
}

function handleFiles(files) {
    [...files].forEach(file => {
        console.log(file);
        uploadFile(file);
    });
}

function uploadFile(file) {
    let parts = getPath('path')
        .split('/')
        .filter(folder => folder!== '');

    let parentFolder = parts[parts.length - 1];
    let formData = new FormData();
    formData.append('file', file);
    formData.append('folderName', parentFolder + '/');

    fetch('/files/upload', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => console.log('Success:' + data.toString()))
        .catch(error => console.error('Error:', error));
}

function getPath(name) {
    name = new RegExp('[?&]' + encodeURIComponent(name) + '=([^&]*)').exec(location.search);
    if (name)
        return decodeURIComponent(name[1]);
}
