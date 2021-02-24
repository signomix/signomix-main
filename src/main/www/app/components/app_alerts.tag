<app_alerts>
    <div class="row" >
        <div class="col-md-12">
            <h2 class="module-title">{ app.texts.alerts.title[app.language] }
                <i class="material-icons clickable" onclick={ refresh() }>refresh</i>&nbsp;
                <i class="material-icons clickable" data-toggle="modal" data-target="#myModal">delete</i>
            </h2>
            <table id="alertlist" class="table table-condensed table-striped">
                <tr>
                    <th>{ app.texts.alerts.type[app.language] }</th>
                    <th>{ app.texts.alerts.message[app.language] }</th>
                    <th>{ app.texts.alerts.eui[app.language] }</th>
                    <th>{ app.texts.alerts.date[app.language] }</th>
                    <th class="text-right">{ app.texts.alerts.action[app.language] }</th>
                </tr>
                <tr each={alert in app.user.alerts}>
                    <td>{ alert.type }</td>
                    <td>{ alert.payload }</td>
                    <td>{ alert.deviceEUI }</td>
                    <td>{ (new Date(alert.createdAt)+'').slice(0,24) }</td>
                    <td class="text-right">
                        <i class="material-icons clickable" onclick={ removeAlert(alert.origin,alert.id) }>delete</i>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <!-- Modal remove-->
        <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modalLabel">{app.texts.alerts.remove_title[app.language]}</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label={app.texts.alerts.cancel[app.language]}>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p>{app.texts.alerts.remove_question[app.language]}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-primary" onclick={ removeAllAlerts() } data-dismiss="modal">{app.texts.alerts.remove[app.language]}</button>
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">{app.texts.alerts.cancel[app.language]}</button>
                    </div>
                </div>
            </div>
        </div>
    <script charset="UTF-8">
        var self = this;
        self.listener = riot.observable();
        
        this.on('mount',function(){
            readAlerts()
        })

        self.listener.on('*', function (eventName) {
            app.log('ALERTS: ' + eventName)
        });
        
        refresh(){
            return function(e){
                e.preventDefault()
                readAlerts()
            }
        }

        var readAlerts = function () {
            getData(app.alertAPI + "/",  // url
                    null,                // query
                    app.user.token,      // token
                    updateMyAlerts,        // callback
                    self.listener,       // event listener
                    'OK',                // success event name
                    null,                // error event name
                    app.debug,           // debug switch
                    globalEvents         // application event listener
                    );
        }

        var updateMyAlerts = function (text) {
            app.user.alerts = JSON.parse(text)
            globalEvents.trigger('alerts:updated')
            riot.update();
        }
        
        removeAlert(origin,id){
            return function(e){
                e.preventDefault()
                deleteData(app.alertAPI + "/"+id,  // url
                    app.user.token,      // token
                    null,        // callback
                    self.listener,       // event listener
                    'OK',                // success event name
                    'cannotRemove',                // error event name
                    app.debug,           // debug switch
                    null         // application event listener
                    );
                readAlerts()
            }
        }
        
        removeAllAlerts(origin,id){
            return function(e){
                e.preventDefault()
                deleteConditional(
                    {'user':app.user.name}, 
                    app.alertAPI+'/*',  // url
                    app.user.token,      // token
                    null,        // callback
                    self.listener,       // event listener
                    'OK',                // success event name
                    'cannotRemove',                // error event name
                    app.debug,           // debug switch
                    null         // application event listener
                    );
                readAlerts()
            }
        }

    </script>
</app_alerts>