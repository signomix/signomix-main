<app_mainarticle>
    <div class="container">
        <div class="row">
            <div class="col-md-12">
                <cs_article class="container" ref="homeart" path="/landingpage/article" eventname="appMainArticleOK" erroreventname="appMainArticleErr" language={ app.language }></cs_article>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        self.refs=[]
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
        self.loadDocuments = function(){
            self.messageVisible=true
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }  
    </script>
</app_mainarticle>
