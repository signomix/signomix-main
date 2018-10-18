<app_session_manager>
    // thread for refreshing session token
    var self = this
    self.myRefreshThread = null
    self.listener = riot.observable()
    
    function startSessionRefresh(){
        if (app.sessionRefreshInterval > 0){
            setInterval(function(){ self.refresh() }, app.sessionRefreshInterval)
        }
    }
    
    function stopRefresh() {
        if (app.sessionRefreshInterval > 0){
            app.log('STOPPING REFRESH THREAD')
            clearInterval(self.myRefreshThread);
        }
    }
    
    self.refresh = function(){
        app.log('REFRESHING SESSION TOKEN')
        var formData = {enything:''}
        sendData(
                formData,
                'PUT',
                app.authAPI,
                app.user.token,
                self.submitted,
                self.listener,
                'submit:OK',
                'submit:ERROR',
                app.debug,
                globalEvents
                )
    }
    
    self.submitted = function(){
    }

    globalEvents.on('auth:loggedin', function (event) {
        startSessionRefresh()
    })

    globalEvents.on('logout:OK', function (event) {
        stopRefresh()
    })

    globalEvents.on('err:401', function (eventName) {
        app.user.name = '';
        app.user.token = '';
        app.user.status = 'logged-out';
        stopRefresh()
        riot.update();
    })
    
    globalEvents.on('err:403', function (eventName) {
        //komunikat o zmianie uprawnień
        riot.update();
    })
</app_session_manager>
