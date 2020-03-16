/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
route(function (id) {
    if (app.languages.indexOf(id) > -1) {
        app.language = id
        globalEvents.trigger('language')
        riot.mount('raw')
        riot.update()
        return
    }
    app.currentPage = 'blog'
    if (app.docPath) {
        app.previousPath = app.docPath
    }
    if (id!='') {
        app.docPath = id.replace(/,/g, "/");
    } else {
        app.docPath = '/posts'
    }
    globalEvents.trigger('pageselected')
    riot.update()
})