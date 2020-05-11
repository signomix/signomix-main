<script>
    import { onMount } from 'svelte';
    import { createEventDispatcher } from 'svelte';
    const dispatch = createEventDispatcher();

    export let navlist =
            [
                {url: "/", label: {en: "Home", pl: "Home"}, target: ""}

            ];
    export let language;
    export let texts;

    onMount(async () => {
        getData('navigation.json', null, updateNav);
    });

    export function languageChanged(name) {
        //alert('language changed to ' + name)
    }

    function handleNav() {
        dispatch('setLocation', {
            text: event.target.href
        })
    }

    function handlePL() {
        dispatch('setLanguage', {
            text: 'pl'
        });
    }
    function handleEN() {
        dispatch('setLanguage', {
            text: 'en'
        });
    }

    function updateNav(code, text) {
        if (code === 404) {
            return;
        }
        navlist = JSON.parse(text);
    }

</script>

<div class="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom box-shadow">
    <h5 class="my-0 mr-md-auto font-weight-normal"><img class="mb-2" src="/resources/logo.png" alt="" height="40">&nbsp;</h5>
    <nav class="my-2 my-md-0 mr-md-3">
        {#each navlist as list}
        <a class="p-2 text-dark" href={list.url} on:click={handleNav} target={list.target}>{list.label[language]}</a>
        {/each}
        {#if language!='en'}
        <a class="p-2 text-dark" href="#!en" on:click={handleEN}><span class="flag-icon flag-icon-gb border border-secondary rounded"></span></a>
        {/if}
        {#if language!='pl'}
        <a class="p-2 text-dark" href="#!pl" on:click={handlePL}><span class="flag-icon flag-icon-pl border border-secondary rounded"></span></a>
        {/if}
    </nav>
    <a class="btn btn-outline-primary" href="/app/#!login">{texts.navigation.signin}</a>
</div>
