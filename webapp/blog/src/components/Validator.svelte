<script>
    import { createEventDispatcher } from 'svelte';
    const dispatch = createEventDispatcher();

    export let language;
    export let texts;
    export let devmode;
    export let apiURL;

    export function languageChanged(name) {
        //alert('language changed to ' + name)
    }

    let message = 'OK';
    let translated = '';
    let myForm;

    function handleSubmit() {
        event.preventDefault()
        var fd = new FormData()
        fd.append('symbols', myForm.elements['symbols'].value)
        fd.append('formula', myForm.elements['formula'].value)
        sendFormData(fd, 'POST', '/calc/api/compiler', null, getResult)
    }
    function getResult(code, text) {
        var info;
        if (code == 404) {
            message = '404: nof found';
            translated = '';
        } else if (code != 200) {
            info = JSON.parse(text);
            message = 'Błąd ' + info['code'] + ': ' + info['message'];
            translated = ''
        } else {
            info = JSON.parse(text)
            translated = info['message']
            message = 'OK'
        }
    }

</script>

<div class="container text-left">
    <div class="row">
        <div class="col">
            <h1>{texts.validator.title}</h1>
            {#if devmode}
            <h2 class="alert-danger text-center">DEV MODE</h2>
            {/if}
            <p>{texts.validator.description1}</p>
            <p>{texts.validator.description2}</p>
            <p>{texts.validator.description3}<br> 
                <a href="https://gitlab.nmg.pl/gskorupa/e5-calc-engine/wikis/Sk%C5%82adnia-formu%C5%82" target="_blank">https://gitlab.nmg.pl/gskorupa/e5-calc-engine/wikis/Sk%C5%82adnia-formu%C5%82</a>.
            </p>
        </div>
    </div>
    <div class="row" style="margin-bottom: 1rem;">
        <div class="col">
            <h2>{texts.validator.formTitle}</h2>
            <form id="form1" bind:this={myForm}>
                <div class="form-group">
                    <label for="symbols">{texts.validator.formLabel1}</label>
                    <input type="text" class="form-control" id="symbols" name="symbols" aria-describedby="symbolsHelp">
                    <small id="symbolsHelp" class="form-text text-muted">Lista symboli oddzielonych przecinkami. Np: a,b,c</small>
                </div>
                <div class="form-group">
                    <label for="formula">{texts.validator.formLabel2}</label>
                    <textarea class="form-control" id="formula" name="formula" rows="4"></textarea>
                </div>
                <button on:click={handleSubmit} class="btn btn-primary">{texts.validator.formButton}</button>
            </form>
        </div>
    </div>
    <div class="row">
        <div class="col">
            <h2>{texts.validator.compilationTitle}</h2>
            <pre>
{message}
            </pre>
        </div>
    </div>
    <div class="row">
        <div class="col">
            <h2>{texts.validator.translationTitle}</h2>
            <pre>
{translated}             
            </pre>
        </div>
    </div>
</div>