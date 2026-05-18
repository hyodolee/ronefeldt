const menuOpen = document.getElementById('menuOpen');
const slidingMenu = document.getElementById('slidingMenu');
const slideMenuClose = document.getElementById('slideMenuClose');
const slideMenuCover = document.getElementById('slideMenuCover');
const slideBannerClose = document.getElementById('slideBannerClose');
const slideBottomBanner = document.getElementById('slideBottomBanner');

function openSlidingMenu() {
    if (!slidingMenu) return;

    slidingMenu.classList.add('is-open');
    slidingMenu.setAttribute('aria-hidden', 'false');
}

function closeSlidingMenu() {
    if (!slidingMenu) return;

    slidingMenu.classList.remove('is-open');
    slidingMenu.setAttribute('aria-hidden', 'true');
    menuOpen?.focus();
}

menuOpen?.addEventListener('click', openSlidingMenu);
slideMenuClose?.addEventListener('click', closeSlidingMenu);
slideMenuCover?.addEventListener('click', closeSlidingMenu);
slideBannerClose?.addEventListener('click', event => {
    event.stopPropagation();
    if (slideBottomBanner) slideBottomBanner.style.display = 'none';
});

window.addEventListener('keydown', event => {
    if (event.key === 'Escape' && slidingMenu?.classList.contains('is-open')) {
        closeSlidingMenu();
    }
});
