(() => {
    "use strict";

    function initializeBookingConfirmation() {
        const form = document.getElementById("confirmForm");
        const submitButton = document.getElementById("paymentSubmitButton");
        const paymentGuide = document.getElementById("paymentGuide");

        if (!form || !submitButton) {
            return;
        }

        const agreements = Array.from(
            form.querySelectorAll(".required-agreement")
        );

        const paymentMethods = Array.from(
            form.querySelectorAll(".payment-method-radio")
        );

        let processing = false;
		let activeBookingId = null;
		let activeTossPayment = null;
		let paymentHistoryGuardActive = false;
		let ignoreNextPopstate = false;
		let cancellationInProgress = false;

        function allAgreementsChecked() {
            return agreements.length > 0
                && agreements.every(agreement => agreement.checked);
        }

        function getSelectedPaymentMethod() {
            return paymentMethods.find(paymentMethod => paymentMethod.checked)?.value || "";
        }

        function paymentMethodSelected() {
            return getSelectedPaymentMethod() !== "";
        }

        function updateState() {
            const allAgreed = allAgreementsChecked();

            paymentMethods.forEach(paymentMethod => {
                paymentMethod.disabled = !allAgreed;

                if (!allAgreed) {
                    paymentMethod.checked = false;
                }

                const label = paymentMethod.closest(".payment-method");

                if (label) {
                    label.classList.toggle("disabled", !allAgreed);
                    label.classList.toggle("selected", paymentMethod.checked);
                    label.setAttribute("aria-disabled", String(!allAgreed));
                }
            });

            const methodSelected = paymentMethodSelected();

            submitButton.disabled =
                processing || !allAgreed || !methodSelected;

            if (paymentGuide) {
                if (processing) {
                    paymentGuide.textContent =
                        "좌석을 확보하고 결제창을 준비하고 있습니다.";
                } else if (!allAgreed) {
                    paymentGuide.textContent =
                        "필수 확인 및 동의 후 결제수단을 선택할 수 있습니다.";
                } else if (!methodSelected) {
                    paymentGuide.textContent =
                        "결제수단을 선택해 주세요.";
                } else {
                    paymentGuide.textContent =
                        "선택한 결제수단으로 결제를 진행합니다.";
                }
            }
        }

        function createCsrfHeaders(headers = {}) {
            const csrfToken =
                document.querySelector('meta[name="_csrf"]')?.content || "";

            const csrfHeader =
                document.querySelector('meta[name="_csrf_header"]')?.content || "";

            if (csrfHeader && csrfToken) {
                headers[csrfHeader] = csrfToken;
            }

            return headers;
        }

        async function readResponse(response) {
            const contentType =
                response.headers.get("content-type") || "";

            let data;

            if (contentType.includes("application/json")) {
                data = await response.json();
            } else {
                data = {
                    message: await response.text()
                };
            }

            if (!response.ok) {
                const error = new Error(
                    data.message || "요청 처리에 실패했습니다."
                );

                error.redirectUrl = data.redirectUrl || "";
                throw error;
            }

            return data;
        }

        async function postForm(url, formData) {
            const response = await fetch(url, {
                method: "POST",
                headers: createCsrfHeaders({
                    "Accept": "application/json"
                }),
                body: formData
            });

            return readResponse(response);
        }

        async function postJson(url, body) {
            const response = await fetch(url, {
                method: "POST",
                headers: createCsrfHeaders({
                    "Content-Type": "application/json",
                    "Accept": "application/json"
                }),
                body: JSON.stringify(body)
            });

            return readResponse(response);
        }

        async function releaseHold(bookingId) {
            if (!Number.isInteger(bookingId) || bookingId <= 0) {
                return;
            }

            try {
                await postJson(
                    "/booking/reserve/pay/cancel",
                    {
                        bookingId: bookingId
                    }
                );
            } catch (error) {
                console.error(
                    "좌석 HOLD 해제 실패:",
                    error
                );
            }
        }
		
		
		function restorePaymentButton() {
		    processing = false;

		    submitButton.textContent =
		        "결제하기";

		    updateState();
		}
		
		
		async function closeActiveTossWindow() {
		    const payment =
		        activeTossPayment;

		    activeTossPayment = null;

		    if (!payment) {
		        return;
		    }

		    try {
		        await payment.destroy();

		    } catch (error) {
		        if (error?.code !== "NO_ACTIVE_PAYMENT_REQUEST") {
		            console.warn(
		                "토스 결제창 닫기 오류:",
		                error
		            );
		        }
		    }
		}


		function addPaymentHistoryGuard() {
		    if (paymentHistoryGuardActive) {
		        return;
		    }

		    window.history.pushState(
		        {
		            tossPaymentGuard: true
		        },
		        "",
		        window.location.href
		    );

		    paymentHistoryGuardActive = true;
		}


		function removePaymentHistoryGuard() {
		    if (!paymentHistoryGuardActive) {
		        return;
		    }

		    paymentHistoryGuardActive = false;

		    ignoreNextPopstate = true;

		    window.history.back();
		}


		async function cancelActiveTossPayment(
		    shouldRemoveHistoryGuard,
		    message
		) {
		    if (cancellationInProgress) {
		        return;
		    }

		    const bookingId =
		        activeBookingId;

		    if (!Number.isInteger(bookingId)
		            || bookingId <= 0) {

		        return;
		    }

		    cancellationInProgress = true;

		    activeBookingId = null;
			
			await closeActiveTossWindow();

		    if (shouldRemoveHistoryGuard) {
		        removePaymentHistoryGuard();
		    }

		    await releaseHold(bookingId);

		    cancellationInProgress = false;

		    restorePaymentButton();

		    if (message) {
		        window.alert(message);
		    }
		}
		
		
		window.addEventListener(
		    "popstate",
		    async () => {

		        if (ignoreNextPopstate) {
		            ignoreNextPopstate = false;
		            return;
		        }

		        if (!paymentHistoryGuardActive
		                || !activeBookingId
		                || cancellationInProgress) {

		            return;
		        }

		        paymentHistoryGuardActive = false;

		        const cancelPayment =
		            window.confirm(
		                "결제를 중단하시겠습니까?\n\n"
		                + "[확인] 결제 중단\n"
		                + "[취소] 계속 결제"
		            );

		        if (cancelPayment) {

		            await cancelActiveTossPayment(
		                false,
		                "결제가 취소되었습니다.\n"
		            );

		            return;
		        }

		        addPaymentHistoryGuard();
		    }
		);
		
		

        async function openTossPayment(
            bookingId,
            paymentMethod
        ) {
            if (typeof TossPayments !== "function") {
                throw new Error(
                    "토스 결제 모듈을 불러오지 못했습니다."
                );
            }

            const prepare = await postJson(
                "/booking/reserve/toss/prepare",
                {
                    bookingId: bookingId,
                    method: paymentMethod
                }
            );

            if (!prepare.clientKey
                    || !prepare.customerKey
                    || !prepare.orderId
                    || !prepare.totalAmount) {

                throw new Error(
                    "토스 결제 준비 정보가 올바르지 않습니다."
                );
            }

            const tossPayments =
                TossPayments(prepare.clientKey);

            const payment =
                tossPayments.payment({
                    customerKey: prepare.customerKey
                });
			
			activeTossPayment = payment;

            const successUrl =
                window.location.origin
                + "/booking/reserve/toss/success"
                + "?bookingId="
                + bookingId;

            const failUrl =
                window.location.origin
                + "/booking/reserve/toss/fail"
                + "?bookingId="
                + bookingId;

            const commonRequest = {
                amount: {
                    currency: "KRW",
                    value: Number(prepare.totalAmount)
                },
                orderId: prepare.orderId,
                orderName: prepare.orderName,
                successUrl: successUrl,
                failUrl: failUrl
            };

            if (paymentMethod === "TOSSPAY") {
                await payment.requestPayment({
                    ...commonRequest,
                    method: "CARD",
                    card: {
                        flowMode: "DIRECT",
                        easyPay: "TOSSPAY"
                    }
                });

                return;
            }

            if (paymentMethod === "TRANSFER") {
                await payment.requestPayment({
                    ...commonRequest,
                    method: "TRANSFER"
                });

                return;
            }

            throw new Error(
                "지원하지 않는 토스 결제수단입니다."
            );
        }

        form.addEventListener(
            "change",
            updateState
        );

        form.addEventListener(
            "submit",
            async event => {
                if (!allAgreementsChecked()) {
                    event.preventDefault();

                    window.alert(
                        "필수 확인 및 동의 항목을 모두 체크해 주세요."
                    );

                    updateState();
                    return;
                }

                const paymentMethod =
                    getSelectedPaymentMethod();

                if (!paymentMethod) {
                    event.preventDefault();

                    window.alert(
                        "결제수단을 선택해 주세요."
                    );

                    updateState();
                    return;
                }

                /*
                 * 카카오페이와 신용카드는 기존 폼 제출을 유지한다.
                 */
                if (paymentMethod === "KAKAOPAY"
                        || paymentMethod === "CARD") {

                    processing = true;
                    submitButton.disabled = true;
                    submitButton.textContent =
                        "좌석을 확인하는 중...";

                    return;
                }

				/*
				 * 토스페이와 계좌이체는 페이지 이동을 막고
				 * 현재 화면에서 토스 결제창을 직접 실행한다.
				 */
				event.preventDefault();

				if (processing) {
				    return;
				}

				const paymentMethodName =
				    paymentMethod === "TOSSPAY"
				        ? "토스페이"
				        : "계좌이체";

				const paymentConfirmed =
				    window.confirm(
				        "1. 항공권 결제는 최대 10분 이내애ㅔ 완료하시기 바랍니다. \n"
				        + "2. 10분이 경과되거나 인증 과정에서 취소하시면,\n"
				        + "- 예약 취소 후 처음부터 다시 진행합니다.\n"
				        + "- 이 경우 좌석 상황에 따라 해당 편 예약이 어려울 수 있습니다."
				        //+ paymentMethodName
				    );
					

				if (!paymentConfirmed) {
				    return;
				}

				processing = true;

                submitButton.disabled = true;
                submitButton.textContent =
                    "좌석을 확인하는 중...";

                updateState();

                let bookingId = null;

                try {
                    const holdResult = await postForm(
                        "/booking/reserve/hold/toss",
                        new FormData(form)
                    );

                    bookingId =
                        Number(holdResult.bookingId);

                    if (!Number.isInteger(bookingId)
                            || bookingId <= 0) {

                        throw new Error(
                            "생성된 예약 정보를 확인할 수 없습니다."
                        );
                    }
					
					activeBookingId = bookingId;

					addPaymentHistoryGuard();

                    submitButton.textContent =
                        paymentMethod === "TOSSPAY"
                            ? "토스페이 QR창을 여는 중..."
                            : "계좌이체창을 여는 중...";

                    await openTossPayment(
                        bookingId,
                        paymentMethod
                    );

					} catch (error) {
					    console.error(
					        "토스 결제 실행 오류:",
					        error
					    );

					    if (bookingId
					            && activeBookingId !== bookingId) {

					        return;
					    }
						
						activeTossPayment = null;

					    if (bookingId) {
					        await releaseHold(bookingId);
					    }

					    activeBookingId = null;

					    removePaymentHistoryGuard();

					    restorePaymentButton();

					    window.alert(
					        error.message
					        || "결제가 취소되었습니다."
					    );

					    if (error.redirectUrl) {
					        window.location.href =
					            error.redirectUrl;
					    }
					}
            }
        );

        window.addEventListener(
            "pageshow",
            () => {
                processing = false;
                submitButton.textContent = "결제하기";
                updateState();
            }
        );

        updateState();
    }

    if (document.readyState === "loading") {
        document.addEventListener(
            "DOMContentLoaded",
            initializeBookingConfirmation,
            { once: true }
        );
    } else {
        initializeBookingConfirmation();
    }
})();