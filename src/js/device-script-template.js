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
var channerReader;

function addNotification(newType,newMessage){
    result.addEvent(newType,newMessage);
}

function putData(name,newValue){
    result.putData(new ChannelData(eui,name,newValue,dataTimestamp));
}

function removeData(name){
    result.removeData(name);
}

function renameData(oldName,newName){
    putData(newName,getValueOf(oldName))
    removeData(oldName)
}

function addCommand(targetEUI,payload){
    result.addCommand(targetEUI,payload);
}

function getValueOf(channelName){
    for (i = 0; i < dataReceived.length; i++) {
        if(dataReceived[i].getName()==channelName){
            return dataReceived[i].getValue();
        }
    }
    return null;
}

function getLastValue(channelName){
    return channelReader.getLastData(channelName);
}

function getAverageOf(channelName,scope){
    return channelReader.getAverageValue(channelName,scope).getValue();
}

function getNewAverageOf(channelName,scope,newValue){
    return channelReader.getAverageValue(channelName,scope,newValue).getValue();
}


function addVirtualData(newEUI,newUser,newName,newValue){
    result.addDataEvent(newEUI,newUser,new ChannelData(newEUI,newName,newValue,dataTimestamp));
}

var processData = function (newEUI, newData, newChannelReader, userID, newTimestamp) {
    result = new ScriptResult();
    eui = newEUI;
    dataTimestamp = newTimestamp;
    dataReceived = newData;
    channelReader = newChannelReader;
    //put original values. 
    if(dataReceived.length>0){
    for (i = 0; i < dataReceived.length; i++) {
        channelData = dataReceived[i];
        result.putData(channelData);
    }
    }
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