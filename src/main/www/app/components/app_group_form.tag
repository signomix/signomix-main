<app_group_form>
    <div class="row">
        <div class="input-field col-md-12">
            <h2 if={ self.mode=='create' }>{ app.texts.device_form.header_g_new[app.language] }</h2>
            <h2 if={ self.mode=='update' }>{ app.texts.device_form.header_g_modify[app.language] }</h2>
            <h2 if={ self.mode=='view' }>{ app.texts.device_form.header_g_view[app.language] }</h2>
        </div>
    </div>
    <div class="row">
        <form class="col-md-12" onsubmit={ self.submitForm }>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="name" 
                        name="name" 
                        label={ app.texts.device_form.name[app.language] } 
                        type="text" required="true" 
                        content={ group.name } 
                        readonly={ !allowEdit } 
                        hint={ app.texts.device_form.name_hint[app.language] }>
                    </form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="eui" name="eui" 
                        label={ app.texts.device_form.eui[app.language] } 
                        type="text" 
                        content={ group.EUI } 
                        required={false} 
                        readonly={ true } 
                        hint={ app.texts.device_form.eui_g_hint[app.language] }>
                    </form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="channels" 
                        name="channels" 
                        label={ app.texts.device_form.channels[app.language] } 
                        type="text" content={ group.channels } 
                        readonly={ !allowEdit } 
                        hint={ app.texts.device_form.channels_hint[app.language] } 
                        onchange={ changeChannels }>
                    </form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="team" 
                        name="team" 
                        label={ app.texts.device_form.team[app.language] } 
                        type="text" 
                        content={ group.team } 
                        readonly={ !allowEdit } 
                        hint={ app.texts.device_form.team_g_hint[app.language] }>
                    </form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="admins" 
                        name="admins" 
                        label={ app.texts.device_form.admins[app.language] } 
                        type="text" 
                        content={ group.administrators } 
                        readonly={ !allowEdit } 
                        hint={ app.texts.device_form.admins_g_hint[app.language] }>
                    </form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="description" 
                        name="description" 
                        label={ app.texts.device_form.description[app.language] } 
                        type="textarea" 
                        content={ group.description } 
                        readonly={ !allowEdit } 
                        rows=2>
                    </form_input>
                </div>
            </div>
            <div class="form-row" if={ self.mode !='create' }>
                <div class="form-group col-md-12">
                    <label for="status">{ app.texts.device_form.owner[app.language] }</label>
                    <p class="form-control-static" id="owner">{group.userID}</p>
                </div>
            </div>
            <div class="form-row" if={ self.communicationError }>
                <div class="form-group col-md-12">
                    <div class="alert alert-danger" role="alert">{ self.errorMessage }</div>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <button type="submit" class="btn btn-primary" disabled={ !allowEdit }>{ app.texts.device_form.save[app.language] }</button>
                    <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.device_form.cancel[app.language] }</button>
                </div>
            </div>
        </form>
    </div>
    <script>
        this.visible = true
        self = this
        self.listener = riot.observable()
        self.callbackListener
        self.allowEdit = false
        self.method = 'POST'
        self.mode = 'view'
        self.communicationError = false
        self.errorMessage = ''
        self.group = {
            'EUI': '',
            'name': '',
            'team': '',
            'administrators': '',
            'channels': '',
            'description': '',
            'groups': ''
        }
        self.accepted = 0

        self.listener.on('*', function(eventName) {
            if (eventName == 'err:402') {
                self.communicationError = true
                self.errorMessage = app.texts.device_form.err402[app.language]
                self.update()
            }
            if (eventName == 'err:400') {
                self.communicationError = true
                self.errorMessage = app.texts.device_form.err400[app.language]
                self.update()
            }
            if (eventName == 'err:401') {
                self.communicationError = true
                self.errorMessage = app.texts.device_form.err401[app.language]
                self.update()
                globalEvents.trigger('err:401')
            }
        })

        globalEvents.on('data:submitted', function(event) {
            app.log('SUBMITTED')
        })

        init(eventListener, id, editable) {
            self.callbackListener = eventListener
            self.allowEdit = editable
            self.method = 'POST'
            if (id != 'NEW') {
                readGroup(id)
                self.method = 'PUT'
                if (self.allowEdit) {
                    self.mode = 'update'
                } else {
                    self.mode = 'view'
                }
            } else {
                self.mode = 'create'
            }
        }

        self.submitForm = function(e) {
            e.preventDefault()
            groupPath = ''
            if (self.mode == 'update') {
                groupPath = (self.method == 'PUT') ? '/' + e.target.elements['eui'].value : ''
            }
            var formData = {
                eui: '',
                name: '',
                team: '',
                administrators: '',
                channels: '',
                description: ''
            }
            formData.eui = e.target.elements['eui'].value
            formData.name = e.target.elements['name'].value
            formData.team = e.target.elements['team'].value
            formData.administrators = e.target.elements['admins'].value
            formData.channels = e.target.elements['channels'].value
            formData.description = e.target.elements['description'].value
            app.log(JSON.stringify(formData))
            sendData(
                formData,
                self.method,
                app.groupAPI + groupPath,
                app.user.token,
                self.submitted,
                self.listener,
                'submit:OK',
                null,
                app.debug,
                null //globalEvents
            )
        }

        self.close = function() {
            self.callbackListener.trigger('cancelled')
        }

        self.submitted = function() {
            self.callbackListener.trigger('submitted')
        }

        var update = function(text) {
            app.log("GROUP: " + text)
            self.group = JSON.parse(text);
            self.group.channels = encodeChannels()
            riot.update();
        }

        var readGroup = function(grEUI) {
            getData(app.groupAPI + '/' + grEUI,
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
        
        encodeChannels = function() {
            var result = ''
            var i = 0
            var channel = {}
            for (var key in self.group.channels) {
                if (self.group.channels.hasOwnProperty(key)) {
                    channel = self.group.channels[key]
                    if (i > 0) {
                        result = result + ','
                    }
                    result = result + channel.name
                    i++
                }
            }
            return result
        }

    </script>
</app_group_form>
