(() => {
    "use strict";

    document.addEventListener(
        "DOMContentLoaded",
        initializeSeatSelection
    );

    function initializeSeatSelection() {
        const form =
            document.getElementById("seatSelectionForm");

        const submitButton =
            document.getElementById("seatSubmitButton");

        if (!form || !submitButton) {
            return;
        }

        let outboundController = null;
        let inboundController = null;

        function syncSubmitButton() {
            const outboundComplete =
                outboundController !== null &&
                outboundController.isComplete();

            const inboundComplete =
                inboundController === null ||
                inboundController.isComplete();

            submitButton.disabled = !(
                outboundComplete &&
                inboundComplete
            );
        }

        outboundController = createLegController({
            legType: "OUTBOUND",
            inputName: "outboundSeatIds",
            hiddenContainerId: "outboundHiddenInputs",
            selectedCountId: "outboundSelectedCount",
            onChange: syncSubmitButton
        });

        inboundController = createLegController({
            legType: "INBOUND",
            inputName: "inboundSeatIds",
            hiddenContainerId: "inboundHiddenInputs",
            selectedCountId: "inboundSelectedCount",
            onChange: syncSubmitButton
        });

        syncSubmitButton();

        form.addEventListener("submit", function (event) {
            const outboundComplete =
                outboundController !== null &&
                outboundController.isComplete();

            const inboundComplete =
                inboundController === null ||
                inboundController.isComplete();

            if (!outboundComplete || !inboundComplete) {
                event.preventDefault();

                window.alert(
                    inboundController === null
                        ? "모든 탑승객의 좌석을 선택해 주세요."
                        : "모든 탑승객의 가는 편과 오는 편 좌석을 선택해 주세요."
                );

                return;
            }

            submitButton.disabled = true;
            submitButton.textContent =
                "예약 내용 확인 화면으로 이동 중...";
        });

        window.addEventListener("pageshow", function () {
            submitButton.textContent =
                "선택 내용 확인";

            syncSubmitButton();
        });
    }

    function createLegController({
        legType,
        inputName,
        hiddenContainerId,
        selectedCountId,
        onChange
    }) {
        const section = document.querySelector(
            `[data-leg-section="${legType}"]`
        );

        if (!section) {
            return null;
        }

        const passengerCount = Number(
            section.dataset.passengerCount
        );

        const passengerTabs = Array.from(
            section.querySelectorAll(".passenger-tab")
        );

        const seatButtons = Array.from(
            section.querySelectorAll(
                ".seat-button:not(:disabled)"
            )
        );

        const hiddenContainer =
            document.getElementById(hiddenContainerId);

        const selectedCount =
            document.getElementById(selectedCountId);

        const selections =
            new Array(passengerCount).fill(null);

        let activePassengerIndex = 0;

        passengerTabs.forEach(function (tab, index) {
            tab.addEventListener("click", function () {
                setActivePassenger(index);
            });
        });

        seatButtons.forEach(function (button) {
            button.addEventListener("click", function () {
                selectSeat(button);
            });
        });

        function setActivePassenger(index) {
            if (
                index < 0 ||
                index >= selections.length
            ) {
                return;
            }

            activePassengerIndex = index;

            passengerTabs.forEach(
                function (tab, tabIndex) {
                    tab.classList.toggle(
                        "active",
                        tabIndex === activePassengerIndex
                    );
                }
            );

            renderSeatButtons();
        }

        function selectSeat(button) {
            const seatId =
                button.dataset.seatId;

            const seatNo =
                button.dataset.seatNo;

            if (!seatId || !seatNo) {
                return;
            }

            const assignedIndexValue =
                button.dataset.assignedIndex;

            if (
                assignedIndexValue !== undefined &&
                Number(assignedIndexValue) !==
                    activePassengerIndex
            ) {
                window.alert(
                    "이미 다른 탑승객에게 선택된 좌석입니다."
                );

                return;
            }

            const currentSelection =
                selections[activePassengerIndex];

            if (
                currentSelection &&
                String(currentSelection.seatId) ===
                    String(seatId)
            ) {
                selections[activePassengerIndex] =
                    null;

                delete button.dataset.assignedIndex;

                render();
                return;
            }

            if (currentSelection) {
                const previousButton =
                    seatButtons.find(
                        function (seatButton) {
                            return (
                                seatButton.dataset.seatId ===
                                String(currentSelection.seatId)
                            );
                        }
                    );

                if (previousButton) {
                    delete previousButton.dataset
                        .assignedIndex;
                }
            }

            selections[activePassengerIndex] = {
                seatId: seatId,
                seatNo: seatNo
            };

            button.dataset.assignedIndex =
                String(activePassengerIndex);

            render();
            moveToNextPassenger();
        }

        function moveToNextPassenger() {
            for (
                let index = activePassengerIndex + 1;
                index < selections.length;
                index++
            ) {
                if (!selections[index]) {
                    setActivePassenger(index);
                    return;
                }
            }

            const firstEmptyIndex =
                selections.findIndex(
                    function (selection) {
                        return !selection;
                    }
                );

            if (firstEmptyIndex !== -1) {
                setActivePassenger(firstEmptyIndex);
            }
        }

        function renderSeatButtons() {
            seatButtons.forEach(function (button) {
                const assignedIndexValue =
                    button.dataset.assignedIndex;

                const assigned =
                    assignedIndexValue !== undefined;

                const current =
                    assigned &&
                    Number(assignedIndexValue) ===
                        activePassengerIndex;

                button.classList.toggle(
                    "assigned",
                    assigned
                );

                button.classList.toggle(
                    "current",
                    current
                );
            });
        }

        function renderPassengerTabs() {
            passengerTabs.forEach(
                function (tab, index) {
                    const seatLabel =
                        tab.querySelector(
                            ".passenger-tab-seat"
                        );

                    if (!seatLabel) {
                        return;
                    }

                    seatLabel.textContent =
                        selections[index]
                            ? selections[index].seatNo
                            : "미선택";
                }
            );
        }

        function renderHiddenInputs() {
            if (!hiddenContainer) {
                return;
            }

            hiddenContainer.innerHTML = "";

            selections.forEach(function (selection) {
                if (!selection) {
                    return;
                }

                const input =
                    document.createElement("input");

                input.type = "hidden";
                input.name = inputName;
                input.value = selection.seatId;

                hiddenContainer.appendChild(input);
            });
        }

        function renderSelectedCount() {
            if (!selectedCount) {
                return;
            }

            selectedCount.textContent = String(
                selections.filter(Boolean).length
            );
        }

        function render() {
            renderSeatButtons();
            renderPassengerTabs();
            renderHiddenInputs();
            renderSelectedCount();

            onChange();
        }

        render();

        return {
            isComplete: function () {
                return (
                    selections.length > 0 &&
                    selections.every(Boolean)
                );
            }
        };
    }
})();