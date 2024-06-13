// Hide context menu on click outside
document.addEventListener('click', function (e) {
    document.getElementById('context-menu').style.display = 'none';

})

// add event listener using event delegation
document.addEventListener('contextmenu', function (e) {
    if (e.target.closest('.card')) {
        e.preventDefault();
        let contextMenu = document.getElementById('context-menu');
        contextMenu.style.display = 'block';
        contextMenu.style.left = e.pageX + 'px';
        contextMenu.style.top = e.pageY + 'px';

        contextMenu.dataset.targetCard = e.target.closest('.card').querySelector('.card-title-text').textContent;
        contextMenu.dataset.targetId = e.target.closest('.card').id;
    }
})

document.addEventListener('dblclick', function (e) {
    if (e.target.closest('.card')) {
        let fileName = e.target.closest('.card').querySelector('.card-title-text').textContent;
        let filePath = getQueryParam(window.location, 'path');
        window.location.assign('?path=' + filePath + fileName);
    }
})

// menu actions
document.getElementById('rename').addEventListener('click', function (e) {
    let oldFileName = document.getElementById('context-menu').dataset.targetCard;
    let newFileName = prompt('Enter new file name', oldFileName);

    if (newFileName) {
        oldFileName = newFileName;
        sendRequest('/files/rename?from=' + oldFileName + '&to=' + newFileName);
    }

})

document.getElementById('delete').addEventListener('click', function (e) {
    let id = document.getElementById('context-menu').dataset.targetId;

    sendRequest('/files/delete?id=' + id, 'DELETE');

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

// Function to get the value of a query parameter from a URL
const getQueryParam = (url, paramName) => {
    const urlObj = new URL(url);
    const params = new URLSearchParams(urlObj.search);
    return params.get(paramName);

}