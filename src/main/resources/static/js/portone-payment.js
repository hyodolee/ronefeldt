(function () {
  const button = document.getElementById('portonePayButton');
  const message = document.getElementById('paymentMessage');

  if (!button) {
    return;
  }

  function setMessage(text, isError) {
    if (!message) {
      return;
    }
    message.textContent = text || '';
    message.classList.toggle('is-error', Boolean(isError));
  }

  async function completePayment(orderId, paymentId) {
    const response = await fetch('/payments/complete', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ orderId: Number(orderId), paymentId })
    });
    const result = await response.json().catch(() => ({
      success: false,
      message: `결제 검증 응답을 해석하지 못했습니다. HTTP ${response.status}`
    }));
    if (!response.ok || !result.success) {
      throw new Error(result.message || '결제 검증에 실패했습니다.');
    }
    window.location.href = result.redirectUrl;
  }

  button.addEventListener('click', async () => {
    if (button.disabled) {
      return;
    }
    if (!window.PortOne) {
      setMessage('PortOne 결제 SDK를 불러오지 못했습니다.', true);
      return;
    }

    button.disabled = true;
    setMessage('결제창을 준비하고 있습니다.', false);

    const dataset = button.dataset;
    try {
      const paymentRequest = {
        storeId: dataset.storeId,
        channelKey: dataset.channelKey,
        paymentId: dataset.paymentId,
        orderName: dataset.orderName,
        totalAmount: Number(dataset.totalAmount),
        currency: 'CURRENCY_KRW',
        payMethod: dataset.payMethod || 'EASY_PAY',
        customer: {
          email: dataset.customerEmail || undefined,
          fullName: dataset.customerName || undefined
        },
        redirectUrl: `${window.location.origin}/order/orderform.html?orderId=${dataset.orderId}`
      };

      if (paymentRequest.payMethod === 'EASY_PAY' && dataset.easyPayProvider) {
        paymentRequest.easyPay = {
          easyPayProvider: dataset.easyPayProvider
        };
      }

      const response = await window.PortOne.requestPayment(paymentRequest);

      if (response && response.code) {
        throw new Error(response.message || '결제가 취소되었거나 실패했습니다.');
      }

      setMessage('결제 결과를 확인하고 있습니다.', false);
      await completePayment(dataset.orderId, response.paymentId || dataset.paymentId);
    } catch (error) {
      setMessage(error.message || '결제 처리 중 오류가 발생했습니다.', true);
      button.disabled = false;
    }
  });
})();
