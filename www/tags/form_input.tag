<form_input>
    <div class="form-check" if={ opts.type == 'check'}>
      <input class="form-check-input" type="checkbox" value="" id={ opts.id } name={ opts.name } checked={ opts.checked } required={ opts.required }>
      <label class="form-check-label" for={ opts.id }>{ (opts.required?'* ':'')+opts.label }</label>
    </div>
    <label for={ opts.id } if={ opts.type != 'check' }>{ (opts.required?'* ':'')+opts.label }</label>
    <input if={ opts.type == 'text' || opts.type == 'password' || opts.type == 'email'} 
        class="form-control" id={ opts.id } name={ opts.name } type={ opts.type } required={ opts.required }
        pattern={ opts.pattern } 
        oninvalid="this.setCustomValidity('{opts.oninvalid}')"
        value={ opts.content }
        readonly={ opts.readonly }
        oninput="this.setCustomValidity('')"
        aria-describedby={ opts.id+'Help' }
    >
    <textarea if={ opts.type == 'textarea'} 
        class="form-control" id={ opts.id } name={ opts.name } rows={ self.rows } required={ opts.required }
        readonly={ opts.readonly } aria-describedby={ opts.id+'Help' }
    >
    { opts.content }
    </textarea>
    <small id={ opts.id+'Help' } class="form-text text-muted" if={ opts.hint }>{ opts.hint }</small>
    <script>
        var self=this
        self.areaRows = 3
        if(opts.rows){
            self.areaRows = opts.rows
        }
        app.log('PATTERN:'+opts.pattern)
    </script>
</form_input>
