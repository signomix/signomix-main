<app_dashboard_form>
    <div class="row">
        <div class="input-field col-md-12">
            <h2 if={ self.mode == 'create' }>{app.texts.dashboard_form.new[app.language]}</h2>
            <h2 if={ self.mode == 'update' }>{app.texts.dashboard_form.modify[app.language]}</h2>
            <h2 if={ self.mode == 'view' }>{app.texts.dashboard_form.view[app.language]}</h2>
        </div>
    </div>
    <!-- Central Modal: REMOVE -->
    <div class="modal fade" id="widgetRemove" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true" if={self.selectedForRemove>-1}>
        <div class="modal-dialog modal-notify modal-danger" role="document">
            <!--Content-->
            <div class="modal-content">
                <!--Header-->
                <div class="modal-header">
                    <p class="heading lead">{app.texts.dashboard_form.f_remove_title[app.language]}</p>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true" class="white-text">&times;</span>
                    </button>
                </div>
                <!--Body-->
                <div class="modal-body">
                    <div class="text-center">
                        <p><b>{ self.dashboard.widgets[self.selectedForRemove].name }</b></p>
                        <p>{app.texts.dashboard_form.f_remove_question[app.language]}</p>
                    </div>
                </div>
                <!--Footer-->
                <div class="modal-footer justify-content-center">
                    <a type="button" class="btn btn-primary-modal" onclick={confirmRemoveWidget(self.selectedForRemove)}>{app.texts.dashboard_form.remove[app.language]} <i class="material-icons clickable">delete</i></a>
                    <a type="button" class="btn btn-outline-secondary-modal waves-effect" data-dismiss="modal">{app.texts.dashboard_form.cancel[app.language]}</a>
                </div>
            </div>
            <!--/.Content-->
        </div>
    </div>
    <!-- Central Modal: REMOVE -->
    
    
    <div class="modal fade" id="widgetEdit" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
        <div class="modal-dialog cascading-modal" role="document">
            <div class="modal-content">
                <div class="modal-header light-blue darken-3 white-text">
                    <h4 class="title"><i class="material-icons clickable">mode_edit</i> {app.texts.dashboard_form.f_widget_formtitle[app.language]}</h4>
                </div>
                <div class="modal-body mb-0">
                    <form onsubmit={saveWidget}>
                        <div class="row">
                        <div class="form-group col-md-12">
                            <label for="w_type" class="active">{app.texts.dashboard_form.f_widget_type[app.language]}</label>
                            <select class="form-control" id="w_type" disabled={!allowEdit} onchange={changeType}>
                                <option value="text" selected={self.editedWidget.type=='text'}>{self.getTypeName('text')}</option>
                                <option value="symbol" selected={self.editedWidget.type=='symbol'}>{self.getTypeName('symbol')}</option>
                                <option value="raw" selected={self.editedWidget.type=='raw'}>{self.getTypeName('raw')}</option>
                                <option value="line" selected={self.editedWidget.type=='line'}>{self.getTypeName('line')}</option>
                                <option value="stepped" selected={self.editedWidget.type=='stepped'}>{self.getTypeName('stepped')}</option>
                                <option value="map" selected={self.editedWidget.type=='map'}>{self.getTypeName('map')}</option>
                                <option value="plan" selected={self.editedWidget.type=='plan'}>{self.getTypeName('plan')}</option>
                                <option value="date" selected={self.editedWidget.type=='date'}>{self.getTypeName('date')}</option>
                                <option value="led" selected={self.editedWidget.type=='led'}>{self.getTypeName('led')}</option>
                                <option value="report" selected={self.editedWidget.type=='report'}>{self.getTypeName('report')}</option>
                                <option value="multimap" selected={self.editedWidget.type=='multimap'}>{self.getTypeName('multimap')}</option>
                                <option value="button" selected={self.editedWidget.type=='button'}>{self.getTypeName('button')}</option>
                                <option value="stopwatch" selected={self.editedWidget.type=='stopwatch'}>{self.getTypeName('stopwatch')}</option>
                                <option value="time" selected={self.editedWidget.type=='time'}>{self.getTypeName('time')}</option>
                                <option value="devinfo" selected={self.editedWidget.type=='devinfo'}>{self.getTypeName('devinfo')}</option>
                                <option value="openweather" selected={self.editedWidget.type=='openweather'}>{self.getTypeName('openweather')}</option>
                            </select>
                        </div>
                        </div>
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_name"
                                name="w_name"
                                label={ app.texts.dashboard_form.f_widget_name[app.language] }
                                type="text"
                                required="true"
                                content={ self.editedWidget.name }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_name_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type!='text' && self.editedWidget.type!='report' && self.editedWidget.type!='multimap' && self.editedWidget.type!='plan' }>
                        <div class="form-group col-md-12">
                            <div class="input-field">
                                <label for="w_dev_id">{ app.texts.dashboard_form.f_widget_deviceid[app.language] }</label>
                                    <select class="form-control" id="w_dev_id" name="w_dev_id" disabled={!allowEdit} required>
                                        <option value="">{ app.texts.dashboard_form.f_widget_select_device[app.language] }</option>
                                        <option each="{d in myDevices}" value="{d.EUI}" selected="{d.EUI==self.editedWidget.dev_id}">{ d.EUI+': '+d.name}</option>
                                    </select>
                            </div>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='report' || self.editedWidget.type=='multimap' || self.editedWidget.type=='plan'}>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_group"
                                name="w_group"
                                label={ app.texts.dashboard_form.f_widget_group[app.language] }
                                type="text"
                                content={ self.editedWidget.group }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_group_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type!='text' }>
                        <div class="form-group col-md-12" if={ self.editedWidget.type!='button' }>
                            <form_input 
                                id="w_channel"
                                name="w_channel"
                                label={ app.texts.dashboard_form.f_widget_channel[app.language] }
                                type="text"
                                content={ self.editedWidget.channel }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_channel_hint[app.language] }
                            ></form_input>
                        </div>
                        <div class="form-group col-md-12" if={ self.editedWidget.type==='button' }>
                            <form_input 
                                id="w_channel"
                                name="w_channel"
                                label={ app.texts.dashboard_form.f_widget_datatype[app.language] }
                                type="text"
                                content={ self.editedWidget.channel }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_datatype_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='report' || self.editedWidget.type=='multimap' || self.editedWidget.type=='plan' }>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_channel_translated"
                                name="w_channel_translated"
                                label={ app.texts.dashboard_form.f_widget_channel_translated[app.language] }
                                type="text"
                                content={ self.editedWidget.channelTranslated }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_channel_translated_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='symbol'}>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_unit"
                                name="w_unit"
                                label={ app.texts.dashboard_form.f_widget_unit[app.language] }
                                type="text"
                                content={ self.editedWidget.unitName }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_unit_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type!='text' && self.editedWidget.type!='report' && self.editedWidget.type!='multimap' && self.editedWidget.type!='button' && self.editedWidget.type!='plan'}>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_query"
                                name="w_query"
                                label={ app.texts.dashboard_form.f_widget_query[app.language] }
                                type="text"
                                content={ self.editedWidget.queryvalue }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_query_hint[app.language] }
                            ></form_input>
                            <div class="alert alert-danger" if="{ self.invalidQuery}">{ app.texts.dashboard_form.f_widget_query_error[app.language] }</div>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='raw'}>
                        <div class="form-group col-md-12">
                            <label for="w_format" class="active">{app.texts.dashboard_form.f_widget_format[app.language]}</label>
                            <select class="form-control" id="w_format" disabled={!allowEdit}>
                                <option value="standard" selected={self.editedWidget.format=='standard'}>standard</option>
                                <option value="timeseries" selected={self.editedWidget.format=='timeseries'}>time series</option>
                            </select>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='line'}>
                        <div class="form-group col-md-12">
                            <label for="w_chartOption" class="active">{app.texts.dashboard_form.f_widget_chartOption[app.language]}</label>
                            <select class="form-control" id="w_chartOption" disabled={!allowEdit}>
                                <option value="dots" selected={self.editedWidget.chartOption=='dots'}>show dots</option>
                                <option value="plain" selected={self.editedWidget.chartOption=='plain'}>line only</option>
                                <option value="area" selected={self.editedWidget.chartOption=='area'}>area below</option>
                                <option value="areaWithDots" selected={self.editedWidget.chartOption=='areaWithDots'}>area with dots</option>
                            </select>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='symbol' || self.editedWidget.type=='led' || self.editedWidget.type=='plan' || self.editedWidget.type=='multimap'}>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_range"
                                name="w_range"
                                label={ app.texts.dashboard_form.f_widget_range[app.language] }
                                type="text"
                                content={ self.editedWidget.range }
                                readonly={ !allowEdit }
                                hint={ self.editedWidget.type=='multimap'?app.texts.dashboard_form.f_widget_range_hint_mm[app.language]:app.texts.dashboard_form.f_widget_range_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type!='text' }>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_title"
                                name="w_title"
                                label={ app.texts.dashboard_form.f_widget_title[app.language] }
                                type="text"
                                content={ self.editedWidget.title }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_title_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row" if={ self.editedWidget.type=='symbol' }>
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_icon"
                                name="w_icon"
                                label={ app.texts.dashboard_form.f_widget_icon[app.language] }
                                type="text"
                                content={ self.editedWidget.icon }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_icon_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row">
                        <div class="form-group col-md-12">
                            <label for="w_width" class="active">{app.texts.dashboard_form.f_widget_width[app.language]}</label>
                            <select class="form-control" id="w_width" disabled={!allowEdit}>
                                <option value="1" selected={self.editedWidget.width==1}>1</option>
                                <option value="2" selected={self.editedWidget.width==2}>2</option>
                                <option value="3" selected={self.editedWidget.width==3}>3</option>
                                <option value="4" selected={self.editedWidget.width==4}>4</option>
                            </select>
                        </div>
                        </div>
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_description"
                                name="w_description"
                                label={ self.editedWidget.type!='plan'?app.texts.dashboard_form.f_widget_description[app.language]:app.texts.dashboard_form.f_widget_svg[app.language] }
                                type="textarea"
                                content={ self.editedWidget.description }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_description_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="modal-footer">
                            <button type="submit" class="btn btn-primary" disabled={!allowEdit}>{app.texts.dashboard_form.save[app.language]}</button>
                            <button type="button" class="btn btn-secondary" data-dismiss="modal">{app.texts.dashboard_form.cancel[app.language]}</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    
    <!-- DASHBOARD form -->
    <div class="row">
        <form class="col-md-12" onsubmit={ self.submitForm }>
            <div class="form-row">
                <div class="form-group col-md-6">
                    <form_input 
                        id="name"
                        name="name"
                        label={ app.texts.dashboard_form.name[app.language] }
                        type="text"
                        required="true"
                        content={ dashboard.name }
                        readonly={ self.mode != 'create' }
                        hint={ app.texts.dashboard_form.name_hint[app.language] }
                        ></form_input>
                </div>
                <div class="form-group col-md-6">
                    <form_input 
                        id="shared"
                        name="shared"
                        label={ app.texts.dashboard_form.shared[app.language] }
                        type="check"
                        checked={ dashboard.shared }
                        readonly={ !allowEdit }
                        ></form_input>
                        </div>
            </div>
            <div class="row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="title"
                        name="title"
                        label={ app.texts.dashboard_form.title[app.language] }
                        type="text"
                        required="true"
                        content={ dashboard.title }
                        readonly={ !allowEdit }
                        hint={ app.texts.dashboard_form.title_hint[app.language] }
                        ></form_input>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="team"
                        name="team"
                        label={ app.texts.dashboard_form.team[app.language] }
                        type="text"
                        content={ dashboard.team }
                        readonly={ !allowEdit }
                        hint={ app.texts.dashboard_form.team_hint[app.language] }
                        ></form_input>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-12">
                    <form_input 
                        id="admins"
                        name="admins"
                        label={ app.texts.dashboard_form.admins[app.language] }
                        type="text"
                        content={ dashboard.administrators }
                        readonly={ !allowEdit }
                        hint={ app.texts.dashboard_form.admins_hint[app.language] }
                        ></form_input>
                </div>
            </div>
            <div class="form-row" if={ !allowEdit }>
                <div class="form-group col-md-12">
                    <label for="status">{ app.texts.device_form.owner[app.language] }</label>
                    <p class="form-control-static" id="owner">{dashboard.userID}</p>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-12">
                    <h4>{ app.texts.dashboard_form.widgets[app.language] } <i class="material-icons clickable" if={allowEdit} onclick={ editWidget(-1) } title="NEW WIDGET" data-toggle="modal" data-target="#widgetEdit">add</i></h4>
                    <table id="devices" class="table table-condensed table-striped">
                        <thead>
                            <tr>
                                <th>{ app.texts.dashboard_form.name[app.language].toUpperCase() }</th>
                                <th>{ app.texts.dashboard_form.type[app.language].toUpperCase() }</th>
                                <th class="text-right">{ app.texts.dashboard_form.action[app.language].toUpperCase() }</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr each={widget,index in self.dashboard.widgets}>
                                <td if={widget.modified}>* {widget.name}</td>
                                <td if={!widget.modified}>{widget.name}</td>
                                <td>{getTypeName(widget.type)}</td>
                                <td class="text-right">
                                    <i class="material-icons clickable" if={allowEdit} onclick={ moveWidgetDown(index) } title="MOVE DOWN">arrow_downward</i>
                                    <i class="material-icons clickable" if={allowEdit} onclick={ moveWidgetUp(index) } title="MOVE UP">arrow_upward</i>
                                    <i class="material-icons clickable" if={!allowEdit} onclick={ editWidget(index) } title="VIEW DEFINITION" data-toggle="modal" data-target="#widgetEdit">open_in_browser</i>
                                    <i class="material-icons clickable" if={allowEdit} onclick={ editWidget(index) } title="MODIFY" data-toggle="modal" data-target="#widgetEdit">mode_edit</i>
                                    <i class="material-icons clickable" if={allowEdit} onclick={ removeWidget(index) } title="REMOVE" data-toggle="modal" data-target="#widgetRemove">delete</i>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-12">
                    <button type="submit" class="btn btn-primary" disabled={ !allowEdit }>{ app.texts.dashboard_form.save[app.language] }</button>
                    <button type="button" class="btn btn-secondary" onclick={ close } >{ app.texts.dashboard_form.cancel[app.language] }</button>
                </div>
            </div>
        </form>
    </div>
        
    <!-- DASHBOARD form -->
    <script>
        this.visible = true
        self = this
        self.listener = riot.observable()
        self.callbackListener
        self.allowEdit = false
        self.method = 'POST'
        self.mode = 'view'
        self.channelsEncoded = ''
        self.invalidQuery = false
        self.isCopy = false
        self.dashboard = {
            'id': '',
            'name': '',
                        'title': '',
                        'userID': '',
                        'team': '',
                        'administrators': '',
                        'shared': false,
                        'widgets':[]
        }
        self.newWidget = function(){
            return {
                'name':'',
                'dev_id': '',
                'channel':'',
                'channelTranslated':'',
                'unitName':'',
                'type':'text',
                'query':'last 1',
                'range':'',
                'title':'',
                'icon': '',
                'width':1,
                'description':'',
                'queryvalue':'1',
                'format': 'standard',
                'chartOption': 'dots',
                'group': '',
                'modified': false
            }
        }
        self.selectedForRemove = - 1
        self.selectedForEdit = - 1
        self.editedWidget = {}
        self.myDevices=[]

        globalEvents.on('data:submitted', function(event){
            app.log("Submitted!")
        });
        
        init(eventListener, id, editable, isCopy){
            self.isCopy=isCopy
            self.callbackListener = eventListener
            self.allowEdit = editable
            self.method = 'POST'
            if (id != 'NEW' && !isCopy){
                readDashboard(id)
                self.method = 'PUT'
                if (self.allowEdit){
                    self.mode = 'update'
                } else {
                    self.mode = 'view'
                }
            } else if (id != 'NEW' && isCopy){
                readDashboard(id)
                id = 'NEW'
                self.method = 'POST'
                self.mode = 'create'
            } else{
                self.editedWidget = self.newWidget()
                self.mode = 'create'
            }
            readMyDevices()
            riot.update()
        }

        self.submitForm = function(e){
            e.preventDefault()
            dashboardPath = ''
                //if(e.target.elements['id']){
            dashboardPath = (self.method == 'PUT') ? '/' + self.dashboard['id'] : ''
                //}
            var formData = {id:'', name:'', title:'', userID:'', shared:false, team:'', widgets:[]}
            formData.id = self.dashboard.id
            formData.name = e.target.elements['name'].value
            formData.title = e.target.elements['title'].value
            formData.userID = app.user.name
            formData.team = e.target.elements['team'].value
            formData.administrators = e.target.elements['admins'].value
            formData.shared = e.target.elements['shared'].checked
            formData.widgets = self.dashboard.widgets
            app.log(JSON.stringify(formData))
            e.target.reset()
            sendJsonData(
                        formData,
                        self.method,
                        app.dashboardAPI + dashboardPath,
                        'Authentication',
                        app.user.token,
                        self.submitted,
                        self.listener,
                        'submit:OK',
                        'submit:ERROR',
                        app.debug,
                        globalEvents
                        )
                //self.callbackListener.trigger('submitted')
        }

        self.close = function(){
            self.callbackListener.trigger('cancelled')
        }

        self.submitted = function(){
            self.callbackListener.trigger('submitted')
        }

        var update = function (text) {
            app.log("DASHBOARD: " + text)
            self.dashboard = JSON.parse(text);
            if(self.isCopy) {
                self.dashboard.id = ''
                self.dashboard.name=self.dashboard.name+'_1'
            }
            for(i=0; i<self.dashboard.widgets.length; i++){
                self.dashboard.widgets[i].modified=false
            }
            riot.update();
        }

        var readDashboard = function (id) {
            getData(app.dashboardAPI + '/' + id,
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

        editWidget(index){
            return function(e){
                e.preventDefault()
                app.log('WIDGET:' + index)
                self.invalidQuery=false
                self.selectedForEdit = index
                if (index > - 1){
                    self.editedWidget = self.dashboard.widgets[index]
                    //if(self.editedWidget.query!='' && self.editedWidget.query.indexOf('last ')==0 && self.editedWidget.query.indexOf(' ',5)==-1){
                    //    self.editedWidget.queryvalue = self.editedWidget.query.substring(self.editedWidget.query.indexOf(' ')+1)
                    //}else if(self.editedWidget.query!=''){
                        self.editedWidget.queryvalue=self.editedWidget.query
                    //}else{
                    //    self.editedWidget.queryvalue = '1'
                    //}
                } else{
                    self.editedWidget = {'name':'', 'dev_id':'', 'channel':'',
                    'unitName':'', 'type':'text', 'query':'last', 'range':'', 
                    'title':'','icon':'', 'width':1, 'description':'', 'queryvalue':'1',
                    'format':'standard','chartOption':'dots', 'group':''}
                }
                app.log(index)
                app.log(self.editedWidget)
                self.selectedForRemove = - 1
                riot.update()
            }
        }
        
        self.changeType = function(e){
            e.preventDefault()
            self.editedWidget.type = e.target.value
            riot.update()
        }
        
        self.parseDQL=function(text){
            var dql=text.trim()
            if(dql=='' || dql=='1' || dql=='last') return true
            var params=dql.split(' ')
            for(i=0; i<params.length;){
                switch(params[i]){
                    case "last":
                    case "average":
                        if( params.length<(i+2) || !Number.isInteger(parseInt(params[i+1])) ){
                            return false
                        }
                        i=i+2;
                        break;
                    case "project":
                    case "state":
                    case "channel":
                    case "group":
                        if(params.length<i+2) return false
                        i=i+2;
                        break;
                    case "timeseries":
                        i=i+1;
                        break;
                    default:
                        return false
                }
            }
            return true
        }
        
        self.saveWidget = function(e){
            e.preventDefault()
            var query=''
            try{
                query=e.target.elements['w_query'].value
            }catch(err){}
            if(query!='' && !self.parseDQL(query)){
                self.invalidQuery=true
                return
            }
            $('#widgetEdit').modal('hide');
            //
            self.editedWidget.name = e.target.elements['w_name'].value
            try{
                self.editedWidget.dev_id = e.target.elements['w_dev_id'].value.replace(/\s+/g,'')
            }catch(err){
                self.editedWidget.dev_id = ''
            }
            try{
                self.editedWidget.group = e.target.elements['w_group'].value.replace(/\s+/g,'')
            }catch(err){
                self.editedWidget.group = ''
            }
            try{
                self.editedWidget.channel = e.target.elements['w_channel'].value.replace(/\s+/g,'')
            }catch(err){
                self.editedWidget.channel = ''
            }
            try{
                self.editedWidget.channelTranslated = e.target.elements['w_channel_translated'].value.replace(/\s+/g,'')
            }catch(err){
                self.editedWidget.channelTranslated = ''
            }
            try{
                self.editedWidget.unitName = e.target.elements['w_unit'].value
            }catch(err){
                self.editedWidget.unitName = ''
            }
            try{
                var n=parseInt(e.target.elements['w_query'].value.trim())
                if(Number.isInteger(n)){
                    self.editedWidget.query = 'last '+n
                }else{
                    self.editedWidget.query = e.target.elements['w_query'].value.trim()
                }
            }catch(err){
                self.editedWidget.query = 'last 1'
            }
            try{
                self.editedWidget.range = e.target.elements['w_range'].value
            }catch(err){
                self.editedWidget.range = ''
            }
            try{
                self.editedWidget.format = e.target.elements['w_format'].value
            }catch(err){
                self.editedWidget.format = 'standard'
            }
            try{
                self.editedWidget.chartOption = e.target.elements['w_chartOption'].value
            }catch(err){
                self.editedWidget.chartOption = 'dots'
            }
            self.editedWidget.type = e.target.elements['w_type'].value
            try{    
                self.editedWidget.title = e.target.elements['w_title'].value
            }catch(err){
                self.editedWidget.title = ''
            }
            try{    
                self.editedWidget.icon = e.target.elements['w_icon'].value
            }catch(err){
                self.editedWidget.icon = ''
            }
            self.editedWidget.width = parseInt(e.target.elements['w_width'].value, 10)
            self.editedWidget.description = e.target.elements['w_description'].value
                
            if(self.editedWidget.width == null || isNaN(self.editedWidget.width) || self.editedWidget.width<1 || self.editedWidget.width >4){
                self.editedWidget.width = 1
            }else{
                app.log("WIDTH:"+self.editedWidget.width)
            }
            self.editedWidget.modified=true
            delete self.editedWidget.queryvalue
            //
            if (self.selectedForEdit > - 1){
                self.dashboard.widgets[self.selectedForEdit] = self.editedWidget
            } else{
                self.dashboard.widgets.push(self.editedWidget)
            }
            self.selectedForEdit = - 1
            self.editedWidget = self.newWidget()
            e.target.reset()
            riot.update()
        }

        removeWidget(index){
            return function(e){
                e.preventDefault()
                app.log('WIDGET:' + index)
                self.selectedForRemove = index
            }
        }
        moveWidgetUp(index){
            return function(e){
                e.preventDefault()
                if (index > 0){
                    self.dashboard.widgets.splice(index-1, 0, self.dashboard.widgets.splice(index, 1)[0]);
                }
                riot.update()
            }
        }
        moveWidgetDown(index){
            return function(e){
                e.preventDefault()
                if (index < self.dashboard.widgets.length-1){
                    self.dashboard.widgets.splice(index+1, 0, self.dashboard.widgets.splice(index, 1)[0]);
                }
                riot.update()
            }
        }
        confirmRemoveWidget(index){
            return function(e){
                e.preventDefault()
                $('#widgetRemove').modal('hide');
                self.dashboard.widgets.splice(index, 1)
                self.selectedForRemove = - 1
                riot.update()
            }
        }

        self.getTypeName = function(name){
            switch (name){
                case 'symbol':
                    return app.texts.dashboard_form.type_symbol[app.language]
                    break
                case 'text':
                    return app.texts.dashboard_form.type_text[app.language]
                    break
                case 'raw':
                    return app.texts.dashboard_form.type_raw[app.language]
                    break
                case 'gauge':
                    return app.texts.dashboard_form.type_gauge[app.language]
                    break
                case 'line':
                    return app.texts.dashboard_form.type_line[app.language]
                    break
                case 'stepped':
                    return app.texts.dashboard_form.type_stepped[app.language]
                    break
                case 'button':
                    return app.texts.dashboard_form.type_button[app.language]
                    break
                case 'map':
                    return app.texts.dashboard_form.type_map[app.language]
                    break
                case 'multimap':
                    return app.texts.dashboard_form.type_multimap[app.language]
                    break
                case 'date':
                    return app.texts.dashboard_form.type_date[app.language]
                    break
                case 'led':
                    return app.texts.dashboard_form.type_led[app.language]
                    break
                case 'plan':
                    return app.texts.dashboard_form.type_plan[app.language]
                    break
                case 'report':
                    return app.texts.dashboard_form.type_report[app.language]
                    break
                case 'stopwatch':
                    return app.texts.dashboard_form.type_stopwatch[app.language]
                    break
                case 'time':
                    return app.texts.dashboard_form.type_time[app.language]
                    break
                case 'devinfo':
                    return app.texts.dashboard_form.type_devinfo[app.language]
                    break
                case 'openweather':
                    return app.texts.dashboard_form.type_openweather[app.language]
                    break
                default:
                    return name
            }
        }
        
        var readMyDevices = function() {
                getData(app.iotAPI,
                    null,
                    app.user.token,
                    updateMyDevices,
                    self.listener, //globalEvents
                    'OK',
                    null, // in case of error send response code
                    app.debug,
                    globalEvents
                    );
        }

        var updateMyDevices = function (text) {
            self.myDevices = JSON.parse(text);
            riot.update();
        }

    </script>
</app_dashboard_form>