<widget_a1>
    <div id={opts.ref} if={type == 'symbol'} class="card widget topspacing p-0">
        <div class="card-header h6 text-left p-1"  onclick={ switchCard() }>
            <i class="material-icons yellow" style="margin-right: 10px; font-size: smaller" if={alertLevel==1}>notifications_active</i>
            <i class="material-icons red" style="margin-right: 10px; font-size: smaller" if={alertLevel==2}>error_outline</i>
            {title}<span class="float-right">&#x2699;</span>
        </div>
        <div class="card-body">
            <div class="row">
                <div class="col-3 text-center" style="padding-bak: 0.4rem;">
                    <img src="/app/resources/iconmonstr/{icon}.svg" class="h-75 inline-block filter-blue img-fluid" if={alertLevel<1}>
                    <img src="/app/resources/iconmonstr/{icon}.svg" class="filter-yellow img-fluid" if={alertLevel==1}>
                    <img src="/app/resources/iconmonstr/{icon}.svg" class="filter-red img-fluid" if={alertLevel==2}>
                </div>
                <div class="col-9 text-center h3" if={ front }>
                    <b>{value}</b> <raw content={ unitName }></raw>
                </div>
                <div class="col-9 text-center h5" if={ !front }>
                    {measureDate}
                </div>
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
                    self.icon = 'dashboard';
                }else{
                    self.measureType = getMeasureType(self.jsonData[0][0]['name'].toLowerCase());
                    if (self.measureType == 0) {
                        self.measureType = getMeasureType(self.title.toLowerCase());
                    };
                    switch (self.measureType){
                        case 1:
                            self.icon = 'temperature'; break;
                        case 2:
                            self.icon = 'humidity'; break;
                        case 3:
                            self.icon = 'pressure'; break;
                        case 4:
                            self.icon = 'calendar'; break;
                        case 5:
                            self.icon = 'speed'; break;
                        case 6:
                            self.icon = 'distance'; break;
                        case 7:
                            self.icon = 'light-off,light-on'; break;
                        case 8:
                            self.icon = 'battery-0,battery1'; break;
                        case 9:
                        case 10:
                            self.icon = 'location'; break;
                        case 11:
                            self.icon = 'altitude'; break;
                        case 12:
                            self.icon = 'calculator'; break;
                        case 13:
                            self.icon = 'timer'; break;
                        case 14:
                            self.icon = 'height'; break;
                        case 15:
                            self.icon = 'pollution'; break;
                        case 16:
                            self.icon = 'wind'; break;
                        case 17:
                            self.icon = 'rain'; break;
                        case 18:
                            self.icon = 'water'; break;
                        case 19:
                            self.icon = 'door-0,door-1'; break;
                        default:
                            self.icon = 'dashboard';
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