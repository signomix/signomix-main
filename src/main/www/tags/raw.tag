<raw>
    this.root.innerHTML=opts.content
    this.on('mount',function(){
        app.log('MOUNT RAW')
        app.log('HTML='+opts.content)
    })
</raw>
