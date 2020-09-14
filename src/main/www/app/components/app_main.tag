<app_main>
    <div class="row">
        <div class="col-md-12">
            <cs_article class="container" ref="homeart" path='/home' language={ app.language }></cs_article>
        </div>
    </div>
    <div class="row">
        <div class="col-md-12">
            <div class="text-center" if={app.user.status == 'logged-in' && !app.user.guest}>
                <a href="#!mydashboards" onclick={ goto('#!mydashboards') } >{ app.texts.main.mydashboards[app.language] }</a><br>
                <a href="#!mydevices" onclick={ goto('#!mydevices') } >{ app.texts.main.mydevices[app.language] }</a><br>
            </div>
            <div class="text-center" if={app.user.status == 'logged-in' && app.user.guest}>
                <a href="/">{ app.texts.main.mainpage[app.language] }</a>
            </div>
            <div class="text-center" if={app.user.status != 'logged-in' }>
                <a class="nav-link text-signo" href="#!login" onclick={ goto('#!login') }>{ app.texts.main.login[app.language] }</a><br>
                <a href="/">{ app.texts.main.mainpage[app.language] }</a>
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
            //app.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }
        goto(address){
            return function(e){
                app.log(address)
                document.location = address
            }
        }
        
    </script>
</app_main>