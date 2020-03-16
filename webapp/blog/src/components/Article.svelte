<script>
    import { onMount } from 'svelte';
    import { createEventDispatcher } from 'svelte';
    const dispatch = createEventDispatcher();

    export let language;
    export let texts;
    export let devmode;
    export let uid;
    export let csAPI

    let pageArticle = {}

    onMount(async () => {
        getData(csAPI + uid + '?language=' + language, null, updatePage);
        feather.replace();
    });

    export function languageChanged(name) {
        language = name;
        getData(csAPI + uid + '?language=' + language, null, updatePage);
    }

    function updatePage(code, text) {
        if (code === 404) {
            pageArticle.title = '404';
            pageArticle.content = '<p>' + uid + ' not found</p>';
            return;
        }
        var doc = JSON.parse(text);
        pageArticle = decodeDocument(doc);
    }
    
    function handleBack() {
        dispatch('goBack', {
            text: event.target.href
        })
    }

</script>

<div class="container text-left">
    <div class="row">
        <div class="col">
            <article class="main">
                <header>
                    <h1>{pageArticle.title} 
                    {#if pageArticle.title!=='404'}<a href={'?doc='+uid+'&language='+language} style="text-decoration: none; color: black;"><i data-feather="link"></i></a>{/if}
                    </h1>
                </header>
                <section>{@html pageArticle.content}</section>
                <p>
                    <a class="btn btn-outline-primary btn-sm" role="button" on:click={handleBack}>&laquo; Back</a>
                </p>
            </article>
        </div>
    </div>
</div>