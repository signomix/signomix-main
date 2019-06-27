<user_form>
    <div class="panel panel-primary">
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
                    <input class="form-control" id="email" name="email" type="email" value={ user.email } readonly={ !allowEdit } required>
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
                            <option value='SIGNOMIX' selected={''==user.generalNotificationChannel || 'SIGNOMIX'==user.generalNotificationChannel }>{ app.texts.user_form.select[app.language] }</option>
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
                            <option value='SIGNOMIX' selected={''==user.infoNotificationChannel || 'SIGNOMIX'==user.generalNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
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
                            <option value='SIGNOMIX' selected={''==user.warningNotificationChannel || 'SIGNOMIX'==user.generalNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
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
                            <option value='SIGNOMIX' selected={''==user.alertNotificationChannel || 'SIGNOMIX'==user.generalNotificationChannel}>{ app.texts.user_form.select[app.language] }</option>
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
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="true" id="confirmed" disabled={ !allowEdit || !adminMode } checked={ user.confirmed }>
                    <label class="form-check-label" for="confirmed">{ app.texts.user_form.confirmed[app.language] }</label>
                </div>
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" value="true" id="unregisterRequested" disabled={ true } checked={ user.unregisterRequested }>
                    <label class="form-check-label" for="unregisterRequested">{ app.texts.user_form.unregisterRequested[app.language] }</label>
                </div>
                <div class="form-group">
                    <label>{ app.texts.user_form.number[app.language] } {user.number}</label>
                </div>
                <button type="submit" class="btn btn-primary  pull-right" disabled={ !allowEdit }>{ app.texts.user_form.save[app.language] }</button>
                <span>&nbsp;</span>
                <button type="button" onclick={ close } class="btn btn-secondary">{ app.texts.user_form.cancel[app.language] }</button>
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
            //
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
        
        userTypeAsNumber(type){
            switch (type.toUpperCase()){
                case 'READONLY':
                case '6':
                    return '6'
                    break
                case 'PRIMARY':
                case '5':
                    return '5'
                    break
                case 'FREE':
                case '4':
                    return '4'
                    break
                case 'DEMO':
                case '3':
                    return '3'
                    break
                case 'APPLICATION':
                case '2':
                    return '2'
                case 'OWNER':
                case '1':
                    return '1'
                    break
                case 'USER':
                case '0':
                    return '0'
                    break
                default:
                    return '4'
            }
        }

        getConfirmedState(){
            if(self.user.confirmed){
                return 'checked'
            }else{
                return ''
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
                formData.type = self.userTypeAsNumber(e.target.elements['type'].value)
                if (e.target.elements['role'].value != '') {
                    formData.role = e.target.elements['role'].value
                }
            }
            if (e.target.elements['genNotCh'].value!='SIGNOMIX' && e.target.elements['generalNotificationChannel'].value != '') {
                formData.generalNotifications = e.target.elements['genNotCh'].value+":"+e.target.elements['generalNotificationChannel'].value
            }else if(e.target.elements['genNotCh'].value=='SIGNOMIX'){
                formData.generalNotifications = 'SIGNOMIX:'
            }
            if (e.target.elements['infoNotCh'].value!='SIGNOMIX' && e.target.elements['infoNotificationChannel'].value != '') {
                formData.infoNotifications = e.target.elements['infoNotCh'].value+":"+e.target.elements['infoNotificationChannel'].value
            }else if(e.target.elements['infoNotCh'].value=='SIGNOMIX'){
                formData.infoNotifications = 'SIGNOMIX:'
            }
            if (e.target.elements['warningNotCh'].value!='SIGNOMIX' && e.target.elements['warningNotificationChannel'].value != '') {
                formData.warningNotifications = e.target.elements['warningNotCh'].value+":"+e.target.elements['warningNotificationChannel'].value
            }else if(e.target.elements['warningNotCh'].value=='SIGNOMIX'){
                formData.warningNotifications = 'SIGNOMIX:'
            }
            if (e.target.elements['alertNotCh'].value!='SIGNOMIX' && e.target.elements['alertNotificationChannel'].value != '') {
                formData.alertNotifications = e.target.elements['alertNotCh'].value+":"+e.target.elements['alertNotificationChannel'].value
            }else if(e.target.elements['alertNotCh'].value=='SIGNOMIX'){
                formData.alertNotifications = 'SIGNOMIX:'
            }
            
            if (e.target.elements['confirmString'].value != '') {formData.confirmString = e.target.elements['confirmString'].value}
            if (e.target.elements['confirmed'].checked) {formData.confirmed = 'true'}else{formData.confirmed = 'false'}
            if (self.mode == 'create') {
                formData.password = generatePassword()
            }
            if (e.target.elements['unregisterRequested'].checked) {formData.unregisterRequested = e.target.elements['unregisterRequested'].value}

            app.log(JSON.stringify(formData))
            urlPath = ''
            if (self.method == 'PUT'){
                urlPath = formData.uid
            }
            sendData(formData,self.method,app.userAPI + '/'+urlPath,app.user.token,self.close,globalEvents)
        }

        self.close = function(object){
        var text = '' + object
        app.log('CALLBACK: ' + object)
        if (text.startsWith('"') || text.startsWith('{')){
        self.callbackListener.trigger('submitted')
        } else if (text.startsWith('error:202')){
        self.callbackListener.trigger('submitted')
        } else if (text.startsWith('[object MouseEvent')){
        self.callbackListener.trigger('cancelled')
            } else if (text.startsWith('error:409')){
                alert('This login is already registered!')
        } else{
        alert(text)
        }
        }

        var update = function (text) {
        app.log("USER: " + text)
        self.user = JSON.parse(text);
        riot.update();
        }
        self.listener.on('*', function(event){
          riot.update()
        })

        var readUser = function (uid) {
            getData(app.userAPI+'/'+uid,null,app.user.token,update,self.listener)
        }

        var generatePassword = function(){
        return window.btoa((new Date().getMilliseconds() + ''))
        }

    </script>
</user_form>
