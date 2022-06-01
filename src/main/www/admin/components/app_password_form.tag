<app_password_form>
    <div class="row">
        <div class="col-md-2">
        </div>
        <div class="col-md-8">
            <!-- Login form -->
            <form onsubmit={ submitForm }>
                <p class="module-title h3 text-center mb-4">{app.texts.app_password_form.title[app.language]}</p>

                <div class="md-form">
                    <i class="fa fa-lock prefix grey-text"></i>
                    <input type="password" id="password" name="password" class="form-control" required>
                    <label for="password">{ app.texts.app_password_form.password[app.language] }</label>
                </div>

                <div class="md-form">
                    <i class="fa fa-lock prefix grey-text"></i>
                    <input type="password" id="password2" name="password2" class="form-control" required>
                    <label for="password2">{ app.texts.app_password_form.password2[app.language] }</label>
                </div>
                <div class="md-form" if={ self.error }>
                    <span class="red-text">password do not match</span>
                </div>

                <div class="text-center">
                    <button type="submit" class="btn btn-primary">{ app.texts.app_password_form.save[app.language] }</button>
                    <button type="button" class="btn btn-secondary" onclick={ close }>{ app.texts.app_password_form.cancel[app.language] }</button>
                </div>
            </form>
            <!-- Login form -->
        </div>
        <div class="col-md-2">
        </div>
    </div>
    <script>
        this.visible = true
        self = this
        self.listener = riot.observable()
        self.callbackListener
        self.method = 'PUT'
        self.mode = 'view'
        self.uid
        self.error = false

        globalEvents.on('data:submitted', function (event) {
            //
        });

        init(eventListener, uid){
            self.uid = uid
            self.callbackListener = eventListener
            app.log('HELLO ' + uid)
        }

        self.submitForm = function (e) {
            e.preventDefault()
            var formData = {
                uid: self.uid
            }
            if (e.target.elements['password2'].value != e.target.elements['password'].value) {
                e.target.reset()
                self.error = true
                return
            }
            self.error=false
            if (e.target.elements['password'].value) {
                formData.password = e.target.elements['password'].value
            }
            urlPath = '/' + formData.uid
            if (app.user.guest){
                formData.recover = 'true'
                urlPath = urlPath+'?recover=true'
            }
            app.log(JSON.stringify(formData))
            
            sendData(
                    formData,
                    self.method,
                    app.userAPI + urlPath,
                    app.user.token,
                    self.close,
                    self.callbackListener, 
                    'passChange:OK',
                    'passChange:ERROR',
                    app.debug,
                    null //globalEvents
                    )
        }

        self.close = function (object) {
            var text = '' + object
            app.log('CALBACK: ' + object)
            if (text.startsWith('"') || text.startsWith('{')) {
                self.callbackListener.trigger('pSubmitted')
            } else if (text.startsWith('error:202')) {
                self.callbackListener.trigger('passSubmitted')
            } else if (text.startsWith('[object MouseEvent')) {
                self.callbackListener.trigger('passCancelled')
            } else if (text.startsWith('[object PointerEvent')) {
                self.callbackListener.trigger('passCancelled')
            } else {
                alert(text)
            }
        }
    </script>
</app_password_form>
