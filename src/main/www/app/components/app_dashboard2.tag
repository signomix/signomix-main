<app_dashboard2>
<!-- Modal -->
    <div class="modal fade" id="linkView" tabindex="-1" role="dialog" aria-labelledby="shared link view" aria-hidden="true" if={ !app.embeded}>
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h4 class="modal-title w-100" id="myModalLabel">{ app.texts.dashboard2.dialogTitle[app.language] }</h4>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
            <div class="modal-body">
                <form>
                    <div class="form-group">
                        <p>{ app.texts.dashboard2.line1[app.language] }</p>
                        <input type="text" class="form-control" id="link" onclick="this.select();" value={ sharedLink }/>
                    </div>
                    <div class="form-group">
                        <p>{ app.texts.dashboard2.line2[app.language] }</p>
                        <textarea class="form-control" rows="5" onclick='this.select();'>{ sharedEmbeded }</textarea>
                        <p>{ app.texts.dashboard2.line3[app.language] }</p>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" data-dismiss="modal">{ app.texts.dashboard2.close[app.language] }</button>
            </div>
        </div>
    </div>
    </div>
<!-- Modal -->
    <div class="row" if={ !accessOK }>
        <div class="col-md-12">
            { app.texts.dashboard2.notfound[app.language] }
        </div>
    </div>
    <div class="row" if={ !app.embeded }>
        <div class="col-md-12">
            <h2 class="module-title text-center">{ dashboardConfig.title }
                <i class="material-icons clickable" onclick={ refresh } if={app.user.status == 'logged-in' && !app.user.guest}>refresh</i>
                <i class="material-icons clickable" data-toggle="modal" data-target="#linkView"
                   if={ dashboardConfig.shared && dashboardConfig.sharedToken && !app.embeded && app.shared==''}>link</i>
            </h2>
        </div>
    </div>

    <virtual each={name, i in rowArr }>
        <div class="row my-n1" if={w_line[i] && w_line[i].length>0}>
            <virtual each={colname, j in colArr }>
                <div class={ getColumnClass(w_line[i][j]) } if={w_line[i].length>j}>
                    <widget_a1 ref={ getRefName(i,j) } icon={w_line[i][j]['icon']} if={w_line[i][j]['type']=='symbol'}></widget_a1>
                    <widget_button ref={ getRefName(i,j) } if={w_line[i][j]['type']=='button'}></widget_button>
                    <widget_chart ref={ getRefName(i,j) } if={w_line[i][j]['type']=='line' || w_line[i][j]['type']=='stepped'}></widget_chart>
                    <widget_date ref={ getRefName(i,j) } if={w_line[i][j]['type']=='date'}></widget_date>
                    <widget_map ref={ getRefName(i,j) } if={w_line[i][j]['type']=='map'}></widget_map>
                    <widget_raw ref={ getRefName(i,j) } if={w_line[i][j]['type']=='raw' || w_line[i][j]['type']=='text'}></widget_raw>
                    <widget_led ref={ getRefName(i,j) } if={w_line[i][j]['type']=='led'}></widget_led>
                    <widget_plan ref={ getRefName(i,j) } if={w_line[i][j]['type']=='plan'}></widget_plan>
                    <widget_report ref={ getRefName(i,j) } if={w_line[i][j]['type']=='report'}></widget_report>
                    <widget_multimap ref={ getRefName(i,j) } if={w_line[i][j]['type']=='multimap'}></widget_multimap>
                    <widget_stopwatch ref={ getRefName(i,j) } if={w_line[i][j]['type']=='stopwatch'}></widget_stopwatch>
                    <widget_time ref={ getRefName(i,j) } if={w_line[i][j]['type']=='time'}></widget_time>
                    <widget_devinfo ref={ getRefName(i,j) } if={w_line[i][j]['type']=='devinfo'}></widget_devinfo>
                </div>
            </virtual>
        </div>
    </virtual>

    <script charset="UTF-8">
    var self = this
    self.rowArr = [0,1,2,3,4,5,6]
    self.colArr = [0,1,2,3]
    self.dashboardConfig = {
        title:'reading dashboard ...',
        widgets:[]
    }
    self.sharedLink = ''
    self.sharedEmbeded
    self.mounted = false
    self.w_line=[]
    self.consolidatedData = '['
    self.consolidatedDataCounter=0
    self.consolidationRequired=false
    self.accessOK = true
    self.refreshInterval = app.dashboardRefreshInterval
    
    globalEvents.on('pageselected:dashboard',function(event){
        if(self.mounted){
            readDashboardConfig(app.user.dashboardID, updateDashboard)
        }
    })
    
    globalEvents.on('err:401',function(event){
        self.accessOK=false
        myStopRefresh()
    })
    globalEvents.on('err:403',function(event){
        self.accessOK=false
        riot.update()
    })

    this.on('mount',function(){
        self.dashboardConfig = {
            title:'reading dashboard ...',
            widgets:[]
        }
        self.sharedLink = ''
        self.sharedEmbeded
        self.accessOK=true
        if(app.embeded){
            self.refreshInterval=app.publicDashboardRefreshInterval;
        }
        if(app.user.status != 'logged-in' || app.user.guest){
            self.refreshInterval=app.publicDashboardRefreshInterval;
        }
        readDashboardConfig(app.user.dashboardID, updateDashboard)
        self.mounted=true
    })
    
    this.on('unmount',function(){
        myStopRefresh()
        //self.mounted=true
        self.mounted=false
    })

    refresh(e){
        app.log('REFRESHING DATA')
        Object.keys(self.refs).forEach(function(key,index) {
            app.log(key)
            if(self.dashboardConfig.widgets.length>index && (self.dashboardConfig.widgets[index]['dev_id']||self.dashboardConfig.widgets[index]['type']=='report'||self.dashboardConfig.widgets[index]['type']=='multimap'||self.dashboardConfig.widgets[index]['type']=='plan')){
                readDashboardData(self.dashboardConfig.widgets[index], updateWidget, 0, index);
            }
        })
        riot.update()
    }

    var readDashboardConfig = function(dashboardID, callback){
        app.log('READING DASHBOARD CONFIG for: '+dashboardID)
        if(!dashboardID){return}
        self.dashboardConfig = {}
        self.w_line=[]
        localParams=app.shared==''?'':'?tid='+app.shared
        var dboard
        var byName=''
        if(dashboardID.endsWith('@')){
            dboard=dashboardID.substring(0, dashboardID.length - 1);
            byName=localParams!=''?'&name=true':'?name=true'
        }else{
            dboard=dashboardID
        }
        getData(
            app.dashboardAPI + "/" + dashboardID + localParams + byName, //url
            null,                                      //query
            (localParams!=''?null:app.user.token),                            //session token
            callback,                    //callback
            null,                           //event listener
            'OK',                                      //success event name
            null,                                      //error event name
            app.debug,                                 //debug switch
            globalEvents                               //application's event listener
        )
    }

    //callback function
    var updateDashboard = function(data){
        app.log('UPDATING DASHBOARD')
        self.dashboardConfig = {
            title:'reading dashboard ...',
            widgets:[]
        }
        rebuild()
        self.dashboardConfig = JSON.parse(data)
        app.log('NEW config IS: '+self.dashboardConfig)
        if(self.dashboardConfig == null){
            self.dashboardConfig = {
            title:'the selected dashboard is unavailable',
            shared:false,
            sharedToken:'',
            widgets:[]
        }
        }
        app.log(self.dashboardConfig)
        rebuild()
        riot.update()
        app.log(self.refs)
        Object.keys(self.refs).forEach(function(key,index) {
            app.log(key)
            self.refs[key].update(self.dashboardConfig.widgets[index])
        });
        //app.log(self.refs.a1)
        app.log('SHARED TOKEN='+self.dashboardConfig.sharedToken)
        if(self.dashboardConfig.sharedToken){
            self.sharedLink = location.origin+'/app/?tid='+self.dashboardConfig.sharedToken+location.hash
            self.sharedEmbeded = 
                    '<IFRAME \nsrc="'
                    +location.origin+'/app/embed.html?tid='+self.dashboardConfig.sharedToken+'/'+location.hash
                    +'" \nwidth="300" \nheight="300">\n</IFRAME>'
        }else{
            self.sharedLink = ''
            self.sharedEmbeded = ''
        }
        self.refresh(null)
    }

    //callback function
    var updateWidget = function(d, tPos){
        var row=parseInt(tPos.substring(0,1))
        var col=parseInt(tPos.substring(2))
        app.log('UPDATING '+tPos+' '+row+' '+col)
        Object.keys(self.refs).forEach(function(key,index) {
            if(index==col){
                self.refs[key].rawdata = d
                self.refs[key].show2()
            }
        })
        riot.update()
    }

    // get data from IoT devices
    var readDashboardData = function (config, callback, row, column) {
        if(config['type']=='button'){
            return
        }
        var query
        var channelName = config.channel
        if(config.query){
            query = config.query
        }else{
            query = 'last 1'
        }
        if(config.format){
            if(config.format == 'timeseries'){
                query = query + ' timeseries'
            }
        }

        var url=''
        if(config.type=='devinfo'||config.type=='devmap'){
            url=app.iotAPI + "/" + config.dev_id
        }else if(config.type=='report'||config.type=='multimap'||config.type=='plan'){
            url=app.groupAPI + "/" + config.group + "/"+channelName+(app.shared!=''?'?tid='+app.shared:'')
        }else if(config.dev_id){
            url=app.iotAPI + "/" + config.dev_id + "/"+channelName+"?"+(app.shared!=''?'tid='+app.shared+'&':'')+"query=" + query
        }
        if(url.length>0) {
            getData(
                url, 
                null,  
                (app.shared==''?app.user.token:null),                            //session token
                callback,
                null, 
                row+':'+column,  //success event name
                null,            //error event name
                app.debug,       //debug switch
                globalEvents    //application's event listener
            )
        }
    }
        
    var rebuild = function(){
        //riot.mount('widget_a1')
        self.w_line = []
        var lineNo = 0
        var colNo = 0
        app.log('dashboardConfig.widgets.length: '+self.dashboardConfig.widgets.length)
        for(i=0; i<self.dashboardConfig.widgets.length; i++){
            if(colNo+self.dashboardConfig.widgets[i].width>4){
                lineNo ++
                colNo=0
            }
            if(colNo==0){
                self.w_line.push(Array(0))
            }
            self.w_line[lineNo].push(self.dashboardConfig.widgets[i])
            colNo=colNo+self.dashboardConfig.widgets[i].width
        }
        app.log('w_line')
        app.log(self.w_line)
    }
    
    // thread for refreshing dashboard data
    var myRefreshThread = setInterval(function(){ self.refresh(null) }, self.refreshInterval);
    
    function myStopRefresh() {
        app.log('STOPPING THREAD')
        clearInterval(myRefreshThread);
    }
    
    getColumnClass(widget){
        switch(widget.width){
            case 1:
                return 'col-md-3'
            case 2:
                return 'col-md-6'
            case 3:
                return 'col-md-9'
            case 4: 
                return 'col-md-12'
            default:
                return 'col-md-3'
        }
    }
    
    getRefName(i,j){
       res=(i*4 +j+1)
       return 'a'+res
    }
    
    </script>
</app_dashboard2>
