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
                <p>{ app.texts.dashboard2.line1[app.language] }</p>
                <input type='text' onclick='this.select();' value={ sharedLink }/>
                <p>&nbsp;</p><p>{ app.texts.dashboard2.line2[app.language] }</p>
                <textarea rows="5" onclick='this.select();'>{ sharedEmbeded }</textarea>
                <p>&nbsp;</p>
                <p>{ app.texts.dashboard2.line3[app.language] }</p>
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
            <h2 class="module-title">{ dashboardConfig.title }
                <i class="material-icons clickable" onclick={ refresh } if={app.user.status == 'logged-in' && !app.user.guest}>refresh</i>
                <i class="material-icons clickable" data-toggle="modal" data-target="#linkView"
                   if={ dashboardConfig.shared && dashboardConfig.sharedToken && !app.embeded && app.shared==''}>link</i>
            </h2>
        </div>
    </div>
    <div class="row" if={w_line[0] && w_line[0].length>0}>
        <div class={ getColumnClass(w_line[0][0]) } >
            <widget_a1 ref="a1"></widget_a1>
        </div>
        <div class={ getColumnClass(w_line[0][1])}  if={w_line[0].length>1}>
            <widget_a1 ref="a2"></widget_a1>                
        </div>
        <div class={ getColumnClass(w_line[0][2])}  if={w_line[0].length>2}>
            <widget_a1 ref="a3"></widget_a1>              
        </div>
        <div class={ getColumnClass(w_line[0][3])}  if={w_line[0].length>3}>
            <widget_a1 ref="a4"></widget_a1>             
        </div>
    </div>
    <div class="row" if={w_line[1] && w_line[1].length>0}>
        <div class={ getColumnClass(w_line[1][0])} >
            <widget_a1 ref="a5"></widget_a1>
        </div>
        <div class={ getColumnClass(w_line[1][1])}  if={w_line[1].length>1}>
            <widget_a1 ref="a6"></widget_a1>                
        </div>
        <div class={ getColumnClass(w_line[1][2])}  if={w_line[1].length>2}>
            <widget_a1 ref="a7"></widget_a1>              
        </div>
        <div class={ getColumnClass(w_line[1][3])}  if={w_line[1].length>3}>
            <widget_a1 ref="a8"></widget_a1>             
        </div>
    </div>
    <div class="row" if={w_line[2] && w_line[2].length>0}>
        <div class={ getColumnClass(w_line[2][0])} >
            <widget_a1 ref="a9"></widget_a1>
        </div>
        <div class={ getColumnClass(w_line[2][1])}  if={w_line[2].length>1}>
            <widget_a1 ref="a10"></widget_a1>                
        </div>
        <div class={ getColumnClass(w_line[2][2])}  if={w_line[2].length>2}>
            <widget_a1 ref="a11"></widget_a1>              
        </div>
        <div class={ getColumnClass(w_line[2][3])}  if={w_line[2].length>3}>
            <widget_a1 ref="a12"></widget_a1>             
        </div>
    </div>
    <div class="row" if={w_line[3] && w_line[3].length>0}>
        <div class={ getColumnClass(w_line[3][0])} >
            <widget_a1 ref="a13"></widget_a1>
        </div>
        <div class={ getColumnClass(w_line[3][1])}  if={w_line[3].length>1}>
            <widget_a1 ref="a14"></widget_a1>                
        </div>
        <div class={ getColumnClass(w_line[3][2])}  if={w_line[3].length>2}>
            <widget_a1 ref="a15"></widget_a1>              
        </div>
        <div class={ getColumnClass(w_line[3][3])}  if={w_line[3].length>3}>
            <widget_a1 ref="a16"></widget_a1>             
        </div>
    </div>
    <div class="row" if={w_line[4] && w_line[4].length>0}>
        <div class={ getColumnClass(w_line[4][0])} >
            <widget_a1 ref="a17"></widget_a1>
        </div>
        <div class={ getColumnClass(w_line[4][1])}  if={w_line[4].length>1}>
            <widget_a1 ref="a18"></widget_a1>                
        </div>
        <div class={ getColumnClass(w_line[4][2])}  if={w_line[4].length>2}>
            <widget_a1 ref="a19"></widget_a1>              
        </div>
        <div class={ getColumnClass(w_line[4][3])}  if={w_line[4].length>3}>
            <widget_a1 ref="a20"></widget_a1>             
        </div>
    </div>
    <div class="row" if={w_line[5] && w_line[5].length>0}>
        <div class={ getColumnClass(w_line[5][0])} >
            <widget_a1 ref="a21"></widget_a1>
        </div>
        <div class={ getColumnClass(w_line[5][1])}  if={w_line[5].length>1}>
            <widget_a1 ref="a22"></widget_a1>                
        </div>
        <div class={ getColumnClass(w_line[5][2])}  if={w_line[5].length>2}>
            <widget_a1 ref="a23"></widget_a1>              
        </div>
        <div class={ getColumnClass(w_line[5][3])}  if={w_line[5].length>3}>
            <widget_a1 ref="a24"></widget_a1>             
        </div>
    </div>
    <div class="row">
        <div class="col-md-12"><span>&nbsp;</span></div>
    </div>
    <script charset="UTF-8">
    var self = this
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
        readDashboardConfig(app.user.dashboardID, updateDashboard)
        self.mounted=true
    })
    
    this.on('unmount',function(){
        myStopRefresh()
        //self.mounted=true
        self.mounted=false
    })

    refresh(e){
        //if(e) {e.preventDefault()}
        app.log('REFRESHING DATA')
        Object.keys(self.refs).forEach(function(key,index) {
            app.log(key)
            if(self.dashboardConfig.widgets.length>index && self.dashboardConfig.widgets[index]['dev_id']){
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
        getData(
            app.dashboardAPI + "/" + dashboardID + localParams, //url
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
        app.log(self.dashboardConfig)
        rebuild()
        riot.update()
        console.log(self.refs)
        Object.keys(self.refs).forEach(function(key,index) {
            app.log(key)
            self.refs[key].update(self.dashboardConfig.widgets[index])
        });
        //console.log(self.refs.a1)
        app.log('SHARED TOKEN='+self.dashboardConfig.sharedToken)
        if(self.dashboardConfig.sharedToken){
            self.sharedLink = location.origin+'/app/?tid='+escape(self.dashboardConfig.sharedToken)+location.hash
            self.sharedEmbeded = 
                    '<IFRAME \nsrc="'
                    +location.origin+'/app/embed.html?tid='+escape(self.dashboardConfig.sharedToken)+'/'+location.hash
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
        var query
        var channelName = config.channel
        if(config.query){
            query = config.query
        }else{
            query = 'last'
        }
        if(config.dev_id){
                getData(
                app.iotAPI + "/" + config.dev_id + "/"+channelName+"?"+(app.shared!=''?'tid='+app.shared+'&':'')+"query=" + query, //url
                null,                                      //query
                (app.shared==''?app.user.token:null),                            //session token
                callback,                    //callback
                null,                           //event listener
                row+':'+column,                            //success event name
                null,                                      //error event name
                app.debug,                                 //debug switch
                globalEvents                               //application's event listener
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
    var myRefreshThread = setInterval(function(){ self.refresh(null) }, app.dashboardRefreshInterval);
    
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
    
    </script>
</app_dashboard2>
