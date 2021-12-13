<widget_report>
    <div id={opts.ref} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center">{title}</div>
        </div>    
        <div class="row px-3 py-1">
            <div class="col-12" if="{ noData }">
                { app.texts.widget_report.NO_DATA[app.language] }
            </div>
            <div class="col-12" if="{ !noData }">
                <div class="table-responsive">
                <table class="table table-sm">
                <thead>
                    <tr class="table-active">
                        <td class="text-uppercase text-right" scope="col">#</td>
                        <td class="text-uppercase text-left" scope="col">{ app.texts.widget_report.EUI[app.language] }</td>
                        <td class="text-uppercase text-left" scope="col">{ app.texts.widget_report.NAME[app.language] }</td>
                        <td class="text-uppercase text-right" scope="col" each={ name in measureNames }>{name}</td>
                        <td class="text-uppercase text-right"scope="col">{ app.texts.widget_report.DATE[app.language] }</td>
                    </tr>
                </thead>
                <tbody>
                    <tr each={ device,index in jsonData}>
                        <td class="text-right">{(index+1)}</td>
                        <td class="text-left"><a href="{'#!dashboard,'+getDeviceEUI(device)+'@'}">{getDeviceEUI(device)}</a></td>
                        <td class="text-left">{getDeviceName(device)}</td>
                        <td class="text-right" each={measure in device}>{(measure?measure.value:'')}</td>
                        <td class="text-right">{getDateFormatted(new Date(getDeviceTimestamp(device)))}</td>
                        <!--<td class="text-right"><a href="{'#!dashboard,'+device[0].deviceEUI}">{ app.texts.widget_report.MORE[app.language] }</a></td>-->
                    </tr>
                </tbody>
                </table>
                </div>    
            </div>
        </div>
    </div> 
    <script>
    var self = this
    // opts: poniższe przypisanie nie jest używane
    //       wywołujemy update() tego taga żeby zminieć parametry
   
    //self.type = opts.type
    //self.visible = opts.visible
    //self.title = opts.title
    //self.description = opts.description
    
    // opts
    
    self.color = 'bg-white'
    self.rawdata = "[]"
    self.jsonData = {}

    self.noData = true
    self.width=100
    self.heightStr='height:100px;'
    self.measureNames = []
    self.groups = []
    
    self.show2 = function(){
        getGroups(self.group)
        if(self.channelTranslated){
            self.measureNames=self.channelTranslated.split(",")
        }else{
            self.measureNames=self.channel.split(",")
        }
        app.log('SHOW2 '+self.type)
        self.jsonData = JSON.parse(this.rawdata)
        self.verify()
        if(self.jsonData[0]) self.noData = false
        getWidth()
    }
    
    var getGroups = function(gr) {
                getData(app.groupAPI+'?group='+gr,
                    null,
                    app.user.token,
                    updateGroups,
                    self.listener, //globalEvents
                    'OK',
                    null, // in case of error send response code
                    app.debug,
                    globalEvents
                    );
        }

    var updateGroups = function (text) {
            self.groups = JSON.parse(text);
            riot.update();
    }
        
    self.getDeviceName = function(device){
        var tmpEUI=self.getDeviceEUI(device);
            for(var i =0; i< self.groups.length; i++){
                if(self.groups[i].EUI===tmpEUI){
                    return self.groups[i].name
                }
            }
        return '';
    }
    self.getDeviceEUI = function(device){
        for(j=0; j<device.length; j++){
           if(device[j]!=null){
             if(device[j].deviceEUI!=null){
               return device[j].deviceEUI;
             }
           }
        }
        return '';
    }
    self.getDeviceTimestamp = function(device){
        var tmpT=null
        for(j=0; j<device.length; j++){
           if(device[j]!=null){tmpT=device[j].timestamp; break;}
        }
        return tmpT;
    }
    self.verify=function(){
        var minimalMeasures = 1; //previously 2 
        var i=0
        var valuesOK=true
        var j
        while(i<self.jsonData.length){
            if(self.jsonData[i]==null || self.jsonData[i].length<minimalMeasures){
                self.jsonData.splice(i,1)
            }else{
                valuesOK=true
                j=0
                while(j<self.jsonData[i].length){
                    if(self.jsonData[i][j]===null){
                        self.jsonData[i][j]={'deviceEUI':null,'name':null,'value':null,'timestamp':null}
                    }
                    j=j+1
                }
                i=i+1
            }
        }
    }
        
    $(window).on('resize', resize)
    
    function getWidth(){
        self.width=$('#'+opts.ref).width()
        self.heightStr='height:'+self.width+'px;'
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
</widget_report>