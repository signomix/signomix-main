/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 */
var loginUrl = "https://docker37254-cricket4home.unicloud.pl/api/login";
var key = "";
var login = "";
var password = "";
var tableId = "mytable";

function afterLogin(newKey, name) {
    var key = newKey.trim();
    if (key == "") {
        alert('Login failed');
    } else {
        app.offline = false;
        app.user.token = key;
        app.user.name = name;
        app.user.status = "logged-in";
    }
}

function loginSubmit(oFormElement, eventBus, successEventName, errorEventName, debug) {
    var login;
    var password;
    var oField = "";
    var sEncoded;
    for (var nItem = 0; nItem < oFormElement.elements.length; nItem++) {
        oField = oFormElement.elements[nItem];
        if (!oField.hasAttribute("name")) {
            continue;
        }
        if (oField.name === "login") {
            login = oField.value;
        } else if (oField.name === "password") {
            password = oField.value;
        }
    }
    var oReq = new XMLHttpRequest();
    oReq.onerror = function (oEvent) {
        if(debug) { console.log(oEvent.toString()) };
        app.user.status = "logged-out";
        eventBus.trigger(errorEventName);
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if(debug) { console.log(oEvent.toString()) };
    };
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            afterLogin(oReq.responseText, login);
            eventBus.trigger(successEventName);
        } else if (this.readyState == 4 && this.status > 400) {
            app.user.status = "logged-out";
            eventBus.trigger(errorEventName);
            if(debug) { console.log("result code == "+this.status) };
        }
    };

    sEncoded = utoa(login + ":" + password);
    //alert("sending " + sEncoded + "to " + loginUrl);
    //oReq.setRequestHeader("Authentication: Basic " + sEncoded);
    oReq.open("post", app.authAPI);
    app.log('app.authAPI='+app.authAPI)
    oReq.withCredentials = true;
    oReq.setRequestHeader("Authentication", "Basic " + sEncoded);
    oReq.setRequestHeader("Accept", "text/plain");
    oReq.send("action=login");
    return false;
}
