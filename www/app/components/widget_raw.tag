<widget_raw>
    <div id={opts.ref} if={type == 'raw'} class="container bg-white border border-info rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <div class="col-12 text-center">{title}</div>
        </div> 
        <div class="row px-3 py-1">
                <div class="col-12">
                    <pre>{rawdata}</pre>
                </div>
        </div>
    </div>
    <div id={opts.ref} if={type == 'text' && description.trim() != ''} class="container bg-white border border-light rounded topspacing p-0">
        <div class="row px-3 pt-1 pb-0">
            <raw content={ description }></raw>
        </div> 
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