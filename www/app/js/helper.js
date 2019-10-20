// Copyright (C) Grzegorz Skorupa 2018.
// Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).

// Calculates alert level
// 
// @param {String} definition 
// @param {Number} v is value to check against definition
// @returns {Number} 2==alert, 1==warning, otherwise 0
//
// definition "{alertCondition}[:{warningConditon}][@variableName]
// condition: [variableName]{comparator}{value}[[variableName]{comparator}{value}]
// comparator is one of: > <
// 
// example 1: "x<-10>40:x<0>30"
// example 2: "<-10>40:<0>30"
// example 3: "<-10>40:<0>30@x"
function getAlertLevel(definition, v) {
    if(definition.indexOf('@')>0) definition=definition.substring(0,definition.indexOf('@'))
    var level = 0;
    var defs = definition.split(":");
    var opd;
    var varName;
    var varValue;
    var gtOperator;
    var pos;
    var regEx=/<|>/g;
    //alert 
    opd = defs[0].split(regEx);
    varName = opd[0];
    varValue = opd[1];
    pos=varName.length;
    gtOperator = ('>' === defs[0].substring(pos,pos+1));
    if (gtOperator && v > parseFloat(varValue)) {
        level = 2;
    } else if (!gtOperator && v < parseFloat(varValue)) {
        level = 2;
    }
    if (opd.length === 3) {
        varValue = opd[2];
        var pos=pos+1+varValue.length;
        gtOperator = ('>' === defs[0].substring(pos,pos+1));
        if (level===2 || (gtOperator && v > parseFloat(varValue))) {
            level = 2;
        } else if (level===2 || (!gtOperator && v < parseFloat(varValue))) {
            level = 2;
        }
    }
    //warning
    if (defs.length === 1 || level===2) {
        return level;
    }
    opd = defs[1].split(regEx);
    varName = opd[0];
    varValue = opd[1];
    pos=varName.length;
    gtOperator = ('>' === defs[1].substring(pos,pos+1))
    if (gtOperator && v > parseFloat(varValue)) {
        level = 1;
    } else if (!gtOperator && v < parseFloat(varValue)) {
        level = 1;
    }
    if (opd.length === 3) {
        varValue = opd[2];
        var pos=pos+1+varValue.length;
        gtOperator = ('>' === defs[1].substring(pos,pos+1));
        if (level===1 || (gtOperator && v > parseFloat(varValue))) {
            level = 1;
        } else if (level===1 || (!gtOperator && v < parseFloat(varValue))) {
            level = 1;
        }
    }
    return level;
}


function getMeasureType(name) {
    if (name.indexOf('temperatur') > -1) {
        return 1;
    }
    if (name.indexOf('humidity') > -1 || name.indexOf('wilgotność') > -1) {
        return 2;
    }
    if (name.indexOf('pressure') > -1 || name.indexOf('time') > -1) {
        return 3;
    }
    if (name.indexOf('date') > -1 || name.indexOf('time') > -1 || name.indexOf('czas') > -1 || name.indexOf('dara') > -1) {
        return 4;
    }
    if (name.indexOf('speed') > -1 || name.indexOf('velocity') > -1 || name.indexOf('prędkość') > -1) {
        return 5;
    }
    if (name.indexOf('distance') > -1 || name.indexOf('length') > -1 || name.indexOf('width') > -1 || name.indexOf('height') > -1) {
        return 6;
    }
    if (name.indexOf('luminance') > -1 || name.indexOf('lux') > -1) {
        return 7;
    }
    if (name.indexOf('battery') > -1) {
        return 8;
    }
    if (name.indexOf('latitude') > -1) {
        return 9;
    }
    if (name.indexOf('longitude') > -1) {
        return 10;
    }
    if (name.indexOf('altitude') > -1) {
        return 11;
    }
    return 0;
}

function getDateFormatted(d) {

    var dt = '' + d.getFullYear()

    var tmp = d.getMonth() + 1
    dt = dt.concat('-', (tmp > 9 ? '' + tmp : '0' + tmp))

    tmp = d.getDate()
    dt = dt.concat('-', (tmp > 9 ? '' + tmp : '0' + tmp))

    tmp = d.getHours()
    dt = dt.concat(' ', (tmp > 9 ? '' + tmp : '0' + tmp))

    tmp = d.getMinutes()
    dt = dt.concat(':', (tmp > 9 ? '' + tmp : '0' + tmp))

    tmp = d.getSeconds()
    dt = dt.concat(':', (tmp > 9 ? '' + tmp : '0' + tmp))

    return dt
}