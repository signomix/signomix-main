<widget_led>
    <div id={ opts.ref } class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1"  onclick={ switchCard() }>
            <i class="material-icons yellow" style="margin-right: 10px; font-size: smaller" if={alertLevel==1}>notifications_active</i>
            <i class="material-icons red" style="margin-right: 10px; font-size: smaller" if={alertLevel==2}>error_outline</i>
             {title}<span class="float-right">&#x2699;</span>
        </div>
        <div class="card-body">
             <div class="row">
                 <div class="col text-center" if={ front }>
                    <img width="25%" style="margin-right: 10px;" src={ getLedImgUrl(alertLevel) }>
                 </div>
                 <div class="col text-center h5" if={ !front }>
                     {measureDate}<br>{value}
                 </div>
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
        self.front = true
        
        self.show2 = function(){
            app.log('SHOW LED')
            self.jsonData = JSON.parse(this.rawdata)
            app.log(this.rawdata)
            if(self.jsonData.length==0 || self.jsonData[0].length==0){
                return
            }
            self.value = parseFloat(self.jsonData[0][0]['value'])
            if(self.range!=''){
                self.alertLevel=getAlertLevel(self.range, self.value)
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
                    return '/images/led-green.svg'
                case 1:
                    return '/images/led-yellow.svg'
                case 2:
                    return '/images/led-red.svg'
                default:
                    return '/images/KO.svg'
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