<app_account>
    <div class="container">
        <!-- Modal -->
        <div class="modal fade" id="unregisterDialog" tabindex="-1" role="dialog" aria-labelledby="unregisterDialogLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="unregisterModalLabel">{app.texts.account.unregister_title[app.language]}</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label={app.texts.account.cancel[app.language]}>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p>{app.texts.account.unregister_question[app.language]}</p>
                        <p class="text-danger">{app.texts.account.unregister_info[app.language]}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary" onclick={ unregister() } data-dismiss="modal">{app.texts.account.unregister[app.language]}</button>
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">{app.texts.account.cancel[app.language]}</button>
                    </div>
                </div>
            </div>
        </div>
        <!-- Modal -->
        <div class="modal fade" id="upgradeDialog" tabindex="-1" role="dialog" aria-labelledby="upgradeDialogLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="upgradeModalLabel">{app.texts.account.upgrade_title[app.language]}</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label={app.texts.account.ok[app.language]}>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p>{app.texts.account.upgrade_info[app.language]}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">{app.texts.account.ok[app.language]}</button>
                    </div>
                </div>
            </div>
        </div>
        <div class="row" if={ app.user.status=='logged-in' && edited }>
             <div class="col-md-12">
                <user_form ref="user_edit"></user_form>
            </div>
        </div>
        <div class="row" if={ app.user.status=='logged-in' && passwordChange }>
             <div class="col-md-12">
                <app_password_form ref="password_change"></app_password_form>
            </div>
        </div>
        <div class="row" if={ !passwordChange && !edited }>
            <div class="col-md-12">
                <h2 class="module-title">{ app.texts.account.title[app.language] }</h2>
            </div>
        </div>
        <div class="row" if={ !passwordChange && !edited }>
            <div class="col-md-6">
                <div class="alert alert-light border-dark">
                    <button type="button" class="btn btn btn-outline-primary" onclick={ editAccount() } style="margin:2px;">{ app.texts.account.modify[app.language] }</button>
                    <button type="button" class="btn btn-outline-primary" onclick={ changePassword() } style="margin:2px;">{ app.texts.account.changepass[app.language] }</button>
                </div>
            </div>
            <div class="col-md-6">
                <div class="alert alert-light border-dark h3 text-justify">
                    <div class="d-flex">
                        <div>{getTariffName()}</div>
                        <div class="ml-auto" if={app.paid}>
                            <button type="button" class="btn btn-secondary ml-auto" 
                                    data-toggle="modal" data-target="#upgradeDialog">{ app.texts.account.changetariff[app.language] }</button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="row" if={ passwordChangeSuccess }>
             <div class="col-md-12">
                <div class="alert alert-success module-title" role="alert">
                    { app.texts.account.passChangeSuccess[app.language] }<a href="/app/#!logout">{ app.texts.account.signout[app.language] }</a>
                </div>
            </div>
        </div>
        <div class="row" if={ !passwordChange && !edited }>
            <div class="col-md-12 pb-2">
            <div class="card table-responsive border-dark">
                <table class="table">
                    <tr><th>{ app.texts.account.uid[app.language] }</th><td>{userProfile.uid}</td></tr>
                    <tr><th>{ app.texts.account.email[app.language] }</th><td>{userProfile.email}</td></tr>
                    <tr><th>{ app.texts.account.name[app.language] }</th><td>{userProfile.name}</td></tr>
                    <tr><th>{ app.texts.account.surname[app.language] }</th><td>{userProfile.surname}</td></tr>
                    <tr><th>{ app.texts.account.generalNotifications[app.language] }</th><td>{userProfile.generalNotificationChannel}</td></tr>
                    <tr><th>{ app.texts.account.infoNotifications[app.language] }</th><td>{userProfile.infoNotificationChannel}</td></tr>
                    <tr><th>{ app.texts.account.warningNotifications[app.language] }</th><td>{userProfile.warningNotificationChannel}</td></tr>
                    <tr><th>{ app.texts.account.alertNotifications[app.language] }</th><td>{userProfile.alertNotificationChannel}</td></tr>
                    <tr><th>{ app.texts.account.prefix[app.language] }</th><td>{userProfile.phonePrefix}</td></tr>
                    <tr><th>{ app.texts.account.preferredLanguage[app.language] }</th><td>{userProfile.preferredLanguage}</td></tr>
                    <tr><th>{ app.texts.account.autologin[app.language] }</th><td>{userProfile.autologin==true?'true':'false'}</td></tr>
                    <tr if={ app.user.roles.indexOf("admin")>-1 }><th>{ app.texts.account.role[app.language] }</th><td>{userProfile.role}</td></tr>
                    <tr><th>{ app.texts.account.confirmString[app.language] }</th><td>{userProfile.confirmString}</td></tr>
                    <tr if={ app.user.roles.indexOf("admin")>-1 }><th>{ app.texts.account.no[app.language] }</th><td>{userProfile.number}</td></tr>
                </table>
            </div>
            </div>
        </div>
        <div class="row" if={ !passwordChange && !edited }>
            <div class="col-md-12">
                <div class="alert alert-danger border-dark text-right">
                    <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#unregisterDialog">{ app.texts.account.wantremove[app.language] }</button>
                </div>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this
        self.userListener = riot.observable()
        self.userProfile = {}
        self.passwordChange = false
        self.edited = false
        self.paswordChangeSuccess = false

        this.on('mount',function(){
            app.log('LOCATION: '+document.location)
            self.passwordChange = false
            self.paswordChangeSuccess = false
            self.edited = false
            readMyAccountData()
        })

        self.userListener.on('*', function (eventName) {
            self.edited = false
            app.log('ACCOUNT LISTENER:'+eventName)
            switch (eventName){
                case 'submitted':
                    app.log('submitted')
                    readMyAccountData()
                    break
                case 'cancelled':
                    break
                case 'pSubmitted':
                    self.passwordChange = false
                    self.passwordChangeSuccess = true
                    break
                case 'passCancelled':
                    self.passwordChange = false
                    self.passwordChangeSuccess = false
                    break
                default:
                    app.log('ACCOUNT: ' + eventName)
            }
            riot.update()
        });

        changePassword(){
            return function(e){
                e.preventDefault()
                self.passwordChange = true
                self.passwordChangeSuccess = false
                riot.update()
                app.log('EDITING ACCOUNT')
                self.refs.password_change.init(self.userListener, app.user.name)
            }
        }

        self.forceChangePassword = function(){
        self.passwordChange = true
        self.passwordChangeSuccess = false
        riot.update()
        self.refs.password_change.init(self.userListener, app.user.name)
        }

        editAccount(){
        return function(e){
        e.preventDefault()
        self.edited = true
        riot.update()
        app.log('EDITING ACCOUNT')
        self.refs.user_edit.init(self.userListener, app.user.name, true, false)
        }
        }

        unregister(){
            return function(e){
                e.preventDefault()
                app.log('UNREGISTERING '+app.user.name+' ...')
                sendData(
                    {unregisterRequested: true}, 
                    'PUT', 
                    app.userAPI+'/'+app.user.name, 
                    app.user.token, 
                    self.closeUnregister, 
                    null, //self.listener, 
                    'submit:OK', 
                    'submit:ERROR', 
                    app.debug, 
                    null //globalEvents
                )
            }
        }

        self.closeUnregister = function(object){
        var text = ''+object
        app.log('CALBACK: '+object)
        if(text.startsWith('{')){
        var newUser = JSON.parse(object)
        if(newUser.unregisterRequested){
            document.location = '#!unregister'
        }
        }else if(text.startsWith('error:')){
        //it should'n happen
        alert(text)
        }
        }

        var readMyAccountData = function () {
            getData(app.userAPI + "/" + app.user.name,
                null,
                app.user.token,
                updateMyData,
                self.listener, //globalEvents
                'OK',
                null, // in case of error send response code
                app.debug,
                globalEvents
            );
        }

        var updateMyData = function (text) {
        app.log("ACCOUNT: " + text)
        self.userProfile = JSON.parse(text);
        riot.update()
        if(app.user.guest){
        self.forceChangePassword()
        }
        }

        self.getTariffName = function(){
            var resp="Free"
            switch(self.userProfile.type){
                case 3:
                case 4:
                case 6:
                    resp="Free"
                    break
                case 0:
                    resp="Standard"
                    break
                case 1:
                    resp="Admin"
                    break;
                case 2:
                    resp="" //application
                    break;
                case 5:
                    resp="Pro"
                    break
                case 7:
                    resp="Free Ext."
                    break
                case 8:
                    resp="Special"
                    break
            }
            return resp
        }

    </script>
</app_account>
