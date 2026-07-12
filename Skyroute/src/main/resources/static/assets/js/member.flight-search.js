(() => {
    "use strict";

    document.addEventListener(
        "DOMContentLoaded",
        initializeResultSearch
    );


    function initializeResultSearch() {

        /*
         * 검색 조건 요약 및 수정 폼
         */
        const summary =
            document.getElementById(
                "searchConditionSummary"
            );

        const form =
            document.getElementById(
                "resultSearchForm"
            );

        /*
         * 검색 조건 수정 폼이 없는 화면에서는
         * 아래 기능을 실행하지 않는다.
         */
        if (!form) {
            return;
        }


        /*
         * 버튼
         */
        const editButton =
            document.getElementById(
                "searchConditionEditButton"
            );

        const cancelButton =
            document.getElementById(
                "searchConditionCancelButton"
            );

        const swapButton =
            document.getElementById(
                "resultAirportSwapButton"
            );

        const submitButton =
            form.querySelector(
                'button[type="submit"]'
            );


        /*
         * 출발지 / 도착지
         */
        const departureAirport =
            document.getElementById(
                "resultDepartureAirportId"
            );

        const arrivalAirport =
            document.getElementById(
                "resultArrivalAirportId"
            );


        /*
         * 출발일 / 귀국일
         */
        const departureDate =
            document.getElementById(
                "resultDepartureDate"
            );

        const returnDate =
            document.getElementById(
                "resultReturnDate"
            );

        const returnDateField =
            document.getElementById(
                "resultReturnDateField"
            );

        const dateGrid =
            document.getElementById(
                "resultDateGrid"
            );


        /*
         * 탑승 인원
         */
        const adultCount =
            document.getElementById(
                "resultAdultCount"
            );

        const childCount =
            document.getElementById(
                "resultChildCount"
            );

        const infantCount =
            document.getElementById(
                "resultInfantCount"
            );


        /*
         * 좌석 등급
         */
        const seatClass =
            document.getElementById(
                "resultSeatClassId"
            );


        /*
         * 편도 / 왕복 라디오 버튼
         */
        const tripTypeInputs =
            Array.from(
                form.querySelectorAll(
                    'input[name="tripType"]'
                )
            );


        /**
         * 현재 선택된 여정 유형을 반환한다.
         *
         * @returns {string} ROUNDTRIP 또는 ONEWAY
         */
        function getTripType() {

            const checkedInput =
                tripTypeInputs.find(
                    input => input.checked
                );

            return checkedInput
                ? checkedInput.value
                : "";
        }


        /**
         * 검색 조건 수정 폼을 열거나 닫는다.
         *
         * @param {boolean} open true면 수정 폼 표시
         */
        function setEditorOpen(open) {

            /*
             * 현재 검색 조건 요약
             */
            if (summary) {
                summary.hidden = open;
            }

            /*
             * 검색 조건 수정 폼
             */
            form.hidden = !open;


            /*
             * 접근성 속성
             */
            if (editButton) {

                editButton.setAttribute(
                    "aria-expanded",
                    String(open)
                );
            }


            /*
             * 수정 폼이 열리면 출발지에 포커스
             */
            if (open && departureAirport) {

                window.setTimeout(
                    () => departureAirport.focus(),
                    0
                );
            }
        }


        /**
         * 편도 / 왕복에 따라 귀국일 입력란을 제어한다.
         */
        function syncTripType() {

            const tripType =
                getTripType();

            const isRoundTrip =
                tripType === "ROUNDTRIP";


            /*
             * 귀국일 입력 영역 표시 여부
             */
            if (returnDateField) {

                returnDateField.hidden =
                    !isRoundTrip;
            }


            /*
             * 편도일 때 귀국일 input을 비활성화한다.
             *
             * disabled 상태의 input은 GET 요청 파라미터에
             * 포함되지 않는다.
             */
            if (returnDate) {

                returnDate.disabled =
                    !isRoundTrip;
            }


            /*
             * 편도일 때 날짜 영역을 한 열로 변경한다.
             */
            if (dateGrid) {

                dateGrid.classList.toggle(
                    "is-oneway",
                    !isRoundTrip
                );
            }
        }


        /**
         * 귀국일의 최소 선택일을 출발일로 설정한다.
         */
        function syncReturnDateMin() {

            if (!departureDate || !returnDate) {
                return;
            }


            /*
             * 귀국일은 출발일보다 빠를 수 없다.
             */
            returnDate.min =
                departureDate.value
                || departureDate.min
                || "";


            /*
             * 기존 귀국일보다 출발일을 뒤로 변경한 경우
             * 잘못된 귀국일 값을 초기화한다.
             */
            if (
                departureDate.value
                && returnDate.value
                && returnDate.value
                    < departureDate.value
            ) {

                returnDate.value = "";
            }
        }


        /**
         * 숫자 입력값을 정수로 변환한다.
         *
         * 빈 값 또는 정수가 아닌 값은 NaN을 반환한다.
         *
         * @param {HTMLInputElement|null} element
         * @returns {number}
         */
        function readInteger(element) {

            if (!element) {
                return Number.NaN;
            }

            const value =
                element.value.trim();

            if (!/^\d+$/.test(value)) {
                return Number.NaN;
            }

            return Number(value);
        }


        /**
         * 검증 오류를 출력하고 해당 입력 요소에 포커스한다.
         *
         * @param {string} message
         * @param {HTMLElement|null} target
         */
        function showError(message, target) {

            window.alert(message);

            if (target) {
                target.focus();
            }
        }


        /**
         * 검색 조건을 검증한다.
         *
         * @param {SubmitEvent} event
         */
        function validateSearch(event) {

            const tripType =
                getTripType();


            const departureAirportId =
                departureAirport
                    ? departureAirport.value
                    : "";

            const arrivalAirportId =
                arrivalAirport
                    ? arrivalAirport.value
                    : "";


            const departureDateValue =
                departureDate
                    ? departureDate.value
                    : "";

            const returnDateValue =
                returnDate
                    ? returnDate.value
                    : "";


            const isRoundTrip =
                tripType === "ROUNDTRIP";


            const adults =
                readInteger(adultCount);

            const children =
                readInteger(childCount);

            const infants =
                readInteger(infantCount);


            const totalPassengers =
                adults
                + children
                + infants;


            let message = "";
            let target = null;


            /*
             * 편도 / 왕복 검증
             */
            if (
                tripType !== "ROUNDTRIP"
                && tripType !== "ONEWAY"
            ) {

                message =
                    "여정 유형을 선택해 주세요.";

                target =
                    tripTypeInputs.length > 0
                        ? tripTypeInputs[0]
                        : null;


            /*
             * 공항 검증
             */
            } else if (!departureAirportId) {

                message =
                    "출발지를 선택해 주세요.";

                target =
                    departureAirport;


            } else if (!arrivalAirportId) {

                message =
                    "도착지를 선택해 주세요.";

                target =
                    arrivalAirport;


            } else if (
                departureAirportId
                === arrivalAirportId
            ) {

                message =
                    "출발지와 도착지는 서로 달라야 합니다.";

                target =
                    arrivalAirport;


            /*
             * 날짜 검증
             */
            } else if (!departureDateValue) {

                message =
                    "출발일을 선택해 주세요.";

                target =
                    departureDate;


            } else if (
                departureDate
                && departureDate.min
                && departureDateValue
                    < departureDate.min
            ) {

                message =
                    "출발일은 오늘 이후 날짜만 선택할 수 있습니다.";

                target =
                    departureDate;


            } else if (
                isRoundTrip
                && !returnDateValue
            ) {

                message =
                    "왕복 여정은 귀국일을 선택해 주세요.";

                target =
                    returnDate;


            } else if (
                isRoundTrip
                && returnDateValue
                    < departureDateValue
            ) {

                message =
                    "귀국일은 출발일 이후여야 합니다.";

                target =
                    returnDate;


            /*
             * 성인 인원 검증
             */
            } else if (
                !Number.isInteger(adults)
                || adults < 1
                || adults > 9
            ) {

                message =
                    "성인은 1명 이상 9명 이하로 선택해 주세요.";

                target =
                    adultCount;


            /*
             * 소아 인원 검증
             */
            } else if (
                !Number.isInteger(children)
                || children < 0
                || children > 8
            ) {

                message =
                    "소아는 0명 이상 8명 이하로 선택해 주세요.";

                target =
                    childCount;


            /*
             * 유아 인원 검증
             */
            } else if (
                !Number.isInteger(infants)
                || infants < 0
                || infants > 8
            ) {

                message =
                    "유아는 0명 이상 8명 이하로 선택해 주세요.";

                target =
                    infantCount;


            /*
             * 전체 인원 검증
             */
            } else if (
                totalPassengers < 1
                || totalPassengers > 9
            ) {

                message =
                    "전체 승객은 1명 이상 9명 이하로 선택해 주세요.";

                target =
                    adultCount;


            /*
             * 유아는 성인 수보다 많을 수 없다.
             */
            } else if (infants > adults) {

                message =
                    "유아 수는 성인 수를 초과할 수 없습니다.";

                target =
                    infantCount;


            /*
             * 좌석 등급 검증
             */
            } else if (
                !seatClass
                || !seatClass.value
            ) {

                message =
                    "좌석 등급을 선택해 주세요.";

                target =
                    seatClass;
            }


            /*
             * 오류가 있으면 요청을 중단한다.
             */
            if (message) {

                event.preventDefault();

                showError(
                    message,
                    target
                );

                return;
            }


            /*
             * 정상 제출 시 중복 클릭을 막는다.
             */
            if (submitButton) {

                submitButton.disabled = true;
                submitButton.textContent = "검색 중...";
            }
        }


        /*
         * 검색 조건 변경 버튼
         */
        if (editButton) {

            editButton.addEventListener(
                "click",
                () => setEditorOpen(true)
            );
        }


        /*
         * 취소 버튼
         */
        if (cancelButton) {

            cancelButton.addEventListener(
                "click",
                () => {

                    /*
                     * 서버에서 처음 렌더링된 검색 조건으로
                     * 수정 폼 값을 복원한다.
                     */
                    form.reset();

                    syncTripType();
                    syncReturnDateMin();

                    setEditorOpen(false);
                }
            );
        }


        /*
         * 출발지 / 도착지 교환 버튼
         */
        if (
            swapButton
            && departureAirport
            && arrivalAirport
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


        /*
         * 편도 / 왕복 변경 이벤트
         */
        tripTypeInputs.forEach(
            input => {

                input.addEventListener(
                    "change",
                    syncTripType
                );
            }
        );


        /*
         * 출발일 변경 이벤트
         */
        if (departureDate) {

            departureDate.addEventListener(
                "change",
                syncReturnDateMin
            );
        }


        /*
         * 검색 폼 제출 이벤트
         */
        form.addEventListener(
            "submit",
            validateSearch
        );


        /*
         * 브라우저 뒤로가기로 화면에 돌아왔을 때
         * 비활성화된 제출 버튼을 복원한다.
         */
        window.addEventListener(
            "pageshow",
            () => {

                if (submitButton) {

                    submitButton.disabled = false;
                    submitButton.textContent = "조건 적용";
                }
            }
        );


        /*
         * 최초 화면 상태 동기화
         */
        syncTripType();
        syncReturnDateMin();


        /*
         * 최초 접근성 상태 설정
         */
        if (editButton) {

            editButton.setAttribute(
                "aria-expanded",
                String(!form.hidden)
            );
        }
    }

})();