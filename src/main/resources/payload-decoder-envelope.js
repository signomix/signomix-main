// envelope for per-device payload encoding
//var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
//var List = Java.type("java.util.ArrayList");

function hexToBytes(hex) {
    for (var bytes = [], c = 0; c < hex.length; c += 2)
    bytes.push(parseInt(hex.substr(c, 2), 16));
    return bytes;
}

function decodeData(eui, originalPayload, timestamp) {
    //var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
    //var List = Java.type("java.util.ArrayList");
    var payload = new Uint8Array(originalPayload.length);
    for(var i = 0; i < originalPayload.length; i++) {
        payload[i] = originalPayload[i];
    }
    return decode(eui, payload, timestamp);
}

function decodeHexData(eui, originalPayload, timestamp) {
    //var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
    //var List = Java.type("java.util.ArrayList");
    var payload = hexToBytes(originalPayload);
    return decode(eui, payload, timestamp);
}

var decode = function(eui, payload, timestamp){
    var ChannelData = Java.type("com.signomix.out.iot.ChannelData");
    var List = Java.type("java.util.ArrayList");
    var result = new List();
    //injectedCode
    return result;
}
