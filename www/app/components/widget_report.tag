<widget_report>
    <div id={opts.ref} if={type == 'report'} class="card card-block topspacing p-0">
        <div class="card-header h6 text-left p-1">{title}</div>
        <div class="card-body" if="{ !noData }">
            <div class="table-responsive">
            <table class="table table-stripped table-sm">
                <thead>
                    <tr>
                        <td class="text-uppercase text-left">{ app.texts.widget_report.EUI[app.language] }</td>
                        <td class="text-uppercase text-right" each={ measure in jsonData[0] }>{measure.name}</td>
                        <td class="text-uppercase text-right">{ app.texts.widget_report.DATE[app.language] }</td>
                    </tr>
                </thead>
                <tbody>
                    <tr each={device in jsonData}>
                        <td class="text-left">{device[0].deviceEUI}</td>
                        <td class="text-right" each={measure in device}>{measure.value}</td>
                        <td class="text-right">{getDateFormatted(new Date(device[0].timestamp))}</td>
                    </tr>
                </tbody>
            </table>
            </div>
        </div>
        <div class="card-body" if="{ noData }">
            { app.texts.widget_report.NO_DATA[app.language] }
        </div>
    </div>
    <script>
    var self = this
    // opts: poniższe przypisanie nie jest używane
    //       wywołujemy update() tego taga żeby zminieć parametry
    self.type = opts.type
    self.visible = opts.visible
    self.title = opts.title
    self.description = opts.description
    // opts
    
    self.color = 'bg-white'
    self.rawdata = "[]"
    self.jsonData = {}

    self.noData = true
    self.width=100
    self.heightStr='height:100px;'
    
    self.show2 = function(){
        app.log('SHOW2 '+self.type)
        self.jsonData = JSON.parse(this.rawdata)
        if(self.jsonData[0]) self.noData = false
        getWidth()
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
        .topspacing{
            margin-top: 10px;
        }
       
    </style>
</widget_report>