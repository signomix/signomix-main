<app_device_form>
    <div class="row">
        <div class="input-field col-md-12">
            <h2 if={ self.mode=='create' }>{ app.texts.device_form.header_new[app.language] }</h2>
            <h2 if={ self.mode=='update' }>{ app.texts.device_form.header_modify[app.language] }</h2>
            <h2 if={ self.mode=='view' }>{ app.texts.device_form.header_view[app.language] }</h2>
        </div>
    </div>
    <div class="row">
        <form class="col-md-12" onsubmit="{ self.submitSelectTemplate }" if="{self.useTemplate}">
            <div class="card text-center col-md-12" style="margin-bottom: 1rem">
                <div class="card-body">
                    <p class="mb-0" style="margin: 10px">{ template.description }</p>
                </div>
            </div>
            <div class="form-group">
                <div class="input-field">
                    <label for="template">{ app.texts.device_form.template[app.language] }</label>
                    <select class="form-control" id="template" name="template" value={ selectedTemplate } onchange={ changeTemplate } disabled={ !(allowEdit && !device.EUI) }>
                        <option each="{t in templates}" value="{t.eui}">{ t.producer+': '+t.eui}</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <button type="submit" class="btn btn-primary">{ app.texts.device_form.next[app.language] }</button>
                <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.device_form.cancel[app.language] }</button>
            </div>
        </form>
        <form class="col-md-12" onsubmit="{ self.submitForm }" if="{!self.useTemplate}">
            <div class="form-row" if="{isVisible('type')}">
                <div class="form-group col-md-6">
                    <label for="type">{ app.texts.device_form.type[app.language] }</label>
                    <select class="form-control" id="type" name="type" value={ device.type } onchange={ changeType } disabled={ !(allowEdit && !device.EUI) } required>
                        <option value="GENERIC">DEFAULT</option>
                        <option value="TTN">TTN</option>
                        <option value="LORA">LORA</option>
                        <option value="KPN">KPN</option>
                        <option value="GATEWAY">HOME GATEWAY</option>
                        <option value="VIRTUAL">VIRTUAL</option>
                        <option value="EXTERNAL">EXTERNAL</option>
                    </select>
                </div>
                <div class="card text-center col-md-6" style="margin-bottom: 10px">
                    <div class="card-body">
                        <p class="mb-0" style="margin: 10px">{ getTypeDescription() }</p>
                    </div>
                </div>
            </div>
            <div class="form-row" if="{!isVisible('type')}">
                <div class="card text-center col-md-12" style="margin-bottom: 10px">
                    <div class="card-body">
                        <p class="mb-0" style="margin: 10px">{ device.description }</p>
                    </div>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12">
                    <form_input id="name" name="name" label="{ app.texts.device_form.name[app.language] }" type="text" required="true" content="{ device.name }" readonly="{ !allowEdit }" hint="{ app.texts.device_form.name_hint[app.language] }"></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('eui')}">
                <div class="form-group col-md-12">
                    <form_input id="eui" name="eui" label={ app.texts.device_form.eui[app.language] } type="text" content={ device.EUI } required={device.type=='TTN' || device.type=='KPN' || device.type=='LORA'} readonly={ !(allowEdit && !device.EUI) } hint={ app.texts.device_form.eui_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('key')}">
                <div class="form-group col-md-12">
                    <form_input id="key" name="key" label={ app.texts.device_form.key[app.language] } type="text" content={ device.key } readonly={ !allowEdit } hint={ app.texts.device_form.key_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('channels')}">
                <div class="form-group col-md-12">
                    <form_input id="channels" name="channels" label={ app.texts.device_form.channels[app.language] } type="text" content={ device.channels } readonly={ !allowEdit } hint={ app.texts.device_form.channels_hint[app.language] } onchange={ changeChannels }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('team')}">
                <div class="form-group col-md-12">
                    <form_input id="team" name="team" label={ app.texts.device_form.team[app.language] } type="text" content={ device.team } readonly={ !allowEdit } hint={ app.texts.device_form.team_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('team')}">
                <div class="form-group col-md-12">
                    <form_input id="admins" name="admins" label={ app.texts.device_form.admins[app.language] } type="text" content={ device.administrators } readonly={ !allowEdit } hint={ app.texts.device_form.admins_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('description')}">
                <div class="form-group col-md-12">
                    <form_input id="description" name="description" label={ app.texts.device_form.description[app.language] } type="textarea" content={ device.description } readonly={ !allowEdit } rows=2></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('encoder')}">
                <div class="form-group col-md-12">
                    <form_input id="encoder" name="encoder" label={ app.texts.device_form.encoder[app.language] } type="textarea" content={ device.encoder } readonly={ !allowEdit } rows=6 hint={ app.texts.device_form.encoder_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12" if="{isVisible('code')}">
                    <form_input id="code" name="code" label={ app.texts.device_form.code[app.language] } type="textarea" content={ device.code } readonly={ !allowEdit } rows=6 hint={ app.texts.device_form.code_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row">
                <div class="form-group col-md-12" if="{isVisible('interval')}">
                    <form_input id="interval" name="interval" label={ app.texts.device_form.interval[app.language] } type="text" content={ device.transmissionInterval/60000 } readonly={ !allowEdit } hint={ app.texts.device_form.interval_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('groups')}">
                <div class="form-group col-md-12">
                    <form_input id="groups" name="groups" label={ app.texts.device_form.groups[app.language] } type="text" content={ device.groups } readonly={ !allowEdit } hint={ app.texts.device_form.groups_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{ device.type=='TTN' && isVisible('appeui') }">
                <div class="form-group col-md-12">
                    <form_input id="appeui" name="appeui" label={ app.texts.device_form.appeui[app.language] } type="text" content={ device.applicationEUI } readonly={ !allowEdit } hint={ app.texts.device_form.appeui_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{ device.type=='TTN' && isVisible('appid')}">
                <div class="form-group col-md-12">
                    <form_input id="appid" name="appid" label={ app.texts.device_form.appid[app.language] } type="text" content={ device.applicationID } readonly={ !allowEdit } hint={ app.texts.device_form.appid_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('project')}">
                <div class="form-group col-md-12">
                    <form_input id="project" name="project" label={ app.texts.device_form.project[app.language] } type="text" content={ device.project } readonly={ !allowEdit } hint={ app.texts.device_form.project_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('state')}">
                <div class="form-group col-md-12">
                    <form_input id="state" name="state" label={ app.texts.device_form.state[app.language] } type="text" content={ device.state } readonly={ !allowEdit } hint={ app.texts.device_form.state_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('latitude')}">
                <div class="form-group col-md-12">
                    <form_input id="latitude" name="latitude" label={ app.texts.device_form.latitude[app.language] } type="text" content={ device.latitude } readonly={ !allowEdit } hint={ app.texts.device_form.latitude_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('longitude')}">
                <div class="form-group col-md-12">
                    <form_input id="longitude" name="longitude" label={ app.texts.device_form.longitude[app.language] } type="text" content={ device.longitude } readonly={ !allowEdit } hint={ app.texts.device_form.longitude_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('downlink')}">
                <div class="form-group col-md-12">
                    <form_input id="downlink" name="downlink" label={ app.texts.device_form.downlink[app.language] } type="text" content={ device.downlink } readonly={ !allowEdit } hint={ app.texts.device_form.downlink_hint[app.language] }></form_input>
                </div>
            </div>
            <div class="form-row" if="{isVisible('active')}">  
                <div class="form-group form-check">
                    <input type="checkbox" class="form-check-input" id="active" value={device.active} readonly={ !allowEdit } checked="{device.active}">
                    <label class="form-check-label" for="active">{ app.texts.device_form.active[app.language] }</label>
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
            <div class="form-row" if={ self.mode !='create' }>
                <div class="form-group col-md-12">
                    <label for="status">{ app.texts.device_form.owner[app.language] }</label>
                    <p class="form-control-static" id="owner">{self.device.userID}</p>
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
        self.useTemplate = false
        self.method = 'POST'
        self.mode = 'view'
        self.channelsEncoded = ''
        self.channelsChanged = false
        self.communicationError = false
        self.errorMessage = ''
        self.templates=[]
        self.template = {}
        self.device = {
            'EUI': '',
            'applicationEUI': '',
            'appplicatioID': '',
            'name': '',
            'key': '',
            'type': 'GENERIC',
            'team': '',
            'administrators': '',
            'channels': '',
            'code': '',
            'encoder': '',
            'description': '',
            'groups': '',
            'lastSeen': -1,
            'transmissionInterval': 0,
            'pattern': '',
            'commandscript': '',
            'template': '',
            'active':'true',
            'state': 0,
            'latitude': '',
            'longitude': '',
            'project':'',
            'downlink':''
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

        init(eventListener, id, editable, fromTemplate){
            self.callbackListener = eventListener
            self.allowEdit = editable
            self.useTemplate = fromTemplate
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
                if(self.useTemplate){
                    readTemplates()
                }
                self.mode = 'create'
            }
            if(fromTemplate){
                self.changeTemplate()
            }
        }

        self.changeType = function(e) {
            e.preventDefault()
            if (e.target) {
                self.device.type = e.target.value
                riot.update()
            } else {
                app.log('UNKNOWN TARGET OF: ' + e)
            }
            if('EXTERNAL'==self.device.type){
                self.template['pattern']=",type,eui,name,key,description,team,active,groups,downlink"
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
            "eui":"-",
            "appid":"",
            "appeui":"",
            "type":"",
            "channels":"",
            "code":"",
            "decoder":"",
            "description":"self defined",
            "interval":0,
            "pattern":",type,eui,name,key,channels,encoder,code,description,team,interval,active,appeui,appid,project,groups,",
            "commandScript":"",
            "producer":"user defined"
        }

        self.template = self.undefined
        self.selectedTemplate = ''
        
        isVisible(fieldName){
            return (self.mode!='create' || self.template.pattern.indexOf(fieldName)>=0)
        }

        getStatus(lastSeen, interval) {
            if (self.now - lastSeen > interval) {
                return 'images/KO.svg'
            } else {
                return 'images/OK.svg'
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
                case 'EXTERNAL':
                    return app.texts.device_form.external_desc[app.language]
                default:
                    return app.texts.device_form.default_desc[app.language]
            }
        }
        
        self.changeTemplate = function(e) {
            var templateEui='-'
            try {
                e.preventDefault()
                templateEui=e.target.value
            }catch(err){}
            for(var i=0; i<self.templates.length; i++){
                if(self.templates[i].eui==templateEui){
                    self.selectedTemplate = self.templates[i].eui
                    self.template=self.templates[i]
                    continue
                }
            }
            self.device.type=self.template.type
            self.device.channels=self.template.channels
            self.device.description=self.template.description
            self.device.encoder=self.template.decoder
            self.device.code=self.template.code
            self.device.appid=self.template.appid
            self.device.appeui=self.template.appeui
            self.device.interval=self.template.interval
            self.device.template=self.template.eui
            self.device.active='true'
            self.device.project=''
            self.device.downlink=''
            self.device.applicationEUI = ''
            self.device.applicationID=''
            self.device.state=''
            self.device.latitude=''
            self.device.longitude=''
            riot.update()
        }
        
        self.submitSelectTemplate =function(e){
            e.preventDefault()
            self.useTemplate=false
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
                administrators: '',
                channels: '',
                transmissionInterval: '',
                description: '',
                code: '',
                pattern: '',
                groups: '',
                commandscript: '',
                template: '',
                project: '',
                active: '',
                state:'',
                latitude:'',
                longitude:'',
                downlink:''
            }
            if(e.target.elements['eui']) formData.eui = e.target.elements['eui'].value
            if (self.device.type == 'TTN') {
                if(e.target.elements['appeui']) {
                    formData.appeui = e.target.elements['appeui'].value
                }else{
                    formData.appeui = self.device.applicationEUI
                }
                if(e.target.elements['appid']) {
                    formData.appid = e.target.elements['appid'].value
                }else{
                    formData.appid = self.device.applicationID
                }
            }
            if(e.target.elements['name']) {
                formData.name = e.target.elements['name'].value
            }else{
                formData.name = self.device.name
            }
            if(e.target.elements['key']) {
                formData.key = e.target.elements['key'].value
            }else{
                formData.key = self.device.key
            }
            if(e.target.elements['type']) {
                formData.type = e.target.elements['type'].value
            }else{
                formData.type = self.device.type
            }
            if(e.target.elements['team']) {
                formData.team = e.target.elements['team'].value
            }else{
                formData.team = self.device.team
            }
            if(e.target.elements['admins']) {
                formData.administrators = e.target.elements['admins'].value
            }else{
                formData.administrators = self.device.administrators
            }
            if(e.target.elements['channels']) {
                formData.channels = e.target.elements['channels'].value
            }else{
                formData.channels = self.device.channels
            }
            if(e.target.elements['groups']) {
                formData.groups = e.target.elements['groups'].value
            }else{
                formData.groups = self.device.groups
            }
            if(e.target.elements['project']) {
                formData.project = e.target.elements['project'].value
            }else{
                formData.project = self.device.project
            }
            if(e.target.elements['active']) {
                var aCheck = e.target.elements['active']
                formData.active = aCheck.checked?'true':'false'
            }else{
                formData.active = self.device.active
            }
            if(e.target.elements['description']) {
                formData.description = e.target.elements['description'].value
            }else{
                formData.description = self.device.description
            }
            //formData.code = escape(trimSpaces(e.target.elements['code'].value))
            //formData.encoder = escape(trimSpaces(e.target.elements['encoder'].value))
            if(e.target.elements['code']) {
                formData.code = trimSpaces(e.target.elements['code'].value)
            }else{
                formData.code = self.device.code
            }
            if(e.target.elements['encoder']) {
                formData.encoder = trimSpaces(e.target.elements['encoder'].value)
            }else{
                formData.encoder = self.device.encoder
            }
            
            formData.commandscript = self.device.commandscript
            formData.template = self.device.template
            if(e.target.elements['interval']) {
                formData.transmissionInterval = 60000 * Number(e.target.elements['interval'].value)
            }else{
                formData.transmissionInterval = self.device.intervalnterval
            }
            if(e.target.elements['state']) {
                formData.state = e.target.elements['state'].value
            }else{
                formData.state = self.device.state
            }
            if(e.target.elements['latitude']) {
                formData.latitude = e.target.elements['latitude'].value
            }else{
                formData.latitude = self.device.latitude
            }
            if(e.target.elements['longitude']) {
                formData.longitude = e.target.elements['longitude'].value
            }else{
                formData.longitude = self.device.longitude
            }
            if(e.target.elements['downlink']) {
                formData.downlink = e.target.elements['downlink'].value
            }else{
                formData.downlink = self.device.downlink
            }
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
            if(self.device.latitude==100000){
                self.device.latitude=''
            }
            if(self.device.longitude==100000){
                self.device.longitude=''
            }
            self.device.channels = encodeChannels()
            self.templateSelected = true
            riot.update();
        }
        
         var updateTemplates = function(text) {
            self.templates = JSON.parse(text);
            self.templates.unshift(self.undefined)
            riot.update()
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
        
        var readTemplates = function() {
            getData(app.templateAPI,
                null,
                app.user.token,
                updateTemplates,
                self.listener, //globalEvents
                'OK',
                null, // in case of error send response code
                app.debug,
                globalEvents
            );
        }

    </script>
</app_device_form>
