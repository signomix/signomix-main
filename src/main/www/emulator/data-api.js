/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
function sendJsonData(data, method, url, callback, authHeader, token) {
    var oReq = new XMLHttpRequest();
    oReq.onerror = function (oEvent) {
        if(app.debug) console.log("onerror " + this.status + " " + oEvent.toString())
    }
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if(app.debug) console.log("onload " + this.status + " " + oEvent.toString())
        if (!(this.status == 200 || this.status == 201)) {
            if (callback != null) callback(status, oEvent.toString());
        }
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (callback != null) callback(this.status, this.responseText);
        }
    }
    oReq.open(method, url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader(authHeader, token);
    }
    oReq.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    oReq.send(JSON.stringify(data));
    return false;
}
