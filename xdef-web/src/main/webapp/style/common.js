//lib jquery 4.0.0
import "./jquery-4.0.0.min.js"
//import "./jquery-4.0.0.js"

//lib jquery.textarea-with-linenumbers
import "./jquery.textarea-with-linenumbers.js"

//lib highlightjs-11.11.1 with languages "common"
import { hljs } from './highlight.min.js'
//import { hljs } from './highlight.js'

//absolute path of the webapp root, derived from the location of this script "<webapp-root>/style/common.js")
const rootApp = new URL("..", import.meta.url).pathname;

function replaceHtml(rootPath, targets) {
    targets.forEach((target) => {
        $(this).find(target.elem + "[" + target.attr + "]").each(function() {
            $(this).attr(target.attr, $(this).attr(target.attr).replace("${rootPath}", rootPath));
        })
    })
}

function loadHeaderFooter(completeFooter, completeHeader) {
    //location of the favicon
    const faviconHref = $('link[rel="icon"]').attr("href");
    //absolute path of the language-subsite root "<webapp-root>/lang/",
    //  derived from the location of the favicon "<webapp-root>/lang/style/favicon.ico")
    const rootLang    = new URL("..", new URL(faviconHref, location.href)).pathname;

    const targets = [
        { elem: "a",      attr: "href"},
        { elem: "img",    attr: "src"},
        { elem: "option", attr: "value"}
    ];

    $("div#header").load(
        rootLang + "style/header.html",
        function(responseText, textStatus, jqXHR) {
            replaceHtml.call(this, rootLang, targets);
            if (completeHeader) {
                completeHeader.call(this, responseText, textStatus, jqXHR);
            }
        }
    );
    $("div#footer").load(
        rootLang + "style/footer.html",
        function(responseText, textStatus, jqXHR) {
            replaceHtml.call(this, rootLang, targets);
            if (completeFooter) {
                completeFooter.call(this, responseText, textStatus, jqXHR);
            }
        }
    );
};

export function initPageBasic(completeFooter, completeHeader) {
    loadHeaderFooter(completeFooter, completeHeader);
}

export function initPageBasicLnums(completeFooter, completeHeader) {
    $("textarea.linenumbers").linenumbers();
    loadHeaderFooter(completeFooter, completeHeader);
}

export function initPageBasicHili(completeFooter, completeHeader) {
    hljs.highlightAll();
    loadHeaderFooter(completeFooter, completeHeader);
}

export function initPageBasicLnumsHili(completeFooter, completeHeader) {
    $("textarea.linenumbers").linenumbers();
    hljs.highlightAll();
    loadHeaderFooter(completeFooter, completeHeader);
}

export function footerVersionActivate() {
    $("#footerVersionPas").css("display", "none");
    $("#footerVersionAct").css("display", "inline");
}

export function footerVersionDeactivate() {
    $("#footerVersionPas").css("display", "inline");
    $("#footerVersionAct").css("display", "none");
}

export function headerLangActivate() {
    $(".headerLangPas").css("display", "none");
    $(".headerLangAct").css("display", "inline");
}

export function setLatestVersion() {
    $.get("../LatestVersion", function(version) {
        $("span.latestVersion").text(version);
        $("a.latestVersion").each(function() {
            const root = $(this);
            ["href", "title"].forEach(attrName => {
                const value = root.attr(attrName);
                if (value) root.attr(attrName, value.replaceAll("--.--.--", version));
            });
        });
    });
}

//init the form-field "databaseName" on the playground-pages, fill it from the "databaseName" cookie, or generate
//a random value if there is none yet (only if fillEmpty is true), and save it back to the cookie only when
//the field's form is submitted (so it reflects whatever value was actually sent, incl. hand-typed ones)
//@param fillEmpty if true (default), an empty field is filled from the cookie or a random value;
//                 if false, an empty field is left empty (the form is still wired to save on submit)
export function initFormFieldDatabaseName(fillEmpty = true) {
    const input = document.getElementById("databaseName");
    if (!input) {
        return;
    }
    function saveToCookie() {
        if (input.value) {
            document.cookie = "databaseName=" + encodeURIComponent(input.value) + "; path=" + rootApp + "; max-age=1800";
        }
    }
    if (fillEmpty && !input.value) {
        const match      = document.cookie.match(/(?:^|;\s*)databaseName=([^;]*)/);
        const fromCookie = match ? decodeURIComponent(match[1]) : null;
        input.value = fromCookie || ("db" + Math.random().toString(36).slice(2, 10));
    }
    if (input.form) {
        input.form.addEventListener("submit", saveToCookie);
    }
}

//error-messages are usually invisible at the beginning not to flash on the page during loading.
//  That's why it appears after a second. VD - visibility delayed
setTimeout(function() { $(".errorVD").css("visibility", "visible"); }, 1000);

//exports to window
window.initPageBasic                = initPageBasic;
window.initPageBasicLnums           = initPageBasicLnums;
window.initPageBasicHili            = initPageBasicHili;
window.initPageBasicLnumsHili       = initPageBasicLnumsHili;
window.footerVersionActivate        = footerVersionActivate;
window.footerVersionDeactivate      = footerVersionDeactivate;
window.headerLangActivate           = headerLangActivate;
window.setLatestVersion             = setLatestVersion;
window.initFormFieldDatabaseName    = initFormFieldDatabaseName;
