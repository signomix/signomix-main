/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
var globalEvents = riot.observable();
var app = {
    "paid":false,
    "user": {
        "number": "",
        "name": "",
        "token": "",
        "role": "",
        "status": "logged-out",
        "alerts": [],
        "dashboardID": '',
        "dashboards": []
    },
    "title": "Signomix CE",
    "serviceUrl": "http://localhost:8080",
    "offline": false,
    "authAPI": "http://localhost:8080/api/auth",
    "alertAPI": "http://localhost:8080/api/alert",
    "userAPI": "http://localhost:8080/api/user",
    "iotAPI": "http://localhost:8080/api/iot/device",
    "groupAPI": "http://localhost:8080/api/iot/group",
    "csAPI": "http://localhost:8080/api/cs",
    "dashboardAPI": "http://localhost:8080/api/dashboard",
    "recoveryAPI": "http://localhost:8080/api/recover",
    "currentPage": "",
    "docPath": "/notfound",
    "language": "en",
    "languages": ["en", "pl", "fr", "it"],
    "debug": false,
    "localUid": 0,
    "dashboardRefreshInterval": 60000,
    "publicDashboardRefreshInterval": 300000,
    "sessionRefreshInterval": 300000,
    "alertRefreshInterval": 30000,
    "log": function (message) {
        if (app.debug) {
            console.log(message)
        }
    },
    "requests": 0
}
// ucs-2 string to base64 encoded ascii
function utoa(str) {
    return window.btoa(unescape(encodeURIComponent(str)));
}
// base64 encoded ascii to ucs-2 string
function atou(str) {
    return decodeURIComponent(escape(window.atob(str)));
}
function getSelectedLocale(){
    return app.language+'-'+app.language.toUpperCase()
}

function getAnchor() {
    return (document.URL.split('#').length > 1) ? document.URL.split('#')[1] : null;
}

function setCookie(cname, cvalue, exdays) {
    var d = new Date();
    d.setTime(d.getTime() + (exdays * 24 * 60 * 60 * 1000));
    var expires = "expires="+d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

function deleteCookie(cname) {
    document.cookie = cname + "=;expires=Thu, 01 Jan 1970 00:00:00 UTC;path=/";
}

function getCookie(cname) {
    var name = cname + "=";
    var ca = document.cookie.split(';');
    for(var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function trimSpaces(text){
    if (!String.prototype.trim) {
        String.prototype.trim = function () {
            return this.toString().replace(/^\s+|\s+$/g, '');
        };
    }
    return text.trim();
}