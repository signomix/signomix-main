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
                            <select class="form-control" id="w_type" disabled={!allowEdit}>
                                <option value="text" selected={self.editedWidget.type=='text'}>{self.getTypeName('text')}</option>
                                <option value="symbol" selected={self.editedWidget.type=='symbol'}>{self.getTypeName('symbol')}</option>
                                <option value="raw" selected={self.editedWidget.type=='raw'}>{self.getTypeName('raw')}</option>
                                <option value="line" selected={self.editedWidget.type=='line'}>{self.getTypeName('line')}</option>
                                <option value="stepped" selected={self.editedWidget.type=='stepped'}>{self.getTypeName('stepped')}</option>
                                <option value="button" selected={self.editedWidget.type=='button'}>{self.getTypeName('button')}</option>
                                <option value="map" selected={self.editedWidget.type=='map'}>{self.getTypeName('map')}</option>
                                <option value="date" selected={self.editedWidget.type=='date'}>{self.getTypeName('date')}</option>
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
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_dev_id"
                                name="w_dev_id"
                                label={ app.texts.dashboard_form.f_widget_deviceid[app.language] }
                                type="text"
                                content={ self.editedWidget.dev_id }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_deviceid_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row">
                        <div class="form-group col-md-12">
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
                        </div>
                        <div class="row">
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
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_query"
                                name="w_query"
                                label={ app.texts.dashboard_form.f_widget_query[app.language] }
                                type="text"
                                content={ self.editedWidget.query }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_query_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_range"
                                name="w_range"
                                label={ app.texts.dashboard_form.f_widget_range[app.language] }
                                type="text"
                                content={ self.editedWidget.range }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_range_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row">
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
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_width"
                                name="w_width"
                                label={ app.texts.dashboard_form.f_widget_width[app.language] }
                                type="text"
                                content={ self.editedWidget.width }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_width_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="row">
                        <div class="form-group col-md-12">
                            <form_input 
                                id="w_description"
                                name="w_description"
                                label={ app.texts.dashboard_form.f_widget_description[app.language] }
                                type="textarea"
                                content={ self.editedWidget.description }
                                readonly={ !allowEdit }
                                hint={ app.texts.dashboard_form.f_widget_description_hint[app.language] }
                            ></form_input>
                        </div>
                        </div>
                        <div class="modal-footer">
                            <button type="submit" class="btn btn-primary" disabled={!allowEdit}>{app.texts.dashboard_form.save[app.language]}</button>
                            <button type="button" class="btn btn-default" data-dismiss="modal">{app.texts.dashboard_form.cancel[app.language]}</button>
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
                    <h4>{ app.texts.dashboard_form.widgets[app.language] } <i class="material-icons clickable" if={allowEdit} onclick={ editWidget(-1) } data-toggle="modal" data-target="#widgetEdit">add</i></h4>
                    <table id="devices" class="table table-condensed">
                        <thead>
                            <tr>
                                <th>{ app.texts.dashboard_form.name[app.language].toUpperCase() }</th>
                                <th>{ app.texts.dashboard_form.type[app.language].toUpperCase() }</th>
                                <th class="text-right">{ app.texts.dashboard_form.action[app.language].toUpperCase() }</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr each={widget,index in self.dashboard.widgets}>
                                <td>{widget.name}</td>
                                <td>{getTypeName(widget.type)}</td>
                                <td class="text-right">
                                    <i class="material-icons clickable" if={allowEdit} onclick={ moveWidgetDown(index) }>arrow_downward</i>
                                    <i class="material-icons clickable" if={allowEdit} onclick={ moveWidgetUp(index) }>arrow_upward</i>
                                    <i class="material-icons clickable" if={!allowEdit} onclick={ editWidget(index) } data-toggle="modal" data-target="#widgetEdit">open_in_browser</i>
                                    <i class="material-icons clickable" if={allowEdit} onclick={ editWidget(index) } data-toggle="modal" data-target="#widgetEdit">mode_edit</i>
                                    <i class="material-icons clickable" if={allowEdit} onclick={ removeWidget(index) } data-toggle="modal" data-target="#widgetRemove">delete</i>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="row">
                <div class="form-group col-md-12">
                    <button type="submit" class="btn btn-primary" disabled={ !allowEdit }>{ app.texts.dashboard_form.save[app.language] }</button>
                    <button type="button" class="btn btn-default" onclick={ close } >{ app.texts.dashboard_form.cancel[app.language] }</button>
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
                self.dashboard = {
                'id': '',
                        'name': '',
                        'title': '',
                        'userID': '',
                        'team': '',
                        'shared': false,
                        'widgets':[]
                }
        self.newWidget = function(){
            return {
                'name':'',
                'dev_id': '',
                'channel':'',
                'unitName':'',
                'type':'',
                'query':'last',
                'range':'',
                'title':'',
                'width':1,
                'description':''
            }
        }
        self.selectedForRemove = - 1
                self.selectedForEdit = - 1
                self.editedWidget = {}

        globalEvents.on('data:submitted', function(event){
        app.log("I'm happy!")
        });
        init(eventListener, id, editable){
        self.callbackListener = eventListener
                self.allowEdit = editable
                self.method = 'POST'
                if (id != 'NEW'){
        readDashboard(id)
                self.method = 'PUT'
                if (self.allowEdit){
        self.mode = 'update'
        } else{
        self.mode = 'view'
        }
        } else{
        self.editedWidget = self.newWidget()
                self.mode = 'create'

        }
        }

        self.submitForm = function(e){
            e.preventDefault()
            dashboardPath = ''
                //if(e.target.elements['id']){
            dashboardPath = (self.method == 'PUT') ? '/' + self.dashboard['id'] : ''
                //}
            var formData = {name:'', title:'', userID:'', shared:false, team:'', widgets:[]}
            //formData.id = e.target.elements['id'].value
            formData.name = e.target.elements['name'].value
            formData.title = e.target.elements['title'].value
            formData.userID = app.user.name
            formData.team = e.target.elements['team'].value
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
                self.selectedForEdit = index
                if (index > - 1){
                    self.editedWidget = self.dashboard.widgets[index]
                } else{
                    self.editedWidget = {'name':'', 'dev_id':'', 'channel':'',
                    'unitName':'', 'type':'', 'query':'last', 'range':'', 'title':'', 'width':1, 'description':''}
                }
                console.log(index)
                console.log(self.editedWidget)
                self.selectedForRemove = - 1
                riot.update()
            }
        }
        self.saveWidget = function(e){
            e.preventDefault()
            $('#widgetEdit').modal('hide');
            //
            self.editedWidget.name = e.target.elements['w_name'].value
                self.editedWidget.dev_id = e.target.elements['w_dev_id'].value
                self.editedWidget.channel = e.target.elements['w_channel'].value
                self.editedWidget.unitName = e.target.elements['w_unit'].value
                self.editedWidget.query = e.target.elements['w_query'].value
                self.editedWidget.range = e.target.elements['w_range'].value
                self.editedWidget.type = e.target.elements['w_type'].value
                self.editedWidget.title = e.target.elements['w_title'].value
                self.editedWidget.width = parseInt(e.target.elements['w_width'].value, 10)
                self.editedWidget.description = e.target.elements['w_description'].value
                
            if(self.editedWidget.width == null || isNaN(self.editedWidget.width) || self.editedWidget.width<1 || self.editedWidget.width >4){
                self.editedWidget.width = 1
            }else{
                console.log("WIDTH:"+self.editedWidget.width)
            }
            if(self.editedWidget.query == null || self.editedWidget.query == ''){
                console.log("MALFORMED QUERY:"+self.editedWidget.query)
                self.editedWidget.query = 'last'
            }else{
                console.log("QUERY:"+self.editedWidget.query)
            }
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
                case 'date':
                    return app.texts.dashboard_form.type_date[app.language]
                    break
                default:
                    return name
            }
        }

    </script>
</app_dashboard_form>