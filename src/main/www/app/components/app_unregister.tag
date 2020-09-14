<app_unregister>
    <div class="row" >
        <div class="col-md-12">
            <cs_article ref="unregisterart" path='/unregistered' language={ app.language }></cs_article>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        
        this.on('unmount',function(){
            app.log('UNREGISTER UNMOUNT')
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].unmount()
            });
            self.refs=[]
        })
        this.on('mount',function(){
            app.log('UNREGISTER MOUNT')
            sendLogout();
        })
        
        sendLogout = function () {
            deleteData(app.authAPI+'/'+app.user.token,
                    app.user.token,
                    self.loadArticles,
                    globalEvents, //globalEvents
                    'OK',
                    null, // in case of error send response code
                    app.debug,
                    globalEvents
                    );
        }
        
        self.loadArticles = function(event){
            app.user.name=null;
            app.user.token=null;
            app.user.status='logged-out';
            //app.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
            riot.update()
        }

    </script>
</app_unregister>