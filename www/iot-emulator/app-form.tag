<app-form>
    <div class="container-fluid">
        <div class="row">
            <div class="col-md-12">
                <form name="target">
                    <div class="form-group">
                        <legend style="margin-top: 20px;">{labels.section1[app.language]}</legend>
                        <div class="form-group">
                            <label>{ getEUI() }</label>
                        </div>
                        <div class="form-group">
                            <label>{ getKey() }</label>
                        </div>
                        <div class="form-check">
                            <label class="form-check-label">
                                <input class="form-check-input" type="radio"
                                       name="exampleRadios" id="exampleRadios1" value="local" ref="targetA" checked onclick={ selectA }>
                                       http://localhost:8080/api/integration
                            </label>
                        </div>
                        <div class="form-check">
                            <label class="form-check-label">
                                <input class="form-check-input" type="radio" 
                                       name="exampleRadios" id="exampleRadios2" value="cloud" ref="targetB" onclick={ selectB }>
                                       https://signomix.com/api/integration
                            </label>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <div class="alert alert-success" role="alert" if={ app.requestResult == 1 }>
                    <strong>{labels.success[app.language]}</strong> {labels.successmessage[app.language]}
                </div>
                <div class="alert alert-warning" role="alert" if={ app.requestResult == 3 }>
                    <strong>{labels.validation[app.language]}</strong> {labels.validationmessage[app.language]}
                </div>
                <div class="alert alert-danger" role="alert" if={ app.requestResult == 2 }>
                    <strong>{labels.error[app.language]}</strong> {labels.errormessage[app.language]}
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-md-12">
                <form onsubmit={ add } name="data">
                      <legend>{labels.section2[app.language]}</legend>
                    <div class="form-group">
                        <label for="temperature">{labels.temperature[app.language]}</label>
                        <input pattern="-?\d+(\.\d+)?" id="temperature" ref="temperature" class="form-control" autofocus required>
                    </div>
                    <div class="form-group">
                        <label for="humidity">{labels.humidity[app.language]}</label>
                        <input pattern="-?\d+(\.\d+)?" id="humidity" ref="humidity" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label for="latitude">{labels.latitude[app.language]}</label>
                        <input pattern="-?\d+(\.\d+)?" id="latitude" ref="latitude" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label for="longitude">{labels.longitude[app.language]}</label>
                        <input pattern="-?\d+(\.\d+)?" id="longitude" ref="longitude" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label for="datetime">{labels.datetime[app.language]}</label>
                        <input type="datetime" id="datetime" ref="datetime" class="form-control" value={new Date().toISOString()} required>
                    </div>
                    <button type="subimt" class="btn btn-primary">{labels.send[app.language]}</button>
                </form>
            </div>
        </div>
    </div>
    <script charset="UTF-8">
        var self = this
        self.EUI = "IOT-EMULATOR"
        self.authString = "6022140857"
        self.apiURL = 'http://localhost:8080/api/integration'
        self.listener = riot.observable()

        globalEvents.on('pageselected:main', function (eventName) {
            self.listener.trigger('start')
        })

        getKey(){
            return 'Device Key: ' + self.authString
        }
        
        getEUI(){
            return 'Device EUI: ' + self.EUI
        }

        selectA(){
            this.refs.targetA.checked = true
            this.refs.targetB.checked = false
            self.apiURL = 'http://localhost:8080/api/integration'
            riot.update()
        }

        selectB() {
            this.refs.targetA.checked = false
            this.refs.targetB.checked = true
            self.apiURL = 'https://signomix.com/api/integration'
            riot.update()
        }

        self.listener.on('*', function (eventName) {
            console.log('ACTION: ' + eventName)
            if (eventName == 'start'){
                app.requestResult = 0
                //self.refs.temperature.setCustomValidity('Wrong data format')
                //self.refs.temperature.setCustomValidity('Wrong data format')
            } else if (eventName.startsWith('dataerror:')){
                app.requestResult = 2
            } else if (eventName == 'invalid'){
                console.log('NOT VALID')
                app.requestResult = 3
            }
            riot.update();
        })

        processResult(e){
            console.log("result=" + e)
            app.requestResult = 1
            riot.update()
        }

        validate(tmpData){
            try{
                var t = parseFloat(tmpData.payload_fields.temperature)
                var h = parseFloat(tmpData.payload_fields.humidity)
                if (h < 0 || h > 100){
                    return false
                }
            } catch (err){
                return false;
            }
            if (new Date(tmpData.time).toString().startsWith('Invalid')){
                return false;
            }
            return true
        }

        add(e) {
            e.preventDefault();
            var data = {}
            data.dev_eui = self.EUI
            data.time = this.refs.datetime.value
            data.payload_fields = []
            var field = {}
            field.name = "Temperature"
            field.value = this.refs.temperature.value
            data.payload_fields.push(field)
            var field2 = {}
            field2.name = "humidity"
            field2.value = this.refs.humidity.value
            data.payload_fields.push(field2)
            var field3 = {}
            field3.name = "latitude"
            field3.value = this.refs.latitude.value
            data.payload_fields.push(field3)
            var field4 = {}
            field4.name = "longitude"
            field4.value = this.refs.longitude.value
            data.payload_fields.push(field4)
            console.log(data)
            if (!self.validate(data)){
                self.listener.trigger('invalid')
                return
            }
            // send
            console.log('sending to ' + self.apiURL);
            sendJsonData(
                data,
                "POST",
                self.apiURL,
                "Authorization",
                self.authString, // auth string
                this.processResult, // callback
                this.listener, // listener
                'OK', //
                null, //
                app.debug, // debug switch 
                null         //
            )
            // clear form
            this.refs.temperature.value = ''
            this.refs.humidity.value = ''
            this.refs.datetime.value = new Date().toISOString()
        }

        this.labels = {
            "section1": {
                "en": "Target system",
                "pl": "Docelowy system"
            },
            "section2": {
                "en": "Data to send",
                "pl": "Dane do wysłania"
            },
            "send": {
                "en": "Send",
                "pl": "Prześlij"
            },
            "temperature": {
                "en": "temperature",
                "pl": "temperatura"
            },
            "humidity": {
                "en": "humidity",
                "pl": "wilgotność"
            },
            "latitude": {
                "en": "latitude",
                "pl": "latitude"
            },
            "longitude": {
                "en": "longitude",
                "pl": "longitude"
            },
            "datetime": {
                "en": "date & time",
                "pl": "data i godzina"
            },
            "error": {
                "en": "Error!",
                "pl": "Błąd!"
            }, 
            "success": {
                "en": "Success!",
                "pl": "Sukces!"
            }, 
            "validation": {
                "en": "Warning!",
                "pl": "Uwaga!"
            }, 
            "errormessage": {
                "en": "A problem has been occurred while submitting your data.",
                "pl": "Podczas wysyłania danych wystąpił błąd."
            }, 
            "successmessage": {
                "en": "Your message has been sent successfully.",
                "pl": "Twoje dane zostały pomyślnie wysłane."
            }, 
            "validationmessage": {
                "en": "Invalid data.",
                "pl": "Niepoprawne dane."
            }
        }
    </script>
</app-form>