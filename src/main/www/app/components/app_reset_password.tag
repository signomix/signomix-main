<app_reset_password>
    <div class="panel panel-default signomix-form">
        <div class="panel-body">
    <div class="row">
        <div class="col-md-12" if={ self.status != 1}>
             <form onsubmit={ submitForm }>
              <p class="module-title h3 text-center mb-4">{app.texts.reset_password.l_title[app.language]}</p>
                <div class="md-form">
                    <i class="fa fa-user prefix grey-text"></i>
                    <input type="text" id="name" name="name" class="form-control" required>
                    <label for="name">{ app.texts.reset_password.l_name[app.language] }</label>
                </div>
                <div class="md-form">
                    <i class="fa fa-at prefix grey-text"></i>
                    <input type="email" id="resetpass" name="resetpass" class="form-control" required>
                    <label for="email">{ app.texts.reset_password.l_email[app.language] }</label>
                </div>
                <div class="md-form">
                    <div class="alert alert-danger" role="alert" if={ self.status == -1 }>
                        { app.texts.reset_password.l_error[app.language] }
                    </div>
                </div>
                <div class="text-center">
                    <button type="submit" class="btn btn-primary">{ app.texts.reset_password.l_save[app.language] }</button>
                </div>
                <div class="md-form">
                    <p class="form-footer">{ app.texts.reset_password.l_description[app.language] }</p>
                </div>
            </form>
        </div>
        <div class="col-md-12" if={ self.status == 1}>
            <div class="alert alert-success module-title" role="alert">
                { app.texts.reset_password.l_success[app.language] } { self.email }
            </div>
        </div>
    </div>
        </div>
    </div>
    <script type="text/javascript" charset="UTF-8">
        self=this
        self.email = ''
        self.listener = riot.observable()
        self.status = 0

        self.listener.on('resetpass:ERR', function (event) {
        self.status = -1
        riot.update()
        });

        showResponse = function(text){
        self.status = 1
        app.log("RESET RESP: "+text)
        riot.update()
        }

        submitForm = function(e){
        e.preventDefault()
        app.log("submitting ..."+e.target)
        sendFormData(e.target, 'POST', app.recoveryAPI, self.listener, showResponse, self.listener, 'resetpass:OK', 'resetpass:ERR', app.debug, null)
        self.email = e.target.resetpass.value;
        e.target.reset()
        }

    </script>
    <style>
        .form-footer{
            margin-top: 20px;
        }
    </style>
</app_reset_password>
