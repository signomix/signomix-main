<app_messagebox>
    <div class="container" show={ messageVisible }>
        <div class="row">
            <div class="col-md-12">
                <div class="card border border-0">
                <cs_article class="container" ref="homeart" path="/landingpage/message" eventname="appMessageboxOK" erroreventname="appMessageboxErr" language={ app.language }></cs_article>
                </div>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        self.refs=[]
        self.messageVisible=false
        this.on('unmount',function(){
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].unmount()
            });
            self.refs=[]
        })
        this.on('mount',function(){
            self.loadDocuments()
        })
        globalEvents.on('language',function(){
            self.loadDocuments()
        })
        globalEvents.on('appMessageboxErr',function(){
            self.messageVisible=false
            riot.update()
        })
        globalEvents.on('appMessageboxOK',function(){
            self.messageVisible=true
            riot.update()
        })
        self.loadDocuments = function(){
            //console.log(self.refs)
            self.messageVisible=true
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }
          
    </script>
</app_messagebox>
