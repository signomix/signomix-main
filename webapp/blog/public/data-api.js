/* Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
 * Licensed under the Apache License, Version 2.0 */

function decodeDocument(doc){
  var result = doc  
  if (result.title) {
    try {
      result.title = decodeURIComponent(result.title)
    } catch (e) {
      result.title = unescape(result.title)
    }
  }
  if (result.summary) {
    try {
      result.summary = decodeURIComponent(result.summary)
    } catch (e) {
      result.summary = unescape(result.summary)
    }
  }
  if (result.content) {
    try {
      result.content = decodeURIComponent(result.content)
    } catch (e) {
      result.content = unescape(result.content)
    }
  }
  return result
}
// ucs-2 string to base64 encoded ascii
function utoa(str) {
  return window.btoa(unescape(encodeURIComponent(str)));
}
// base64 encoded ascii to ucs-2 string
function atou(str) {
  return decodeURIComponent(escape(window.atob(str)));
}

function getDataCallEventName(success, defaultName, status){
    var errorPrefix = 'err:';
    var successName = 'dataLoaded';
    var startName = 'sending';
    if(success===(undefined||null)){
        return startName;
    }
    if(success===true){
        return successName;
    }else{
        if(defaultName!==(undefined||null)){
            return defaultName;
        }else{
            return errorPrefix+status;
        }
    }
}

function getData(url, token, callback) {
    const oReq = new XMLHttpRequest();
    oReq.onerror = function (oEvent) {
    }
    oReq.onloadend = function(oEvent){
    }
    oReq.onabort = function(oEvent){
    }
    oReq.timeout = function(oEvent){
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) {
            callback(this.status, this.responseText);
        }
    };
    oReq.open("get", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send();
}

function sendData(data, method, url, token, callback) {
    const urlEncodedData = "";
    const urlEncodedDataPairs = [];
    const oReq = new XMLHttpRequest();
    //Object.keys(data).forEach(key=>{urlEncodedDataPairs.push(encodeURIComponent(key)+'='+encodeURIComponent(data[key]))});
    data.forEach((name)=>urlEncodedDataPairs.push(encodeURIComponent(name)+'='+encodeURIComponent(data[name])));
    urlEncodedData = urlEncodedDataPairs.join('&').replace(/%20/g, '+');
    oReq.onerror = function (oEvent) {
        app.requests--;
    }
    oReq.onloadend = function(oEvent){
        app.requests--;
    }
    oReq.onabort = function(oEvent){
        app.requests--;
    }
    oReq.timeout = function(oEvent){
        app.requests--;
    }
    oReq.onreadystatechange = function () {
        callback(this.status,this.responseText);
    }

    app.requests++;
    oReq.open(method, url);
    oReq.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(urlEncodedData);
    return false;
}

function sendFormData(formData, method, url, token, callback) {
    var oReq = new XMLHttpRequest();
    oReq.onerror = function (oEvent) {
    }
    oReq.onloadend = function(oEvent){
    }
    oReq.onabort = function(oEvent){
    }
    oReq.timeout = function(oEvent){
    }   
    oReq.onreadystatechange = function () {
        if (this.readyState == 4) callback(this.status,this.responseText);
    };
    oReq.open(method, url);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(formData);
    return false;
}

function deleteData(url, token, callback, eventListener, successEventName, errorEventName, debug, appEventBus) {
    var oReq = new XMLHttpRequest();
    oReq.onerror = function (oEvent) {
        app.requests--;
        eventListener.trigger(getDataCallEventName(false,errName,this.status));
    }
    oReq.onloadend = function(oEvent){
        app.requests--;
        eventListener.trigger(getDataCallEventName(true));
    }
    oReq.onabort = function(oEvent){
        app.requests--;
    }
    oReq.timeout = function(oEvent){
        app.requests--;
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            app.log(JSON.parse(this.responseText));
            if (callback != null) {
                callback(this.responseText);
            } else {
                eventListener.trigger(getDataCallEventName(true));
            }
        }else if (this.status == 401 || this.status == 403) {
            eventListener.trigger(getDataCallEventName(false,null,this.status));
        }
    };
    app.requests++;
    eventListener.trigger(getDataCallEventName(null));
    oReq.open("DELETE", url, true);
    if (token != null) {
        oReq.withCredentials = true;
        oReq.setRequestHeader("Authentication", token);
    }
    oReq.send(null);
    return false;
}

function afterLogin(newKey, name) {
    var key = newKey.trim();
    if (key == "") {
        app.log('Login failed');
    } else {
        app.offline = false;
        app.user.token = key;
        app.user.name = name;
        app.user.status = "logged-in";
    }
}

function afterLoginError(){
    app.user.status = "logged-out";
}

function loginSubmit(oFormElement, eventListener, successEventName, errName) {
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
        app.requests--;
        eventListener.trigger(getDataCallEventName(false,errName,this.status));
    }
    oReq.onloadend = function(oEvent){
        app.requests--;
        eventListener.trigger(getDataCallEventName(true));
    }
    oReq.onabort = function(oEvent){
        app.requests--;
    }
    oReq.timeout = function(oEvent){
        app.requests--;
    }
    oReq.onreadystatechange = function () {
        if (this.readyState == 4 && this.status == 200) {
            afterLogin(oReq.responseText, login);
            eventListener.trigger(successEventName);
        } else if (this.readyState == 4 && this.status > 400) {
            afterLoginError();
            eventListener.trigger(getDataCallEventName(false,errName,this.status));
        }
    };
    sEncoded = utoa(login + ":" + password);
    app.requests++;
    eventListener.trigger(getDataCallEventName(null));
    oReq.open("post", app.authAPI);
    oReq.withCredentials = true;
    oReq.setRequestHeader("Authentication", "Basic " + sEncoded);
    oReq.setRequestHeader("Accept", "text/plain");
    oReq.send("action=login");
    return false;
}