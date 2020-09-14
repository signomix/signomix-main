<script>
    import { onMount } from 'svelte';
    import { createEventDispatcher } from 'svelte';
    const dispatch = createEventDispatcher();

    export let path;
    export let homePath;
    export let languages;
    export let language;
    export let defaultLanguage;

    let navlist =
    {
        "title": "",
        "logo": "",
        "signin":{},
        "elements": [
            { url: "/", label: { en: "Home", pl: "Home" }, target: "" }

        ]
    };
    onMount(async () => {
        navlist =  await contentClient.getJsonFile(homePath+`navigation.json`);
        document.title = navlist.title;
    });
    function handleLang(x) {
        dispatch('setLanguage', {
            language: x
        })
    }

</script>

<div class="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom box-shadow">
    <h5 class="my-0 mr-md-auto font-weight-normal"><img class="mb-2" src={navlist.logo} alt="" height="40">&nbsp;</h5>
    <nav class="my-2 my-md-0 mr-md-3">
        {#each navlist.elements as element}
        <a class="p-2 text-dark" href={element.url} target={element.target}>{element.label[language]}</a>
        {/each}
        {#if languages.length>1}
        {#each languages as lang}
        {#if lang!==language}
        <a class="p-2 text-dark" 
            on:click={() => handleLang(lang)}><img class="flag" alt={lang} src={'resources/flags/4x3/'+lang+'.svg'}></a>
        {/if}
        {/each}
        {/if}
    </nav>
    <a class="btn btn-outline-primary" href="/app/#!login">{navlist.signin[language]}</a>
</div>
<style>
    nav img{
        
        height: 40px;
    }
    a.nav-item{
        font-size: large;
    }
    .flag{
        width: 1.6rem; 
        border-width: 0px; 
        border-color: lightgray;
        border-style: solid;
    }
</style>