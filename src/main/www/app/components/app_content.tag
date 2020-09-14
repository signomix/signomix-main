<app_content>
    <main class="background-image">
        <div  class="container white" if={ infoToShow }>
              <div class="row">
                <div class="col-md-12">
                    <div class="card red lighten-1 text-center z-depth-2">
                        <div class="card-body">
                            <p class="white-text mb-0">{app.texts.content.message[app.language]}</p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='documents'}><app_documentation/></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='blog'}><app_articles/></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='help'}><app_help/></div>
        <div  class="container-fluid topspacing" if={ app.currentPage=='main'}><app_main></app_main></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='login'}><app_login></app_login></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='logout'}><app_logout></app_logout></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='register'}><app_register></app_register></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='resetPassword'}><app_reset_password></app_reset_password></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='unregister'}><app_unregister></app_unregister></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='dashboard'}><app_dashboard2></app_dashboard2></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='account'}><app_account></app_account></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='mydevices'}><app_mydevices></app_mydevices></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='mydashboards'}><app_mydashboards></app_mydashboards></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='alerts'}><app_alerts></app_alerts></div>
        <div  class="container-fluid topspacing white" if={ app.currentPage=='subscribe'}><app_subscribe></app_subscribe></div>
    </main>
    <script>
        var self = this
        self.infoToShow = false
        globalEvents.on('pageselected', function (event) {
            self.infoToShow = false
            riot.update()
        })
        globalEvents.on('err:401', function (eventName) {
            self.infoToShow = true
            riot.update()
        })
        
    </script>
</app_content>