/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
route(function (id) {
    console.log('PAGE ID=' + id)
    if (app.docPath) {
        app.previousPath = app.docPath
    }
    switch (id) {
        case "pl":
            app.language = 'pl'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "en":
            app.language = 'en'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "fr":
            app.language = 'fr'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "it":
            app.language = 'it'
            riot.mount('raw')
            riot.update()
            globalEvents.trigger('language')
            break;
        case "":
            app.currentPage = 'blog'
            app.docPath = "/posts";
            globalEvents.trigger('pageselected:blog');
            break;
        default:
            app.currentPage = 'blog'
            app.docPath = id.replace(/,/g, "/");
            globalEvents.trigger('pageselected:blog');
            //route(id)
            break;
    }
    //globalEvents.trigger('pageselected');
    riot.update();
})