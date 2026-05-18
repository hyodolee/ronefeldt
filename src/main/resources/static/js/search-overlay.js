const searchOpen = document.getElementById('searchOpen');
const searchOverlay = document.getElementById('searchOverlay');
const searchClose = document.getElementById('searchClose');
const searchKeyword = document.getElementById('searchKeyword');

function openSearchOverlay(event) {
    event?.preventDefault();
    if (!searchOverlay) return;

    searchOverlay.classList.add('is-open');
    searchOverlay.setAttribute('aria-hidden', 'false');
    setTimeout(() => searchKeyword?.focus(), 120);
}

function closeSearchOverlay() {
    if (!searchOverlay) return;

    searchOverlay.classList.remove('is-open');
    searchOverlay.setAttribute('aria-hidden', 'true');
    searchOpen?.focus();
}

searchOpen?.addEventListener('click', openSearchOverlay);
searchClose?.addEventListener('click', closeSearchOverlay);

window.addEventListener('keydown', event => {
    if (event.key === 'Escape' && searchOverlay?.classList.contains('is-open')) {
        closeSearchOverlay();
    }
});
