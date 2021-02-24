<widget_chart>
    <div id="{opts.ref}" if="{type == 'line' || type == 'stepped'}" class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1" onclick={ switchCard() }>{title}<span class="float-right">&#x2699;</span></div>
        <div class="card-body"  if={ front }><canvas ref="line0" id="line0"></canvas></div>
        <div class="card-body table-responsive" if={ !front } >
            <table id="devices" class="table table-condensed">
                <thead>
                    <tr>
                        <th scope="col">#</th>
                        <th scope="col">{ app.texts.widget_chart.timestamp[app.language] }</th>
                        <th scope="col" if="{!multiLine}"><span class="float-right">{ jsonData[0][0].name }</span></th>
                        <th scope="col" if="{multiLine}" each="{jsonData[0]}"><span class="float-right">{ name }</span></th>
                    </tr>
                </thead>
                <tbody if="{!multiLine}">
                    <tr each="{item,index in jsonData[0]}" class='small'>
                        <td>{ index }</td>
                        <td>{formatDate(new Date(item.timestamp), true)}</td>
                        <td><span class="float-right">{item.value}</span></td>
                    </tr>
                </tbody>
                <tbody if="{multiLine}">
                    <tr each="{item,index in jsonData}" class='small'>
                        <td>{ index }</td>
                        <td>{formatDate(new Date(item[0].timestamp), true)}</td>
                        <td each="{item}"><span class="float-right">{value}</span></td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <script>
        var self = this
        // opts: poniższe przypisanie nie jest używane
        //       wywołujemy update() tego taga żeby zminieć parametry
        self.type = opts.type
        self.title = opts.title
        // opts

        self.front = true
        self.rawdata = "[]"
        self.jsonData = []
        self.line = this.refs.line0
        self.ctxL = {}
        self.chart = {}
        //self.deviceEUI = ''
        self.width = 100
        self.heightStr = 'height:100px;'
        self.multiLine = false

        self.show2 = function(){
            self.jsonData = JSON.parse(this.rawdata)
            app.log(self.jsonData)
            if (self.jsonData.length == 0 || self.jsonData[0].length == 0){
                return;
            }
            getWidth()
            self.multiLine = self.jsonData[0].length > 1 && self.jsonData[0][1]['name'] != self.jsonData[0][0]['name']
            self.showMultiLineGraph(self.type,false,self.chartOption)
        }
        
        self.showMultiLineGraph = function(chartType,afterSwitch,chartOption){
            if (!self.front || self.jsonData[0].length == 0 ){ 
                app.log('return because '+self.front+' '+self.jsonData[0].length)
                return 
            }
            self.line = this.refs.line0
            self.ctxL = self.line.getContext('2d')
            var minWidth = 400
            var largeSize = self.width > minWidth
            var numberOfLines = self.multiLine?self.jsonData[0].length:1
            var colors = ['blue','green','red','black']
            var areaColors = ['powderblue','palegreen','lightpink','silver']
            var axesNames = ['axis1','axis2','axis3','axis4']
            
            var chartData = {
                labels: [],
                datasets: []
            }
            for(i=0; i<numberOfLines; i++){
                chartData.datasets.push(
                {
                    label: self.jsonData[0][i].name,
                    backgroundColor: areaColors[i],
                    borderColor: colors[i],
                    steppedLine: (chartType == 'stepped'?true:false),
                    data: [],
                    fill: (chartOption=='area'||chartOption=='areaWithDots'?true:false),
                    yAxisID: axesNames[i]
                }        
                )
            }
            
            var options = {
                responsive: true,
                title:{
                    display:true,
                    text: self.title + ' ' + firstDate + ' - ' + lastDate
                },
                scales: {
                    xAxes: [{
                        display: true,
                        type: 'time',
                        distribution: 'linear',
                        bounds: 'data',
                        time:{
                            unit: 'millisecond'
                        },
                        ticks:{
                            source: 'data'
                        },
                        scaleLabel: {
                            display: false,
                            labelString: 'Time'
                        }
                    }],
                    yAxes: []
                },
                elements: {
                    point:{
                        radius: (chartOption=='plain'||chartOption=='area'?0:3)
                    }
                }
            }
            
            var firstDate = ''
            var lastDate = ''
            if(self.multiLine){
                for (i = 0; i < self.jsonData.length; i++){
                    for (j=0; j < self.jsonData[i].length && j < 4; j++){
                        chartData.datasets[i].data.push({t:self.jsonData[i][j]['timestamp'], y:self.jsonData[i][j]['value']})
                        console.log(j+' '+self.jsonData[i][j]['timestamp']+' '+self.jsonData[i][j]['value'])
                    }
                }
            }else{
                for (i = 0; i < self.jsonData[0].length; i++){
                    chartData.datasets[0].data.push({t:self.jsonData[0][i]['timestamp'], y:self.jsonData[0][i]['value']})
                    console.log(i+'i '+self.jsonData[0][i]['timestamp']+' '+self.jsonData[0][i]['value'])
                }
            }
            if (self.toLocaleTimeStringSupportsLocales()){
                firstDate = new Date(self.jsonData[0][0]['timestamp']).toLocaleDateString(app.language)
                lastDate = new Date(self.jsonData[self.jsonData.length - 1][0]['timestamp']).toLocaleDateString(app.language)
            }else{
                firstDate = new Date(self.jsonData[0][0]['timestamp']).toISOString().substring(0, 10)
                lastDate = new Date(self.jsonData[self.jsonData.length - 1][0]['timestamp']).toISOString().substring(0, 10)
            }
            options.scales.xAxes[0].time.min=self.jsonData[0][0]['timestamp']
            options.scales.xAxes[0].time.max=self.jsonData[0][self.jsonData[0].length - 1]['timestamp']
            if (self.multiLine){
                for (i = 0; i < self.jsonData.length; i++){
                    if (largeSize){
                        if (self.toLocaleTimeStringSupportsLocales()){
                            chartData.labels.push(new Date(self.jsonData[i][0]['timestamp']).toLocaleTimeString(app.language))
                        }else{
                            chartData.labels.push(self.formatDate(new Date(self.jsonData[i][0]['timestamp']), false))
                        }
                    } else{
                        chartData.labels.push('')
                    }
                }                
            } else{
                for (i = 0; i < self.jsonData[0].length; i++){
                    if (largeSize){
                        if (self.toLocaleTimeStringSupportsLocales()){
                            chartData.labels.push(new Date(self.jsonData[0][i]['timestamp']).toLocaleTimeString(app.language))
                        }else{
                            chartData.labels.push(self.formatDate(new Date(self.jsonData[0][i]['timestamp']), false))
                        }
                    } else{
                        chartData.labels.push('')
                    }
                }                
            }

            for(i=0; i<numberOfLines; i++){
                options.scales.yAxes.push(
                    {
                        id: axesNames[i],
                        type: 'linear',
                        position: i==0?'left':'right',
                        display: true,
                        scaleLabel: {
                            display: true,
                            labelString: self.jsonData[0][i].name
                        }
                    }
                )
            }
            
            try{
                if(self.chart && !afterSwitch){
                    self.chart.data = chartData
                    self.chart.options = options
                    self.chart.update({duration:0})
                }else{
                    self.chart = new Chart(self.ctxL, {
                        type: 'line',
                        data: chartData,
                        options: options
                    })
                }
            }catch(err){
                self.chart = new Chart(self.ctxL, {
                    type: 'line',
                    data: chartData,
                    options: options
                })
            }
    
        }

        switchCard(){
            return function(e){
                self.front = !self.front
                riot.update()
                self.showMultiLineGraph(self.type,true,self.chartOption)
            }
        }

        self.formatDate = function(myDate, full){
            if (full){
                return myDate.toLocaleString(getSelectedLocale())
            } else{
                return myDate.getHours() + ':' + myDate.getMinutes() + ':' + myDate.getSeconds()
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
            self.width = $('#' + opts.ref).width()
            self.heightStr = 'height:' + self.width + 'px;'
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
</widget_chart>