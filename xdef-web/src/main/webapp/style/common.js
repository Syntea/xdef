import "./jquery-4.0.0.min.js";
//import "./jquery-4.0.0.js";
import "./jquery.textarea-with-linenumbers.js";


function replaceHtml(rootPath, targets) {
    targets.forEach((target) => {
        $(this).find(target.elem + "[" + target.attr + "]").each(function() {
            $(this).attr(target.attr, $(this).attr(target.attr).replace("${rootPath}", rootPath));
        })
    })
}

export function loadHeaderFooter(completeFooter, completeHeader) {
    const faviconHref = $('link[rel="icon"]').attr("href");
    const rootPathRes = /^(.*)image\/favicon\.ico$/.exec(faviconHref);
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

export function linenumbers() {
    $("textarea.lined").linenumbers();
}

export function footVerActivate() {
    $("#footVerPas").css("display", "none");
    $("#footVerAct").css("display", "inline");
}

export function footVerDeactivate() {
    $("#footVerPas").css("display", "inline");
    $("#footVerAct").css("display", "none");
}

export function headLangActivate() {
    $("#headLangPas").css("display", "none");
    $("#headLangAct").css("display", "inline");
}


//exports to window
window.loadHeaderFooter     = loadHeaderFooter;
window.linenumbers          = linenumbers;
window.footVerActivate      = footVerActivate;
window.footVerDeactivate    = footVerDeactivate;
window.headLangActivate     = headLangActivate;
