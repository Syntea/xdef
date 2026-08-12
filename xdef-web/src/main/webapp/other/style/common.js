/** look of the "button.pulldownIcon" control in both states of the block it belongs to, see togglePulldown() */
const pulldownIconText = {
    collapsed: { arrow: "▼", text: "expand"   },
    expanded:  { arrow: "▲", text: "collapse" }
};

/** a little joke: the control gains one arrow on every toggle; having reached the upper count it starts
 *  losing them again down to the lower one, and so on back and forth */
const pulldownIconArrows = { min: 4, max: 20 };

/**
 * Expand a shortened "div.pulldown" block, or collapse it back, and switch its "button.pulldownIcon"
 * control accordingly. Called from that control, which is placed right above the block (the block itself
 * is not clickable, so that the text and the code samples in it can be selected and copied).
 *
 * @param {HTMLElement} elem the clicked element - the "button.pulldownIcon", or the "div.pulldown" itself.
 */
export function togglePulldown(elem) {
    const pulldown = elem.classList.contains("pulldown") ? elem : elem.nextElementSibling;
    if (!pulldown || !pulldown.classList.contains("pulldown")) {
        return;
    }

    const expanded = pulldown.classList.toggle("expanded");
    const icon     = pulldown.previousElementSibling;

    animatePulldown(pulldown, expanded);

    if (icon && icon.classList.contains("pulldownIcon")) {
        switchPulldownIcon(icon, expanded);
    }
}

/**
 * Switch a "button.pulldownIcon" control into the state its block has just got into: the arrows point to
 * what the next click will do, and - a little joke - they gain one on every switch, up to
 * {@link pulldownIconArrows}.max, then they lose one down to its .min, and so on back and forth.
 * <p>
 * The word behind the arrows says what the next click does as well, unless the control has a word of its
 * own in its "data-text" attribute (a control that does both ways at once has nothing else to say).
 *
 * @param {HTMLElement} icon     the control to be switched.
 * @param {boolean}     expanded whether the block belonging to it has just been expanded, or collapsed.
 */
function switchPulldownIcon(icon, expanded) {
    const iconText = expanded ? pulldownIconText.expanded : pulldownIconText.collapsed;

    //number of the arrows, going up and down between the two counts, see pulldownIconArrows;
    //  kept aside from the text of the control, which carries the arrows and a word together
    let step   = Number(icon.dataset.arrowStep) || 1;
    let arrows = (Number(icon.dataset.arrows) || pulldownIconArrows.min) + step;
    if (arrows >= pulldownIconArrows.max) {
        arrows = pulldownIconArrows.max;
        step   = -1;
    } else if (arrows <= pulldownIconArrows.min) {
        arrows = pulldownIconArrows.min;
        step   = 1;
    }
    icon.dataset.arrowStep = step;
    icon.dataset.arrows    = arrows;

    const text = icon.dataset.text;
    if (text) {
        //a control doing both ways at once: its word stays as it is and the arrows alternate, so that
        //  neither of them claims a direction - it would be wrong as soon as one block is switched alone
        icon.textContent = (pulldownIconText.collapsed.arrow + pulldownIconText.expanded.arrow)
            .repeat(Math.ceil(arrows / 2)).slice(0, arrows) + " " + text;
    } else {
        icon.textContent = iconText.arrow.repeat(arrows) + " " + iconText.text;
        //the control is a button, so it can tell its state to the assistive technologies
        icon.setAttribute("aria-expanded", expanded);
    }
}

/**
 * Run the expand/collapse transition of a "div.pulldown" between its collapsed height and the real height
 * of its content. The css alone can only transition to a fixed "max-height" large enough for any content,
 * which makes the part of the transition between that value and the real height invisible - a delay before
 * anything starts to move, the more striking the shorter the content is.
 * <p>
 * Once expanded, the limit is dropped altogether, so that the content is never cut off when it reflows
 * (e.g. on a resize of the window). Collapsing therefore has to put the current height back first, and let
 * the browser take it, before the collapsed value can be transitioned to.
 *
 * @param {HTMLElement} pulldown the block being expanded or collapsed.
 * @param {boolean}     expanded whether the block is being expanded, or collapsed.
 */
function animatePulldown(pulldown, expanded) {
    if (expanded) {
        pulldown.style.maxHeight = pulldown.scrollHeight + "px";
        //no limit after the transition has finished, unless it has been collapsed again meanwhile
        pulldown.addEventListener("transitionend", () => {
            if (pulldown.classList.contains("expanded")) {
                pulldown.style.maxHeight = "none";
            }
        }, { once: true });
    } else {
        pulldown.style.maxHeight = pulldown.scrollHeight + "px";
        void pulldown.offsetHeight;         //let the browser take that height, otherwise nothing animates
        pulldown.style.maxHeight = "";      //back to the collapsed height given by the css
    }
}

/**
 * Expand, or collapse, all the expandable blocks of the page at once - both the "div.pulldown" ones
 * (their controls are switched with them, see togglePulldown()) and the native "details" ones.
 *
 * @param {boolean} [expand] true to expand them all, false to collapse them all; when not given, they are
 *   all collapsed if any of them is expanded, and all expanded if none of them is - so that a single
 *   control can do both.
 */
export function togglePulldownAll(expand) {
    const pulldowns = [...document.querySelectorAll("div.pulldown")];
    const details   = [...document.querySelectorAll("details")];

    if (expand === undefined) {
        expand = !pulldowns.some(pulldown => pulldown.classList.contains("expanded"))
              && !details.some(details => details.open);
    }

    pulldowns.forEach(pulldown => {
        if (pulldown.classList.contains("expanded") !== expand) {
            togglePulldown(pulldown);
        }
    });
    details.forEach(details => {
        details.open = expand;
    });

    //a page whose control is a "pulldownIcon" gets it switched too - including the joke with the arrows
    document.querySelectorAll(".pulldownAll button.pulldownIcon").forEach(icon => {
        switchPulldownIcon(icon, expand);
    });
}



//exports to window - the controls in the pages call them from their "onclick"
window.togglePulldown               = togglePulldown;
window.togglePulldownAll            = togglePulldownAll;
