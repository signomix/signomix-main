<widget_plan>
    <div id={opts.ref} if={type == 'plan'} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center">{title}</div>
        </div>    
        <div class="row px-3 py-1">
            <div id="{planID}" class="col-12 text-center"></div>
        </div>
    </div>
    <script>
    var self = this
    self.color = 'bg-white'
    self.rawdata = "[]"
    self.jsonData = {}
    self.planDefinition = ''
    self.noData = true
    self.width=100
    self.heightStr='height:100px;'
    self.measureNames = []
    self.groups = []
    self.scaling=1
    self.planID='plan_'+opts.ref
    
    self.show2 = function(){
        self.planDefinition=this.description
        if(self.channelTranslated){
            self.measureNames=self.channelTranslated.split(",")
        }else{
            self.measureNames=self.channel.split(",")
        }
        self.jsonData = JSON.parse(this.rawdata)
        self.verify()
        //if(self.jsondata && self.jsonData[0]) self.noData = false
        self.calcAlert=(self.range!='' && self.range.indexOf('@')>0)
        if(self.calcAlert){
            self.rangeName=self.range.substring(self.range.indexOf('@')+1)
        }
        getWidth()
        getGroup(self.group)
    }
    
    var getGroup = function(gr) {
            getData(app.groupAPI+'?group='+gr,
                    null,
                    app.user.token,
                    updateGroup,
                    self.listener, //globalEvents
                    'OK',
                    null, // in case of error send response code
                    app.debug,
                    globalEvents
                );
        }

    var updateGroup = function (text) {
        self.groups = JSON.parse(text);
        setPlanDef(self.jsonData)
        riot.update();
    }
        
    self.getDeviceName = function(eui){
        for(var i =0; i< self.groups.length; i++){
            if(self.groups[i].EUI===eui){
                return self.groups[i].name
            }
        }
        return eui;
    }
    self.getDeviceLocation = function(eui){
        for(var i =0; i< self.groups.length; i++){
            if(self.groups[i].EUI===eui){
                return {x:self.groups[i].latitude,y:self.groups[i].longitude}
            }
        }
        return location;
    }
    
    self.verify=function(){
        if(!self.jsondata) return;
        var i=0
        var valuesOK=true
        var j
        while(i<self.jsonData.length){
            if(self.jsonData[i]==null || self.jsonData[i].length<2){
                self.jsonData.splice(i,1)
            }else{
                valuesOK=true
                j=0
                while(j<self.jsonData[i].length){
                    if(self.jsonData[i][j]===null){
                        self.jsonData[i][j]={'eui':'','name':null,'value':null,'timestamp':null}
                    }
                    j=j+1
                }
                i=i+1
            }
        }
    }
    
    self.getRemValue = function(){
        return parseFloat(getComputedStyle(document.body).fontSize)/self.scaling;
    }
    
    function setPlanDef(deviceData){
        //svg will be scaled and moved properly only if starting svg width equals 1000
        var pd=self.planDefinition.trim()
        //var radius=10
        var radius=self.getRemValue()/2;
        pd=pd.substring(0,pd.length-6);
        var devs='';
        var cTemplate='<circle cx="_x" cy="_y" r="'+radius+'" stroke="black" stroke-width="2" fill="_c"/>'
        var oneDev
        var deviceLocation
        if(deviceData){
            for(let i=0;i<deviceData.length; i++){
                oneDev=cTemplate
                deviceLocation=self.getDeviceLocation(deviceData[i][0].deviceEUI)
                oneDev=oneDev.replace('_x',deviceLocation.x)
                oneDev=oneDev.replace('_y',deviceLocation.y)
                oneDev=oneDev.replace('_c',self.getMarkerColor(deviceData[i]))
                devs=devs+oneDev+self.getNameplate(deviceLocation.x+radius+10,deviceLocation.y,self.scaling,self.groups[i].name,deviceData[i])
            }
        }
        pd=pd+devs+'</svg>'
        document.getElementById(self.planID).innerHTML=pd
    }
    
    self.getNameplate = function(x,y,scaling,name,data){
        if(self.measureNames[0]=='#'){
            return ''
        }
        var line='<tspan x="_x" y="_y">_label: _v</tspan>'
        var fontSize=1/scaling
        var remValue=self.getRemValue();
        var text='<text x="'+x+'" y="'+y+'" style="fill:darkblue;font-size:'+fontSize+'em;">[ '+name+' ]'
        for(i=0;i<data.length;i++){
            if (typeof self.measureNames[i] !== "undefined") {
                var l=line
                l=l.replace('_x',x)
                l=l.replace('_y',y+remValue*(i+1))
                l=l.replace('_label',self.measureNames[i])
                l=l.replace('_v',data[i].value)
                text=text+l
            }
        }
        return text+'</text>'
    }
    
    self.getMarkerColor = function (point){
        result='lightblue'
        if(!self.calcAlert){
            return result
        }
        for(var i=0;i<point.length;i++){
            if(point[i]){
                if(point[i].name==self.rangeName){
                    var lv=getAlertLevel(self.range, point[i].value)
                    switch(lv){
                        case 0:
                            result='green'
                            break
                        case 1:
                            result='yellow'
                            break
                        case 2:
                            result='red'
                            break
                    }
                    return result;
                }
            }
        }
        return result
    }
        
    $(window).on('resize', resize)
    
    function getWidth(){
        self.width=$('#'+opts.ref).width()
        self.heightStr='height:'+self.width+'px;'
        self.scaling=self.width/1000
    }
    
    function resize(){
        getWidth()
        self.show2()
    }
        
    </script>
    <style>
        .topspacing{
            margin-top: 10px;
        }
       
    </style>
</widget_plan>