<script>
    import { onMount } from 'svelte';
    import Navbar from "./components/Navbar.svelte";
    import Article from "./components/Article.svelte";
    import Footer from "./components/Footer.svelte";

    export let texts;
    export let languages;
    export let language;
    export let defaultLanguage;
    export let csAPI;
    export let docuid;
    export let devModePort;
    export let devMode = false;
    export let cmsMode;

    // child components which must be binded
    let navbar;
    let article;
    let footer;

    let selected = '';
    let path
    let homePath
    let prefix
    let pageArticle =
            {
                title: '',
                summary: '',
                content: ''
            }

    let articles = []
    let tmpLang = window.localStorage.getItem("language")
    if (languages.length > 1 && null !== tmpLang && "" !== tmpLang && "undefined" !== tmpLang) {
        language = tmpLang
    } else {
        language = defaultLanguage
    }
    prefix = language === defaultLanguage ? '' : language + '_';
    console.log("language:[" + language + "]")
    path = window.location.pathname
    homePath = getRoot(path)
    console.log(window.location.search)
    console.log()
    if (window.location.search.startsWith('?doc=')) {
        var langPos = (window.location.search.lastIndexOf('&'));
        let lang = window.location.search.substring(langPos + 10);
        if (lang.endsWith('/')) {
            lang = lang.slice(0, -1);
        }
        if (lang.length === 2) {
            language = lang;
        }
        docuid = window.location.search.substring(5, langPos);
    } else {
        docuid = '';
    }

    /**
     * Load default language version of the application texts on mount
     * @return {undefined}
     */
    onMount(async () => {
        devMode = window.origin.endsWith(':' + devModePort);
        getData(homePath + `texts_` + language + '.json', null, updateTexts);
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
        console.log(location)
    }

    /*
     * Handle goBack events
     * @param {type} event
     * @return {undefined}
     */
    function handleGoBack(event) {
        if (window.location.search.startsWith('?doc=')) {
            window.location.search = '';
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
        language = event.detail.language
        prefix = language === defaultLanguage ? '' : language + '_';
        window.localStorage.setItem("language", language);
        if (typeof article !== 'undefined') {
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
        //articles = l.reverse();
        articles = l.sort(function (a, b) {
            return b.extra - a.extra
        })
        for (var i = 0; i < articles.length; i++) {
            articles[i] = decodeDocument(articles[i]);
        }
    }
    function viewArticle(event) {
        event.preventDefault()
        docuid = event.target.pathname;
    }

    function getRoot(pathName) {
        let pos;
        if (pathName.startsWith('/')) {
            pos = pathName.indexOf('/', 1);
        } else {
            pos = pathName.indexOf('/', 0);
        }
        if (pos > -1) {
            return pathName.substring(0, pos + 1);
        }
        return '/';
    }
</script>

<main>
    <Navbar path='/blog' homePath={homePath} bind:this={navbar} languages={languages} language={language}
            defaultLanguage={defaultLanguage} on:setLanguage={handleSetLanguage} />
    <!--
    <Navbar language={language} texts={texts} on:setLanguage={handleSetLanguage} on:setLocation={handleSetLocation} bind:this={navbar}/>-->
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
                    <a class="btn btn-outline-primary btn-sm" role="button" style="margin-top: 1rem;" href={listArticle.uid} on:click={viewArticle}>{texts.list.details} &raquo;</a>                 
                </article>
            </div>
        </div>
        {/each}
    </div>
    {/if}
    <Footer csAPI={csAPI} language={language} texts={texts} on:setLanguage={handleSetLanguage} bind:this={footer}/>
</main>
<style>
    article.list{
        margin-top: 2rem;
    }
    article.folder{
        margin-top: 2rem;
    }
</style>