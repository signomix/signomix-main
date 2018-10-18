// default script
var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
var IotEvent = Java.type("com.signomix.iot.IotEvent");
var ScriptResult = Java.type("com.signomix.out.script.ScriptResult");
var channelData;
var now = Date.now();

var processData = function (eui, dataReceived, channelReader, userID, dataTimestamp) {
    var result = new ScriptResult();
    //put original values. 
    for (i = 0; i < dataReceived.length; i++) {
        channelData = dataReceived[i];
        result.putData(channelData);
    }
    //injectedCode
    return result;
}

