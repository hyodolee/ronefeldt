const topBanner = document.getElementById('topBanner') || document.getElementById('top-bar');
const topBannerClose = document.getElementById('topBannerClose') || document.getElementById('topBarClose');
const siteHeader = document.getElementById('siteHeader');
const pageTop = document.getElementById('pageTop');
const bannerImages = document.querySelectorAll('.banner-image');
const revealTargets = document.querySelectorAll('.reveal-up');
const productGrid = document.querySelector('.product-grid');
const viewToggles = document.querySelectorAll('.view-toggle');

topBannerClose?.addEventListener('click', () => {
    topBanner?.classList.add('is-hidden', 'hidden');
});

window.addEventListener('scroll', () => {
    siteHeader?.classList.toggle('is-scrolled', window.scrollY > 12);
}, { passive: true });

pageTop?.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));

function setProductView(view) {
    if (!productGrid || !viewToggles.length) return;
    const isList = view === 'list';

    productGrid.classList.toggle('is-list', isList);
    viewToggles.forEach(toggle => {
        const active = toggle.dataset.view === view;
        toggle.classList.toggle('is-active', active);
        toggle.setAttribute('aria-pressed', String(active));
    });
}

viewToggles.forEach(toggle => {
    toggle.addEventListener('click', () => {
        setProductView(toggle.dataset.view || 'grid');
    });
});

if (bannerImages.length > 1) {
    let activeBanner = 0;
    setInterval(() => {
        bannerImages[activeBanner].classList.remove('is-active');
        activeBanner = (activeBanner + 1) % bannerImages.length;
        bannerImages[activeBanner].classList.add('is-active');
    }, 5000);
}

if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
            if (!entry.isIntersecting) return;
            entry.target.classList.add('is-visible');
            observer.unobserve(entry.target);
        });
    }, { threshold: 0.18 });

    revealTargets.forEach(target => observer.observe(target));
} else {
    revealTargets.forEach(target => target.classList.add('is-visible'));
}
