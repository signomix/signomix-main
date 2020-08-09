<widget_chart>
    <div id="{opts.ref}" if="{type == 'line' || type == 'stepped'}" class="container-fluid bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center" onclick={ switchCard()}>{title}</div>
        </div> 
        <div class="row px-3 py-1" if={ front }>
                <div class="col-12">
                    <canvas ref="line0" id="line0"></canvas>
                </div>
        </div>
        <div class="row px-3 py-1" if={ !front }>
            <div if={ ! dataAvailable} class="col-12">
                <p>{ app.texts.widget_chart.nodata[app.language] }</p>
            </div>
            <div if={dataAvailable} class="col-12 table-responsive">
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
        self.width = 100
        self.heightStr = 'height:100px;'
        self.multiLine = false
        self.dataAvailable=false;

        self.show2 = function(){
            self.jsonData = JSON.parse(this.rawdata)
            app.log(self.jsonData)
            if (self.jsonData.length == 0 || self.jsonData[0].length == 0){
                self.dataAvailable=false;
                return;
            }
            self.dataAvailable=true;
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
                        
            var firstDate = ''
            var lastDate = ''
            var dFirst,dLast
            if(self.multiLine){
                for (var i = 0; i < self.jsonData.length; i++){
                    for (var j=0; j < self.jsonData[i].length && j < 4; j++){
                        chartData.datasets[j].data.push({x:self.jsonData[i][j]['timestamp'], y:self.jsonData[i][j]['value']})
                    }
                }
                dFirst=self.jsonData[0][0]['timestamp']
                dLast=self.jsonData[self.jsonData.length - 1][0]['timestamp']
            }else{
                for (var i = 0; i < self.jsonData[0].length; i++){
                    chartData.datasets[0].data.push({x:new Date(self.jsonData[0][i]['timestamp']), y:self.jsonData[0][i]['value']})
                }
                dFirst=self.jsonData[0][0]['timestamp']
                dLast=self.jsonData[0][self.jsonData[0].length - 1]['timestamp']
            }
            if (self.toLocaleTimeStringSupportsLocales()){
                firstDate = new Date(dFirst).toLocaleDateString(app.language)
                lastDate = new Date(dLast).toLocaleDateString(app.language)    
            }else{
                firstDate = new Date(dFirst).toISOString().substring(0, 10)
                lastDate = new Date(dLast).toISOString().substring(0, 10)
            }

            var options = {
                responsive: true,
                title:{
                    display:true,
                    text: self.title + ' ' + firstDate + ' - ' + lastDate
                },
                tooltips: {
                    callbacks: {
                        label: function(tooltipItem, data) {
                            var label = data.datasets[tooltipItem.datasetIndex].label || '';
                            if (label) {
                                label += ': ';
                            }
                            label += tooltipItem.yLabel;
                            return label;
                        },
                        title: function(tooltipItems, data){
                            return self.formatDate(new Date(tooltipItems[0].xLabel),true)
                        }
                    }
                },
                scales: {
                    xAxes: [{
                        display: false,
                        type: 'linear',
                        ticks:{
                            min:dFirst,
                            max:dLast
                        },
                        scaleLabel: {
                            display: false,
                            labelString: ''
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