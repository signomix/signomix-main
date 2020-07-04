<widget_date>
    <div id={ opts.ref } class="card topspacing p-0">
        <div class="card-header h6 text-left p-1">
            {title}<span class="float-right">&#x2699;</span>
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-4 text-left">
                    <h3><i class="las {icon} text-primary" style="height: 1em;"></i></h3>
                </div>
                <div class="col-8 text-right">
                    <h5>{dt}</h5>
                </div>
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