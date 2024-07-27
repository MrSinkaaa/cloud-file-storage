const contextMenu = document.getElementById('context-menu');
// Hide context menu on click outside
document.addEventListener('click', function (e) {
    contextMenu.style.display = 'none';

})

// add event listener using event delegation
document.addEventListener('contextmenu', function (e) {
    const card = e.target.closest('.card');

    if (card) {
        e.preventDefault();
        contextMenu.style.display = 'block';
        contextMenu.style.left = e.pageX + 'px';
        contextMenu.style.top = e.pageY + 'px';

        contextMenu.dataset.targetCard = card.querySelector('.card-title-text').textContent;
        contextMenu.dataset.targetId = card.id;
        contextMenu.dataset.type = card.dataset.type;

        if (card.dataset.type === 'folder') {
            document.getElementById('download').style.display = 'none';
        } else {
            document.getElementById('download').style.display = 'block';
        }
    }
})

document.addEventListener('dblclick', function (e) {
    const card = e.target.closest('.card');
    const type = card.dataset.type;

    if (card && (type === 'folder')) {

        const fileName = card.getAttribute('data-path');
        const filePath = getQueryParam(window.location, 'path') || '';

        if (filePath) {
            window.location.assign(`?path=${fileName}`);
        } else {
            const url = new URL(window.location);
            url.searchParams.set('path', fileName);
            url.searchParams.delete('query');
            url.pathname = '';

            const decodedPath = decodeURIComponent(url);
            window.location.assign(decodedPath);
        }

    }
})

// menu actions
document.getElementById('rename').addEventListener('click', function (e) {
    const oldFileName = contextMenu.dataset.targetCard;
    const id = contextMenu.dataset.targetId;
    const type = contextMenu.dataset.type;
    const newFileName = prompt('Enter new file name', oldFileName);

    if (newFileName) {
        if (type === 'file') {
            sendRequest('/files?id=' + id + '&newName=' + newFileName, 'PATCH');
        } else {
            sendRequest('/folder/rename?id=' + id + '&newFolderName=' + newFileName, 'PATCH');
        }
    }

})

document.getElementById('delete').addEventListener('click', function (e) {
    const id = contextMenu.dataset.targetId;
    const type = contextMenu.dataset.type;

    if (type === 'file') {
        sendRequest('/files?id=' + id, 'DELETE');
    } else {
        sendRequest('/folder/delete?id=' + id, 'DELETE');
    }
})

document.getElementById('download').addEventListener('click', function (e) {
    const id = contextMenu.dataset.targetId;
    const type = contextMenu.dataset.type;

    if (type === 'file') {
        downloadFile(`/files?id=${id}`);
    }
})

// send request to server
const sendRequest = (url, method = 'POST') => {

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
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
        .catch(error => console.log(error));
}

// Function to get the value of a query parameter from a URL
const getQueryParam = (url, paramName) => {
    const urlObj = new URL(url);
    const params = new URLSearchParams(urlObj.search);
    return params.get(paramName);

}


const displayErrorModal = (error) => {
    document.getElementById('error-status').innerText = error.status;
    document.getElementById('error-message').innerText = error.message;
    document.getElementById('error-modal').style.display = 'block';
}

document.addEventListener("DOMContentLoaded", function () {
    const modal = document.getElementById('error-modal');

    if (modal) {
        modal.style.display = 'block';
    }
})

const closeModal = () => {
    document.getElementById('error-modal').style.display = 'none';
}
