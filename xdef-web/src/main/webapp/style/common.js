//import "./jquery-4.0.0.min.js";
//import("./jquery-4.0.0.min.js");

function loadHeader(url, data, complete) {
    $("div#header").load(url, data, complete);
    //setTimeout(function() { $("div#header").load("header0.html"); }, 1000);
};

function loadFooter(url, data, complete) {
    $("div#footer").load(url, data, complete);
};

//$(init);
//init();
//window.init = init;
