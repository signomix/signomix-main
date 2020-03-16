<script>
    import { onMount } from 'svelte';
    import Navbar from "./components/Navbar.svelte";
    import Article from "./components/Article.svelte";
    import Footer from "./components/Footer.svelte";

    export let texts;
    export let language;
    export let csAPI;
    export let docuid;
    export let devModePort;
    export let devMode = false;

    // child components which must be binded
    let navbar;
    let article;
    let footer;

    let selected = '';

    let pageArticle =
            {
                title: '',
                summary: '',
                content: ''
            }

    let articles = []

    /**
     * Load default language version of the application texts on mount
     * @return {undefined}
     */
    onMount(async () => {
        console.log(window.location.search)
        if (window.location.search.startsWith('?doc=')) {
            var langPos=(window.location.search.lastIndexOf('&'));
            let lang=window.location.search.substring(langPos+1);
            docuid = window.location.search.substring(5,langPos)+'?'+lang;
        } else {
            docuid = '';
        }
        devMode = window.origin.endsWith(':' + devModePort);
        getData(`texts_` + language + '.json', null, updateTexts);
        getData(csAPI + '/posts?language=' + language, null, updatePage);
    });

    /*
     * Handle setLocation events
     * @param {type} event
     * @return {undefined}
     */
    function handleSetLocation(event) {
        console.log(event)
        location = event.detail.text
        alert(location)
        console.log(location)
    }

    /*
     * Handle goBack events
     * @param {type} event
     * @return {undefined}
     */
    function handleGoBack(event) {
        if (window.location.search.startsWith('?doc=')) {
            window.location.search='';
        } else {
            docuid = '';
        }
    }

    /**
     * Handle setLanguage events
     * @param {type} event
     * @return {undefined}
     */
    function handleSetLanguage(event) {
        if (event.detail.text === 'pl') {
            language = 'pl';
        } else if (event.detail.text === 'en') {
            language = 'en';
        } else {
            return
        }
        if(typeof article !== undefined) {
            article.languageChanged(language);
        }
        getData(`texts_` + language + '.json', null, updateTexts);
        getData(csAPI + '/posts?language=' + language, null, updatePage);
    }

    function updateTexts(code, text) {
        if (code === 404) {
            return;
        }
        texts = JSON.parse(text);
        document.title = texts.title;
    }

    function updatePage(code, text) {
        if (code === 404) {
            pageArticle.summary = '/posts not found';
            readList()
            return;
        }
        var doc = JSON.parse(text);
        pageArticle = decodeDocument(doc);
        readList()
    }

    function readList() {
        getData(csAPI + '?path=/posts/&language=' + language, null, updateList)
    }
    function updateList(code, text) {
        if (code === 404) {
            articles = [{uid: "/posts/a", title: "title1"}];
            return;
        }
        let l = JSON.parse(text);
        articles = l.reverse();
        for(var i=0; i<articles.length; i++){
            articles[i]=decodeDocument(articles[i]);
        }
    }
    function viewArticle(event) {
        event.preventDefault()
        docuid = event.target.pathname;
    }
</script>

<main>
    <Navbar language={language} texts={texts} on:setLanguage={handleSetLanguage} on:setLocation={handleSetLocation} bind:this={navbar}/>
    {#if docuid!==''}
    <Article language={language} texts={texts} on:goBack={handleGoBack} bind:this={article} devmode={devMode} csAPI={csAPI} uid={docuid}/>
    {:else}
    <div class="container top-spacing">
        <div class="row">
            <div class="col-md-12">
                <article class="folder">
                    <header>
                        <h1>{@html pageArticle.title}</h1>
                        <div>{@html pageArticle.summary}</div>
                    </header>
                </article>
            </div>
        </div>
        <!--list-->
        {#each articles as listArticle}
        <div class="row">
            <div class="col-md-12">
                <article class="list">
                    <header>
                        <h1>{listArticle.title}</h1>
                        <div>{@html listArticle.summary}</div>
                    </header>
                    <p><a href={listArticle.uid} on:click={viewArticle}>{texts.list.details} &raquo;</a></p>                  
                </article>
            </div>
        </div>
        {/each}
    </div>
    {/if}
    <Footer csAPI={csAPI} language={language} texts={texts} on:setLanguage={handleSetLanguage} bind:this={footer}/>
</main>