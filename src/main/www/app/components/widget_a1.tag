<widget_a1>
    <div id={opts.ref} if={type == 'symbol'} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center" onclick={ switchCard()}>{title}</div>
        </div>    
        <div class="row px-3 py-1">
                <div class="col-4">
                    <h1>
                        <i class="las {icon} text-primary" if={alertLevel<1}></i>
                        <i class="las {icon} text-warning" if={alertLevel==1}></i>
                        <i class="las {icon} text-danger" if={alertLevel==2}></i>
                    </h1>
                </div>
                <div class="col-8 text-right">
                    <h1 if={front}>{value} <raw content={ unitName }></raw></h1>
                    <h5 if={ !front }>{measureDate}</h5>
                </div>
        </div>
    </div> 
    <script>
        var self = this;
        self.iconName = opts.icon;
        self.value = '-';
        self.measureDate = '-';
        self.alertLevel = -1;
        self.front = true;
        self.rawdata = "[]";
        self.jsonData = {};
        //

        self.setIconName = function(){
            if(self.iconName==''||self.iconName==undefined){
                if (self.jsonData == null || self.jsonData.length == 0 || self.jsonData[0].length == 0){;
                    self.icon = 'la-tachometer-alt';
                }else{
                    self.measureType = getMeasureType(self.jsonData[0][0]['name'].toLowerCase());
                    if (self.measureType == 0) {
                        self.measureType = getMeasureType(self.title.toLowerCase());
                    };
                    switch (self.measureType){
                        case 1:
                            self.icon = 'la-thermometer-three-quarters'; break;
                        case 2:
                            self.icon = 'la-tint'; break;
                        case 3:
                            self.icon = 'la-tachometer-alt'; break;
                        case 4:
                            self.icon = 'la-calendar'; break;
                        case 5:
                            self.icon = 'la-tachometer-alt'; break;
                        case 6:
                            self.icon = 'la-ruler-horizontal'; break;
                        case 7:
                            self.icon = 'la-lightbulb'; break;
                        case 8:
                            self.icon = 'la-battery-empty,la-battery-full'; break;
                        case 9:
                        case 10:
                            self.icon = 'la-map-marker-alt'; break;
                        case 11:
                            self.icon = 'la-mountain'; break;
                        case 12:
                            self.icon = 'la-calculator'; break;
                        case 13:
                            self.icon = 'la-clock'; break;
                        case 14:
                            self.icon = 'la-ruler-vertical'; break;
                        case 15:
                            self.icon = 'la-smog'; break;
                        case 16:
                            self.icon = 'la-wind'; break;
                        case 17:
                            self.icon = 'la-cloud-showers-heavy'; break;
                        case 18:
                            self.icon = 'la-tint'; break;
                        case 19:
                            self.icon = 'la-door-closed,la-door-open'; break;
                        default:
                            self.icon = 'la-tachometer-alt';
                    };
                }            
            }else{
                self.icon=self.iconName;
            }
            var iconIdx = 0;
            if(self.alertLevel>-1){
                iconIdx=self.alertLevel;
            }else{
                if (!(self.jsonData == null || self.jsonData.length == 0 || self.jsonData[0].length == 0)){;
                    iconIdx = Math.round(parseFloat(self.jsonData[0][0]['value']));
                }
            }
            var iconArr = self.icon.split(',');
            if (iconIdx > iconArr.length - 1){
                iconIdx = iconArr.length - 1;
            }
            self.icon = iconArr[iconIdx];
        }


        self.show2 = function(){
            self.icon == self.iconName
            self.jsonData = JSON.parse(this.rawdata);
            self.getValue();
            self.setIconName();
        };
        
        self.getValue = function(){
            self.alertLevel = -1;
            if (self.jsonData == null || self.jsonData.length == 0 || self.jsonData[0].length == 0){    
                return;
            };
            self.value = parseFloat(self.jsonData[0][0]['value']);
            self.measureDate = getDateFormatted(new Date(self.jsonData[0][0]['timestamp']));
            self.alertLevel = getAlertLevel(self.range, self.value);
        };
        //
        this.on('update', function(e){
        });
        //
        self.listener = riot.observable();
        self.listener.on('*', function(eventName){
            app.log("widget_a1 listener on event: " + eventName);
        });

        switchCard(){
            return function(e){
                self.front = !self.front;
                riot.update();
            };
        };

        self.formatDate = function(myDate, full){
            if (full){
                return myDate.toLocaleString(getSelectedLocale());
            } else{
                return myDate.getHours() + ':' + myDate.getMinutes() + ':' + myDate.getSeconds();
            };
        };
        
        self.toLocaleTimeStringSupportsLocales = function() {
            try {
                new Date().toLocaleTimeString('i');
            } catch (e) {
                return e.name === 'RangeError';
            };
            return false;
        }
        //
        $(window).on('resize', resize);
        function resize(){
            self.show2();
        };
    </script>
    <style>
        .topspacing{
            margin-top: 10px;
        }
    </style>
</widget_a1>