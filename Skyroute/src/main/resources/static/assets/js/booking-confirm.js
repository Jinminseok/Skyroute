(() => {
    "use strict";

    function initializeBookingConfirmation() {
        const form =
            document.getElementById("confirmForm");

        const submitButton =
            document.getElementById("paymentSubmitButton");

        const paymentGuide =
            document.getElementById("paymentGuide");

        if (!form || !submitButton) {
            return;
        }

        const agreements = Array.from(
            form.querySelectorAll(".required-agreement")
        );

        const paymentMethods = Array.from(
            form.querySelectorAll(".payment-method-radio")
        );

        function allAgreementsChecked() {
            return agreements.length > 0
                && agreements.every(
                    agreement => agreement.checked
                );
        }

        function paymentMethodSelected() {
            return paymentMethods.some(
                paymentMethod => paymentMethod.checked
            );
        }

        function updateState() {
            const allAgreed =
                allAgreementsChecked();

            paymentMethods.forEach(paymentMethod => {
                paymentMethod.disabled = !allAgreed;

                if (!allAgreed) {
                    paymentMethod.checked = false;
                }

                const label =
                    paymentMethod.closest(".payment-method");

                if (label) {
                    label.classList.toggle(
                        "disabled",
                        !allAgreed
                    );

                    label.classList.toggle(
                        "selected",
                        paymentMethod.checked
                    );

                    label.setAttribute(
                        "aria-disabled",
                        String(!allAgreed)
                    );
                }
            });

            const methodSelected =
                paymentMethodSelected();

            submitButton.disabled =
                !allAgreed || !methodSelected;

            if (paymentGuide) {
                if (!allAgreed) {
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

        form.addEventListener(
            "change",
            updateState
        );

        form.addEventListener(
            "submit",
            event => {
                if (!allAgreementsChecked()) {
                    event.preventDefault();

                    window.alert(
                        "필수 확인 및 동의 항목을 모두 체크해 주세요."
                    );

                    updateState();
                    return;
                }

                if (!paymentMethodSelected()) {
                    event.preventDefault();

                    window.alert(
                        "결제수단을 선택해 주세요."
                    );

                    updateState();
                    return;
                }

                submitButton.disabled = true;
                submitButton.textContent =
                    "좌석을 확인하는 중...";
            }
        );

        window.addEventListener(
            "pageshow",
            updateState
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