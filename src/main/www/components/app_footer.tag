<app_footer>
    <footer class="footer border-top bg-white text-signo text-right mt-4">
        <div class="container">
            <cs_article class="container" ref="homeart" path="/landingpage/footer.html" eventname="appMainArticleOK" erroreventname="appMainArticleErr" language={ app.language }></cs_article>
        </div>
    </footer>
    <script charset="UTF-8">
        var self = this;
        self.refs = []
        this.on('unmount', function () {
            Object.keys(self.refs).forEach(function (key) {
                self.refs[key].unmount()
            });
            self.refs = []
        })
        this.on('mount', function () {
            self.loadDocuments()
        })
        globalEvents.on('language', function () {
            self.loadDocuments()
        })
        self.loadDocuments = function () {
            self.messageVisible = true
            Object.keys(self.refs).forEach(function (key) {
                self.refs[key].updateContent()
            });
        }
    </script>
</app_footer>
