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
    var ChannelData = Java.type("com.signomix.common.iot.ChannelData");
    var List = Java.type("java.util.ArrayList");
    var result = new List();
    //injectedCode
    return result;
}

// http://www.onicos.com/staff/iz/amuse/javascript/expert/utf.txt

/* utf.js - UTF-8 <=> UTF-16 convertion
 *
 * Copyright (C) 1999 Masanao Izumo <iz@onicos.co.jp>
 * Version: 1.0
 * LastModified: Dec 25 1999
 * This library is free.  You can redistribute it and/or modify it.
 */

function Utf8ArrayToStr(array) {
    var out, i, len, c;
    var char2, char3;
  
    out = "";
    len = array.length;
    i = 0;
    while (i < len) {
      c = array[i++];
      switch (c >> 4)
      { 
        case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
          // 0xxxxxxx
          out += String.fromCharCode(c);
          break;
        case 12: case 13:
          // 110x xxxx   10xx xxxx
          char2 = array[i++];
          out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
          break;
        case 14:
          // 1110 xxxx  10xx xxxx  10xx xxxx
          char2 = array[i++];
          char3 = array[i++];
          out += String.fromCharCode(((c & 0x0F) << 12) |
                                     ((char2 & 0x3F) << 6) |
                                     ((char3 & 0x3F) << 0));
          break;
      }
    }    
    return out;
  }
