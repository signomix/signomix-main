// default script
var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
var IotEvent = Java.type("com.signomix.iot.IotEvent");
var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
var channelData;
var now = Date.now();
var result;
var eui;
var dataTimestamp;
var dataReceived;
var channelReader;

var sgx = {}
sgx.eui = ''
sgx.transmissionTimestamp = 0
sgx.addList = function (timestamp) {
    result.addDataList(timestamp);
}
sgx.put = function (name, newValue, timestamp) {
    result.putData(sgx.eui, name, newValue, timestamp);
}
sgx.accept = function (name) {
    for (i = 0; i < dataReceived.length; i++) {
        if (dataReceived[i].getName() == name) {
            result.putData(sgx.eui, name, dataReceived[i].getValue(), dataReceived[i].getTimestamp());
        }
    }
}
sgx.getModulo = function (value, divider) {
    return result.getModulo(value, divider);
}
sgx.getOutput = function () {
    return result.getOutput();
}
sgx.getTimestampUTC = function (y, m, d, h, min, s) {
    return Date.UTC(y, m - 1, d, h, min, s);
}
sgx.getValue = function (channelName) {
    for (i = 0; i < dataReceived.length; i++) {
        if (dataReceived[i].getName() == channelName) {
            return dataReceived[i].getValue();
        }
    }
    return null;
}
sgx.getTimestamp = function (channelName) {
    for (i = 0; i < dataReceived.length; i++) {
        if (dataReceived[i].getName() == channelName) {
            return dataReceived[i].getTimestamp();
        }
    }
    return null;
}
sgx.getLastValue = function (channelName) {
    return channelReader.getLastData(channelName);
}
sgx.getAverageOf = function (channelName, scope) {
    return channelReader.getAverageValue(channelName, scope).getValue();
}
sgx.getNewAverageOf = function (channelName, scope, newValue) {
    return channelReader.getAverageValue(channelName, scope, newValue).getValue();
}
sgx.addVirtualData = function (newEUI, newUser, newName, newValue) {
    result.addDataEvent(newEUI, newUser, new ChannelData(newEUI, newName, newValue, dataTimestamp));
}
sgx.addNotification = function (newType, newMessage) {
    result.addEvent(newType, newMessage);
}
sgx.addCommand = function (targetEUI, payload) {
    result.addCommand(targetEUI, payload);
}
sgx.swap32 = function (val) {
    return ((val & 0xFF) << 24)
            | ((val & 0xFF00) << 8)
            | ((val >> 8) & 0xFF00)
            | ((val >> 24) & 0xFF);
}
sgx.reverseHex=function(hexStr){
    if (!(typeof hexStr === 'string' || hexStr instanceof String)){
        return 0
    }
    if(hexStr.length % 2 !== 0){
        return 0
    }
    var result=''
    for(i=hexStr.length-2;i>=0;i=i-2){
        result=result+hexStr.substring(i,i+2)
    }
    return result
}

//NOT NEEDED?
function modifyTimestamp(name, newTimestamp) {
    result.modifyTimestamp(name, newTimestamp);
}
function removeData(name) {
    result.removeData(name);
}
function renameData(oldName, newName) {
    result.rename(oldName, newName);
}

//DEPRECATED:
function addNotification(newType, newMessage) {
    result.addEvent(newType, newMessage);
}
function putData(name, newValue) {
    result.putData(new ChannelData(eui, name, newValue, dataTimestamp));
}
function putData(name, newValue, newTimestamp) {
    result.putData(new ChannelData(eui, name, newValue, newTimestamp));
}

function addCommand(targetEUI, payload) {
    result.addCommand(targetEUI, payload);
}

function getValueOf(channelName) {
    for (i = 0; i < dataReceived.length; i++) {
        if (dataReceived[i].getName() == channelName) {
            return dataReceived[i].getValue();
        }
    }
    return null;
}

function getLastValue(channelName) {
    return channelReader.getLastData(channelName);
}

function getAverageOf(channelName, scope) {
    return channelReader.getAverageValue(channelName, scope).getValue();
}

function getNewAverageOf(channelName, scope, newValue) {
    return channelReader.getAverageValue(channelName, scope, newValue).getValue();
}


function addVirtualData(newEUI, newUser, newName, newValue) {
    result.addDataEvent(newEUI, newUser, new ChannelData(newEUI, newName, newValue, dataTimestamp));
}

var processData = function (newEUI, newData, newChannelReader, userID, newTimestamp) {
    result = new ScriptResult();
    eui = newEUI;
    dataTimestamp = newTimestamp;
    dataReceived = newData;
    channelReader = newChannelReader;
    //put original values. 
    if (dataReceived.length > 0) {
        for (i = 0; i < dataReceived.length; i++) {
            channelData = dataReceived[i];
            result.putData(channelData);
        }
    }
    sgx.eui = newEUI;
    sgx.transmissionTimestamp = newTimestamp;
    //injectedCode
    return result;
}

var processRawData = function (newEUI, requestBody, newChannelReader, userID, newTimestamp) {
    result = new ScriptResult();
    eui = newEUI;
    dataTimestamp = newTimestamp;
    dataReceived = [];
    channelReader = newChannelReader;
    //injectedCode
    return result;
}