// Copyright (C) Grzegorz Skorupa 2018.
// Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).

// @param {String} definition 
// @param {Number} test is value to check against definition
// @returns {Number} 2==alert, 1==warning, otherwise 0
//
function getAlertLevel(definition, test) {
    
    // definition "<-10>40:<0>30"
    // definition "{alertCondition}[:{warningConditon}]
    // condition: {comparator}{value}[{comparator}{value}]
    // comparator: [>|<]
    if (!definition || !test) {
        return 0
    }
    var r = definition.split(':')
    if (r.length == 0) {
        return 0
    }
    var alert = []
    var warning = []
    var tmp = ''
    var sub
    
    // parse alert condition
    var levelDefinition = r[0]
    for (i = 0; i < levelDefinition.length; i++) {
        sub = levelDefinition.substr(i, 1)
        if (sub == '<' || sub == '>') {
            if (tmp) {
                alert.push(tmp)
            }
            alert.push(sub)
            tmp = ''
        } else {
            tmp = tmp.concat(sub)
        }
    }
    alert.push(tmp)

    //parse warning condition
    if (r.length > 1) {
        tmp = ''
        levelDefinition = r[1]
        for (i = 0; i < levelDefinition.length; i++) {
            sub = levelDefinition.substr(i, 1)
            if (sub == '<' || sub == '>') {
                if (tmp) {
                    warning.push(tmp)
                }
                warning.push(sub)
                tmp = ''
            } else {
                tmp = tmp.concat(sub)
            }
        }
        warning.push(tmp)
    }
    
    // transform to float values
    try {
        if (warning.length > 1) {
            warning[1] = parseFloat(warning[1])
        }
        if (warning.length > 3) {
            warning[3] = parseFloat(warning[3])
        }
        if (alert.length > 1) {
            alert[1] = parseFloat(alert[1])
        }
        if (alert.length > 3) {
            alert[3] = parseFloat(alert[3])
        }
    } catch (err) {
        return -1
    }

    // analyze conditions
    var isAlert = false
    if (alert.length > 1) {
        switch (alert[0]) {
            case '<':
                isAlert = test < alert[1]
                break
            case '>':
                isAlert = test > alert[1]
                break
        }
    }
    if (alert.length > 3) {
        switch (alert[2]) {
            case '<':
                isAlert = isAlert || test < alert[3]
                break
            case '>':
                isAlert = isAlert || test > alert[3]
                break
        }
    }
    if (isAlert) {
        return 2
    }
    var isWarning = false
    if (warning.length > 1) {
        switch (warning[0]) {
            case '<':
                isWarning = test < warning[1]
                break
            case '>':
                isWarning = test > warning[1]
                break
        }
    }
    if (warning.length > 3) {
        switch (warning[2]) {
            case '<':
                isWarning = isWarning || test < warning[3]
                break
            case '>':
                isWarning = isWarning || test > warning[3]
                break
        }
    }
    if (isWarning) {
        return 1
    }   
    return 0
}

function getMeasureType(name){
    if(name.indexOf('temperature')>-1){
        return 1;
    }
    if(name.indexOf('humidity')>-1){
        return 2;
    }
    if(name.indexOf('pressure')>-1){
        return 3;
    }
    if(name.indexOf('date')>-1 || name.indexOf('time')>-1 ){
        return 4;
    }
    if(name.indexOf('speed')>-1 || name.indexOf('velocity')>-1){
        return 5;
    }
    if(name.indexOf('distance')>-1 || name.indexOf('length')>-1 || name.indexOf('width')>-1 || name.indexOf('height')>-1){
        return 6;
    }
    if(name.indexOf('luminance')>-1 || name.indexOf('lux')>-1){
        return 7;
    }
    if(name.indexOf('battery')>-1){
        return 8;
    }
    return 0;
}

function getDateFormatted(d){
    var dt=''+d.getFullYear()

    var tmp=d.getMonth()+1
    dt=dt.concat('-',(tmp>9 ? ''+tmp : '0'+tmp))

    tmp=d.getDate()
    dt=dt.concat('-',(tmp>9 ? ''+tmp : '0'+tmp))
    
    tmp=d.getHours()
    dt=dt.concat(' ',(tmp>9 ? ''+tmp : '0'+tmp))

    tmp=d.getMinutes()
    dt=dt.concat(':',(tmp>9 ? ''+tmp : '0'+tmp))

    tmp=d.getSeconds()
    dt=dt.concat(':',(tmp>9 ? ''+tmp : '0'+tmp))
    
    return dt
}