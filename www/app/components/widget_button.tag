<widget_button>
    <div id={opts.ref} if={ type == 'button' } class="card text-center topspacing p-0">
        <div class="card-body text-center" if={ !app.user.guest }>
            <button type="button" class="btn btn-danger btn-block" data-toggle="modal" data-target="#myModal">{ title }</button>
        </div>
        <div class="card-body text-center" if={ app.user.guest }>
            <p>---</p>
        </div>
    </div>
    <div id="myModal" class="modal fade" if={ type == 'button' }>
        <div class="modal-dialog modal-sm">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">{ title }</h4>
                </div>
                <div class="modal-body">
                    <p>{ app.texts.widget_a1.device[app.language] } { dev_id }</p>
                    <p>{ app.texts.widget_a1.newvalue[app.language] } { channel }</p>
                    <input type='text' value="0" name="newvalue" id='newvalue2set'>
                    <p>{ description }</p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary pull-right" data-dismiss="modal">{ app.texts.widget_a1.cancel[app.language] }</button>
                    <button type="button" class="btn btn-primary" data-dismiss="modal" onclick={ sendReset() }>{ app.texts.widget_a1.save[app.language] }</button>
                </div>
            </div>
        </div>
    </div>

    <script>
    var self = this
    // object fields (title,device_id,channel ...) are set by running update() from app_dashboard2.tag
    
    self.show2 = function(){
        self.value = ''
    }
            
    self.listener = riot.observable()
    self.listener.on('*',function(eventName){
        app.log("widget_a1 listener on event: "+eventName)
    })
    
    self.submitted = function(){
        app.log('submitted')
    }
    
    self.getCommand = function(text){
        return text.substring(text.indexOf('[')+1,text.indexOf(']'))
    }
    
    self.getHexValue = function(){
        
    }
    
    sendReset(){
        return function(e){
            e.preventDefault()
            var value = document.getElementById('newvalue2set').value //TODO: change
            var dataToSend = {}
            dataToSend['value'] = parseInt(value)
            dataToSend['command'] = self.getCommand(self.channel)
            console.log(dataToSend)
            sendJsonData(
                dataToSend, 
                'POST', 
                app.actuatorAPI+'/'+self.dev_id,
                'Authentication',
                app.user.token, 
                self.submitted, 
                self.listener, 
                'submit:OK', 
                'submit:ERROR', 
                app.debug, 
                globalEvents
            )
        }
    }
        
    </script>
    <style>
        .topspacing{
            margin-top: 10px;
        }
       
    </style>
</widget_button>