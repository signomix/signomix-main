<widget_time>
    <div id={ opts.ref } class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1">
            {title}<span class="float-right">&#x2699;</span>
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-2 text-center">
                    <i class="material-icons md-48 blue">{iconName}</i>
                </div>
                <div class="col-10 text-center h3">
                    {dt}
                </div>
            </div>
        </div>
    </div>
    <script>
        var self = this
        self.rawdata = "[]"
        self.jsonData = {}
        self.h=0
        self.m=0
        self.s=0
        self.iconName = 'hourglass_empty'
        
        self.show2 = function(){
            self.jsonData = JSON.parse(this.rawdata)
            if(self.jsonData.length>0 && self.jsonData[0].length>0){
                if(parseFloat(self.jsonData[0][0]['value'])<=0){
                  self.dt='00:00:00'
                }else{
                  self.value = Math.floor((parseFloat(self.jsonData[0][0]['value']))/1000)
                self.h=Math.floor(self.value/3600)
                self.m=Math.floor((self.value%3600)/60)
                self.s=self.value-self.h*3600-(60*self.m)
                self.dt = getStopwatchFormatted(self.h, self.m, self.s)
                }
            }else{
                self.dt='00:00:00'
            }
        }
    </script>
    <style>
        .topspacing{ margin-top: 10px; }
    </style>
</widget_time>