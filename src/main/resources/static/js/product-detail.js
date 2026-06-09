const cartForm = document.querySelector('.cart-add-form');
const quantityInput = cartForm?.querySelector('input[name="quantity"]');
const totalPrice = document.getElementById('detailTotalPrice');
const totalQuantity = document.getElementById('detailTotalQuantity');
const linePrice = document.querySelector('.line-price');
const detailPanel = document.querySelector('.detail-sticky-panel');
const detailHero = document.querySelector('.detail-hero');
const detailLayout = document.querySelector('.detail-layout');

function formatWon(value) {
  return `${Number(value || 0).toLocaleString('ko-KR')}\uC6D0`;
}

function syncDetailTotal() {
  if (!cartForm || !quantityInput || !totalPrice || !totalQuantity) return;

  const price = Number(cartForm.dataset.price || 0);
  const quantity = Math.max(0, Number(quantityInput.value || 0));
  const total = price * quantity;

  totalPrice.textContent = formatWon(total);
  totalQuantity.textContent = `(${quantity}\uAC1C)`;
  if (linePrice) {
    linePrice.textContent = formatWon(total);
  }
}

function syncFloatingPanel() {
  if (!detailPanel || !detailHero || !detailLayout || window.innerWidth <= 1100) {
    detailPanel?.classList.remove('is-fixed');
    detailPanel?.style.removeProperty('--detail-panel-left');
    detailPanel?.style.removeProperty('--detail-panel-width');
    return;
  }

  const panelWidth = detailPanel.offsetWidth;
  const fixedSideGap = Math.min(Math.max(window.innerWidth * 0.055, 42), 108);
  const panelLeft = window.innerWidth - fixedSideGap - panelWidth;

  detailPanel.style.setProperty('--detail-panel-left', `${Math.round(panelLeft)}px`);
  detailPanel.style.setProperty('--detail-panel-width', `${Math.round(panelWidth)}px`);

  const trigger = detailHero.offsetTop + Math.max(360, detailHero.offsetHeight * 0.72);
  detailPanel.classList.toggle('is-fixed', window.scrollY > trigger);
}

quantityInput?.addEventListener('input', syncDetailTotal);
quantityInput?.addEventListener('change', syncDetailTotal);
window.addEventListener('scroll', syncFloatingPanel, { passive: true });
window.addEventListener('resize', syncFloatingPanel);

syncDetailTotal();
syncFloatingPanel();
