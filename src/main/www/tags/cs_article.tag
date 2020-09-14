<cs_article>
    <article>
        <header>
            <h1 ref="a_title" if={ article.title }>{ article.title }</h1>
            <p ref="a_summary" if={ article.summary }>{ article.summary }</p>
        </header>
        <section ref="a_content">{ article.content }</section>
    </article>
    <script charset="UTF-8">
        var self = this;
        self.opts = opts
        self.listener = riot.observable()
        self.article = {
            title: '',
            summary:'',
            content: 'loading content '+opts.path+' ...'
        }
        this.on('mount',function(){
            app.log('ARTICLE MOUNT')
            app.log(self.refs)
        })
        self.listener.on('*',function(){
            self.update()
        })
        this.on('unmount',function(){
            app.log('ARTICLE UNMOUNT')
        })     
        self.updateContent = function(){
            readDocument(self.opts.path, self.opts.language, self.showMe, self.opts.eventname, self.opts.erroreventname)
        }
        readDocument = function (path, language, callback, successeventname, erroreventname) {
            getData(app.csAPI + path+'?language='+language,
                null,
                null,
                callback,
                self.listener, //globalEvents
                successeventname,
                erroreventname, // in case of error send response code
                app.debug,
                globalEvents
                )
        }
        self.showMe = function (response, eventName) {
            var doc = JSON.parse(response);
            try{
                self.article['title'] = decodeURIComponent(doc.title)
            }catch(e){
                self.article['title'] = unescape(doc.title)
            }
            try{
                self.article['summary'] = decodeURIComponent(doc.summary)
            }catch(e){
                self.article['summary'] = unescape(doc.summary)
            }
            try{
                self.article['content'] = decodeURIComponent(doc.content)
            }catch(e){
                self.article['content'] = unescape(doc.content)
            }
            self.update()
            if(self.article['title']){
                self.refs.a_title.innerHTML=self.article['title']
            }
            if(self.article['summary']){
                self.refs.a_summary.innerHTML=self.article['summary']
            }
            self.refs.a_content.innerHTML=self.article['content']
            globalEvents.trigger(eventName)
        }
    </script>
</cs_article>