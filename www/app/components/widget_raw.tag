<widget_raw>
    <div id={opts.ref} if={type == 'raw'} class="card card-block topspacing p-0">
        <div class="card-header h6 text-left p-1">{title}</div>
        <div class="card-body"><pre>{rawdata}</pre></div>
    </div>
    <div id={opts.ref} if={type == 'text' && description.trim() != ''} class="card card-block topspacing p-1">
        <raw content={ description }></raw>
    </div>
    <div id={opts.ref} if={type == 'text' && description.trim() == ''} class="topspacing p-1">
    &nbsp;
    </div>
    <script>
    var self = this
    // opts: poniższe przypisanie nie jest używane
    //       wywołujemy update() tego taga żeby zminieć parametry
    self.type = opts.type
    self.visible = opts.visible
    self.title = opts.title
    self.unitName = opts.unitName
    self.description = opts.description
    self.range = opts.range
    // opts
    
    self.color = 'bg-white'
    self.rawdata = "[]"

    self.noData = false
    self.width=100
    self.heightStr='height:100px;'
    
    self.show2 = function(){
        app.log('SHOW2 '+self.type)
        getWidth()
    }
        
    $(window).on('resize', resize)
    
    function getWidth(){
        self.width=$('#'+opts.ref).width()
        self.heightStr='height:'+self.width+'px;'
    }
    
    function resize(){
        getWidth()
        self.show2()
    }
        
    </script>
    <style>
        .topspacing{
            margin-top: 10px;
        }
       
    </style>
</widget_raw>