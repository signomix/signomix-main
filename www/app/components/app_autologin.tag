<app_autologin>
    <script>
        var self = this
        self.token = ''
        self.login = ''

        self.on('mount', function () {
            self.token = getCookie('signomixToken')
            self.login = getCookie('signomixUser')
            app.log('cooke automatic login as user '+self.login)
            app.log('using token '+self.token)
            app.log('guest=='+app.user.guest)
            if (!app.user.guest && self.token && self.login) {
                getData(app.authAPI+'/'+self.token, null, null, getUserData, globalEvents, 'data:ok', 'data.error', app.debug)
                app.requests=0
            }
        })

        getUserData = function(data){
            riot.update()
            getData(app.userAPI + '/' + self.login, null, self.token, fillUserData, globalEvents, 'user:ok', 'user.error', app.debug)
            getData(app.alertAPI, null, self.token, saveResponse, globalEvents, 'data:ok', 'data.error', app.debug)
            readDashboardList(self.token, saveDashboards)            
        }
        fillUserData = function (text) {
            tmpUser = JSON.parse(text);
            app.offline = false;
            app.user.token = self.token;
            app.user.name = self.login;
            app.user.status = "logged-in";
            app.user.role = tmpUser.role
            app.user.roles = tmpUser.role.split(",")
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
