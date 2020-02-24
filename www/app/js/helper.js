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
function getAlertLevel(definition, value){
    //remove whitespaces and measure name
    let def=definition.replace(/\s+/g, '');
    if(def.indexOf("@")>0){
        def=def.substring(0,definition.indexOf("@"))
    }
    if(def.length==0) return 0;
    let defs=def.split(":");
    let alertDef=defs[0];
    let warningDef="";
    if(defs.length>1){
        warningDef=defs[1];
    }
    let alertRule=getAlertRule(alertDef);
    let warningRule=getAlertRule(warningDef);
    if(isRuleMet(alertRule,value)){
        return 2;
    }
    if(isRuleMet(warningRule,value)){
        return 1;
    }
    return 0;
}

function isRuleMet(rule,value){
    if(!isNaN(rule.value1)){
        if(rule.comparator1==1 && value<rule.value1){
            return true;
        }
        if(rule.comparator1==2 && value>rule.value1){
            return true;
        }
    }
    // OR
    if(!isNaN(rule.value2)){
        if(rule.comparator2==1 && value<rule.value2){
            return true;
        }
        if(rule.comparator2==2 && value>rule.value2){
            return true;
        }
    }
    return false;
}

function getAlertRule(definition){
    let rule={
        varName:null,
        comparator1:0,
        value1:NaN,
        comparator2:0,
        value2:NaN
    }
    if(definition.length==0){
        return rule;
    }
    let chr;
    let element="";
    let elementNumber=0;
    for(let i=0; i<definition.length; i++){
        chr=definition.charAt(i);
        if(chr==">" || chr=="<"){
            if(element!="" && rule.varName==null){
                rule.varName=element;
                element="";
            }else if(element=="" && rule.varName==null){
                rule.varName="x";
            }else if(element!="" && Number.isNaN(rule.value1)){
                rule.value1=Number(element);
                element="";
            }else if(element!="" && Number.isNaN(rule.value2)){
                rule.value2=Number(element);
                element="";
            }
            if(rule.comparator1==0){
                rule.comparator1=chr=="<"?1:2;
            }else{
                rule.comparator2=chr=="<"?1:2;
            }
        }else{
            element=element+chr;
        }
    }
    if(element.length>0){
        if(Number.isNaN(rule.value1)){
            rule.value1=Number(element);
        }else if(Number.isNaN(rule.value2)){
            rule.value2=Number(element);
        }
    }
    return rule;
}

function getMeasureType(name) {
    if (name.indexOf('temperatur') > -1) {
        return 1;
    }
    if (name.indexOf('humidity') > -1) {
        return 2;
    }
    if (name.indexOf('pressure') > -1) {
        return 3;
    }
    if (name.indexOf('date') > -1 || name.indexOf('time') > -1) {
        return 4;
    }
    if (name.indexOf('speed') > -1 || name.indexOf('velocity') > -1) {
        return 5;
    }
    if (name.indexOf('distance') > -1 || name.indexOf('length') > -1 || name.indexOf('width') > -1 || name.indexOf('height') > -1) {
        return 6;
    }
    if (name.indexOf('luminance') > -1 || name.indexOf('lux') > -1 || name.indexOf('light') > -1) {
        return 7;
    }
    if (name.indexOf('battery') > -1 || name.indexOf('voltage') > -1) {
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

function getStopwatchFormatted(h,m,s) {

    var dt = ''
    dt = dt.concat('', (h > 9 ? '' + h : '0' + h))
    dt = dt.concat(':', (m > 9 ? '' + m : '0' + m))
    dt = dt.concat(':', (s > 9 ? '' + s : '0' + s))
    return dt
}