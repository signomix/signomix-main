<app_subscribe>
    <div class="panel panel-default signomix-form">
        <div class="panel-body">
            <div  if={ !self.success }>
            <div class="row">
                <div class="col-md-12"><h3 class="module-title">{app.texts.subscribe.l_title[app.language]}</h3></div>
            </div>
            <div class="row">
                <div class="col-md-12 alert alert-light" ><span>{ app.texts.subscribe.l_comment1[app.language] }</span></div>
            </div>
            </div>
            <div class="row" if={ self.alert }>
                <div class="col-md-12 alert alert-warning" ><span><strong>{ app.texts.subscribe.l_warning[app.language] } </strong>{ self.alertText }</span></div>
            </div>
            <div class="row" if={ self.success }>
                <div class="col-md-12 alert alert-success" >
                <p><span>{ app.texts.subscribe.l_successText1[app.language] }</span></p>
                <p><span>{ app.texts.subscribe.l_successText2[app.language] + ' '+self.registeredEmail}</span></p>
                <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.subscribe.l_OK[app.language] }</button>
                </div>
            </div>
            <div class="row" if={ !self.success }>
                <div class="col-md-12">
                <form class="card border-0 p2" onsubmit={ submitRegistrationForm } id="registration-form">
                      <div class="form-group">
                    <form_input 
                        id="email"
                        name="email"
                        label={ app.texts.subscribe.l_email[app.language] }
                        type="email"
                        required="true"
                        oninvalid={ app.texts.subscribe.l_emailHint[app.language] }
                        hint={ app.texts.subscribe.l_emailHint[app.language] }/>
                    <form_input 
                        id="name"
                        name="name"
                        label={ app.texts.subscribe.l_name[app.language] }
                        type="text"
                        required="true"
                        oninvalid={ app.texts.subscribe.l_nameHint[app.language] }
                        hint={ app.texts.subscribe.l_nameHint[app.language] }/>
                                    <div class="form-check form-check-inline">
                    <label class="form-check-label">{ app.texts.user_form.preferredLanguage[app.language] }</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="preferredLanguage" id="preferredLanguage1" value="pl" checked>
                    <label class="form-check-label" for="preferredLanguage1">Polski</label>
                </div>
                <div class="form-check form-check-inline">
                    <input class="form-check-input" type="radio" name="preferredLanguage" id="preferredLanguage2" value="en">
                    <label class="form-check-label" for="preferredLanguage2">English</label>
                </div>  
                    <div class="form-check" style="margin-bottom: 20px;">
                        <input class="form-check-input" type="checkbox" value="y" id="accept" name="accept" required>
                        <label class="form-check-label" for="accept">
                        { app.texts.subscribe.l_legalText1[app.language] } <a href="#!doc,legal">{ app.texts.subscribe.l_legalText2[app.language] }</a>.
                        </label>
                    </div>
                    <div class="form-group">
                        <button type="submit" class="btn btn-primary">{ app.texts.subscribe.l_register[app.language] }</button>
                        <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.subscribe.l_cancel[app.language] }</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
<script>
    self = this
    self.alert = false;
    self.alertText = ''
    self.success = false
    self.registeredEmai = ''
    self.listener = riot.observable();
    
    self.listener.on('*', function (eventName) {
        app.log('ALERTS: ' + eventName)
        if('err:409'==eventName){
            self.alert=true
            self.alertText = app.texts.subscribe.l_alertName[app.language]
            riot.update()
        }
    });

    submitRegistrationForm = function (e) {
        e.preventDefault()
        self.alert = false
        riot.update()
        app.log("registering ..." + e.target)
        var formData = {
            confirmed: false
        }
        formData.email = e.target.elements['email'].value
        formData.name = e.target.elements['name'].value
        formData.accept = e.target.elements['accept'].value
        formData.preferredLanguage=e.target.elements['preferredLanguage'].value
        self.registeredEmail = formData.email
            //send
            app.log(JSON.stringify(formData))
            urlPath = ''
            sendData(
                formData,
                'POST',
                app.subscribeAPI,
                null,
                self.close,
                self.listener, 
                'submit:OK',
                null,
                app.debug,
                null //globalEvents
            )
        riot.update()
    }

    self.close = function (object) {
        var text = '' + object
        app.log(text)
        if (text.startsWith('"')) {
            self.success = true
        } else if (text.startsWith('error:409')) {
            self.alert = true
            self.alertText = app.texts.subscribe.l_alertName[app.language]
        } else if (text.startsWith('error:')) {
            self.alert = true
            self.alertText = app.texts.subscribe.l_alertError[app.language]
        } else {
            app.currentPage = 'main'
        }
        riot.update()
    }

</script>
</app_subscribe>
