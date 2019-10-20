<widget_a1>
    <div id={opts.ref} if={type == 'symbol'} class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1"  onclick={ switchCard() }>
            <i class="material-icons yellow" style="margin-right: 10px; font-size: smaller" if={alertLevel==1}>notifications_active</i>
            <i class="material-icons red" style="margin-right: 10px; font-size: smaller" if={alertLevel==2}>error_outline</i>
             {title}<span class="float-right">&#x2699;</span>
        </div>
        <div class="card-body">
             <div class="row">
                 <div class="col-3 text-center">
                    <i class="material-icons md-48 blue" if={alertLevel==0}>{iconName}</i>
                    <i class="material-icons md-48 yellow" if={alertLevel==1}>{iconName}</i>
                    <i class="material-icons md-48 red" if={alertLevel==2}>{iconName}</i>
                 </div>
                 <div class="col-9 text-center h3" if={ front }>
                      <b>{value}</b> <raw content={ unitName }></raw>
                 </div>
                 <div class="col-9 text-center h5" if={ !front }>
                        {measureDate}
                 </div>
            </div>
        </div>
    </div> 

    <script>
    var self = this
    //self.refs = this.refs
    // opts: poniższe przypisanie nie jest używane
    //       wywołujemy update() tego taga żeby zminieć parametry
    self.type = opts.type
    self.visible = opts.visible
    self.title = opts.title
    self.unitName = opts.unitName
    self.description = opts.description
    self.range = opts.range
    // opts
    
    self.value = '-'
    self.measureDate = '-'
    self.color = 'bg-white'
    self.levels =[]
    self.alertLevel = 0
    self.tableIndex = 0
    
    self.front = true

    self.rawdata = "[]"
    self.jsonData = {}
    self.gauge = this.refs.gauge00
    self.line = this.refs.line0
    self.chart = {}
    self.deviceEUI = ''
    self.channelName = ''

    self.noData = false
    self.width=100
    self.heightStr='height:100px;'
    
    this.on('mount',function(){
        app.log('MOUNTING A1')
    })
    
    self.show2 = function(){
        app.log('SHOW2 '+self.type)
        self.jsonData = JSON.parse(this.rawdata)
        app.log(self.jsonData)
        self.levels = []
        self.tableIndex = 0
        getWidth()
        self.showSymbol()
    }
    
    self.showSymbol = function(){
        app.log('SYMBOL')
        self.iconName = 'data_usage'
        self.alertLevel=0
        if(self.jsonData == null || self.jsonData.length==0 || self.jsonData[0].length==0){
            return
        }
        self.measureType=getMeasureType(self.jsonData[0][0]['name'].toLowerCase())
        if(self.measureType==0) self.measureType=getMeasureType(self.title.toLowerCase())
        switch(self.measureType){
            case 1:
                self.iconName='whatshot'
                break
            case 2:
                self.iconName='opacity'
                break
            case 3:
                self.iconName='get_app'
                break
            case 4:
                self.iconName='event'
                break
            case 5:
                self.iconName='speed'
                break
            case 6:
                self.iconName='straighten'
                break
            case 7:
                self.iconName='wb_incandescent'
                break
            case 8:
                self.iconName='battery_unknown'
                break
            case 9:
            case 10:
                self.iconName='location_on'
                break
            case 11:
                self.iconName='landscape'
                break
        }
        self.value = parseFloat(self.jsonData[0][0]['value'])
        self.measureDate = getDateFormatted(new Date(self.jsonData[0][0]['timestamp']))
        self.alertLevel=getAlertLevel(self.range, self.value)
    }
        
    this.on('update', function(e){
    })
    
    self.listener = riot.observable()
    self.listener.on('*',function(eventName){
        app.log("widget_a1 listener on event: "+eventName)
    })
    
    self.getNextIndex = function(){
        return self.tableIndex++
    }
        
    switchCard(){
        return function(e){
            self.tableIndex = 0
            self.front=!self.front
            riot.update()
        }
    }
    
    self.formatDate = function(myDate, full){
        if(full){
            return myDate.toLocaleString(getSelectedLocale())
        }else{
            return myDate.getHours()+':'+myDate.getMinutes()+':'+myDate.getSeconds()
        }
    }
    
    self.submitted = function(){
        app.log('submitted')
    }
    
    self.toLocaleTimeStringSupportsLocales = function() {
        try {
            new Date().toLocaleTimeString('i');
        } catch (e) {
            return e.name === 'RangeError';
        }
        return false;
    }
    
    $(window).on('resize', resize)
    
    function getWidth(){
        self.width=$('#'+opts.ref).width()
        self.heightStr='height:'+self.width+'px;'
    }
    
    function resize(){
        getWidth()
        self.show2()
        //riot.update()
    }
        
    </script>
    <style>
        .topspacing{
            margin-top: 10px;
        }
       
    </style>
</widget_a1>