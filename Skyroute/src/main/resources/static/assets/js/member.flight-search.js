(() => {
    "use strict";

    document.addEventListener("DOMContentLoaded", () => {
        initializeResultSearch();
        initializeItinerarySelection();
    });

    function initializeResultSearch() {
        const summary = document.getElementById("searchConditionSummary");
        const form = document.getElementById("resultSearchForm");

        if (!form) {
            return;
        }

        const editButton = document.getElementById("searchConditionEditButton");
        const cancelButton = document.getElementById("searchConditionCancelButton");
        const swapButton = document.getElementById("resultAirportSwapButton");
        const submitButton = form.querySelector('button[type="submit"]');

        const departureAirport = document.getElementById("resultDepartureAirportId");
        const arrivalAirport = document.getElementById("resultArrivalAirportId");

        const departureDate = document.getElementById("resultDepartureDate");
        const returnDate = document.getElementById("resultReturnDate");
        const returnDateField = document.getElementById("resultReturnDateField");
        const dateGrid = document.getElementById("resultDateGrid");

        const adultCount = document.getElementById("resultAdultCount");
        const childCount = document.getElementById("resultChildCount");
        const infantCount = document.getElementById("resultInfantCount");

        const seatClass = document.getElementById("resultSeatClassId");

        const tripTypeInputs = Array.from(
            form.querySelectorAll('input[name="tripType"]')
        );

        function getTripType() {
            const checkedInput = tripTypeInputs.find(input => input.checked);
            return checkedInput ? checkedInput.value : "";
        }

        function setEditorOpen(open) {
            if (summary) {
                summary.hidden = open;
            }

            form.hidden = !open;

            if (editButton) {
                editButton.setAttribute("aria-expanded", String(open));
            }

            if (open && departureAirport) {
                window.setTimeout(() => departureAirport.focus(), 0);
            }
        }

        function syncTripType() {
            const isRoundTrip = getTripType() === "ROUNDTRIP";

            if (returnDateField) {
                returnDateField.hidden = !isRoundTrip;
            }

            if (returnDate) {
                returnDate.disabled = !isRoundTrip;
            }

            if (dateGrid) {
                dateGrid.classList.toggle("is-oneway", !isRoundTrip);
            }
        }

        function syncReturnDateMin() {
            if (!departureDate || !returnDate) {
                return;
            }

            returnDate.min =
                departureDate.value ||
                departureDate.min ||
                "";

            if (
                departureDate.value &&
                returnDate.value &&
                returnDate.value < departureDate.value
            ) {
                returnDate.value = "";
            }
        }

        function readInteger(element) {
            if (!element) {
                return Number.NaN;
            }

            const value = element.value.trim();

            if (!/^\d+$/.test(value)) {
                return Number.NaN;
            }

            return Number(value);
        }

        function showError(message, target) {
            window.alert(message);

            if (target) {
                target.focus();
            }
        }

        function validateSearch(event) {
            const tripType = getTripType();

            const departureAirportId = departureAirport
                ? departureAirport.value
                : "";

            const arrivalAirportId = arrivalAirport
                ? arrivalAirport.value
                : "";

            const departureDateValue = departureDate
                ? departureDate.value
                : "";

            const returnDateValue = returnDate
                ? returnDate.value
                : "";

            const isRoundTrip = tripType === "ROUNDTRIP";

            const adults = readInteger(adultCount);
            const children = readInteger(childCount);
            const infants = readInteger(infantCount);

            const totalPassengers =
                adults +
                children +
                infants;

            let message = "";
            let target = null;

            if (
                tripType !== "ROUNDTRIP" &&
                tripType !== "ONEWAY"
            ) {
                message = "여정 유형을 선택해 주세요.";
                target = tripTypeInputs.length > 0
                    ? tripTypeInputs[0]
                    : null;
            } else if (!departureAirportId) {
                message = "출발지를 선택해 주세요.";
                target = departureAirport;
            } else if (!arrivalAirportId) {
                message = "도착지를 선택해 주세요.";
                target = arrivalAirport;
            } else if (departureAirportId === arrivalAirportId) {
                message = "출발지와 도착지는 서로 달라야 합니다.";
                target = arrivalAirport;
            } else if (!departureDateValue) {
                message = "출발일을 선택해 주세요.";
                target = departureDate;
            } else if (
                departureDate &&
                departureDate.min &&
                departureDateValue < departureDate.min
            ) {
                message = "출발일은 오늘 이후 날짜만 선택할 수 있습니다.";
                target = departureDate;
            } else if (
                isRoundTrip &&
                !returnDateValue
            ) {
                message = "왕복 여정은 귀국일을 선택해 주세요.";
                target = returnDate;
            } else if (
                isRoundTrip &&
                returnDateValue < departureDateValue
            ) {
                message = "귀국일은 출발일 이후여야 합니다.";
                target = returnDate;
            } else if (
                !Number.isInteger(adults) ||
                adults < 1 ||
                adults > 9
            ) {
                message = "성인은 1명 이상 9명 이하로 선택해 주세요.";
                target = adultCount;
            } else if (
                !Number.isInteger(children) ||
                children < 0 ||
                children > 8
            ) {
                message = "소아는 0명 이상 8명 이하로 선택해 주세요.";
                target = childCount;
            } else if (
                !Number.isInteger(infants) ||
                infants < 0 ||
                infants > 8
            ) {
                message = "유아는 0명 이상 8명 이하로 선택해 주세요.";
                target = infantCount;
            } else if (
                totalPassengers < 1 ||
                totalPassengers > 9
            ) {
                message = "전체 승객은 1명 이상 9명 이하로 선택해 주세요.";
                target = adultCount;
            } else if (infants > adults) {
                message = "유아 수는 성인 수를 초과할 수 없습니다.";
                target = infantCount;
            } else if (
                !seatClass ||
                !seatClass.value
            ) {
                message = "좌석 등급을 선택해 주세요.";
                target = seatClass;
            }

            if (message) {
                event.preventDefault();
                showError(message, target);
                return;
            }

            if (submitButton) {
                submitButton.disabled = true;
                submitButton.textContent = "검색 중...";
            }
        }

        if (editButton) {
            editButton.addEventListener(
                "click",
                () => setEditorOpen(true)
            );
        }

        if (cancelButton) {
            cancelButton.addEventListener(
                "click",
                () => {
                    form.reset();
                    syncTripType();
                    syncReturnDateMin();
                    setEditorOpen(false);
                }
            );
        }

        if (
            swapButton &&
            departureAirport &&
            arrivalAirport
        ) {
            swapButton.addEventListener(
                "click",
                () => {
                    const departureValue =
                        departureAirport.value;

                    departureAirport.value =
                        arrivalAirport.value;

                    arrivalAirport.value =
                        departureValue;
                }
            );
        }

        tripTypeInputs.forEach(input => {
            input.addEventListener(
                "change",
                syncTripType
            );
        });

        if (departureDate) {
            departureDate.addEventListener(
                "change",
                syncReturnDateMin
            );
        }

        form.addEventListener(
            "submit",
            validateSearch
        );

        window.addEventListener(
            "pageshow",
            () => {
                if (submitButton) {
                    submitButton.disabled = false;
                    submitButton.textContent = "조건 적용";
                }
            }
        );

        syncTripType();
        syncReturnDateMin();

        if (editButton) {
            editButton.setAttribute(
                "aria-expanded",
                String(!form.hidden)
            );
        }
    }

    function initializeItinerarySelection() {
        const form = document.getElementById(
            "itinerarySelectionForm"
        );

        if (!form) {
            return;
        }

        const reserveStartButton = document.getElementById(
            "reserveStartButton"
        );

        if (!reserveStartButton) {
            return;
        }

        const tripTypeInput = form.querySelector(
            'input[name="tripType"]'
        );

        const outboundInputs = Array.from(
            form.querySelectorAll(
                'input[name="outboundFlightId"]'
            )
        );

        const inboundInputs = Array.from(
            form.querySelectorAll(
                'input[name="inboundFlightId"]'
            )
        );

        function getCheckedValue(inputs) {
            const checkedInput = inputs.find(
                input => input.checked
            );

            return checkedInput
                ? checkedInput.value
                : "";
        }

        function isRoundTrip() {
            return tripTypeInput &&
                tripTypeInput.value === "ROUNDTRIP";
        }

        function isSelectionComplete() {
            const outboundFlightId =
                getCheckedValue(outboundInputs);

            if (!outboundFlightId) {
                return false;
            }

            if (
                isRoundTrip() &&
                !getCheckedValue(inboundInputs)
            ) {
                return false;
            }

            return true;
        }

        function syncReserveButton() {
            reserveStartButton.disabled =
                !isSelectionComplete();
        }

        outboundInputs.forEach(input => {
            input.addEventListener(
                "change",
                syncReserveButton
            );
        });

        inboundInputs.forEach(input => {
            input.addEventListener(
                "change",
                syncReserveButton
            );
        });

        form.addEventListener(
            "submit",
            event => {
                if (!isSelectionComplete()) {
                    event.preventDefault();

                    if (isRoundTrip()) {
                        window.alert(
                            "가는 편과 오는 편 항공편을 모두 선택해 주세요."
                        );
                    } else {
                        window.alert(
                            "가는 편 항공편을 선택해 주세요."
                        );
                    }

                    return;
                }

                reserveStartButton.disabled = true;
                reserveStartButton.textContent =
                    "탑승객 입력 화면으로 이동 중...";
            }
        );

        window.addEventListener(
            "pageshow",
            () => {
                reserveStartButton.textContent =
                    "탑승객 정보 입력";

                syncReserveButton();
            }
        );

        syncReserveButton();
    }
})();