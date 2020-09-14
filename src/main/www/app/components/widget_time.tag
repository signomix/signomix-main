<widget_time>
    <div id={opts.ref} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center">{title}</div>
        </div>    
        <div class="row px-3 py-1">
                <div class="col-4">
                    <h1>
                        <i class="las {icon} text-primary" style="height: 1em;"></i>
                    </h1>
                </div>
                <div class="col-8 text-right">
                    <h1>{dt}</h1>
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
        
        self.show2 = function(){
            self.icon = 'la-clock'
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