/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
var globalEvents = riot.observable();

var app = {
    "requestResult": 0,
    "user": {
        "name": "",
        "token": "",
        "status": "logged-out",
        "alerts": []
    },
    "offline": false,
    "currentPage": "main",
    "language": "en",
    "languages": ["en","pl"],
    "debug": true
}
