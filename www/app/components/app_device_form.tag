<app_device_form>
    <div class="row">
        <div class="input-field col-md-12">
            <h2 if={ self.mode=='create' }>{ app.texts.device_form.header_new[app.language] }</h2>
            <h2 if={ self.mode=='update' }>{ app.texts.device_form.header_modify[app.language] }</h2>
            <h2 if={ self.mode=='view' }>{ app.texts.device_form.header_view[app.language] }</h2>
        </div>
    </div>
    <div class="row">
        <form class="col-md-12" onsubmit={ self.submitForm }>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <label for="type">{ app.texts.device_form.type[app.language] }</label>
                    <select class="form-control" id="type" name="type" value={ device.type } onchange={ changeType } disabled={ !(allowEdit && !device.EUI) } required>
                        <option value="GENERIC">DEFAULT</option>
                        <option value="TTN">TTN</option>
                        <option value="LORA">LORA</option>
                        <option value="KPN">KPN</option>
                        <option value="GATEWAY">HOME GATEWAY</option>
                        <option value="VIRTUAL">VIRTUAL</option>
                    </select>
                </div>
                <div class="card text-center z-depth-2 col-md-6" style="margin-bottom: 10px">
                    <div class="card-body">
                        <p class="mb-0" style="margin: 10px">{ getTypeDescription() }</p>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="name" name="name" label={ app.texts.device_form.name[app.language] } type="text" required="true" content={ device.name } readonly={ !allowEdit } hint={ app.texts.device_form.name_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="eui" name="eui" label={ app.texts.device_form.eui[app.language] } type="text" content={ device.EUI } required={device.type=='TTN' || device.type=='KPN' || device.type=='LORA'} readonly={ !(allowEdit && !device.EUI) } hint={ app.texts.device_form.eui_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="key" name="key" label={ app.texts.device_form.key[app.language] } type="text" content={ device.key } readonly={ !allowEdit } hint={ app.texts.device_form.key_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="channels" name="channels" label={ app.texts.device_form.channels[app.language] } type="text" content={ device.channels } readonly={ !allowEdit } hint={ app.texts.device_form.channels_hint[app.language] } onchange={ changeChannels }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="team" name="team" label={ app.texts.device_form.team[app.language] } type="text" content={ device.team } readonly={ !allowEdit } hint={ app.texts.device_form.team_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="description" name="description" label={ app.texts.device_form.description[app.language] } type="textarea" content={ device.description } readonly={ !allowEdit } rows=2></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="encoder" name="encoder" label={ app.texts.device_form.encoder[app.language] } type="textarea" content={ device.encoder } readonly={ !allowEdit } rows=6 hint={ app.texts.device_form.encoder_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="code" name="code" label={ app.texts.device_form.code[app.language] } type="textarea" content={ device.code } readonly={ !allowEdit } rows=6 hint={ app.texts.device_form.code_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="interval" name="interval" label={ app.texts.device_form.interval[app.language] } type="text" content={ device.transmissionInterval/60000 } readonly={ !allowEdit } hint={ app.texts.device_form.interval_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="groups" name="groups" label={ app.texts.device_form.groups[app.language] } type="text" content={ device.groups } readonly={ !allowEdit } hint={ app.texts.device_form.groups_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if={ device.type=='TTN' }>
                <div class="form-group col-md-12">
                    <form_input id="appeui" name="appeui" label={ app.texts.device_form.appeui[app.language] } type="text" content={ device.applicationEUI } hint={ app.texts.device_form.appeui_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if={ device.type=='TTN' }>
                <div class="form-group col-md-12">
                    <form_input id="appid" name="appid" label={ app.texts.device_form.appid[app.language] } type="text" content={ device.applicationID } hint={ app.texts.device_form.appid_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if={ self.mode !='create' }>
                <div class="form-group col-md-12">
                    <label for="status">{ app.texts.device_form.devicestatus[app.language] }</label>
                    <p class="form-control-static" id="status">
                        <img height="16px" style="margin-right: 10px;" src={ getStatus(self.device.lastSeen, self.device.transmissionInterval) }>last seen { getLastSeenString(self.device.lastSeen)}
                    </p>
                </div>
            </div>
            <div class="form-row" if={ self.mode=='update' && self.channelsChanged }>
                <div class="form-group col-md-12">
                    <div class="alert alert-danger" role="alert">{ app.texts.device_form.channels_alert[app.language] }</div>
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
        self.now = Date.now()
        self.listener = riot.observable()
        self.callbackListener
        self.templateSelected = true
        self.selectedTemplate = 'UNDEFINED'
        self.allowEdit = false
        self.method = 'POST'
        self.mode = 'view'
        self.channelsEncoded = ''
        self.channelsChanged = false
        self.communicationError = false
        self.errorMessage = ''
        self.template = {
            'EUI': 'UNDEFINED',
            'type':'GENERIC',
            'description': '{"en": "",'+
                    '"it": "",'+
                    '"fr": "",'+
                    '"pl": ""}'
        }
        self.device = {
            'EUI': '',
            'applicationEUI': '',
            'appplicatioID': '',
            'name': '',
            'key': '',
            'type': 'GENERIC',
            'team': '',
            'channels': '',
            'code': '',
            'encoder': '',
            'description': '',
            'groups': '',
            'lastSeen': -1,
            'transmissionInterval': 0,
            'pattern': '',
            'commandscript': '',
            'template': ''
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
                readDevice(id)
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

        self.changeType = function(e) {
            if (e.target) {
                self.device.type = e.target.value
                riot.update()
            } else {
                app.log('UNKNOWN TARGET OF: ' + e)
            }
        }

        self.changeChannels = function(e) {
            if (self.device.channels != e.target.value.trim()) {
                self.channelsChanged = true
            } else {
                self.channelsChanged = false
            }
            riot.update()
        }

        self.undefined = {
            'EUI': 'UNDEFINED',
            'type': 'GENERIC',
            'applicationID': '',
            'channels': '',
            'code': '',
            'encoder': '',
            'pattern': '',
            'groups': '',
            'transmissionInterval': '',
            'commandscript': '',
            'description': '{"en":"Device line not specified. You must define required parameters yourself.","it":"Device line not specified. You must define required parameters yourself.","fr":"Device line not specified. You must define required parameters yourself.","pl":"Linia produktów nie jest wybrana. Musisz zdefiniować wymagane parametry."}'
        }

        self.template = self.undefined
        self.selectedTemplate = 'UNDEFINED'

        getStatus(lastSeen, interval) {
            if (self.now - lastSeen > interval) {
                return '/images/KO.svg'
            } else {
                return '/images/OK.svg'
            }
        }

        getLastSeenString(lastSeen) {
            if (lastSeen && lastSeen > 0) {
                return new Date(lastSeen).toLocaleString()
            } else {
                return '---'
            }
        }

        getTypeDescription() {
            switch (self.device.type) {
                case 'GENERIC':
                    return app.texts.device_form.generic_desc[app.language]
                case 'TTN':
                    return app.texts.device_form.ttn_desc[app.language]
                case 'LORA':
                    return app.texts.device_form.lora_desc[app.language]
                case 'KPN':
                    return app.texts.device_form.kpn_desc[app.language]
                case 'GATEWAY':
                    return app.texts.device_form.gateway_desc[app.language]
                case 'VIRTUAL':
                    return app.texts.device_form.virtual_desc[app.language]
                default:
                    return app.texts.device_form.default_desc[app.language]
            }
        }

        self.submitForm = function(e) {
            e.preventDefault()
            devicePath = ''
            if (self.mode == 'update') {
                devicePath = (self.method == 'PUT') ? '/' + e.target.elements['eui'].value : ''
            }
            var formData = {
                eui: '',
                appeui: '',
                appid: '',
                name: '',
                key: '',
                type: '',
                team: '',
                channels: '',
                transmissionInterval: '',
                description: '',
                code: '',
                pattern: '',
                groups: '',
                commandscript: '',
                template: ''
            }
            formData.eui = e.target.elements['eui'].value
            if (self.device.type == 'TTN') {
                formData.appeui = e.target.elements['appeui'].value
                formData.appid = e.target.elements['appid'].value
            }
            formData.name = e.target.elements['name'].value
            formData.key = e.target.elements['key'].value
            formData.type = e.target.elements['type'].value
            formData.team = e.target.elements['team'].value
            formData.channels = e.target.elements['channels'].value
            formData.groups = e.target.elements['groups'].value
            formData.description = e.target.elements['description'].value
            //formData.code = escape(trimSpaces(e.target.elements['code'].value))
            //formData.encoder = escape(trimSpaces(e.target.elements['encoder'].value))
            formData.code = trimSpaces(e.target.elements['code'].value)
            formData.encoder = trimSpaces(e.target.elements['encoder'].value)
            formData.pattern = self.device.pattern
            formData.commandscript = self.device.commandscript
            formData.template = self.device.template
            formData.transmissionInterval = 60000 * Number(e.target.elements['interval'].value)
            app.log(JSON.stringify(formData))
            sendData(
                formData,
                self.method,
                app.iotAPI + devicePath,
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

        encodeChannels = function() {
            var result = ''
            var i = 0
            var channel = {}
            for (var key in self.device.channels) {
                if (self.device.channels.hasOwnProperty(key)) {
                    //app.log(key + " -> " + self.device.channels[key]);
                    channel = self.device.channels[key]
                    if (i > 0) {
                        result = result + ','
                    }
                    result = result + channel.name
                    i++
                }
            }
            return result
        }

        var update = function(text) {
            app.log("DEVICE: " + text)
            self.device = JSON.parse(text);
            if (self.device.code) {
                self.device.code = unescape(self.device.code)
            } else {
                self.device.code = ''
            }
            if (self.device.encoder) {
                self.device.encoder = unescape(self.device.encoder)
            } else {
                self.device.encoder = ''
            }
            self.device.channels = encodeChannels()
            self.templateSelected = true
            riot.update();
        }

        var readDevice = function(devEUI) {
            getData(app.iotAPI + '/' + devEUI,
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

    </script>
</app_device_form>
