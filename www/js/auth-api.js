/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
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
    //riot.update()
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
        app.log(oEvent.toString())
        app.user.status = "logged-out";
        eventBus.trigger(errorEventName);
    }
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        app.log(oEvent.toString())
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            app.requests--;
            if (this.status == 200) {
                afterLogin(oReq.responseText, login);
                eventBus.trigger(successEventName);
            } else if (this.status > 400) {
                app.user.status = "logged-out";
                eventBus.trigger(errorEventName);
                app.log("result code == " + this.status)
            }
        }
    }
    app.requests++;
    sEncoded = utoa(login + ":" + password);
    oReq.open("post", app.authAPI);
    app.log('app.authAPI=' + app.authAPI)
    oReq.withCredentials = true;
    oReq.setRequestHeader("Authentication", "Basic " + sEncoded);
    oReq.setRequestHeader("Accept", "text/plain");
    oReq.send("action=login");
    return false;
}