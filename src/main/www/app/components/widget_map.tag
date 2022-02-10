<widget_map>
    <div id={ref} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center">{title}</div>
        </div>    
        <div class="row px-3 py-1">
                <div class="col-12"><div style={ heightStr } id={ ref+'_m' }>
{ app.texts.widget_map.nodata[app.language] }
        </div></div>
        </div>
    </div> 
    <script>
    var self = this
    //self.refs = this.refs
    // opts: poniższe przypisanie nie jest używane
    //       wywołujemy update() tego taga żeby zminieć parametry
    self.title = opts.title
    self.ref = opts.ref
    // opts
    
    self.value = '-'
    self.measureDate = '-'
    self.tstamp = 0;
    self.front = true

    self.rawdata = "[]"
    self.jsonData = {}

    // map
    self.prevLat = 0.0
    self.prevLon = 0.0
    self.lat = 0.0
    self.lon = 0.0
    self.mapUrl = ''
    self.mapExternalUrl = ''
    self.noData = false
    
    self.heightStr='width:100%;height:100px;'
    var map;
    var marker;
    
    
    this.on('mount',function(){
        app.log('MOUNTING MAP WIDGET')
        getWidth()
    })
    
    self.show2 = function(){
        app.log('SHOW2: widget_map')
        self.jsonData = JSON.parse(self.rawdata)
        app.log(self.jsonData)
        //getWidth()
        self.verify()
        self.showMap()
    }
    
    self.verify=function(){
        try{
            if(self.jsonData==null || jsonData.length==0){
                self.noData=true
            }
        }catch(err){
            self.nodaData=true
        }
        let i=0
        while(i<self.jsonData.length){
            if(self.jsonData[i]==null || self.jsonData[i].length<2 || self.jsonData[i][0]==null || self.jsonData[i][1]==null 
                    || (self.jsonData[i][0]['value']==0.0 && self.jsonData[i][1]['value']==0.0)){
                self.jsonData.splice(i,1)
            }else{
                i=i+1
            }
        }
        self.noData=false
    }
    
    self.showMap = function(){
        if(self.jsonData.length==0 || self.jsonData[0].length<2){
            self.noData = true
            return
        }
        riot.update()
        self.noData=false
        var p1=self.jsonData[0][0]['name'].toLowerCase()
        var p2=self.jsonData[0][1]['name'].toLowerCase()
        var lonFirst=false
        
        if(p2=='latitude'&&p1=='longitude' || p2=='lat'&&p1=='lon'){
            lonFirst=true
        }
        if(lonFirst){
            self.lat=parseFloat(self.jsonData[self.jsonData.length-1][1]['value'])
            self.lon=parseFloat(self.jsonData[self.jsonData.length-1][0]['value'])
        }else{
            self.lat=parseFloat(self.jsonData[self.jsonData.length-1][0]['value'])
            self.lon=parseFloat(self.jsonData[self.jsonData.length-1][1]['value'])
        }
        self.measureDate = new Date(self.jsonData[self.jsonData.length-1][0]['timestamp']).toLocaleString(getSelectedLocale())
        if(self.lat==self.prevLat && self.lon==self.prevLon){
            return
        }
        self.prevLat=self.lat
        self.prevLon=self.lon
        
        // Leaflet
        
        var zoom = 15;
        try{
            map = L.map(self.ref+'_m')
        }catch(err){
            app.log(err)
        }
        map.setView([self.lat, self.lon], zoom)
        
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        }).addTo(map);
        
        try{
            marker.setLatLng([self.lat, self.lon])
            marker.setPopupContent(self.lat+','+self.lon)
        }catch(err){
            marker=L.marker([self.lat, self.lon])
            marker.addTo(map).bindPopup(self.lat+','+self.lon)
        }
        
        if(self.jsonData.length>1){
            var latlngs =[]
            var polyline
            for(i=0; i<self.jsonData.length; i++){
                if(lonFirst){
                    latlngs.push(
                        [
                        parseFloat(self.jsonData[i][1]['value']),
                        parseFloat(self.jsonData[i][0]['value'])
                        ]
                    )
                }else{
                    latlngs.push(
                        [
                        parseFloat(self.jsonData[i][0]['value']),
                        parseFloat(self.jsonData[i][1]['value'])
                        ]
                    )
                }
            }
            app.log(latlngs)
            polyline = L.polyline(latlngs, {
                color: 'red'
            }).addTo(map);
            // zoom the map to the polyline
            map.fitBounds(polyline.getBounds());
        }
        //
        riot.update()
    }
    
    this.on('update', function(e){
    })
    
    self.listener = riot.observable()
    self.listener.on('*',function(eventName){
        app.log("widget_a1 listener on event: "+eventName)
    })
    
    
    //$(window).on('resize', resize)
    
    function getWidth(){
        self.width=$('#'+self.ref).width()
        self.heightStr='width:100%;height:'+self.width+'px;'
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
</widget_map>