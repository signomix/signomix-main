/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

// get data from the service
function getData(url, query, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    oReq.onerror = function (oEvent) {
        app.log('ONERROR');
        app.requests--;
        app.log("onerror " + this.status + " " + oEvent.toString())
        if (appEventBus == null) {
            eventBus.trigger("data:"+this.status);
        } else {
            appEventBus.trigger("data:"+this.status);
        }
    }
    oReq.onloadend = function(oEvent){
        app.log('ONLOADEND'+this.status);
        app.requests--;
        if (eventBus != null) {
            eventBus.trigger("dataloaded");
        }
        if(appEventBus!=null){
            appEventBus.trigger("dataloaded");
        }
    }
    oReq.onabort = function(oEvent){
        app.log('ONABORT'+this.status);
        app.requests--;
        if (appEventBus == null) {
            eventBus.trigger("data:"+this.status);
        } else {
            appEventBus.trigger("data:"+this.status);
        }
    }
    oReq.timeout = function(oEvent){
        app.log('ONTIMEOUT'+this.status);
        app.requests--;
        if (appEventBus == null) {
            eventBus.trigger("data:"+this.status);
        } else {
            appEventBus.trigger("data:"+this.status);
        }
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            if (this.status == 200) {
                try{
                app.log(JSON.parse(this.responseText));
                }catch(err){}
                if (callback != null) {
                    callback(this.responseText, successEventName);
                } else {
                    eventBus.trigger(successEventName);
                }
            } else {
                var tmpErrName
                if (errorEventName == null) {
                    tmpErrName=defaultErrorEventName + this.status
                } else {
                    tmpErrName=errorEventName
                }
                if (appEventBus == null) {
                    eventBus.trigger(tmpErrName);
                } else {
                    appEventBus.trigger(tmpErrName);
                }
            }
        }else{
            app.log('READYSTATE='+this.readyState);
        }
    };
    app.log('SENDING');
    app.requests++;
    oReq.open("get", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(query);
    //app.responses--;
    return false;
}

function sendFormData(oFormElement, method, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    app.log("sendFormData ...")
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        if (appEventBus == null) {
            eventBus.trigger("auth"+this.status);
        } else {
            appEventBus.trigger("auth"+this.status);
        }
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            app.requests--;
            if (this.status == 200 || this.status == 201) {
                app.log(JSON.parse(this.responseText));
                if (callback != null) {
                    callback(this.responseText);
                } else {
                    eventBus.trigger(successEventName);
                }
            } else {
                /*if (errorEventName == null) {
                    eventBus.trigger(defaultErrorEventName + this.status);
                } else {
                    eventBus.trigger(errorEventName);
                }*/
                var tmpErrName
                if (errorEventName == null) {
                    tmpErrName=defaultErrorEventName + this.status
                } else {
                    tmpErrName=errorEventName
                }
                if (appEventBus == null) {
                    eventBus.trigger(tmpErrName);
                } else {
                    appEventBus.trigger(tmpErrName);
                }

            }
        }
    }
    app.requests++;
    // method declared in the form is ignored
    oReq.open(method, url);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(new FormData(oFormElement));
    return false;
}

function sendData(data, method, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    app.log("sendData ...")
    var urlEncodedData = "";
    var urlEncodedDataPairs = [];
    var name;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    // Turn the data object into an array of URL-encoded key/value pairs.
    for (name in data) {
        urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(data[name]));
    }
// Combine the pairs into a single string and replace all %-encoded spaces to 
// the '+' character; matches the behaviour of browser form submissions.
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        callback(oEvent.toString());
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            app.requests--;
            if (this.status > 199 && this.status < 203) {
                app.log(JSON.parse(this.responseText));
                if (callback != null) {
                    callback(this.responseText);
                } else {
                    eventBus.trigger(successEventName);
                }
            } else {
                app.log("onreadystatechange")
                //if (callback != null) {
                //    callback('error:' + this.status)
                //} else {
                    if (errorEventName == null) {
                        eventBus.trigger(defaultErrorEventName + this.status);
                    } else {
                        eventBus.trigger(errorEventName);
                    }
                //}
            }
        }
    }
    app.requests++;
    // method declared in the form is ignored
    oReq.open(method, url);
    oReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(urlEncodedData);
    return false;
}

function deleteData(url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        if (appEventBus == null) {
            eventBus.trigger("auth"+this.status);
        } else {
            appEventBus.trigger("auth"+this.status);
        }
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            app.requests--;
            if (this.status == 200) {
                app.log(JSON.parse(this.responseText));
                if (callback != null) {
                    callback(this.responseText);
                } else {
                    eventBus.trigger(successEventName);
                }
            } else {
                /*if (errorEventName == null) {
                    eventBus.trigger(defaultErrorEventName + this.status);
                } else {
                    eventBus.trigger(errorEventName);
                }*/
                var tmpErrName
                if (errorEventName == null) {
                    tmpErrName=defaultErrorEventName + this.status
                } else {
                    tmpErrName=errorEventName
                }
                if (appEventBus == null) {
                    eventBus.trigger(tmpErrName);
                } else {
                    appEventBus.trigger(tmpErrName);
                }

            }
        }
    }
    app.requests++;
    oReq.open("DELETE", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(null);
    return false;
}

function deleteConditional(data, url, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    var urlEncodedData = "";
    var urlEncodedDataPairs = [];
    var name;
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    for (name in data) {
        urlEncodedDataPairs.push(encodeURIComponent(name) + '=' + encodeURIComponent(data[name]));
    }
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        if (appEventBus == null) {
            eventBus.trigger("auth"+this.status);
        } else {
            appEventBus.trigger("auth"+this.status);
        }
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            app.requests--;
            if (this.status == 200) {
                app.log(JSON.parse(this.responseText));
                if (callback != null) {
                    callback(this.responseText);
                } else {
                    eventBus.trigger(successEventName);
                }
            } else {
                /*if (errorEventName == null) {
                    eventBus.trigger(defaultErrorEventName + this.status);
                } else {
                    eventBus.trigger(errorEventName);
                }*/
                var tmpErrName
                if (errorEventName == null) {
                    tmpErrName=defaultErrorEventName + this.status
                } else {
                    tmpErrName=errorEventName
                }
                if (appEventBus == null) {
                    eventBus.trigger(tmpErrName);
                } else {
                    appEventBus.trigger(tmpErrName);
                }

            }
        }
    }
    app.requests++;
    oReq.open("DELETE", url, true);
    oReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(urlEncodedData);
    return false;
}

function sendJsonData(data, method, url, authHeader, token, callback, eventBus, successEventName, errorEventName, debug, appEventBus) {
    var oReq = new XMLHttpRequest();
    var defaultErrorEventName = "err:";
    oReq.onerror = function (oEvent) {
        app.log("onerror " + this.status + " " + oEvent.toString())
        if (appEventBus == null) {
            eventBus.trigger("auth"+this.status);
        } else {
            appEventBus.trigger("auth"+this.status);
        }
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            app.requests--;
            if (this.status == 200 || this.status == 201) {
                app.log(JSON.parse(this.responseText));
                if (callback != null) {
                    callback(this.responseText);
                } else {
                    eventBus.trigger(successEventName);
                }
            } else {
                /*if (errorEventName == null) {
                    eventBus.trigger(defaultErrorEventName + this.status);
                } else {
                    eventBus.trigger(errorEventName);
                }*/
                var tmpErrName
                if (errorEventName == null) {
                    tmpErrName=defaultErrorEventName + this.status
                } else {
                    tmpErrName=errorEventName
                }
                if (appEventBus == null) {
                    eventBus.trigger(tmpErrName);
                } else {
                    appEventBus.trigger(tmpErrName);
                }
            }
        }
    }
    app.requests++;
    oReq.open(method, url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader(authHeader, token);
    }
    oReq.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    oReq.send(JSON.stringify(data));
    return false;
}