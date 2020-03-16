<script>
    import { onMount } from 'svelte';
    export let language;
    export let texts;
    export let devmode;
    export let csAPI;

    let article =
            {
                title: '',
                summary: '',
                content: ''
            }

    onMount(() => {
        getData(csAPI + '/landingpage/footer.html?language=' + language, null, update)
    });

    export function languageChanged(name) {
        language = name;
        getData(csAPI + '/landingpage/footer.html?language=' + language, null, update)
    }

    function update(code, text) {
        if (code === 404) {
            article.content = '<p><i>/landingpage/footer.html</i> not found</p>';
            return;
        }
        article = decodeDocument(JSON.parse(text));
    }

</script>

<footer class="footer border-top bg-white text-signo text-right mt-4">
    <div class="container">
        {@html article.content}
    </div>
</footer>