//lib jquery 4.0.0
import "./jquery-4.0.0.min.js"
//import "./jquery-4.0.0.js"

//lib jquery.textarea-with-linenumbers
import "./jquery.textarea-with-linenumbers.js"

//lib highlightjs-11.11.1 with languages "common"
import { hljs } from './highlight.min.js'
//import { hljs } from './highlight.js'

/** Absolute path of the webapp root, derived from the location of this script ("<webapp-root>/style/common.js") */
const rootApp = new URL("..", import.meta.url).pathname;

/**
 * Rewrite "${rootPath}"-prefixed attributes (as inserted by loadHeaderFooter()) into real, resolved
 * paths within the just-loaded header/footer fragment.
 *
 * @param {string} rootPath value to substitute for the "${rootPath}" placeholder
 * @param {Array<{elem: string, attr: string}>} targets element/attribute pairs to rewrite,
 *                                                      e.g. {elem: "a", attr: "href"}
 */
function replaceHtml(rootPath, targets) {
    targets.forEach((target) => {
        $(this).find(target.elem + "[" + target.attr + "]").each(function() {
            $(this).attr(target.attr, $(this).attr(target.attr).replaceAll("${rootPath}", rootPath));
        })
    })
}

/**
 * Load the shared header/footer fragments into the page's "#header"/"#footer" divs and rewrite their
 * "${rootPath}"-prefixed links. The root is derived from the current page's own favicon location (not
 * from rootApp/common.js's location), since each language subsite (cs/, es/, eo/, ...) has its own
 * localized style/header.html + style/footer.html.
 *
 * @param {Function} [completeFooter] called as completeFooter(responseText, textStatus, jqXHR) after
 *   the footer fragment has loaded and been rewritten.
 * @param {Function} [completeHeader] called as completeHeader(responseText, textStatus, jqXHR) after
 *   the header fragment has loaded and been rewritten.
 */
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
}

/**
 * Basic page init: load the shared header/footer.
 *
 * @param {Function} [completeFooter] see loadHeaderFooter().
 * @param {Function} [completeHeader] see loadHeaderFooter().
 */
export function initPageBasic(completeFooter, completeHeader) {
    loadHeaderFooter(completeFooter, completeHeader);
}

/**
 * Page init for pages with line-numbered textareas: enables line numbers on all
 * "textarea.linenumbers" elements, then loads the shared header/footer.
 *
 * @param {Function} [completeFooter] see loadHeaderFooter().
 * @param {Function} [completeHeader] see loadHeaderFooter().
 */
export function initPageBasicLnums(completeFooter, completeHeader) {
    $("textarea.linenumbers").linenumbers();
    loadHeaderFooter(completeFooter, completeHeader);
}

/**
 * Page init for pages with syntax-highlighted code blocks: runs highlight.js over the page, then loads
 * the shared header/footer.
 *
 * @param {Function} [completeFooter] see loadHeaderFooter().
 * @param {Function} [completeHeader] see loadHeaderFooter().
 */
export function initPageBasicHili(completeFooter, completeHeader) {
    hljs.highlightAll();
    loadHeaderFooter(completeFooter, completeHeader);
}

/**
 * Page init combining initPageBasicLnums() and initPageBasicHili(): line numbers, syntax highlighting,
 * then the shared header/footer.
 *
 * @param {Function} [completeFooter] see loadHeaderFooter().
 * @param {Function} [completeHeader] see loadHeaderFooter().
 */
export function initPageBasicLnumsHili(completeFooter, completeHeader) {
    $("textarea.linenumbers").linenumbers();
    hljs.highlightAll();
    loadHeaderFooter(completeFooter, completeHeader);
}

/** Show the "active" footer version marker and hide the "passive" one. */
export function footerVersionActivate() {
    $("#footerVersionPas").css("display", "none");
    $("#footerVersionAct").css("display", "inline");
}

/** Show the "passive" footer version marker and hide the "active" one. */
export function footerVersionDeactivate() {
    $("#footerVersionPas").css("display", "inline");
    $("#footerVersionAct").css("display", "none");
}

/** Show the active-language marker(s) in the header and hide the passive one(s). */
export function headerLangActivate() {
    $(".headerLangPas").css("display", "none");
    $(".headerLangAct").css("display", "inline");
}

/**
 * Handle a change of the header's language-select: save the chosen language into the "lang" cookie (read
 * server-side, e.g. by Playground, to localize X-definition report messages), then navigate to the
 * selected language's page.
 *
 * @param {HTMLSelectElement} select the language-select element; the selected option's "lang" attribute
 *   is the 2-letter language code, its value is the target URL.
 */
export function headerLangChange(select) {
    const lang = select.options[select.selectedIndex].lang;
    if (lang) {
        document.cookie = "lang=" + encodeURIComponent(lang) + "; path=" + rootApp + "; max-age=31536000";
    }
    location = select.value;
}

/**
 * Redirect from this (English) landing page to a localized "&lt;lang&gt;/index.html" landing page, based
 * on the "lang" cookie (see headerLangChange()) or else the browser's preferred language(s). Does nothing
 * if the resolved language is English, unset, or not one of the localized subsites.
 */
export function redirectToLangIndex() {
    const supported = ["cs", "es", "eo"];

    const cookieMatch = document.cookie.match(/(?:^|;\s*)lang=([^;]*)/);
    let   lang        = cookieMatch ? decodeURIComponent(cookieMatch[1]) : null;

    if (!lang) {
        const browserLangs = navigator.languages && navigator.languages.length ? navigator.languages : [navigator.language];
        lang = browserLangs.map(tag => tag.split("-")[0].toLowerCase()).find(l => supported.includes(l));
    } else {
        lang = lang.split("-")[0].toLowerCase();
    }

    if (supported.includes(lang)) {
        location.replace(rootApp + lang + "/");
    }
}

/**
 * Fetch the latest released X-definition version from the "LatestVersion" servlet and substitute it
 * into every "span.latestVersion" text and every "--.--.--"  placeholder found in the href/title of
 * elements matching "a.latestVersion".
 */
export function setLatestVersion() {
    $.get(rootApp + "LatestVersion", function(version) {
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

/**
 * Init the form-field "databaseName" on the Playground pages: fill it from the "databaseName" cookie,
 * or generate a random value if there is none yet (only if fillEmpty is true), and save it back to the
 * cookie only when the field's form is submitted with a non-empty value (so it reflects whatever value
 * was actually sent, incl. hand-typed ones).
 *
 * @param {boolean} [fillEmpty=true] if true (default), an empty field is filled from the cookie or a
 *   random value; if false, an empty field is left empty (the form is still wired to save on submit).
 */
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
window.headerLangChange             = headerLangChange;
window.redirectToLangIndex          = redirectToLangIndex;
window.setLatestVersion             = setLatestVersion;
window.initFormFieldDatabaseName    = initFormFieldDatabaseName;
