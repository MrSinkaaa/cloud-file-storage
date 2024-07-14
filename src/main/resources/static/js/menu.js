// Hide context menu on click outside
document.addEventListener('click', function (e) {
    document.getElementById('context-menu').style.display = 'none';

})

// add event listener using event delegation
document.addEventListener('contextmenu', function (e) {
    if (e.target.closest('.card')) {
        e.preventDefault();
        const contextMenu = document.getElementById('context-menu');
        contextMenu.style.display = 'block';
        contextMenu.style.left = e.pageX + 'px';
        contextMenu.style.top = e.pageY + 'px';

        contextMenu.dataset.targetCard = e.target.closest('.card').querySelector('.card-title-text').textContent;
        contextMenu.dataset.targetId = e.target.closest('.card').id;
        contextMenu.dataset.type = e.target.closest('.card').type;
    }
})

document.addEventListener('dblclick', function (e) {
    if (e.target.closest('.card')) {
        const fileName = e.target.closest('.card').querySelector('.card-title-text').textContent;
        const filePath = getQueryParam(window.location, 'path');
        window.location.assign('?path=' + filePath + fileName);
    }
})

// menu actions
document.getElementById('rename').addEventListener('click', function (e) {
    const oldFileName = document.getElementById('context-menu').dataset.targetCard;
    const id = document.getElementById('context-menu').dataset.targetId;
    const type = document.getElementById('context-menu').dataset.type;
    const newFileName = prompt('Enter new file name', oldFileName);

    if(newFileName) {
        if(type === 'file') {
            sendRequest('/files?id=' + id + '&newName=' + newFileName, 'PATCH');
        } else {
            sendRequest('/folder/rename?id=' + id + '&newFolderName=' + newFileName, 'PATCH');
        }
    }

})

document.getElementById('delete').addEventListener('click', function (e) {
    const id = document.getElementById('context-menu').dataset.targetId;
    const type = document.getElementById('context-menu').dataset.type;

    if(type === 'file') {
        sendRequest('/files?id=' + id, 'DELETE');
    } else {
        sendRequest('/folder/delete?id=' + id, 'DELETE');
    }
})

document.getElementById('download').addEventListener('click', function (e) {
    const id = document.getElementById('context-menu').dataset.targetId;

    downloadFile(`/files?id=${id}`);
})

// send request to server
const sendRequest = (url, method = 'POST') => {

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => console.log('Success ' + data.toString()))
        .catch(error => console.error('Error:', error));
}

const downloadFile = (url) => {
    fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/octet-stream'
        },
        responseType: 'blob'
    })
        .then(response => {
            return response.blob().then(blob => {
                return {
                    blob: blob,
                    headers: response.headers
                };
            });
        })
        .then(obj => {
            const url = window.URL.createObjectURL(obj.blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            // Extract filename from response headers if possible
            const disposition = obj.headers.get('Content-Disposition');
            let filename = '';
            if (disposition && disposition.indexOf('attachment') !== -1) {
                const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
                const matches = filenameRegex.exec(disposition);
                if (matches != null && matches[1]) {
                    filename = matches[1].replace(/['"]/g, '');
                }
            }
            a.download = filename || 'downloaded_file';
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
        })
        .catch(err => console.error('File download failed', err));
}

// Function to get the value of a query parameter from a URL
const getQueryParam = (url, paramName) => {
    const urlObj = new URL(url);
    const params = new URLSearchParams(urlObj.search);
    return params.get(paramName);

}