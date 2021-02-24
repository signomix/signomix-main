/// default script
var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
var ChannelData = Java.type("com.signomix.out.iot.ChannelData");

//deprecated
var result=new ScriptResult()
var dataReceived=[]
var channelReader={}
//

var sgx0 = {}
sgx0.dataReceived=[]
sgx0.result=new ScriptResult()
sgx0.dataTimestamp=0
sgx0.channelReader={}

sgx0.accept = function (name) {
    for (i = 0; i < this.dataReceived.length; i++) {
        if (this.dataReceived[i].getName() == name) {
            this.result.putData(this.eui, name, this.dataReceived[i].getValue(), this.dataReceived[i].getTimestamp());
        }
    }
}
sgx0.addCommand = function (targetEUI, payload) {
    //JSON payload
    this.result.addCommand(targetEUI, payload, false);
}
sgx0.addHexCommand = function (targetEUI, payload) {
    //for TTN devices payload must be String representing byte array as hex values
    //eg. 00FFAA01
    this.result.addCommand(targetEUI, payload, true);
}
sgx0.addNotification = function (newType, newMessage) {
    this.result.addEvent(newType, newMessage);
}
sgx0.addVirtualData = function (newEUI, newUser, newName, newValue) {
    this.result.addDataEvent(newEUI, newUser, new ChannelData(newEUI, newName, newValue, this.dataTimestamp));
}
sgx0.getAverageOf = function (channelName, scope) {
    return this.channelReader.getAverageValue(channelName, scope).getValue();
}
sgx0.getLastValue = function (channelName) {
    return this.channelReader.getLastData(channelName);
}
sgx0.getModulo = function (value, divider) {
    return this.result.getModulo(value, divider);
}
sgx0.getNewAverageOf = function (channelName, scope, newValue) {
    return this.channelReader.getAverageValue(channelName, scope, newValue).getValue();
}
sgx0.getOutput = function () {
    return this.result.getOutput();
}
sgx0.getTimestamp = function (channelName) {
    var ts=0
    for (i = 0; i < this.dataReceived.length; i++) {
        if (this.dataReceived[i].getName() == channelName) {
            ts=this.dataReceived[i].getTimestamp()
            break
        }
    }
    if(ts==0) ts=Date.now()
    return ts;
}
sgx0.getTimestampUTC = function (y, m, d, h, min, s) {
    return Date.UTC(y, m - 1, d, h, min, s);
}
sgx0.getValue = function (channelName) {
    for (i = 0; i < this.dataReceived.length; i++) {
        if (this.dataReceived[i].getName() == channelName) {
            return this.dataReceived[i].getValue();
        }
    }
    return null;
}
sgx0.getStringValue = function (channelName) {
    for (i = 0; i < this.dataReceived.length; i++) {
        if (this.dataReceived[i].getName() == channelName) {
            return this.dataReceived[i].getStringValue();
        }
    }
    return null;
}
sgx0.put = function (name, newValue, timestamp) {
    this.result.putData(this.eui, name, newValue, timestamp);
}
sgx0.reverseHex=function(hexStr){
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
sgx0.swap32 = function (val) {
    return ((val & 0xFF) << 24)
            | ((val & 0xFF00) << 8)
            | ((val >> 8) & 0xFF00)
            | ((val >> 24) & 0xFF);
}
sgx0.distance = function(latitude1, longitude1, latitude2, longitude2){
    return this.result.getDistance(latitude1, longitude1, latitude2, longitude2);
}
sgx0.xaddList = function (timestamp) {
    this.result.addDataList(timestamp);
}
//NOT NEEDED?
//function modifyTimestamp(name, newTimestamp) {
//    sgx.result.modifyTimestamp(name, newTimestamp);
//}
//function removeData(name) {
//    sgx.result.removeData(name);
//}
//function renameData(oldName, newName) {
//    sgx.result.rename(oldName, newName);
//}

//DEPRECATED:
//function addNotification(newType, newMessage) {
//    result.addEvent(newType, newMessage);
//}
//function putData(name, newValue) {
//    result.putData(new ChannelData(eui, name, newValue, dataTimestamp));
//}
//function putData(name, newValue, newTimestamp) {
//    result.putData(new ChannelData(eui, name, newValue, newTimestamp));
//}

//function addCommand(targetEUI, payload) {
//    result.addCommand(targetEUI, payload);
//}

//function getValueOf(channelName) {
//    for (i = 0; i < sgx.dataReceived.length; i++) {
//        if (dataReceived[i].getName() == channelName) {
//            return dataReceived[i].getValue();
//        }
//    }
//    return null;
//}

//function getLastValue(channelName) {
//    return channelReader.getLastData(channelName);
//}

//function getAverageOf(channelName, scope) {
//    return channelReader.getAverageValue(channelName, scope).getValue();
//}

//function getNewAverageOf(channelName, scope, newValue) {
///    return channelReader.getAverageValue(channelName, scope, newValue).getValue();
//}


//function addVirtualData(newEUI, newUser, newName, newValue) {
//    result.addDataEvent(newEUI, newUser, new ChannelData(newEUI, newName, newValue, dataTimestamp));
//}

var processData = function (eui, dataReceived, channelReader, userID, dataTimestamp, latitude, longitude, altitude) {
    var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
    var IotEvent = Java.type("com.signomix.iot.IotEvent");
    var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
    var channelData={};

    var sgx=Object.create(sgx0)
    sgx.eui=eui
    sgx.latitude=latitude
    sgx.longitude=longitude
    sgx.altitude=altitude
    sgx.result=new ScriptResult()
    sgx.dataReceived=dataReceived
    sgx.dataTimestamp=dataTimestamp
    sgx.channelReader=channelReader
   
    
    //put original values. 
    if (dataReceived.length > 0) {
        for (i = 0; i < dataReceived.length; i++) {
            channelData = dataReceived[i];
            sgx.result.putData(channelData);
        }
    }
    //injectedCode
    
    return sgx.result;
}

var processRawData = function (eui, requestBody, channelReader, userID, dataTimestamp) {
    var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
    var IotEvent = Java.type("com.signomix.iot.IotEvent");
    var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
    var channelData={};

    var sgx=Object.create(sgx0)
    sgx.eui=eui
    sgx.result=new ScriptResult()
    sgx.dataReceived=[]
    sgx.dataTimestamp=dataTimestamp
    sgx.channelReader=channelReader

    //injectedCode
    return sgx.result;
}