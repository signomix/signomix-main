<widget_a1>
    <div id={opts.ref} if={type == 'raw'} class="card card-block topspacing p-0">
        <div class="card-header h6 text-left p-1">{title}</div>
        <div class="card-body"><pre>{rawdata}</pre></div>
    </div>
    <div id={opts.ref} if={type == 'line' || type == 'stepped'} class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1" onclick={ switchCard() }>{title}<span class="float-right">&#x2699;</span></div>
        <div class="card-body"  if={ front }><canvas ref="line0" id="line0"></canvas></div>
        <div class="card-body table-responsive" if={ !front } >
            <table id="devices" class="table table-condensed">
                <thead>
                    <tr><th scope="col">#</th><th scope="col">{ app.texts.widget_a1.timestamp[app.language] }</th><th scope="col"><span class="float-right">{ app.texts.widget_a1.value[app.language] }</span></th></tr>
                </thead>
                <tbody>
                    <tr each={jsonData[0]}  class='small'>
                        <td>{ getNextIndex() }</td><td>{formatDate(new Date(timestamp), true)}</td><td><span class="float-right">{value}<raw content={ unitName }></raw></span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <div id={opts.ref} if={type == 'text'} class="card card-block topspacing p-1">
        <raw content={ description }></raw>
    </div>
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
                 <div class="col-9 text-center h5" if={ front }>
                        {value} <raw content={ unitName }></raw>
                 </div>
                 <div class="col-9 text-center h5" if={ !front }>
                        {measureDate}
                 </div>
            </div>
        </div>
    </div> 
    <div id={opts.ref} if={ type == 'button' } class="card text-center topspacing p-0">
        <div class="card-body text-center" if={ !app.user.guest }>
            <button type="button" class="btn btn-danger btn-block" data-toggle="modal" data-target="#myModal">{ title }</button>
        </div>
        <div class="card-body text-center" if={ app.user.guest }>
            <p>---</p>
        </div>
    </div>
    <div id="myModal" class="modal fade" if={ type == 'button' }>
        <div class="modal-dialog modal-sm">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">{ title }</h4>
                </div>
                <div class="modal-body">
                    <p>{ app.texts.widget_a1.device[app.language] } { deviceEUI }</p>
                    <p>{ app.texts.widget_a1.newvalue[app.language] } { channelName }</p>
                    <input type='text' value="0" name="newvalue" id='newvalue2set'>
                    <p>{ description }</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-default pull-right" data-dismiss="modal">{ app.texts.widget_a1.cancel[app.language] }</button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" onclick={ sendReset() }>{ app.texts.widget_a1.save[app.language] }</button>
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
        if(self.jsonData.length>0 && self.jsonData[0].length>0){
            self.deviceEUI = self.jsonData[0][0]['deviceEUI']
            self.channelName = self.jsonData[0][0]['name']
        }
        self.levels = []
        self.tableIndex = 0
        getWidth()
        switch(self.type){
            case 'gauge':
                self.showGauge()
                break
            case 'symbol':
                self.showSymbol()
                break
            case 'line':
            case 'stepped':
                self.showLineGraph(self.type)
                break
            case 'button':
                self.showButton()
                break
        }
    }
    
    self.showSymbol = function(){
        app.log('SYMBOL')
        self.iconName = 'data_usage'
        self.alertLevel=0
        if(self.jsonData.length==0 || self.jsonData[0].length==0){
            return
        }
        self.measureType=getMeasureType(self.jsonData[0][0]['name'])
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
                self.iconName='label_important'
                break
            case 6:
                self.iconName='linear_scale'
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

    self.showButton = function(){
        app.log('BUTTON')
        if(self.jsonData.length==0 || self.jsonData[0].length==0){
            return
        }
        self.channelName = self.jsonData[0][0]['name']
        self.value = parseFloat(self.jsonData[0][0]['value'])
        app.log('BUTTON name: '+self.channelName)
    }
    
    self.showLineGraph = function(chartType){
        app.log('GRAPH '+chartType)
        if(!self.front){ return }
        self.line = this.refs.line0
        var ctxL = self.line.getContext('2d');
        var minWidth=400
        var largeSize=self.width>minWidth
        var chartData = {
                labels: [],
                datasets: [{
                    backgroundColor: 'blue',
                    borderColor: 'blue',
                    steppedLine: (chartType=='stepped'?'before':false),
                    data: [],
                    fill: false,
                }
                ]
            }
        if(largeSize){
            chartData.datasets=
            [{
                    label: self.channelName,
                    backgroundColor: 'blue',
                    borderColor: 'blue',
                    steppedLine: (chartType=='stepped'?'before':false),
                    data: [],
                    fill: false,
                }
            ]
        }
        var firstDate = ''
        var lastDate = ''
        if(self.toLocaleTimeStringSupportsLocales()){
            for(i=0;i<self.jsonData[0].length;i++){
                chartData.datasets[0].data.push(self.jsonData[0][i]['value'])
                if(largeSize){
                chartData.labels.push(new Date(self.jsonData[0][i]['timestamp']).toLocaleTimeString(app.language))
                }else{
                chartData.labels.push('')
                }
            }
            if(self.jsonData[0].length>0){
                firstDate = new Date(self.jsonData[0][0]['timestamp']).toLocaleDateString(app.language)
                lastDate = new Date(self.jsonData[0][self.jsonData[0].length-1]['timestamp']).toLocaleDateString(app.language)
            }
        }else{
            for(i=0;i<self.jsonData[0].length;i++){
                chartData.datasets[0].data.push(self.jsonData[0][i]['value'])
                if(largeSize){
                chartData.labels.push(self.formatDate(new Date(self.jsonData[0][i]['timestamp']),false))
                }else{
                chartData.labels.push('')
                }
            }
            if(self.jsonData[0].length>0){
                firstDate = new Date(self.jsonData[0][0]['timestamp']).toISOString().substring(0,10)
                lastDate = new Date(self.jsonData[0][self.jsonData.length-1]['timestamp']).toISOString().substring(0,10)
            }
        }    
        var options = {
                responsive: true,
                legend:null,
                title:{
                    display:true,
                    text: self.title + ' ' + firstDate + ' - '+lastDate
                },
                scales: {
                    xAxes: [{
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: 'Time'
                        }
                    }],
                    yAxes: [{
                        id: 'first-axis',
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: self.channelName
                        }
                    }]
                }
            }
        self.chart = new Chart(ctxL, {
            type: 'line',
            data: chartData,
            options: options
        })

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
            if(self.front && (self.type=='line' || self.type=='stepped')){
                self.showLineGraph()
            }
        }
    }
    
    self.splitRange = function(text,separator){
        var afterSplit = text.split(separator)
        var result = []
        for(i=0;i<afterSplit.length;i++){
            result.push(Number(afterSplit[i]))
        }
        return result
    }
    
    self.formatDate = function(myDate, full){
        if(full){
            return myDate.toLocaleString(getSelectedLocale())
        }else{
            return myDate.getHours()+':'+myDate.getMinutes()+':'+myDate.getSeconds()
        }
    }
    
    self.submitted = function(){
        console.log('submitted')
    }
    
    sendReset(){
        return function(e){
            e.preventDefault()
            if(self.jsonData.length==0 || self.jsonData[0].length==0){
                return
            }
            //console.log(self.rawdata)
            //console.log('sendReset '+(self.deviceEUI)+ ' '+(self.channelName)+ ' '+(document.getElementById('newvalue2set').value))
            var value = document.getElementById('newvalue2set').value
            var dataToSend = {}
            dataToSend[self.channelName] = value
            //console.log(dataToSend)
            sendJsonData(
                dataToSend, 
                'POST', 
                app.actuatorAPI+'/'+self.deviceEUI,
                'Authentication',
                app.user.token, 
                self.submitted, 
                self.listener, 
                'submit:OK', 
                'submit:ERROR', 
                app.debug, 
                globalEvents
            )
        }
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