<app_documentation>
    <div class="row toc-top">
        <div class="col-md-12 text-left">
            <a href="#!doc,toc">{app.texts.documentation.TOC[app.language]}</a>
        </div>
    </div>
    <div class="row" >
        <div class="col-md-12">
            <cs_article class="container" ref="art1" path={ app.docPath } language={ app.language }></cs_article>
        </div>
    </div>
    <div class="row toc-bottom">
        <div class="col-md-12 text-left">
            <a href="#!doc,toc">{app.texts.documentation.TOC[app.language]}</a>
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
        
        globalEvents.on('pageselected:doc',function(event){
                app.log("Load documents again?")
                riot.update()
                self.loadDocuments()
        });
        globalEvents.on('language',function(){
            self.loadDocuments()
        })
        self.loadDocuments = function(){
            app.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }
    </script>
    <style>
        .toc-top{
            border-bottom-color: darkgray;
            border-bottom-style: solid;
            border-bottom-width: 1px;
            margin-bottom: 0rem;
        }
        .toc-bottom{
            border-top-color: darkgray;
            border-top-style: solid;
            border-top-width: 1px;
            margin-top: 0rem;
        }
    </style>
</app_documentation>