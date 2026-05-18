document.addEventListener('click', event => {
    const likeButton = event.target.closest('.prd-like');
    if (!likeButton) return;

    event.preventDefault();
    event.stopPropagation();
    likeButton.classList.toggle('liked');
});
