<app_alert_client>
    // thread for refreshing user alerts
    var self = this
    self.myRefreshThread = null
    self.listener = riot.observable()
    
    function startRefresh(){
        if (app.alertRefreshInterval > 0){
            setInterval(function(){ self.refresh() }, app.alertRefreshInterval)
        }
    }
    
    function stopRefresh() {
        if (app.alertRefreshInterval > 0){
            app.log('STOPPING REFRESH THREAD')
            clearInterval(self.myRefreshThread);
        }
    }
    
    self.refresh = function(){
        app.log('REFRESHING ALERTS')
            readAlerts()
    }
    
    var readAlerts = function () {
        getData(app.alertAPI + "/",  // url
                null,                // query
                app.user.token,      // token
                updateMyAlerts,        // callback
                self.listener,       // event listener
                'OK',                // success event name
                null,                // error event name
                app.debug,           // debug switch
                globalEvents         // application event listener
        );
    }

    var updateMyAlerts = function (text) {
        app.user.alerts = JSON.parse(text)
        globalEvents.trigger('alerts:updated')
        riot.update();
    }

    globalEvents.on('auth:loggedin', function (event) {
        startRefresh()
    })

    globalEvents.on('logout:OK', function (event) {
        stopRefresh()
    })

    globalEvents.on('err:401', function (eventName) {
        stopRefresh()
    })
    
</app_alert_client>