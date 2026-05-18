// ── PRODUCTS ──
const PRDS=[
  {name:'자스민 골드 LeafCup®',price:26000,img:'https://teehaus.co.kr/web/product/medium/202504/bdff9eb6dc86ddfb89ca8d9f941eb38c.png',sold:true},
  {name:'자스민 골드 TeaCaddy®',price:29000,img:'https://teehaus.co.kr/web/product/medium/202504/60344d98469c52c71d15db5f7e448803.png',sold:true,desc:'향긋한 자스민 향을 지닌 녹차'},
  {name:'Rich Aroma LeafCup',price:25000,img:'https://teehaus.co.kr/web/product/medium/202503/d06664755e02edd978e71168f7af3a66.jpg',sold:true},
  {name:'Healthy Week LeafCup',price:25000,img:'https://teehaus.co.kr/web/product/medium/202503/1cb9e1581adf882dd9714037a5fe7e2a.jpg',sold:true},
  {name:'Week Focus LeafCup',price:25000,img:'https://teehaus.co.kr/web/product/medium/202503/46114c70e4865d5c89226c3b74ee436e.jpg',sold:true},
  {name:'Tee Tumbler',price:40000,img:'https://teehaus.co.kr/web/product/medium/202503/435fa0164ade192c11741ca536f79ef2.jpg',sold:true},
  {name:'Ronnefeldt Weekend Bag',price:35000,img:'https://teehaus.co.kr/web/product/medium/202503/2c1462ba0259b69b37cb87353dcccd3c.jpg',sold:false},
  {name:'자스민 골드 TeaCaddy®',price:29000,img:'https://teehaus.co.kr/web/product/medium/202504/60344d98469c52c71d15db5f7e448803.png',sold:true},
];
function makeCard(p){
  const d=document.createElement('div');d.className='prd-card';
  d.innerHTML=`<div class="prd-img"><img src="${p.img}" alt="${p.name}" loading="eager" fetchpriority="high">${p.sold?'<div class="sold-badge">SOLD OUT</div>':''}<div class="prd-actions"><button type="button" class="prd-action prd-like" aria-label="관심상품" title="관심상품"><i class="fa-regular fa-heart"></i></button><a class="prd-action prd-open" href="${p.url||p.img}" target="_blank" rel="noopener" aria-label="새창으로 열기" title="새창으로 열기"><i class="fa-solid fa-arrow-up-right-from-square"></i></a></div></div><div class="prd-info"><div class="prd-meta"><span class="prd-name">${p.name}</span><span class="prd-review">리뷰 0</span></div>${p.desc?`<div class="prd-subdesc">${p.desc}</div>`:''}<div class="prd-price">${p.price.toLocaleString()}원</div></div>`;
  return d;
}
const MONTH_BEST=[
  [
    {...PRDS[0],desc:'여름 녹차에 자스민을 블렌딩 하여, 깔끔한 맛과 향이 기분전환을 도와주며, 스트레스와 소화불량 해소에 도움이 되는 녹차'},
    PRDS[1],
  ],
  [PRDS[2], PRDS[3]],
  [PRDS[4], PRDS[5]],
];
const monthBestPages=document.getElementById('month-best-pages');
const monthBestDots=document.getElementById('month-best-dots');
const monthBestTrack=document.createElement('div');
monthBestTrack.className='month-best-track';
MONTH_BEST.forEach(group=>{
  const page=document.createElement('div');
  page.className='month-best-page';
  const grid=document.createElement('div');
  grid.className='prd-grid2';
  group.forEach(p=>grid.appendChild(makeCard(p)));
  page.appendChild(grid);
  monthBestTrack.appendChild(page);
});
monthBestPages.appendChild(monthBestTrack);
MONTH_BEST.forEach((_,i)=>{
  const dot=document.createElement('button');
  dot.type='button';
  dot.className='r-dot'+(i===0?' on':'');
  dot.setAttribute('aria-label', `먼스 베스트 ${i + 1}번째 보기`);
  dot.onclick=()=>{
    monthBestTrack.style.transform=`translateX(-${i*100}%)`;
    [...monthBestDots.children].forEach((d,idx)=>d.classList.toggle('on',idx===i));
  };
  monthBestDots.appendChild(dot);
});
const TABS=['신상품','베스트'];
const tnav=document.getElementById('tab-nav'),tpanels=document.getElementById('tab-panels'),productDots=document.getElementById('product-dots');
const PRODUCT_GROUPS=[
  [
    [PRDS[1], PRDS[2], PRDS[4], PRDS[3]],
    [PRDS[0], PRDS[5], PRDS[6], PRDS[7]],
  ],
  [
    [PRDS[0], PRDS[1], PRDS[2], PRDS[3]],
    [PRDS[4], PRDS[5], PRDS[6], PRDS[7]],
  ],
];
let activeProductTab = 0;
let activeProductPage = 0;

function renderProductPanel(tabIndex, pageIndex) {
  const panel = document.getElementById('tp' + tabIndex);
  if (!panel) return;

  const track = panel.querySelector('.product-pages-track');
  if (!track) return;

  activeProductTab = tabIndex;
  activeProductPage = pageIndex;

  track.style.transform = `translateX(-${pageIndex * 100}%)`;
  [...productDots.children].forEach((dot, i) => dot.classList.toggle('on', i === pageIndex));
}

function renderProductDots(tabIndex) {
  productDots.innerHTML = '';
  PRODUCT_GROUPS[tabIndex].forEach((_, i) => {
    const dot = document.createElement('button');
    dot.type = 'button';
    dot.className = 'product-dot' + (i === activeProductPage ? ' on' : '');
    dot.setAttribute('aria-label', `상품 ${i + 1}번째 보기`);
    dot.onclick = () => renderProductPanel(activeProductTab, i);
    productDots.appendChild(dot);
  });
}

TABS.forEach((t,i)=>{
  const btn=document.createElement('button');btn.className='tab-btn'+(i===0?' on':'');btn.textContent=t;
  btn.onclick=()=>{document.querySelectorAll('.tab-btn').forEach(b=>b.classList.remove('on'));document.querySelectorAll('.tab-panel').forEach(p=>p.classList.remove('on'));btn.classList.add('on');document.getElementById('tp'+i).classList.add('on');activeProductPage=0;renderProductDots(i);renderProductPanel(i,0)};
  tnav.appendChild(btn);
  const panel=document.createElement('div');panel.className='tab-panel'+(i===0?' on':'');panel.id='tp'+i;
  const viewport=document.createElement('div');viewport.className='product-pages';
  const track=document.createElement('div');track.className='product-pages-track';
  PRODUCT_GROUPS[i].forEach(group=>{
    const page=document.createElement('div');page.className='product-page';
    const grid=document.createElement('div');grid.className='prd-grid4';
    group.forEach(p=>grid.appendChild(makeCard(p)));
    page.appendChild(grid);
    track.appendChild(page);
  });
  viewport.appendChild(track);
  panel.appendChild(viewport);
  tpanels.appendChild(panel);
});
renderProductDots(0);
renderProductPanel(0,0);

// ── CAMPAIGN SWIPER ──
const CAMP=[
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2026/04/18/b7e5ac42014e29b7634de60762b7e2ba.png',title:'고아웃캠핑페스티발 26th 에서 만나는 로네펠트',desc:'고아웃캠핑페스티발에서 시원한 바닷바람과 함께 즐기는 로네펠트의 특별한 티타임에 여러분을 초대합니다!'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2026/04/18/ea49efbba0c7ba17370d9c160b456ad0.jpg',title:'레스케이프 호텔에서 만나는 로네펠트',desc:'Tea Pairing with Ronnefeldt'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2026/03/11/ad1af41a609959c473cb6e2e1a9602fd.jpg',title:'씨네큐브',desc:'씨네큐브에서 &lt;두 번째 계절&gt;을 관람하시는 분들께'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2025/10/25/ad20f91d9be5d0dcb8760cd21674a9b7.png',title:'로네펠트 X 세화미술관 「Art+ 인문·예술 아카데미」',desc:'로네펠트가 함께하는 세화미술관 「Art+ 인문·예술 아카데미」'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2026/03/11/cdd3b3ad778c44ca84634341699f062f.jpg',title:'로네펠트 X 씨네큐브 티켓 증정 이벤트',desc:"씨네큐브 영화 '내 말 좀 들어줘' 티켓을 드립니다."},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2025/02/28/60715ef906e4ada4ac898c14948fa9d1.jpg',title:'아시아나 비즈니스에서 만나는 로네펠트',desc:'이제 아시아나항공 비즈니스 클래스에서도 우리 로네펠트 티를 즐길 수 있어요.'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2025/02/26/2f215efd5399316fa2788c3c3b1faae5.jpg',title:'쉐프 최현석님의 로네펠트 하이볼',desc:'로네펠트 하이볼을 지금 GS25에서 만나보세요! 최현석 쉐프님과 함께 개발한 로네펠트 하이볼이...'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2025/02/26/1acdfb7634c5c148f8518d11fd763774.jpg',title:'200주년 기념 티 출시',desc:'로네펠트가 창립 200주년을 맞아 기념티를 출시합니다.'},
  {img:'https://teehaus.co.kr/file_data/ronnefeldt/gallery/2025/03/09/982a41fe1088b499545e32246f50fe65.jpg',title:'로네펠트 X 롯데호텔',desc:'"Afternoon Tea Brunch" - with Ronnefeldt'},
];
const campWrap=document.getElementById('campSlides');
CAMP.forEach((c,i)=>{
  const s=document.createElement('div');s.className='swiper-slide camp-slide';
  const loading=i<3?'eager':'lazy';
  const priority=i<3?' fetchpriority="high"':'';
  s.innerHTML=`<div class="camp-slide-img"><img src="${c.img}" alt="${c.title}" loading="${loading}"${priority}></div><div class="camp-slide-info"><span class="camp-border-line top"></span><span class="camp-border-line right"></span><span class="camp-border-line bottom"></span><span class="camp-border-line left"></span><div class="camp-slide-title">${c.title}</div><div class="camp-slide-desc">${c.desc}</div></div>`;
  campWrap.appendChild(s);
});

// ── CAMPAIGN SWIPER (실제 사이트 구조 동일하게 재현) ──
const CAMP_DELAY = 3000;

// 커스텀 스크롤바 핸들 업데이트 함수
function updateCampScrollbar(swiper) {
  const drag = document.getElementById('campDrag');
  const bar  = document.getElementById('campScrollbar');
  if (!drag || !bar) return;

  const totalSlides  = swiper.slides.length;
  const perView      = swiper.params.slidesPerView;
  const barW         = bar.offsetWidth;
  const dragW        = Math.round(barW * perView / totalSlides);
  const maxLeft      = barW - dragW;
  const progress     = swiper.activeIndex / Math.max(1, totalSlides - perView);
  const dragLeft     = Math.round(Math.min(maxLeft, progress * maxLeft));

  drag.style.width   = dragW + 'px';
  drag.style.left    = dragLeft + 'px';
}

let campHeightMap = new Map();

function getCampaignInfoHeight(info) {
  if (!info) return 0;
  return Math.ceil(info.getBoundingClientRect().height || info.offsetHeight || info.scrollHeight);
}

const campSwiper = new Swiper('#campSwiper', {
  slidesPerView  : 3,
  spaceBetween   : 16,
  slidesPerGroup : 3,
  loop           : false,   // 실제 사이트와 동일
  speed          : 700,     // 실제 사이트와 동일
  autoHeight     : false,   // campaign height is animated manually; load corrections stay instant.
  autoplay: {
    delay                : CAMP_DELAY,
    disableOnInteraction : false,
    stopOnLastSlide      : true,
  },
  pagination : false,
  scrollbar  : false,       // Swiper 기본 scrollbar 비활성 → 커스텀 사용
  navigation : {
    nextEl : '.camp-next',
    prevEl : '.camp-prev',
  },
  on: {
    init() {
      measureCampaignLayout(this);
      setCampaignHeight(this, getCampaignActiveHeight(this), 0);
      updateCampScrollbar(this);
    },
    slideChange() {
      setCampaignHeight(this, getCampaignActiveHeight(this), 700);
      updateCampScrollbar(this);
    },
    resize() {
      measureCampaignLayout(this);
      setCampaignHeight(this, getCampaignActiveHeight(this), 0);
      updateCampScrollbar(this);
    },
    slideChangeTransitionEnd() {
      // 마지막 그룹(activeIndex가 마지막 슬라이드 그룹의 시작)에 도착했는지 확인
      // 슬라이드 9개, slidesPerGroup:3 → 그룹 시작 인덱스: 0, 3, 6
      // 마지막 그룹 = activeIndex >= (총슬라이드 - slidesPerView)
      const lastGroupStart = this.slides.length - this.params.slidesPerView;
      if (this.activeIndex >= lastGroupStart) {
        // 이 페이지를 충분히 보여준 뒤 (CAMP_DELAY) 처음으로 부드럽게 복귀
        setTimeout(() => {
          this.slideTo(0, 700); // 700ms 부드러운 전환 (실제 사이트와 동일)
          updateCampScrollbar(this);
          // autoplay 재시작
          setTimeout(() => this.autoplay.start(), 800);
        }, CAMP_DELAY);
      }
    },
  },
});

function getCampaignGroupInfo(swiper) {
  const slides = [...campWrap.querySelectorAll('.camp-slide')];
  const perView = Math.round(swiper.params.slidesPerView || 3);
  const groupSize = Math.round(swiper.params.slidesPerGroup || perView);
  const starts = [];
  for (let i = 0; i < slides.length; i += groupSize) starts.push(i);
  return { slides, perView, groupSize, starts };
}

function getCampaignImageMax(slides) {
  const sec = document.querySelector('.campaign-sec');
  const header = sec.querySelector('.camp-header');
  const bottom = sec.querySelector('.camp-bottom');
  const secStyle = getComputedStyle(sec);
  const headerStyle = getComputedStyle(header);
  const secPadTop = parseFloat(secStyle.paddingTop) || 0;
  const headerMargin = parseFloat(headerStyle.marginBottom) || 0;
  const infoReserve = Math.max(...slides.map(slide => {
    const info = slide.querySelector('.camp-slide-info');
    return getCampaignInfoHeight(info);
  }), 0);
  const available =
    sec.clientHeight - secPadTop - header.offsetHeight - headerMargin - bottom.offsetHeight - infoReserve - 8;
  return Math.max(220, available);
}

function measureCampaignLayout(swiper) {
  const { slides, perView, starts } = getCampaignGroupInfo(swiper);
  slides.forEach(slide => {
    const box = slide.querySelector('.camp-slide-img');
    if (!box) return;
    box.classList.remove('is-capped');
    box.style.removeProperty('height');
  });

  const imageMax = getCampaignImageMax(slides);
  slides.forEach(slide => {
    const box = slide.querySelector('.camp-slide-img');
    const img = box && box.querySelector('img');
    if (!box || !img) return;

    const naturalHeight = Math.ceil(img.getBoundingClientRect().height || box.getBoundingClientRect().height);
    if (naturalHeight > imageMax) {
      box.style.height = imageMax + 'px';
      box.classList.add('is-capped');
    }
  });

  const slideHeights = slides.map(slide =>
    Math.ceil(Math.max(slide.scrollHeight, slide.getBoundingClientRect().height))
  );

  campHeightMap = new Map();

  starts.forEach(start => {
    const visibleHeights = slideHeights.slice(start, start + perView);
    campHeightMap.set(start, {
      swiperHeight: Math.max(...visibleHeights, 0)
    });
  });

  swiper.el.classList.add('camp-fitted');
}

function getCampaignActiveHeight(swiper) {
  const { groupSize } = getCampaignGroupInfo(swiper);
  const activeStart = Math.floor(swiper.activeIndex / groupSize) * groupSize;
  const group = campHeightMap.get(activeStart) || campHeightMap.get(0);
  return group ? group.swiperHeight : swiper.el.getBoundingClientRect().height;
}

function setCampaignHeight(swiper, targetHeight, duration) {
  const el = swiper.el;
  const nextHeight = Math.round(targetHeight);
  const currentHeight = Math.round(el.getBoundingClientRect().height || nextHeight);
  if (!duration || Math.abs(currentHeight - nextHeight) < 2) {
    el.style.transitionDuration = '0ms';
    el.style.height = nextHeight + 'px';
    return;
  }

  el.style.transitionDuration = '0ms';
  el.style.height = currentHeight + 'px';
  el.offsetHeight;
  requestAnimationFrame(() => {
    el.style.transitionDuration = duration + 'ms';
    el.style.height = nextHeight + 'px';
  });
}

function refreshCampaignLayoutInstant() {
  measureCampaignLayout(campSwiper);
  setCampaignHeight(campSwiper, getCampaignActiveHeight(campSwiper), 0);
  updateCampScrollbar(campSwiper);
}

function getCampScrollbarState(swiper) {
  const bar = document.getElementById('campScrollbar');
  const drag = document.getElementById('campDrag');
  if (!bar || !drag) return null;

  const totalSlides = swiper.slides.length;
  const perView = Math.round(swiper.params.slidesPerView || 3);
  const groupSize = Math.round(swiper.params.slidesPerGroup || perView);
  const maxIndex = Math.max(0, totalSlides - perView);
  const barRect = bar.getBoundingClientRect();
  const dragW = Math.round(barRect.width * perView / totalSlides);
  const maxLeft = Math.max(0, barRect.width - dragW);

  return { bar, drag, groupSize, maxIndex, barRect, dragW, maxLeft };
}

function getCampIndexFromPointer(swiper, clientX, pointerOffset) {
  const state = getCampScrollbarState(swiper);
  if (!state) return swiper.activeIndex;

  const offset = pointerOffset == null ? state.dragW / 2 : pointerOffset;
  const rawLeft = clientX - state.barRect.left - offset;
  const clampedLeft = Math.max(0, Math.min(state.maxLeft, rawLeft));
  const progress = state.maxLeft ? clampedLeft / state.maxLeft : 0;
  const rawIndex = progress * state.maxIndex;
  return Math.min(
    state.maxIndex,
    Math.round(rawIndex / state.groupSize) * state.groupSize
  );
}

function setCampDragPreview(swiper, clientX, pointerOffset) {
  const state = getCampScrollbarState(swiper);
  if (!state) return;

  const offset = pointerOffset == null ? state.dragW / 2 : pointerOffset;
  const left = Math.max(0, Math.min(state.maxLeft, clientX - state.barRect.left - offset));
  state.drag.style.width = state.dragW + 'px';
  state.drag.style.left = Math.round(left) + 'px';
}

function moveCampaignByScrollbar(clientX, pointerOffset, speed) {
  const targetIndex = getCampIndexFromPointer(campSwiper, clientX, pointerOffset);
  campSwiper.autoplay.stop();
  campSwiper.slideTo(targetIndex, speed);
  setCampaignHeight(campSwiper, getCampaignActiveHeight(campSwiper), speed);
  updateCampScrollbar(campSwiper);
}

function bindCampScrollbar() {
  const state = getCampScrollbarState(campSwiper);
  if (!state) return;

  let dragging = false;
  let pointerOffset = null;

  state.bar.addEventListener('pointerdown', e => {
    e.preventDefault();
    const nextState = getCampScrollbarState(campSwiper);
    if (!nextState) return;

    dragging = true;
    const dragRect = nextState.drag.getBoundingClientRect();
    pointerOffset =
      e.target === nextState.drag
        ? Math.max(0, Math.min(nextState.dragW, e.clientX - dragRect.left))
        : nextState.dragW / 2;

    nextState.drag.classList.add('dragging');
    nextState.bar.setPointerCapture(e.pointerId);
    setCampDragPreview(campSwiper, e.clientX, pointerOffset);
  });

  state.bar.addEventListener('pointermove', e => {
    if (!dragging) return;
    setCampDragPreview(campSwiper, e.clientX, pointerOffset);
  });

  const finishDrag = e => {
    if (!dragging) return;
    dragging = false;
    state.drag.classList.remove('dragging');
    if (state.bar.hasPointerCapture(e.pointerId)) state.bar.releasePointerCapture(e.pointerId);
    moveCampaignByScrollbar(e.clientX, pointerOffset, 700);
    pointerOffset = null;
  };

  state.bar.addEventListener('pointerup', finishDrag);
  state.bar.addEventListener('pointercancel', finishDrag);
}

campWrap.querySelectorAll('img').forEach(img => {
  if (img.complete) return;
  img.addEventListener('load', refreshCampaignLayoutInstant, { once:true });
  img.addEventListener('error', refreshCampaignLayoutInstant, { once:true });
});
refreshCampaignLayoutInstant();
bindCampScrollbar();
window.addEventListener('resize', refreshCampaignLayoutInstant);
if (document.fonts && document.fonts.ready) {
  document.fonts.ready.then(refreshCampaignLayoutInstant);
}

// Instagram
const ig=document.getElementById('insta-grid');
if (ig) {
  [
    'https://teehaus.co.kr/web/product/medium/202504/bdff9eb6dc86ddfb89ca8d9f941eb38c.png',
    'https://teehaus.co.kr/web/product/medium/202503/d06664755e02edd978e71168f7af3a66.jpg',
    'https://teehaus.co.kr/web/product/medium/202504/60344d98469c52c71d15db5f7e448803.png',
    'https://teehaus.co.kr/web/product/medium/202503/2c1462ba0259b69b37cb87353dcccd3c.jpg',
    'https://teehaus.co.kr/web/product/medium/202503/435fa0164ade192c11741ca536f79ef2.jpg',
    'https://teehaus.co.kr/web/product/medium/202503/46114c70e4865d5c89226c3b74ee436e.jpg',
  ].forEach(src=>{
    const d=document.createElement('div');d.className='insta-post';
    const img=document.createElement('img');img.src=src;img.alt='';img.loading='lazy';
    img.onerror=()=>{d.style.background='#d4d0c8'};
    d.appendChild(img);ig.appendChild(d);
  });
}

// ── BANNER SLIDER ──
const slides=document.querySelectorAll('.slide');
const sdots=document.getElementById('sl-dots'),scnt=document.getElementById('sl-counter');
let sc=0,stimer;const stotal=slides.length;
slides.forEach((_,i)=>{
  const d=document.createElement('button');d.className='s-dot'+(i===0?' on':'');
  d.onclick=()=>{clearInterval(stimer);slideTo(i);stimer=setInterval(()=>slideTo((sc+1)%stotal),5000)};
  sdots.appendChild(d);
});
function slideTo(n){slides[sc].classList.remove('on');sdots.children[sc].classList.remove('on');sc=n;slides[sc].classList.add('on');sdots.children[sc].classList.add('on');scnt.textContent=(sc+1)+' / '+stotal}
stimer=setInterval(()=>slideTo((sc+1)%stotal),5000);
document.getElementById('sl-prev').onclick=()=>{clearInterval(stimer);slideTo((sc-1+stotal)%stotal);stimer=setInterval(()=>slideTo((sc+1)%stotal),5000)};
document.getElementById('sl-next').onclick=()=>{clearInterval(stimer);slideTo((sc+1)%stotal);stimer=setInterval(()=>slideTo((sc+1)%stotal),5000)};

// ── FULLPAGE ──
const fpInner=document.getElementById('fp-inner');
const fpDots=document.getElementById('fp-dots');
const headerEl=document.getElementById('header');
const socialEl=document.getElementById('social-bar');
const topBarEl=document.getElementById('top-bar');
const topBarClose=document.getElementById('topBarClose');
const secs=[...document.querySelectorAll('.fp-sec')];
let cur=0,scrolling=false;
const TOP_BAR_COOKIE='teehaus_top_banner_closed';
let topBarClosed=document.cookie.split('; ').some(v=>v===TOP_BAR_COOKIE+'=1');
const N=secs.length;
for(let i=0;i<N-1;i++){const d=document.createElement('button');d.className='fp-dot';d.onclick=()=>goTo(i);fpDots.appendChild(d)}

// ANIMATION: reset then fire
function triggerAnims(secEl){
  const els=secEl.querySelectorAll('.anim-top,.anim-bot,.anim-left,.anim-right');
  els.forEach(el=>el.classList.remove('anim-in'));
  // double rAF ensures browser processes the removal before adding
  requestAnimationFrame(()=>requestAnimationFrame(()=>{
    els.forEach(el=>el.classList.add('anim-in'));
  }));
}

function updateUI(){
  const isDark=secs[cur].dataset.dark==='1';
  const isFooter=!!secs[cur].dataset.footer;
  headerEl.classList.toggle('dark',isDark);
  topBarEl.classList.toggle('hidden',cur!==0||topBarClosed);
  socialEl.className=isDark?'dark':'';
  if(isFooter) socialEl.classList.add('hidden');
  [...fpDots.children].forEach((d,i)=>{
    d.classList.toggle('active',i===cur);
    d.classList.toggle('dark-dot',isDark);
  });
}

topBarClose.addEventListener('click',()=>{
  if(confirm('오늘 하루동안 열지않기')){
    topBarClosed=true;
    const maxAge=60*60*24;
    document.cookie=TOP_BAR_COOKIE+'=1; max-age='+maxAge+'; path=/';
    topBarEl.classList.add('hidden');
  }
});

function goTo(n){
  if(n<0||n>=N||n===cur||scrolling) return;
  cur=n;
  fpInner.style.transform=`translateY(-${cur*100}vh)`;
  updateUI();
  triggerAnims(secs[cur]);
  scrolling=true;
  setTimeout(()=>scrolling=false,950);
}

let lastW=0;
window.addEventListener('wheel',e=>{
  e.preventDefault();
  if(scrolling) return;
  const now=Date.now();if(now-lastW<800) return;lastW=now;
  if(e.deltaY>0) goTo(cur+1); else goTo(cur-1);
},{passive:false});
let ty=0;
window.addEventListener('touchstart',e=>{ty=e.touches[0].clientY},{passive:true});
window.addEventListener('touchend',e=>{if(scrolling) return;const d=ty-e.changedTouches[0].clientY;if(Math.abs(d)<50) return;if(d>0) goTo(cur+1); else goTo(cur-1)},{passive:true});
window.addEventListener('keydown',e=>{if(e.key==='ArrowDown'||e.key==='PageDown') goTo(cur+1);if(e.key==='ArrowUp'||e.key==='PageUp') goTo(cur-1)});

updateUI();
// 첫 섹션 애니메이션 — 페이지 로드 후 실행
setTimeout(()=>triggerAnims(secs[0]),400);
