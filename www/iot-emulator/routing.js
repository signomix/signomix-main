/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

route(function(id){
    switch (id){
        case "about":
            app.currentPage = "about";
            globalEvents.trigger('pageselected:about');
            break;
        case "main":
            app.currentPage = "main";
            globalEvents.trigger('pageselected:main');
            break;
        default:
            app.currentPage = "main";
            globalEvents.trigger('pageselected:main');
            break;
    }
    riot.update();
})

