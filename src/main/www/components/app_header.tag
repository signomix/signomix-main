<app_header>
    <div class="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom box-shadow">
        <h5 class="my-0 mr-md-auto font-weight-normal"><img class="mb-2" src="/resources/logo.png" alt="" height="40">&nbsp;</h5>
        <nav class="my-2 my-md-0 mr-md-3">
        <a class="p-2 text-dark" href="/">{ app.texts.main.home[app.language] }</a>
        <a class="p-2 text-dark" href="/blog">Blog</a>
        <a class="p-2 text-dark" href="/status/">Status</a>
        <a class="p-2 text-dark" href="/app/#!doc,toc">{ app.texts.main.documentation[app.language] }</a>
        <a class="p-2 text-dark" href="/app/#!register">{ app.texts.main.signup[app.language] }</a>
        <a class="p-2 text-dark" href="#!en" onclick={goto('en')}><span class="flag-icon flag-icon-gb border border-secondary rounded"></span></a>
        <a class="p-2 text-dark" href="#!pl" onclick={goto('pl')}><span class="flag-icon flag-icon-pl border border-secondary rounded"></span></a>
        </nav>
        <a class="btn btn-outline-primary" href="/app/#!login">{ app.texts.main.signin[app.language] }</a>
        </div>
    <script>
        goto(address){
            return function(e){
                app.language=address
                riot.update()
            }
        }
    </script>
</app_header>
