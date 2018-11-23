<app_case>
    <div class="modal-header">
        <img class="card-img-top close" src="https://signomix.com/landingpage/case1top.png" data-dismiss="modal" alt="Card image" show={app.caseSelected==1}>
        <img class="card-img-top close" src="https://signomix.com/landingpage/case2top.png" data-dismiss="modal" alt="Card image" show={app.caseSelected==2}>
        <img class="card-img-top close" src="https://signomix.com/landingpage/case3top.png" data-dismiss="modal" alt="Card image" show={app.caseSelected==3}>
        <img class="card-img-top close" src="https://signomix.com/landingpage/case4top.png" data-dismiss="modal" alt="Card image" show={app.caseSelected==4}>
    </div>
    <div class="modal-body">
        <div class="container">
        <div class="row">
            <div class="col-md-12">
                <div class="card border border-0" show={app.caseSelected==1}>
                <cs_article class="container" ref="caseart1" path="/landingpage/case1" eventname="caseOK" erroreventname="caseErr" language={ app.language }></cs_article>
                </div>
                <div class="card border border-0" show={app.caseSelected==2}>
                <cs_article class="container" ref="caseart2" path="/landingpage/case2" eventname="caseOK" erroreventname="caseErr" language={ app.language }></cs_article>
                </div>
                <div class="card border border-0" show={app.caseSelected==3}>
                <cs_article class="container" ref="caseart3" path="/landingpage/case3" eventname="caseOK" erroreventname="caseErr" language={ app.language }></cs_article>
                </div>
                <div class="card border border-0" show={app.caseSelected==4}>
                <cs_article class="container" ref="caseart4" path="/landingpage/case4" eventname="caseOK" erroreventname="caseErr" language={ app.language }></cs_article>
                </div>
            </div>
        </div>
        </div>        
    </div>
    <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-dismiss="modal" >Close</button>
    </div>
    <script charset="UTF-8">
        var self = this;
        self.refs=[]
        this.on('unmount',function(){
            //self.clearDocuments()
        })
        this.on('mount',function(){
            self.loadDocuments()
        })
        self.loadDocuments = function(){
            console.log(self.refs)
            Object.keys(self.refs).forEach(function(key) {
                self.refs[key].updateContent()
            });
        }
    </script>
</app_case>
