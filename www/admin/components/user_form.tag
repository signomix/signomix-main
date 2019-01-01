<user_form>
    <div class="panel panel-primary signomix-form">
        <div class="panel-heading module-title" if={ self.mode == 'create' }><h2>{ app.texts.user_form.user_new[app.language] }</h2></div>
        <div class="panel-heading module-title" if={ self.mode == 'update' }><h2>{ app.texts.user_form.user_modify[app.language] }</h2></div>
        <div class="panel-heading module-title" if={ self.mode == 'view' }><h2>{ app.texts.user_form.user_view[app.language] }</h2></div>
        <div class="panel-body">
            <form onsubmit={ self.submitForm }>
                  <div class="form-group">
                    <label for="uid">{ app.texts.user_form.uid[app.language] }</label>
                    <input class="form-control" id="uid" name="uid" type="text" value={ user.uid } readonly={ self.mode != 'create' } required>
                </div>
                <div class="form-group">
                    <label for="email">{ app.texts.user_form.email[app.language] }</label>
                    <input class="form-control" id="email" name="email" type="text" value={ user.email } readonly={ !allowEdit } required>
                </div>
                <div class="form-group" if={ adminMode }>
                     <label for="type">{ app.texts.user_form.type[app.language] }</label>
                    <input class="form-control" id="type" name="type" type="text" value={ userTypeAsString(user.type) } readonly={ !allowEdit } required>
                </div>
                <div class="form-group" if={ adminMode }>
                     <label for="role">{ app.texts.user_form.role[app.language] }</label>
                    <input class="form-control" id="role" name="role" type="text"  value={ user.role } readonly={ !allowEdit }>
                </div>
                <div class="form-group border-dark border-bottom">
                    <label>{ app.texts.user_form.notificationsGroup[app.language] }</label><br>
                </div>
                <div class="form-row">
                    <div class="col-md-3 my-1">
                        <label class="mr-sm-2" for="genNotCh">GENERAL</label>
                        <select class="custom-select mr-sm-2" id="genNotCh" disabled={ !allowEdit }>
                            <option value='0' selected={''==user.generalNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
                            <option value="SMTP" selected={'SMTP'==user.generalNotificationChannel.substring(0,user.generalNotificationChannel.indexOf(':'))}>E-mail</option>
                            <option value="SLACK" selected={'SLACK'==user.generalNotificationChannel.substring(0,user.generalNotificationChannel.indexOf(':'))}>Slack</option>
                            <option value="PUSHOVER" selected={'Pushover'==user.generalNotificationChannel.substring(0,user.generalNotificationChannel.indexOf(':'))}>Pushover</option>
                        </select>
                    </div>
                    <div class="col-md-9 my-1">
                        <div class="form-group">
                            <label for="generalNotificationChannel">{ app.texts.user_form.config[app.language] }</label>
                            <input class="form-control" id="generalNotificationChannel" 
                                   name="generalNotificationChannel" type="text" readonly={ !allowEdit }
                                   value={ user.generalNotificationChannel.substring(user.generalNotificationChannel.indexOf(':')+1) }
                            >
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="col-md-3 my-1">
                        <label class="mr-sm-2" for="infoNotCh">INFO</label>
                        <select class="custom-select mr-sm-2" id="infoNotCh" disabled={ !allowEdit }>
                            <option value='0' selected={''==user.infoNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
                            <option value="SMTP" selected={'SMTP'==user.infoNotificationChannel.substring(0,user.infoNotificationChannel.indexOf(':'))}>E-mail</option>
                            <option value="SLACK" selected={'SLACK'==user.infoNotificationChannel.substring(0,user.infoNotificationChannel.indexOf(':'))}>Slack</option>
                            <option value="PUSHOVER" selected={'PUSHOVER'==user.infoNotificationChannel.substring(0,user.infoNotificationChannel.indexOf(':'))}>Pushover</option>
                        </select>
                    </div>
                    <div class="col-md-9 my-1">
                        <div class="form-group">
                            <label for="infoNotificationChannel">{ app.texts.user_form.config[app.language] }</label>
                            <input class="form-control" id="infoNotificationChannel" 
                                   name="infoNotificationChannel" type="text" readonly={ !allowEdit }
                                   value={ user.infoNotificationChannel.substring(user.infoNotificationChannel.indexOf(':')+1) }
                            >
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="col-md-3 my-1">
                        <label class="mr-sm-2" for="warningNotCh">WARNING</label>
                        <select class="custom-select mr-sm-2" id="warningNotCh" disabled={ !allowEdit }>
                            <option value='0' selected={''==user.warningNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
                            <option value="SMTP" selected={'SMTP'==user.warningNotificationChannel.substring(0,user.warningNotificationChannel.indexOf(':'))}>E-mail</option>
                            <option value="SLACK" selected={'SLACK'==user.warningNotificationChannel.substring(0,user.warningNotificationChannel.indexOf(':'))}>Slack</option>
                            <option value="PUSHOVER" selected={'PUSHOVER'==user.warningNotificationChannel.substring(0,user.warningNotificationChannel.indexOf(':'))}>Pushover</option>
                        </select>
                    </div>
                    <div class="col-md-9 my-1">
                        <div class="form-group">
                            <label for="warningNotificationChannel">{ app.texts.user_form.config[app.language] }</label>
                            <input class="form-control" id="warningNotificationChannel" 
                                   name="warningNotificationChannel" type="text" readonly={ !allowEdit }
                                   value={ user.warningNotificationChannel.substring(user.warningNotificationChannel.indexOf(':')+1) }
                            >
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <div class="col-md-3 my-1">
                        <label class="mr-sm-2" for="alertNotCh">ALERT</label>
                        <select class="custom-select mr-sm-2" id="alertNotCh" disabled={ !allowEdit }>
                            <option value='0' selected={''==user.alertNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
                            <option value="SMTP" selected={'SMTP'==user.alertNotificationChannel.substring(0,user.alertNotificationChannel.indexOf(':'))}>E-mail</option>
                            <option value="SLACK" selected={'SLACK'==user.alertNotificationChannel.substring(0,user.alertNotificationChannel.indexOf(':'))}>Slack</option>
                            <option value="PUSHOVER" selected={'PUSHOVER'==user.alertNotificationChannel.substring(0,user.alertNotificationChannel.indexOf(':'))}>Pushover</option>
                        </select>
                    </div>
                    <div class="col-md-9 my-1">
                        <div class="form-group">
                            <label for="alertNotificationChannel">{ app.texts.user_form.config[app.language] }</label>
                            <input class="form-control" id="alertNotificationChannel" 
                                   name="alertNotificationChannel" type="text" readonly={ !allowEdit }
                                   value={ user.alertNotificationChannel.substring(user.alertNotificationChannel.indexOf(':')+1) }
                            >
                        </div>
                    </div>
                </div>
                <div class="form-group border-dark border-top">
                </div>
                  
                <div class="form-group">
                    <label for="confirmString">{ app.texts.user_form.confirmString[app.language] }</label>
                    <input class="form-control" id="confirmString" name="confirmString" type="text" value={ user.confirmString } readonly={ true }>
                </div>
                <div class="form-group">
                    <label for="confirmed">{ app.texts.user_form.confirmed[app.language] }</label>
                    <input class="form-control" id="confirmed" name="confirmed" type="text" value={ user.confirmed } readonly={ !allowEdit || !adminMode } required>
                </div>
                <div class="form-group">
                    <label for="unregisterRequested">{ app.texts.user_form.unregisterRequested[app.language] }</label>
                    <input class="form-control" id="unregisterRequested" name="unregisterRequested" type="text" value={ user.unregisterRequested } readonly={ true } required>
                </div>
                <div class="form-group" if={ adminMode }>
                    <label>{ app.texts.user_form.number[app.language] } {user.number}</label><br>
                </div>
                <div class="form-group">
                    <button type="submit" class="btn btn-primary" disabled={ !allowEdit }>{ app.texts.user_form.save[app.language] }</button>
                    <button type="button" onclick={ close } class="btn btn-secondary">{ app.texts.user_form.cancel[app.language] }</button>
                </div>                
            </form>
        </div>
    </div>
    <script>
        this.visible = true
        self = this
        self.listener = riot.observable()
        self.callbackListener
        self.allowEdit = false
        self.adminMode = false
        self.method = 'POST'
        self.mode = 'view'

        self.user = {
            'uid': '',
            'email': '',
            'type': '',
            'role': '',
            'confirmString': '',
            'confirmed': false,
            'password': '',
            'generalNotificationChannel': '',
            'infoNotificationChannel': '',
            'warningNotificationChannel': '',
            'alertNotificationChannel': '',
            'unregisterRequested': false
        }

        globalEvents.on('data:submitted', function(event){
            if (app.debug) { console.log("I'm happy!") }
        });

        init(eventListener, uid, editable, isAdmin){
            self.callbackListener = eventListener
            self.allowEdit = editable
            self.adminMode = isAdmin
            self.method = 'POST'
            app.log('HELLO ' + uid)
            app.log('CALLBACK: ' + self.callbackListener)
            app.log('EDITABLE: ' + self.allowEdit)
            if (uid != 'NEW'){
                readUser(uid)
                self.method = 'PUT'
                if (self.allowEdit){
                    self.mode = 'update'
                } else{
                    self.mode = 'view'
                }
            } else{
                self.mode = 'create'
            }
        }


        userTypeAsString(type){
            switch (type){
                case 6:
                    return 'READONLY'
                    break
                case 5:
                    return 'PRIMARY'
                    break
                case 4:
                    return 'FREE'
                    break
                case 3:
                    return 'DEMO'
                    break
                case 2:
                    return 'APPLICATION'
                case 1:
                    return 'OWNER'
                    break
                case 0:
                    return 'USER'
                    break
                default:
                    return 'FREE'
            }
        }

        self.submitForm = function(e){
            app.log('SUBMITFORM')
            e.preventDefault()
            var formData = { }
            if (e.target.elements['uid'].value) {
                formData.uid = e.target.elements['uid'].value
            }

            formData.email = e.target.elements['email'].value
            if (self.adminMode){
                formData.type = e.target.elements['type'].value
                if (e.target.elements['role'].value != '') {
                    formData.role = e.target.elements['role'].value
                }
            }
            if (e.target.elements['genNotCh'].value!='0' && e.target.elements['generalNotificationChannel'].value != '') {
                formData.generalNotifications = e.target.elements['genNotCh'].value+":"+e.target.elements['generalNotificationChannel'].value
            }
            if (e.target.elements['infoNotCh'].value!='0' && e.target.elements['infoNotificationChannel'].value != '') {
                formData.infoNotifications = e.target.elements['infoNotCh'].value+":"+e.target.elements['infoNotificationChannel'].value
            }
            if (e.target.elements['warningNotCh'].value!='0' && e.target.elements['warningNotificationChannel'].value != '') {
                formData.warningNotifications = e.target.elements['warningNotCh'].value+":"+e.target.elements['warningNotificationChannel'].value
            }
            if (e.target.elements['alertNotCh'].value!='0' && e.target.elements['alertNotificationChannel'].value != '') {
                formData.alertNotifications = e.target.elements['alertNotCh'].value+":"+e.target.elements['alertNotificationChannel'].value
            }
            
            if (e.target.elements['confirmString'].value != '') {formData.confirmString = e.target.elements['confirmString'].value}
            if (e.target.elements['confirmed'].value != '') {formData.confirmed = e.target.elements['confirmed'].value}
            if (self.mode == 'create') {
                formData.password = generatePassword()
            }
            if (e.target.elements['unregisterRequested'].value != '') {
                formData.unregisterRequested = e.target.elements['unregisterRequested'].value
            }
            if (self.adminMode && e.target.elements['type'].value != '') {
                switch(e.target.elements['type'].value){
                    case 'USER':
                        formData.type = 0
                        break
                    case 'OWNER':
                        formData.type = 1
                        break
                    case 'APPLICATION':
                        formData.type = 2
                        break
                    case 'DEMO':
                        formData.type = 3
                        break
                    case 'FREE':
                        formData.type = 4
                        break
                    case 'PRIMARY':
                        formData.type = 5
                        break
                    case 'READONLY':
                        formData.type = 6
                        break
                    default:
                        formData.type = 4
                        break
                }
            }

            app.log(JSON.stringify(formData))
            urlPath = ''
            if (self.method == 'PUT'){
                urlPath = '/' + formData.uid
            }
            sendData(
                formData,
                self.method,
                app.userAPI + urlPath,
                app.user.token,
                self.close,
                null, //self.listener, 
                'submit:OK',
                'submit:ERROR',
                app.debug,
                globalEvents
            )
            //self.callbackListener.trigger('submitted')
        }

        self.close = function(object){
        var text = '' + object
        console.log('CALBACK: ' + object)
        if (text.startsWith('"') || text.startsWith('{')){
        self.callbackListener.trigger('submitted')
        } else if (text.startsWith('error:202')){
        self.callbackListener.trigger('submitted')
        } else if (text.startsWith('[object MouseEvent')){
        self.callbackListener.trigger('cancelled')
        } else{
        alert(text)
        }
        }

        var update = function (text) {
        app.log("USER: " + text)
        self.user = JSON.parse(text);
        riot.update();
        }

        var readUser = function (uid) {
        getData(app.userAPI + '/' + uid,
        null,
        app.user.token,
        update,
        self.listener, //globalEvents
        'OK',
        null, // in case of error send response code
        app.debug,
        globalEvents
        );
        }

        var generatePassword = function(){
        return window.btoa((new Date().getMilliseconds() + ''))
        }

        
    </script>
</user_form>
