'use strict';

/**
 * This function is used to call the API.
 * If the request returns a 401 (UNAUTHORIZED) status code, the user is redirected to the login page.
 * @param method HTTP method to use (GET, POST, ...)
 * @param url URL to call
 * @param form form to send (null if there is no form to send)
 * @param onloadCallback callback function to call when the request is done
 */
function callAPI(method, url, form, onloadCallback) {
    let req = new XMLHttpRequest();

    req.onload = () => {
        if(req.status === 401){
            window.location.href = 'index.html';
        } else {
            onloadCallback(req);
        }
    };

    req.open(method, url);
    req.setRequestHeader('Accept', 'application/json');

    if(form != null){
        req.send(new FormData(form));
    } else {
        req.send();
    }
}

/**
 * This function formats a number as a currency in EUR.
 * @param amount amount to format
 * @returns {string} formatted amount
 */
function formatCurrency(amount){
    return new Intl.NumberFormat('it-IT', { style: 'currency', currency: 'EUR' }).format(amount);
}

/**
 * This function formats a date as a string.
 * @param date date to format
 * @returns {string} formatted date using it-IT locale
 */
function formatDate(date){
    return new Date(date).toLocaleDateString('it-IT', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}

/**
 * This function returns the time between two dates as a string.
 * @returns {string} time between two dates
 */
function timeBetween(date1, date2) {
    const duration = date2 - date1;
    const durationInSeconds = Math.floor(duration / 1000);
    const durationInMinutes = Math.floor(durationInSeconds / 60);
    const durationInHours = Math.floor(durationInMinutes / 60);
    const durationInDays = Math.floor(durationInHours / 24);

    if (duration < 0) {
        return "Asta scaduta!";
    }

    if (durationInHours === 0) {
        return "Meno di 1 ora";
    }

    let hours;
    if (durationInHours % 24 === 1) {
        hours = "1 ora";
    } else {
        hours = `${durationInHours % 24} ore`;
    }

    if (durationInDays === 0) {
        return hours;
    }

    let days;
    if (durationInDays === 1) {
        days = "1 giorno";
    } else {
        days = `${durationInDays} giorni`;
    }

    if (durationInHours % 24 === 0) {
        return days;
    } else {
        return `${days} e ${hours}`;
    }
}

/**
 * This function saves an auction in the local storage.
 * @param id id of the auction to save
 */
function saveRecentAuction(id) {
    let savedAuctions = localStorage.getItem('saved_auctions');
    if(savedAuctions === null){
        savedAuctions = [];
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    // Check if the auction is already in the list
    if(savedAuctions.indexOf(id) !== -1){
        // If it is, remove it, so it can be added at the top
        savedAuctions.splice(savedAuctions.indexOf(id), 1);
    }

    savedAuctions.push(id);

    // If there are more than 5 auctions saved, remove the oldest one
    if (savedAuctions.length > 5) {
        savedAuctions.shift();
    }

    localStorage.setItem('saved_auctions', JSON.stringify(savedAuctions));
    localStorage.setItem('updated_at', new Date().getTime().toString());
}

/**
 * This function returns the list of saved auctions.
 * @returns {any|*[]} list of saved auctions
 */
function getRecentAuctions() {
    let savedAuctions = localStorage.getItem('saved_auctions');
    if(savedAuctions === null){
        return [];
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    return savedAuctions;
}

/**
 * This function removes an auction from the list of saved auctions.
 * @param id id of the auction to remove
 */
function removeRecentAuction(id) {
    let savedAuctions = localStorage.getItem('saved_auctions');
    if(savedAuctions === null){
        return;
    } else {
        savedAuctions = JSON.parse(savedAuctions);
    }

    if(savedAuctions.indexOf(id) !== -1){
        savedAuctions.splice(savedAuctions.indexOf(id), 1);
    }

    localStorage.setItem('saved_auctions', JSON.stringify(savedAuctions));
}

/**
 * This function saves the user id and name in the local storage.
 * If the new logged user is different from the previous one, or if the last localStorage update is more than 30 days old,
 * the local storage is cleared.
 * @param user user to save after the login
 */
function saveUser(user) {
    let user_id = user.id;
    if(
        user_id !== parseInt(localStorage.getItem('user_id')) ||
        new Date().getTime() - parseInt(localStorage.getItem('updated_at')) > 1000 * 60 * 60 * 24 * 30
    ){
        localStorage.clear();
    }
    localStorage.setItem('user_id', user_id);
    localStorage.setItem('firstname', user.firstname);
}