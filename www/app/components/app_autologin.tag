<app_autologin>
    <script>
        var self = this
        self.token = ''
        self.login = ''
        self.userNumber = ''
        self.autologin = null

        self.on('mount', function () {
            self.token = getCookie('signomixToken')
            //self.login = getCookie('signomixUser')
            self.userNumber = getCookie('signomixUser')
            console.log('cooke automatic log-in as user '+self.userNumber)
            console.log('using token '+self.token)
            console.log('guest=='+app.user.guest)
            if (!app.user.guest && self.token && self.userNumber) {
                getData(app.authAPI+'/'+self.token, null, null, getUserData, globalEvents, 'data:ok', 'data.error', app.debug)
                app.requests=0
            }
        })

        getUserData = function(data){
            console.log('getUserData:'+data)
            riot.update()
            getData(app.userAPI + '?n=' + self.userNumber, null, self.token, fillUserData, globalEvents, 'user:ok', 'user.error', app.debug)
            getData(app.alertAPI, null, self.token, saveResponse, globalEvents, 'data:ok', 'data.error', app.debug)
            readDashboardList(self.token, saveDashboards)            
        }
        fillUserData = function (text) {
            tmpUser = JSON.parse(text);
            app.offline = false;
            app.user.token = self.token;
            app.user.uid=tmpUser.uid
            app.user.name = tmpUser.uid;
            app.user.status = "logged-in";
            app.user.role = tmpUser.role
            app.user.roles = tmpUser.role.split(",")
            app.user.autologin = tmpUser.autologin
            riot.update()
        }
        saveResponse = function (text) {
            app.user.alerts = JSON.parse(text);
            riot.update()
        }
        var saveDashboards = function (data) {
            app.log('MY DASHBOARDS: ' + data)
            app.user.dashboards = JSON.parse(data)
            if (app.user.dashboards.length > 0) {
                //app.user.dashboardID = app.user.dashboards[0].id
            }
            globalEvents.trigger('dashboards:ready')
        }
        var readDashboardList = function (token, callback) {
            getData(
                    app.dashboardAPI,
                    null,
                    token,
                    callback,
                    null,
                    'OK',
                    null,
                    app.debug,
                    globalEvents
                    )
        }
    </script>
</app_autologin>
