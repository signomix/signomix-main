<widget_devinfo>
    <div id={opts.ref} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center">{title}</div>
        </div>    
        <div class="row px-3 py-1" if="{visualisation=='time'||visualisation=='number'}">
                <div class="col-4">
                    <h1>
                        <i class="las {icon} text-primary" style="height: 1em;"></i>
                    </h1>
                </div>
                <div class="col-8 text-right">
                    <h2>{dt}</h2>
                </div>
        </div>
        <div class="row px-3 py-1" if="{visualisation=='raw'}">
                <div class="col-12">
                    <pre>{rawdata}</pre>
                </div>
        </div>
    </div> 
    <script>
    var self = this
    // opts: poniższe przypisanie nie jest używane
    //       wywołujemy update() tego taga żeby zminieć parametry
    self.type = opts.type
    self.visible = opts.visible
    self.title = opts.title
    self.unitName = opts.unitName
    self.description = opts.description
    self.range = opts.range
    // opts
    
    self.color = 'bg-white'
    self.rawdata = "[]"

    self.noData = false
    self.width=100
    self.heightStr='height:100px;'
    self.visualisation='raw'
    
    self.show2 = function(){
        self.icon = 'la-tachometer-alt'
        app.log('SHOW2 '+self.type)
        self.jsonData = JSON.parse(self.rawdata)
        if(self.channel=='latitude'||self.channel=='longitude'||self.channel=='altitude'){
            self.visualisation='map'
        }else if(self.channel=='lastseen'){
            self.visualisation='date'
        }else if(self.channel.indexOf('@state')){
            let subt=self.channel.substring(0,self.channel.indexOf('@'))
            if(subt=='time'||subt=='date'||subt=='number'){
                self.visualisation=subt
            }
        }else if(self.channel=='state'){
            self.visualisation='number'
        }
        if(self.visualisation=='time'){
            self.iconName = 'hourglass_empty'
            if(parseFloat(self.jsonData['state'])<=0){
                self.dt='00:00:00'
            }else{
                self.value = Math.floor((parseFloat(self.jsonData['state']))/1000)
                self.h=Math.floor(self.value/3600)
                self.m=Math.floor((self.value%3600)/60)
                self.s=self.value-self.h*3600-(60*self.m)
                self.dt = getStopwatchFormatted(self.h, self.m, self.s)
            }
        }else if(self.visualisation=='number'){
            self.dt=Math.floor(parseFloat(self.jsonData['state']))
        }
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
</widget_devinfo>