<app_login> 
    <div class="panel panel-default signomix-form">
        <div class="panel-body">
        <div class="row">
            <div class="col">
            <p class="module-title h3 text-center mb-4">{app.texts.login.l_title[app.language]}</p>
            </div>
        </div>
            <div class="row text-center" if={app.user.status=='logged-in'}>
            <div class="col">
                <div class="alert alert-warning" role="alert">
                    <p>{app.texts.login.l_warning[app.language]} {app.user.uid}<br>
                    {app.texts.login.l_warning2[app.language]}</p>
                    <button type="button" class="btn btn-light" onClick="document.location='/app'">{app.texts.login.l_keep_session[app.language]}</button>
                </div>
            </div>
        </div>
            <div class="row">
            <div class="col">
            <form onsubmit={ submitLoginForm }>
                <div class="form-group">
                    <form_input 
                        id="login"
                        name ="login"
                        label={ app.texts.login.l_name[app.language] }
                        type="text"
                        required="true"
                        />
                </div>
                <div class="form-group">
                    <form_input 
                        id="password"
                        name ="password"
                        label={ app.texts.login.l_password[app.language] }
                        type="password"
                        required="true"
                        />
                </div>
                <div class="form-group" if={ self.loginError }>
                    <div class="alert alert-danger" role="alert">{ app.texts.login.l_error[app.language] }</div>
                </div>
                <div class="form-group">
                    <p class="form-footer"><a href='#!resetpass'>{ app.texts.login.l_resetpass[app.language] }</a></p>
                    <p class="form-footer">{ app.texts.login.l_noaccountyet[app.language] } <a href='#!register'>{ app.texts.login.l_signup[app.language] }</a></p>
                </div>
                <button type="submit" class="btn btn-primary">{ app.texts.login.l_save[app.language] }</button>
                <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.login.l_cancel[app.language] }</button>
            </form>
            </div>
        </div>
    </div>
    </div>

    <script>
        self=this
        self.loginError = false
        
        globalEvents.on('auth.error', function (event) {
            self.loginError = true
            self.update()
        });
        
        globalEvents.on('auth:loggedin', function (event) {
            app.log("Login success!")
            setCookie('signomixToken',app.user.token, 365)
            //setCookie('signomixUser',app.user.name, 365)
            //setCookie('signomixUser',app.user.number, 365)
            app.currentPage = 'main'
            getData(app.userAPI+'/'+app.user.name, null, app.user.token, saveUserData, globalEvents, 'user:ok', 'user.error', app.debug)
            getData(app.alertAPI, null, app.user.token, saveResponse, globalEvents, 'data:ok', 'data.error', app.debug)
            readDashboardList(saveDashboards)
        });

        saveUserData = function(text){
            tmpUser = JSON.parse(text);
            app.user.role = tmpUser.role
            app.user.roles = tmpUser.role.split(",")
            app.user.number=tmpUser.number
            app.user.autologin = tmpUser.autologin
            if(app.user.autologin) {
                setCookie('signomixUser',app.user.number, 365)
            }else{
                deleteCookie('signomixUser')
            }
            riot.update()
        }
        saveResponse = function(text){
            app.user.alerts = JSON.parse(text);
            riot.update()
        }

        submitLoginForm = function(e){
            e.preventDefault()
            app.log("submitting ..."+e.target)
            loginSubmit(e.target, globalEvents, 'auth:loggedin', 'auth.error', app.debug);
            e.target.reset()
        }
        
            //callback function
        var saveDashboards = function(data){
            app.log('MY DASHBOARDS2: '+data)
            app.user.dashboards = JSON.parse(data)
            if(app.user.dashboards.length>0) {
                //app.user.dashboardID = app.user.dashboards[0].id
            }
            globalEvents.trigger('dashboards:ready')
            riot.update()
        }
        
        var readDashboardList = function(callback){
            getData(
                app.dashboardAPI,
                null,
                app.user.token,
                callback,
                null,
                'OK',
                null,
                app.debug,
                globalEvents
            )
        }

        self.resetStatus = function(e){
            self.loginError = false;
            self.update()
        }
        
        self.close = function(e){
            route('main')
            //riot.update()
        }
        
    </script>
    <style>
        .form-footer{
            margin-top: 20px;
            margin-bottom: 20px;
        }
    </style>
</app_login>
