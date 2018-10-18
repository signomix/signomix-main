/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
var globalEvents = riot.observable();

var app = {
    "myData": {"todos": []},
    "user": {
        "name": "",
        "token": "",
        "status": "logged-out",
        "alerts": [],
        "dashboardID": '',
        "dashboards": []
    },
    "offline": false,
    "authAPI": "http://localhost:8080/api/auth",
    "csAPI": "http://localhost:8080/api/cs",
    "cmAPI": "http://localhost:8080/api/cm",
    "userAPI": "http://localhost:8080/api/user",
    "currentPage": "main",
    "language": "en",
    "languages": ["en", "pl", "fr", "it"],
    "debug": false,
    "localUid": 0,
    "dconf": {"widgets":[]}, // configurations of user's widgets on the dashboard page
     //   {},{},{},{},{},{},{},{},{},{},{},{}
    //],
    "widgets": [ // widgets on the dashboard page - hardcoded structure
        [{}, {}, {}, {}],
        [{}, {}, {}, {}],
        [{}, {}, {}, {}]
    ],
    "log": function(message){if(app.debug){console.log(message)}}
}

// ucs-2 string to base64 encoded ascii
    function utoa(str) {
        return window.btoa(unescape(encodeURIComponent(str)));
    }
    // base64 encoded ascii to ucs-2 string
    function atou(str) {
        return decodeURIComponent(escape(window.atob(str)));
    }