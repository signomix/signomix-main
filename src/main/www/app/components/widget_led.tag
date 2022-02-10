<widget_led>
    <div id={ opts.ref } class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center" onclick={ switchCard()}>{title}</div>
        </div>
        <div class="row px-3 py-1">
            <div class="col-12 text-center" if={ front }>
                <img width="20%" src={ getLedImgUrl(alertLevel) }>
            </div>
            <div class="col-12 text-right" if={ !front }>
                <h5>{measureDate}</h5>
            </div>
        </div>
    </div>
    <script>
        var self = this
        self.rawdata = "[]"
        self.jsonData = {}
        self.alertLevel = 0
        self.levels = []
        self.iconName = 'event'
        self.value = '-'
        self.range = ''
        self.measureDate = '-'
        self.timestamp = 0
        self.front = true
        
        self.show2 = function(){
            app.log('SHOW LED')
            self.jsonData = JSON.parse(this.rawdata)
            app.log(this.rawdata)
            if(self.jsonData.length==0 || self.jsonData[0].length==0){
                return
            }
            self.value = parseFloat(self.jsonData[0][0]['value'])
            self.timestamp=self.jsonData[0][0]['timestamp']
            if(self.range!=''){
                self.alertLevel=getAlertLevel(self.range, self.value, self.timestamp)
            }else{
                self.alertLevel=self.value==0?0:2
            }
            self.deviceEUI = self.jsonData[0][0]['deviceEUI']
            self.channelName = self.jsonData[0][0]['name']
            self.measureDate = getDateFormatted(new Date(self.jsonData[0][0]['timestamp']))
        }
        
        switchCard(){
            return function(e){
                self.front=!self.front
                riot.update()
            }
        }
        
        getLedImgUrl(level){
            switch(level){
                case 0:
                    return 'images/led-green.svg'
                case 1:
                    return 'images/led-yellow.svg'
                case 2:
                    return 'images/led-red.svg'
                case 3:
                    return 'images/led-grey.svg'
                default:
                    return 'images/KO.svg'
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
        .topspacing{ margin-top: 10px; }
    </style>
</widget_led>