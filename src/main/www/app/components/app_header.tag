<app_header>
    <header>
        <!--Navbar-->
        <nav class="navbar navbar-expand-lg navbar-light bg-light border-bottom fixed-top">
            <a class="navbar-brand text-signo" href={ (app.user.roles.indexOf("guest")>-1)?'/':'#!' }>
                <img src="resources/logo.png" height="32px" style="margin-right:0.5em;"><strong>{ getDistroType()}</strong>
            </a>
            <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarNavDropdown" aria-controls="navbarNavDropdown" aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <!--<span hidden={ app.requests<=0 }><spinner/></span>-->
            <div class="collapse navbar-collapse" id="navbarNavDropdown">
                <ul class="navbar-nav mr-auto">
<!--<li class="nav-item" if={app.user.status == 'logged-in' && app.user.roles.indexOf("admin")>-1}><a class="nav-link text-signo" href="/admin" data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.header.admin[app.language] }</a></li>-->
                    <li class="nav-item">
                        <a class="nav-link text-signo" href="#!" onclick={ goto('#!') } data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.main.mainpage[app.language] }</a>
                    </li>
                    <li class="nav-item" if={app.user.status == 'logged-in' && !app.user.guest && app.user.alerts }>
                        <a class="nav-link text-signo" href="#!alerts" onclick={ goto('#!alerts') } data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.header.alerts[app.language] } <span if={ app.user.alerts.length>0 } class="badge badge-pill badge-danger">{app.user.alerts.length}</span></a>
                    </li>
                    <li class="nav-item" if={app.user.status == 'logged-in' && !app.user.guest }>
                        <a class="nav-link text-signo" href="#!mydashboards" onclick={ goto('#!mydashboards') } data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.header.mydashboards[app.language] }</a>
                    </li>
                    <li class="nav-item" if={app.user.status == 'logged-in' && !app.user.guest }>
                        <a class="nav-link text-signo" href="#!mydevices" onclick={ goto('#!mydevices') } data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.header.mydevices[app.language] }</a>
                    </li>
                    <li class="nav-item" if={app.user.status == 'logged-in' && !app.user.guest }>
                        <a class="nav-link text-signo" href="#!account" onclick={ goto('#!account') } data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.header.account[app.language] }</a>
                    </li>
<!--
                    <li class="nav-item" if={ app.distroType.toLowerCase() != 'mini' && app.user.status == 'logged-in' && !app.user.guest }>
                        <a class="nav-link text-signo" href="#!doc,toc" onclick={ goto('#!doc,toc') } data-toggle="collapse" data-target="#navbarNavDropdown">{ app.texts.header.documentation[app.language] }</a>
                    </li>
-->
                    <li class="nav-item" if={!app.user.guest && app.user.status != 'logged-in' }>
                        <a class="nav-link text-signo" href="#!login" onclick={ goto('#!login') } data-toggle="collapse" data-target="#navbarNavDropdown"><i class="material-icons" style="vertical-align: middle;">perm_identity</i> { app.texts.header.login[app.language] } ({ app.texts.header.guest[app.language] })</a>
                    </li>
                    <li class="nav-item" if={app.user.status == 'logged-in' && !app.user.guest}>
                        <a class="nav-link text-signo" href="#!logout"onclick={ goto('#!logout') } data-toggle="collapse" data-target="#navbarNavDropdown"><i class="material-icons" style="vertical-align: middle;">perm_identity</i> { app.texts.header.logout[app.language] } ({app.user.name})</a>
                    </li>
                    <!-- <li class="nav-item" if={app.distroType.toLowerCase() != 'mini' && app.language!='en'}> -->
                    <li class="nav-item" if={app.distroType.toLowerCase() != 'mini'}>
                        <a href="#!en" class="nav-link text-signo" onclick={ goto('#!en') } data-toggle="collapse" data-target="#navbarNavDropdown">
                        <span class="flag-icon flag-icon-gb border border-secondary rounded"></span>
                        </a>
                    </li>
                    <li class="nav-item" if={app.distroType.toLowerCase() != 'mini'}>
                        <a href="#!pl" class="nav-link text-signo" onclick={ goto('#!pl') } data-toggle="collapse" data-target="#navbarNavDropdown">
                        <span class="flag-icon flag-icon-pl border border-secondary rounded"></span>
                        </a>
                    </li>
                    </ul>
                </div>
        </nav>
    </header>
    <app_session_manager/>
    <app_alert_client/>
    <script>
        var self = this

        globalEvents.on('dashboards:ready', function(eventName){
            self.update()
        })
        globalEvents.on('alerts:updated', function(eventName){
            self.update()
        })

        goto(address){
            return function(e){
                app.log(address)
                document.location = '/app/'+address
            }
        }
        getDistroType(){
            if (app.distroType.toLowerCase() == 'mini'){
                return 'mini'
            } else{
                return ''
            }
        }

        self.isIndicatorVisible = function(){
            if (app.requests > 0){
                return 'visibility: visible'
            } else{
                return 'visibility: hidden'
            }
        }
                
    </script>
</app_header>