// Auto-dismiss toast alerts after 4 seconds
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.app-alert').forEach(function (el) {
        setTimeout(function () {
            el.style.transition = 'opacity .4s ease, transform .4s ease';
            el.style.opacity = '0';
            el.style.transform = 'translateX(40px)';
            setTimeout(function () { el.remove(); }, 400);
        }, 4000);
    });
});

// Populate the delete confirmation modal with the correct target
function setupDeleteModal(modalId) {
    var modal = document.getElementById(modalId);
    if (!modal) return;
    modal.addEventListener('show.bs.modal', function (event) {
        var trigger = event.relatedTarget;
        var action = trigger.getAttribute('data-action');
        var label = trigger.getAttribute('data-label') || 'this item';
        modal.querySelector('form').setAttribute('action', action);
        var target = modal.querySelector('[data-delete-label]');
        if (target) target.textContent = label;
    });
}

document.addEventListener('DOMContentLoaded', function () {
    setupDeleteModal('deleteExpenseModal');
    setupDeleteModal('deleteCategoryModal');
});


/* ============================================================
   PREMIUM UI: background FX, global 3D loader, tilt, counters
   (Additive progressive enhancement — no existing behaviour changed)
   ============================================================ */
(function () {
    var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    var isTouch = window.matchMedia && window.matchMedia('(hover: none)').matches;

    // Global loader API (safe no-ops until DOM ready)
    window.AppLoader = {
        show: function () { var l = document.getElementById('app-loader'); if (l) l.classList.add('show'); },
        hide: function () { var l = document.getElementById('app-loader'); if (l) l.classList.remove('show'); }
    };

    document.addEventListener('DOMContentLoaded', function () {
        injectFx();
        injectLoader();
        bindLoaderTriggers();
        initTilt();
        animateCounters();
    });

    function injectFx() {
        if (document.querySelector('.fx-bg')) return;
        var fx = document.createElement('div');
        fx.className = 'fx-bg';
        fx.setAttribute('aria-hidden', 'true');
        fx.innerHTML = '<span class="fx-orb fx-orb-1"></span>' +
                       '<span class="fx-orb fx-orb-2"></span>' +
                       '<span class="fx-orb fx-orb-3"></span>';
        document.body.appendChild(fx);
    }

    function injectLoader() {
        if (document.getElementById('app-loader')) return;
        var l = document.createElement('div');
        l.id = 'app-loader';
        l.className = 'app-loader';
        l.setAttribute('aria-hidden', 'true');
        l.setAttribute('role', 'status');
        l.innerHTML =
            '<div class="loader-cube">' +
            '<span class="face f1"></span><span class="face f2"></span>' +
            '<span class="face f3"></span><span class="face f4"></span>' +
            '<span class="face f5"></span><span class="face f6"></span>' +
            '</div><div class="loader-text">Loading</div>';
        document.body.appendChild(l);
    }

    function bindLoaderTriggers() {
        // Forms: login / register / add / edit / delete / filter / export
        document.addEventListener('submit', function (e) {
            var form = e.target;
            if (!form || form.nodeName !== 'FORM') return;
            window.AppLoader.show();
            var action = (form.getAttribute('action') || '') + '';
            if (/export/i.test(action)) {
                // File download does not navigate away — auto hide.
                setTimeout(window.AppLoader.hide, 2500);
            }
        }, true);

        // Internal (same-origin) link navigation
        document.addEventListener('click', function (e) {
            var a = e.target.closest ? e.target.closest('a[href]') : null;
            if (!a) return;
            var href = a.getAttribute('href');
            if (!href || href.charAt(0) === '#') return;
            if (a.hasAttribute('download')) return;
            if (a.hasAttribute('data-bs-toggle')) return;
            if (a.target && a.target !== '_self') return;
            if (/^(javascript:|mailto:|tel:)/i.test(href)) return;
            if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
            var url;
            try { url = new URL(a.href, window.location.href); } catch (err) { return; }
            if (url.origin !== window.location.origin) return;
            window.AppLoader.show();
        });

        // Always hide on (re)display, including bfcache back/forward
        window.addEventListener('pageshow', window.AppLoader.hide);
        window.addEventListener('load', window.AppLoader.hide);
    }

    // 3D tilt on stat cards & auth card (desktop pointer only)
    function initTilt() {
        if (reduce || isTouch) return;
        var els = document.querySelectorAll('.stat-card, .auth-card');
        els.forEach(function (el) {
            el.addEventListener('mousemove', function (ev) {
                var r = el.getBoundingClientRect();
                var px = (ev.clientX - r.left) / r.width - 0.5;
                var py = (ev.clientY - r.top) / r.height - 0.5;
                var max = el.classList.contains('auth-card') ? 6 : 9;
                el.style.transform = 'perspective(820px) rotateY(' + (px * max).toFixed(2) + 'deg) rotateX(' +
                    (-py * max).toFixed(2) + 'deg) translateY(-4px)';
            });
            el.addEventListener('mouseleave', function () { el.style.transform = ''; });
        });
    }

    // Animated count-up for dashboard statistic values
    function animateCounters() {
        if (reduce) return;
        document.querySelectorAll('.stat-value').forEach(function (el) {
            var raw = (el.textContent || '').trim();
            var hasRupee = raw.indexOf('\u20B9') === 0;
            var num = parseFloat(raw.replace(/[^0-9.]/g, ''));
            if (isNaN(num)) return;
            var dotPart = raw.split('.')[1] || '';
            var decimals = dotPart.length;
            var dur = 900, start = null;

            function fmt(v) {
                var s = decimals > 0 ? v.toFixed(decimals) : String(Math.round(v));
                var parts = s.split('.');
                parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                return (hasRupee ? '\u20B9' : '') + parts.join('.');
            }
            function step(now) {
                if (start === null) start = now;
                var t = Math.min(1, (now - start) / dur);
                var eased = 1 - Math.pow(1 - t, 3);
                el.textContent = fmt(num * eased);
                if (t < 1) requestAnimationFrame(step);
                else el.textContent = fmt(num);
            }
            requestAnimationFrame(step);
        });
    }
})();
