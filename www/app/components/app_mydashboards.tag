<app_mydashboards>
    <div class="row" if={ selected }>
        <div class="col-md-12">
            <app_dashboard_form ref="d_edit"></app_dashboard_form>
        </div>
    </div>
    <div class="row" if={ !selected }>
        <div class="col-md-12">
            <h2 class="module-title">{app.texts.mydashboards.pagetitle[app.language]}
                <i class="material-icons clickable" onclick={ refresh() }>refresh</i>
                <i class="material-icons clickable" onclick={ editDefinition('NEW', true) }>add</i>
            </h2>
            <table class="table table-condensed">
                <tr>
                    <th>{app.texts.mydashboards.title[app.language]}</th>
                    <th class="text-right">{app.texts.mydashboards.action[app.language]}</th>
                </tr>
                <tr each={ dashboard in definitions }>
                    <td><a href="#dashboard,{dashboard.id}">{ dashboard.title }</a></td>
                    <td class="text-right">
                        <i class="material-icons clickable" onclick={ editDefinition(dashboard.id, false) } if={dashboard.userID==app.user.name}>open_in_browser</i>
                        <i class="material-icons clickable" onclick={ editDefinition(dashboard.id, true) } if={dashboard.userID==app.user.name}>mode_edit</i>
                        <i class="material-icons clickable" onclick={ selectForRemove(dashboard.id) } if={dashboard.userID==app.user.name} data-toggle="modal" data-target="#myModal">delete</i>
                    </td>
                </tr>
            </table>
        </div>
    </div>
    <!-- Modal -->
        <div class="modal fade" id="myModal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
            <div class="modal-dialog" role="document">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title" id="modalLabel">{app.texts.mydashboards.remove_title[app.language]}</h5>
                        <button type="button" class="close" data-dismiss="modal" aria-label={app.texts.mydashboards.cancel[app.language]}>
                            <span aria-hidden="true">&times;</span>
                        </button>
                    </div>
                    <div class="modal-body">
                        <p><b>{ getDashboardName(selectedForRemove) }</b></p>
                        <p>{app.texts.mydashboards.remove_question[app.language]}</p>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-dismiss="modal">{app.texts.mydashboards.cancel[app.language]}</button>
                        <button type="button" class="btn btn-primary" onclick={ removeDashboard() } data-dismiss="modal">{app.texts.mydashboards.remove[app.language]}</button>
                    </div>
                </div>
            </div>
        </div>
    <script charset="UTF-8">
        var self = this;
        self.listener = riot.observable();
        self.definitions = []
        self.selected = ''
        self.selectedForRemove = ''
        
        this.on('mount',function(){
            readDefinitions()
        })

        self.listener.on('*', function (eventName) {
            app.log('MY DASHBOARDS: ' + eventName)
            switch (eventName){
                case 'submitted':
                    //console.log('after submit device')
                    self.selected = ''
                    readDefinitions()  //this line results in logout,login error
                    break
                case 'cancelled':
                    self.selected = ''
                    break
                default:
                    app.log('DASHBOARD: error ' + eventName)
            }
            riot.update()
        });
        
        refresh(){
            return function(e){
                e.preventDefault()
                readDefinitions()
            }
        }
        
        editDefinition(id, allowEdit){
            return function(e){
                e.preventDefault()
                self.selected=id
                riot.update()
                app.log('SELECTED FOR EDITING: '+id)
                self.refs.d_edit.init(self.listener, id, allowEdit)
            }
        }
        
        selectForRemove(dashboardId){
            return function(e){
                e.preventDefault()
                self.selectedForRemove=dashboardId
                riot.update()
                app.log('SELECTED FOR REMOVE: '+dashboardId)
            }
        }
        
        removeDashboard(){
            return function(e){
                console.log('REMOVING ... '+self.selectedForRemove)
                deleteData( 
                    app.dashboardAPI+'/'+self.selectedForRemove, 
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
            readDefinitions()
        }
        
        var readDefinitions = function () {
            getData(app.dashboardAPI + "/",  // url
                    null,                // query
                    app.user.token,      // token
                    updateMyDefinitions,        // callback
                    self.listener,       // event listener
                    'OK',                // success event name
                    null,                // error event name
                    app.debug,           // debug switch
                    globalEvents         // application event listener
                    );
        }

        var updateMyDefinitions = function (text) {
            self.definitions = JSON.parse(text)
            app.user.dashboards = self.definitions
            riot.update();
        }
        
        getDashboardName(id){
            return id.substring(id.indexOf('~')+1)
        }

    </script>
</app_mydashboards>