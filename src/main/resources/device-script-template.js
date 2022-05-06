/// default script
var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
var ChannelData = Java.type("com.signomix.common.iot.ChannelData");

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
sgx0.addCommand = function (targetEUI, payload, overwrite) {
    //JSON payload
    this.result.addCommand(targetEUI, this.eui, payload, false, overwrite);
}
sgx0.addHexCommand = function (targetEUI, payload, overwrite) {
    //for TTN devices payload must be String representing byte array as hex values
    //eg. 00FFAA01
    this.result.addCommand(targetEUI, this.eui, payload, true, overwrite);
}
sgx0.addNotification = function (newType, newMessage) {
    //this.result.log(">>>>"+newType+">>"+newMessage+">>");
    this.result.addEvent(newType, newMessage);
}
sgx0.addVirtualData = function (newEUI, newName, newValue) {
    this.result.addDataEvent(newEUI, this.eui, new ChannelData(newEUI, newName, newValue, this.dataTimestamp));
}
sgx0.getAverageOf = function (channelName, scope) {
    return this.channelReader.getAverageValue(channelName, scope).getValue();
}
sgx0.getNewAverageOf = function (channelName, scope, newValue) {
    return this.channelReader.getAverageValue(channelName, scope, newValue).getValue();
}
sgx0.getMinimumOf = function (channelName, scope) {
    return this.channelReader.getMinimalValue(channelName, scope).getValue();
}
sgx0.getNewMinimumOf = function (channelName, scope, newValue) {
    return this.channelReader.getMinimalValue(channelName, scope, newValue).getValue();
}
sgx0.getMaximumOf = function (channelName, scope) {
    return this.channelReader.getMaximalValue(channelName, scope).getValue();
}
sgx0.getNewMaximumOf = function (channelName, scope, newValue) {
    return this.channelReader.getMaximalValue(channelName, scope).getValue();
}
sgx0.getSumOf = function (channelName, scope) {
    return this.channelReader.getSummaryValue(channelName, scope).getValue();
}
sgx0.getNewSumOf = function (channelName, scope, newValue) {
    return this.channelReader.getSummaryValue(channelName, scope, newValue).getValue();
}
sgx0.getLastValue = function (channelName) {
    var tmpLastData=this.channelReader.getLastData(channelName);
    if(tmpLastData!=null){
        return tmpLastData.value
    }else{
        return null
    }
}
sgx0.getLastData = function (channelName) {
    return this.channelReader.getLastData(channelName);
}
sgx0.getModulo = function (value, divider) {
    return this.result.getModulo(value, divider);
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

sgx0.setState = function(newState){
    this.result.setDeviceState(newState);
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

var processData = function (eui, dataReceived, channelReader, userID, dataTimestamp, 
    latitude, longitude, altitude, state, alert,
    devLatitude, devLongitude, devAltitude, newCommand, requestData) {
    var ChannelData = Java.type("com.signomix.common.iot.ChannelData");
    var IotEvent = Java.type("com.signomix.event.IotEvent");
    var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
    var channelData={};

    var sgx=Object.create(sgx0)
    sgx.eui=eui
    sgx.latitude=latitude
    if(sgx.latitude==null){sgx.latitude=devLatitude}
    sgx.longitude=longitude
    if(sgx.longitude==null){sgx.longitude=devLongitude}
    sgx.altitude=altitude
    if(sgx.altitude==null){sgx.altitude=devAltitude}
    sgx.result=new ScriptResult()
    sgx.dataReceived=dataReceived
    sgx.dataTimestamp=Number(dataTimestamp)
    sgx.channelReader=channelReader
    sgx.state=state
    sgx.alert=alert
    sgx.virtualCommand=newCommand
    sgx.requestData=requestData
    
    //put original values. 
    if (dataReceived.length > 0) {
        for (i = 0; i < dataReceived.length; i++) {
            channelData = dataReceived[i];
            sgx.result.putData(channelData);
        }
    }
    sgx.result.setDeviceState(state);
    //injectedCode
    
    return sgx.result;
}

var processRawData = function (eui, requestBody, channelReader, userID, dataTimestamp) {
    var ChannelData = Java.type("com.signomix.common.iot.ChannelData");
    var IotEvent = Java.type("com.signomix.event.IotEvent");
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