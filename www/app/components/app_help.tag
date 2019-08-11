<app_help>
    <div class="row text-right">
        <div class="col-md-12 topspacing">
            <a href="#!help,toc">Index</a>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <cs_article class="container" ref="art1" path={ app.docPath } language={ app.language }></cs_article>
        </div>
    </div>
    <div class="row text-right">
        <div class="col-md-12">
            <a href="#!help,toc">Index</a>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this;
        
        this.on('unmount',function(){
            app.log('ABOUT UNMOUNT')
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].unmount()
            });
            self.refs=[]
        })
        this.on('mount',function(){
            app.log('ABOUT MOUNT')
            app.log('Load documents')
            self.loadDocuments()
        })
        
        globalEvents.on('pageselected:help',function(event){
                app.log("Load documents again?")
                riot.update()
                self.loadDocuments()
        });
 
        self.loadDocuments = function(){
            app.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }
    </script>
</app_help>