//lib jquery 4.0.0
import "./jquery-4.0.0.min.js"
//import "./jquery-4.0.0.js"

//lib jquery.textarea-with-linenumbers
import "./jquery.textarea-with-linenumbers.js"

//lib highlightjs-11.11.1 with languages "common"
import { hljs } from './highlight.min.js'
//import { hljs } from './highlight.js'



function replaceHtml(rootPath, targets) {
    targets.forEach((target) => {
        $(this).find(target.elem + "[" + target.attr + "]").each(function() {
            $(this).attr(target.attr, $(this).attr(target.attr).replace("${rootPath}", rootPath));
        })
    })
}

function loadHeaderFooter(completeFooter, completeHeader) {
    const faviconHref = $('link[rel="icon"]').attr("href");
    const rootPathRes = /^(.*)style\/favicon\.ico$/.exec(faviconHref);
    var   rootPath    = "";
    if (rootPathRes) {
        rootPath = rootPathRes[1];
    }

    const targets = [
        { elem: "a",      attr: "href"},
        { elem: "img",    attr: "src"},
        { elem: "option", attr: "value"}
    ];

    $("div#header").load(
        rootPath + "style/header.html",
        function(responseText, textStatus, jqXHR) {
            replaceHtml.call(this, rootPath, targets);
            if (completeHeader) {
                completeHeader.call(this, responseText, textStatus, jqXHR);
            }
        }
    );
    $("div#footer").load(
        rootPath + "style/footer.html",
        function(responseText, textStatus, jqXHR) {
            replaceHtml.call(this, rootPath, targets);
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

//error-messages are usually invisible at the beginning not to flash on the page during loading.
//  That's why it appears after a second. VD - visibility delayed
setTimeout(function() { $(".errorVD").css("visibility", "visible"); }, 1000);

//exports to window
window.initPageBasic            = initPageBasic;
window.initPageBasicLnums       = initPageBasicLnums;
window.initPageBasicHili        = initPageBasicHili;
window.initPageBasicLnumsHili   = initPageBasicLnumsHili;
window.footerVersionActivate    = footerVersionActivate;
window.footerVersionDeactivate  = footerVersionDeactivate;
window.headerLangActivate       = headerLangActivate;
window.setLatestVersion         = setLatestVersion;
