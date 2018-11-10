<widget_date>
    <div id={ opts.ref } class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1">
            {title}<span class="float-right">&#x2699;</span>
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-2 text-center">
                    <i class="material-icons md-48 blue">{iconName}</i>
                </div>
                <div class="col-10 text-center h5">
                    {dt}
                </div>
            </div>
        </div>
    </div>
    <script>
        var self = this
        self.rawdata = "[]"
        self.jsonData = {}
        self.dt=""
        self.iconName = 'event'
        
        self.show2 = function(){
            self.jsonData = JSON.parse(this.rawdata)
            if(self.jsonData.length==0){
                return
            }
            self.value = parseFloat(self.jsonData[0]['value'])
            self.d=new Date(self.value)
            self.dt = getDateFormatted(self.d)
        }
    </script>
    <style>
        .topspacing{ margin-top: 10px; }
    </style>
</widget_date>