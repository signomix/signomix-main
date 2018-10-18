<app_mydevices>
    <div class="row" if={ selected }>
         <div class="col-md-12">
            <app_device_form ref="dev_edit"></app_device_form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <h2 class="module-title">{app.texts.mydevices.devices[app.language]}
                <i class="material-icons clickable" onclick={ refresh() }>refresh</i>
                <i class="material-icons clickable" onclick={ editDevice('NEW', true) }>add</i>
            </h2>
            <table id="devices" class="table table-condensed">
                <thead>
                    <tr>
                        <th>{app.texts.mydevices.header_eui[app.language]}</th>
                        <th>{app.texts.mydevices.header_name[app.language]}</th>
                        <th>{app.texts.mydevices.header_type[app.language]}</th>
                        <th>{app.texts.mydevices.header_status[app.language]}</th>
                        <th class="text-right">{app.texts.mydevices.header_action[app.language]}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr each={device in myDevices}>
                        <td>{device.EUI}</td>
                        <td>{device.name}</td>
                        <td>{device.type}</td>
                        <td><img height="16px" style="margin-right: 10px;" src={ getStatus(device.lastSeen, device.transmissionInterval) }></td>
                        <td class="text-right">
                            <i class="material-icons clickable" onclick={ editDevice(device.EUI, false) }>open_in_browser</i>
                            <i class="material-icons clickable" onclick={ editDevice(device.EUI, true) }>mode_edit</i>
                            <i class="material-icons clickable" onclick={ selectForRemove(device.EUI) } data-toggle="modal" data-target="#myModal">delete</i>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
    <!-- Modal -->
        <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modalLabel">{app.texts.mydevices.remove_title[app.language]}</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label={app.texts.mydevices.cancel[app.language]}>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p><b>{ selectedForRemove }</b></p>
                        <p>{app.texts.mydevices.remove_question[app.language]}</p>
                        <p class="text-danger">{app.texts.mydevices.remove_info[app.language]}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">{app.texts.mydevices.cancel[app.language]}</button>
                        <button type="button" class="btn btn-primary" onclick={ removeDevice() } data-dismiss="modal">{app.texts.mydevices.remove[app.language]}</button>
                    </div>
                </div>
            </div>
        </div>
        <script charset="UTF-8">
        var self = this
        self.devListener = riot.observable()
        //self.userListener = riot.observable()
        self.info = {}
        self.myDevices = []
        self.selected = ''
        self.selectedForRemove = ''
        self.edited = false
        self.now = Date.now()
        
        this.on('mount',function(){
            self.selected = ''
            self.edited = false
            //readMyAccountData()
            readMyDevices()
        })

        self.devListener.on('*', function (eventName) {
            switch (eventName){
                case 'submitted':
                    //console.log('after submit device')
                    self.selected = ''
                    readMyDevices()  //this line results in logout,login error
                    break
                case 'cancelled':
                    self.selected = ''
                    break
                default:
                    app.log('ACCOUNT: error ' + eventName)
            }
            riot.update()
        });
        
        getStatus(lastSeen, interval){
            if(self.now-lastSeen>interval){
                return '/images/KO.svg'
            }else{
                return '/images/OK.svg'
            }
        }
        
        editDevice(devEUI, allowEdit){
            return function(e){
                e.preventDefault()
                self.selected=devEUI
                riot.update()
                app.log('SELECTED FOR EDITING: '+devEUI)
                self.refs.dev_edit.init(self.devListener, devEUI, allowEdit)
            }
        }
                
        selectForRemove(devEUI){
            return function(e){
                e.preventDefault()
                self.selectedForRemove=devEUI
                riot.update()
                app.log('SELECTED FOR REMOVE: '+devEUI)
            }
        }
        
        removeDevice(){
            return function(e){
                console.log('REMOVING ... '+self.selectedForRemove)
                deleteData( 
                    app.iotAPI+'/'+self.selectedForRemove, 
                    app.user.token, 
                    self.afterRemove, 
                    null, //self.listener, 
                    'submit:OK', 
                    'submit:ERROR', 
                    app.debug, 
                    null //globalEvents
                )
            }
        }
        
        self.afterRemove = function(object){
            self.selectedForRemove = ''
            readMyDevices()
        }
        
        refresh(){
            return function(e){
                e.preventDefault()
                readMyDevices()
            }
        }
        
        var readMyDevices = function() {
                app.log('reading devices ...')
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
            app.log("ACCOUNT: " + text)
            self.myDevices = JSON.parse(text);
            riot.update();
        }

    </script> 
</app_mydevices>
