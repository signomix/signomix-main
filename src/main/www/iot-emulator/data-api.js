/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
// get data from the service
function getData(url, query, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {

    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (this.status != 200) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };

    oReq.open("get", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(query);
    return false;
}

function sendFormData(oFormElement, method, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    if (debug) {
        console.log("postFormData")
    }
    ;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (this.status != 200) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };

    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && (this.status == 200 || this.status == 201)) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };
    // method declared in the form is ignored
    oReq.open(method, url);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(new FormData(oFormElement));
    return false;
}

function sendJsonData(data, method, url, authHeader, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    if (debug) {
        console.log("sendJsonData")
        console.log(method)
        console.log(url)
        console.log(token)
    }
    ;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (!(this.status == 200 || this.status == 201)) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };

    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && (this.status == 200 || this.status == 201)) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };

    oReq.open(method, url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader(authHeader, token);
    }
    oReq.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    oReq.send(JSON.stringify(data));
    return false;
}

function deleteData(url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {

    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "dataerror:";
    oReq.onerror = function (oEvent) {
        if (debug) {
            console.log("onerror " + this.status + " " + oEvent.toString())
        }
        ;
    };
    oReq.onload = function (oEvent) {/*getdataCallback(elementId, statusId);*/
        if (debug) {
            console.log("onload " + this.status + " " + oEvent.toString())
        }
        ;
        if (this.status != 200) {
            var fullErrName
            if (errorEventName == null) {
                fullErrName = defaultErrorEventName + this.status;
            } else {
                fullErrName = errorEventName;
            }
            if (appEventBus == null) {
                eventBus.trigger(fullErrName);
            } else {
                appEventBus.trigger(fullErrName);
            }
        }
        ;
    };
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            console.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventBus.trigger(successEventName);
            }
        } else if (this.readyState == 4 && this.status == 0) {
            //eventBus.trigger(errorEventName);
            if (errorEventName == null) {
                eventBus.trigger(defaultErrorEventName + this.status);
            } else {
                eventBus.trigger(errorEventName);
            }
        }
    };

    oReq.open("DELETE", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(null);
    return false;
}
