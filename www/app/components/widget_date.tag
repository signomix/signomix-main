<widget_date>
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
        self.dt=""
        
        self.show2 = function(){
            self.icon = 'la-calendar'
            self.jsonData = JSON.parse(this.rawdata)
            if(self.jsonData.length>0 && self.jsonData[0].length>0){
                self.value = parseFloat(self.jsonData[0][0]['value'])
                self.d=new Date(Math.floor(self.value))
                self.dt = getDateFormatted(self.d)
            }
        }
    </script>
    <style>
        .topspacing{ margin-top: 10px; }
    </style>
</widget_date>