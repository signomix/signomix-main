<app_alerts>
    <div class="row" >
        <div class="col-md-12">
            <h2 class="module-title">{ app.texts.alerts.title[app.language] }
                <i class="material-icons clickable" onclick={ refresh() }>refresh</i>
            </h2>
            <table id="alertlist" class="table table-condensed">
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

    </script>
</app_alerts>