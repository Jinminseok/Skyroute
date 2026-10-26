(() => {
  "use strict";

  const WEEKDAY_NAMES = ["일", "월", "화", "수", "목", "금", "토"];
  const RECENT_AIRPORT_KEY = "skyrouteRecentAirports";

  const AIRPORT_DISPLAY_NAMES = Object.freeze({
    ICN: "서울/인천국제공항",
    GMP: "서울/김포국제공항",
    PUS: "부산/김해국제공항",
    CJU: "제주/제주국제공항",
    NRT: "도쿄/나리타국제공항",
    HND: "도쿄/하네다공항",
    KIX: "오사카/간사이국제공항",
    FUK: "후쿠오카/후쿠오카공항",
    PVG: "상하이/푸둥국제공항",
    SHA: "상하이/홍차오국제공항",
    BKK: "방콕/수완나품국제공항",
    SIN: "싱가포르/창이국제공항",
    JFK: "뉴욕/존 F. 케네디국제공항",
    LAX: "로스앤젤레스/국제공항",
  });

  const state = {
    airports: [],
    seatClasses: [],
    currentAirportTarget: "departure",
    selectedAirportRegion: "KOREA",
    activePanel: null,
    activeTriggerId: null,
    calendarBaseMonth: null,
    pendingDepartureDate: "",
    pendingReturnDate: "",
    pendingSeatClassId: "",
    sliderTimerId: null,
  };

  const byId = (id) => document.getElementById(id);

  const all = (selector, root = document) =>
    Array.from(root.querySelectorAll(selector));

  document.addEventListener("DOMContentLoaded", initializePage);

  function initializePage() {
    loadCatalogData();
    bindPageEvents();
    initializeAgeCalculator();
    initializeSeatDescriptions();
    initializeEventSlider();

    syncAirportDisplay();
    syncTripType();
    syncDateDisplay();
    syncPassengerDisplay();
    syncSeatDisplay();

    renderAirportSearchResults("");
    renderAirportRegion(state.selectedAirportRegion);
  }

  function bindPageEvents() {
    const form = byId("flightSearchForm");
    const backdrop = byId("homeSelectorBackdrop");
    const swapButton = byId("airportSwapButton");
    const airportKeyword = byId("airportKeyword");
    const clearKeywordButton = byId("airportKeywordClear");
    const showAllCityButton = byId("showAllCityButton");
    const backToSearchButton = byId("backToAirportSearchButton");
    const clearRecentButton = byId("clearRecentAirportButton");
    const calendarResetButton = byId("calendarResetButton");
    const passengerApplyButton = byId("passengerApplyButton");
    const ageToggle = byId("ageCalculatorToggle");
    const ageCalculateButton = byId("ageCalculateButton");
    const birthYear = byId("birthYear");
    const birthMonth = byId("birthMonth");
    const seatApplyButton = byId("seatApplyButton");

    if (form) {
      form.addEventListener("submit", validateSearchForm);
    }

    all('input[name="tripType"]').forEach((radio) => {
      radio.addEventListener("change", handleTripTypeChange);
    });

    all("[data-panel-open]").forEach((button) => {
      button.addEventListener("click", () => {
        const panelName = button.dataset.panelOpen;

        /* 열려 있는 패널의 버튼을 다시 누르면 닫는다. */
        if (
          state.activePanel === panelName &&
          button.getAttribute("aria-expanded") === "true"
        ) {
          closeSelectorPanel();
          return;
        }

        if (panelName === "airport") {
          openAirportPanel(button.dataset.airportTarget || "departure");
          return;
        }

        if (panelName === "date") {
          openDatePanel();
          return;
        }

        if (panelName === "passenger") {
          openPassengerPanel();
          return;
        }

        if (panelName === "seat") {
          openSeatPanel();
        }
      });
    });

    all("[data-selector-close]").forEach((button) => {
      button.addEventListener("click", closeSelectorPanel);
    });

    /* 모달 바깥(딤 배경) 클릭 시 닫기 */
    if (backdrop) {
      backdrop.addEventListener("click", closeSelectorPanel);
    }

    if (swapButton) {
      swapButton.addEventListener("click", swapAirports);
    }

    if (airportKeyword) {
      airportKeyword.addEventListener("input", (event) => {
        renderAirportSearchResults(event.target.value);
      });

      airportKeyword.addEventListener("keydown", (event) => {
        if (event.key === "Enter") {
          event.preventDefault();
        }
      });
    }

    if (clearKeywordButton) {
      clearKeywordButton.addEventListener("click", clearAirportKeyword);
    }

    if (showAllCityButton) {
      showAllCityButton.addEventListener("click", showAirportCityView);
    }

    if (backToSearchButton) {
      backToSearchButton.addEventListener("click", showAirportSearchView);
    }

    all(".home-region-button").forEach((button) => {
      button.addEventListener("click", () => {
        renderAirportRegion(button.dataset.region || "OTHER");
      });
    });

    if (clearRecentButton) {
      clearRecentButton.addEventListener("click", clearRecentAirports);
    }

    all("[data-calendar-move]").forEach((button) => {
      button.addEventListener("click", () => {
        moveCalendar(Number(button.dataset.calendarMove || 0));
      });
    });

    if (calendarResetButton) {
      calendarResetButton.addEventListener("click", resetPendingDates);
    }

    all("[data-date-apply]").forEach((button) => {
      button.addEventListener("click", () => {
        applyDateSelection(button.dataset.dateApply === "passenger");
      });
    });

    all("[data-passenger-type][data-count-delta]").forEach((button) => {
      button.addEventListener("click", () => {
        changePassengerCount(
          button.dataset.passengerType,
          Number(button.dataset.countDelta || 0),
        );
      });
    });

    if (passengerApplyButton) {
      passengerApplyButton.addEventListener("click", () => {
        syncPassengerDisplay();
        closeSelectorPanel();
      });
    }

    if (ageToggle) {
      ageToggle.addEventListener("click", toggleAgeCalculator);
    }

    if (ageCalculateButton) {
      ageCalculateButton.addEventListener("click", calculatePassengerAge);
    }

    if (birthYear) {
      birthYear.addEventListener("change", updateBirthDayOptions);
    }

    if (birthMonth) {
      birthMonth.addEventListener("change", updateBirthDayOptions);
    }

    all(".home-seat-option").forEach((button) => {
      button.addEventListener("click", () => {
        selectPendingSeatClass(button);
      });
    });

    if (seatApplyButton) {
      seatApplyButton.addEventListener("click", applySeatClassSelection);
    }

    document.addEventListener("click", (event) => {
      const calendarButton = event.target.closest("[data-calendar-date]");

      if (calendarButton && !calendarButton.disabled) {
        selectCalendarDate(calendarButton.dataset.calendarDate);
      }
    });

    document.addEventListener("keydown", (event) => {
      if (event.key === "Escape") {
        closeSelectorPanel();
      }
    });

    /* 창 크기가 바뀌면 열려 있는 모달 위치를 다시 계산한다. */
    window.addEventListener("resize", () => {
      if (state.activePanel) {
        positionSelectorPanel();
      }
    });
  }

  /* =========================================================
     데이터 로딩
  ========================================================= */

  function loadCatalogData() {
    const airportMap = new Map();

    all(".js-airport-source").forEach((source) => {
      const airport = {
        id: String(source.dataset.id || "").trim(),
        code: String(source.dataset.code || "").trim(),
        name: String(source.dataset.name || "").trim(),
        country: String(source.dataset.country || "").trim(),
        flightType: String(source.dataset.flightType || "").trim(),
      };

      if (!airport.id || !airport.code) {
        return;
      }

      airportMap.set(airport.id, airport);
    });

    state.airports = Array.from(airportMap.values()).sort((left, right) => {
      const leftDomestic =
        getAirportRegion(left.country) === "KOREA"
          ? 0
          : 1;

      const rightDomestic =
        getAirportRegion(right.country) === "KOREA"
          ? 0
          : 1;

      if (leftDomestic !== rightDomestic) {
        return leftDomestic - rightDomestic;
      }

      return left.name.localeCompare(right.name, "ko");
    });

    state.seatClasses = all(".js-seat-source")
      .map((source) => ({
        id: String(source.dataset.id || "").trim(),
        name: String(source.dataset.name || "").trim(),
        order: Number(source.dataset.order || 0),
      }))
      .filter((seatClass) => seatClass.id && seatClass.name)
      .sort((left, right) => left.order - right.order);
  }

  function getAirportById(id) {
    const normalizedId = String(id || "");

    return (
      state.airports.find(
        (airport) => airport.id === normalizedId,
      ) || null
    );
  }

  function getUniqueDisplayAirports(airports) {
    const uniqueMap = new Map();

    airports.forEach((airport) => {
      const key = `${airport.code}:${airport.name}`;

      if (!uniqueMap.has(key)) {
        uniqueMap.set(key, airport);
      }
    });

    return Array.from(uniqueMap.values());
  }

  function getSeatClassById(id) {
    const normalizedId = String(id || "");

    return (
      state.seatClasses.find(
        (seatClass) => seatClass.id === normalizedId,
      ) || null
    );
  }

  function getSelectedTripType() {
    const selected = document.querySelector(
      'input[name="tripType"]:checked',
    );

    return selected
      ? selected.value
      : "ROUNDTRIP";
  }

  /* =========================================================
     선택 패널 공통
  ========================================================= */

  function openSelectorPanel(panelName, triggerId) {
    const section = byId("homeSelectorSection");
    const targetPanel = byId(`${panelName}Panel`);

    if (!section || !targetPanel) {
      return;
    }

    all("[data-selector-panel]").forEach((panel) => {
      panel.hidden = panel !== targetPanel;
    });

    all("[data-panel-open]").forEach((trigger) => {
      const isActive = trigger.id === triggerId;

      trigger.classList.toggle(
        "is-active",
        isActive,
      );

      trigger.setAttribute(
        "aria-expanded",
        String(isActive),
      );
    });

    section.dataset.activePanel = panelName;

    if (panelName !== "airport") {
      section.classList.remove("is-city-view");
    }

    section.hidden = false;

    const backdrop = byId("homeSelectorBackdrop");

    if (backdrop) {
      backdrop.hidden = false;
    }

    section.classList.remove("is-opening");

    void section.offsetWidth;

    section.classList.add("is-opening");

    state.activePanel = panelName;
    state.activeTriggerId = triggerId;

    positionSelectorPanel(triggerId);
    scrollPanelIntoView(section);
  }

  /*
   * 눌린 버튼 바로 아래에 패널이 붙도록 좌표를 계산한다.
   * 카드 오른쪽으로 넘치면 안쪽으로 당겨 준다.
   */
  function positionSelectorPanel(triggerId) {
    const section = byId("homeSelectorSection");
    const card = byId("flightSearchForm");

    if (!section || !card || section.hidden) {
      return;
    }

    /* 전체 도시 보기는 카드 전체 폭을 쓴다. */
    if (section.classList.contains("is-city-view")) {
      section.style.setProperty("--panel-offset", "0px");

      return;
    }

    const trigger = byId(
      triggerId || state.activeTriggerId || "",
    );

    if (!trigger) {
      return;
    }

    const cardRect = card.getBoundingClientRect();
    const triggerRect = trigger.getBoundingClientRect();
    const panelWidth = section.offsetWidth;

    const maxOffset = Math.max(
      cardRect.width - panelWidth,
      0,
    );

    const rawOffset = triggerRect.left - cardRect.left;

    const offset = Math.min(
      Math.max(rawOffset, 0),
      maxOffset,
    );

    section.style.setProperty(
      "--panel-offset",
      `${Math.round(offset)}px`,
    );
  }

  /* 패널이 화면 아래로 잘리면 그만큼만 스크롤한다. */
  function scrollPanelIntoView(section) {
    const rect = section.getBoundingClientRect();

    if (rect.bottom <= window.innerHeight) {
      return;
    }

    window.scrollBy({
      top: rect.bottom - window.innerHeight + 24,
      behavior: "smooth",
    });
  }

  function closeSelectorPanel() {
    const section = byId("homeSelectorSection");
    const backdrop = byId("homeSelectorBackdrop");

    if (backdrop) {
      backdrop.hidden = true;
    }

    if (!section) {
      return;
    }

    all("[data-selector-panel]").forEach((panel) => {
      panel.hidden = true;
    });

    all("[data-panel-open]").forEach((trigger) => {
      trigger.classList.remove("is-active");

      trigger.setAttribute(
        "aria-expanded",
        "false",
      );
    });

    section.hidden = true;

    section.classList.remove(
      "is-opening",
      "is-city-view",
    );

    section.style.removeProperty("--panel-offset");

    delete section.dataset.activePanel;

    state.activePanel = null;
    state.activeTriggerId = null;
  }

  /* =========================================================
     공항 선택
  ========================================================= */

  function openAirportPanel(target) {
    state.currentAirportTarget =
      target === "arrival"
        ? "arrival"
        : "departure";

    const title = byId("airportPanelTitle");

    if (title) {
      title.textContent =
        state.currentAirportTarget === "departure"
          ? "출발지 검색"
          : "도착지 검색";
    }

    showAirportSearchView();
    clearAirportKeyword(false);

    openSelectorPanel(
      "airport",
      state.currentAirportTarget === "departure"
        ? "departureFieldButton"
        : "arrivalFieldButton",
    );

    window.setTimeout(() => {
      const keywordInput = byId("airportKeyword");

      if (keywordInput) {
        keywordInput.focus();
      }
    }, 80);
  }

  function showAirportSearchView() {
    const searchView = byId("airportSearchView");
    const cityView = byId("airportCityView");
    const selectorSection = byId("homeSelectorSection");

    if (searchView) {
      searchView.hidden = false;
    }

    if (cityView) {
      cityView.hidden = true;
    }

    if (selectorSection) {
      selectorSection.classList.remove("is-city-view");
    }

    positionSelectorPanel();
  }

  function showAirportCityView() {
    const searchView = byId("airportSearchView");
    const cityView = byId("airportCityView");
    const selectorSection = byId("homeSelectorSection");

    if (searchView) {
      searchView.hidden = true;
    }

    if (cityView) {
      cityView.hidden = false;
    }

    if (selectorSection) {
      selectorSection.classList.add("is-city-view");
    }

    positionSelectorPanel();

    renderAirportRegion(state.selectedAirportRegion);
    renderRecentAirports();
  }

  function clearAirportKeyword(focus = true) {
    const keywordInput = byId("airportKeyword");

    if (!keywordInput) {
      return;
    }

    keywordInput.value = "";
    renderAirportSearchResults("");

    if (focus) {
      keywordInput.focus();
    }
  }

  function renderAirportSearchResults(keyword) {
    const normalizedKeyword = String(keyword || "")
      .trim()
      .toLowerCase();

    const resultList = byId("airportSearchResultList");
    const countElement = byId("airportResultCount");
    const clearButton = byId("airportKeywordClear");

    if (!resultList || !countElement) {
      return;
    }

    const filtered = getUniqueDisplayAirports(
      state.airports.filter((airport) => {
        const haystack = [
          airport.code,
          airport.name,
          airport.country,
        ]
          .join(" ")
          .toLowerCase();

        return (
          !normalizedKeyword ||
          haystack.includes(normalizedKeyword)
        );
      }),
    );

    resultList.innerHTML = "";
    countElement.textContent = `${filtered.length}개 공항`;

    if (clearButton) {
      clearButton.classList.toggle(
        "is-visible",
        Boolean(normalizedKeyword),
      );
    }

    if (filtered.length === 0) {
      resultList.innerHTML =
        '<div class="home-airport-empty">'
        + "검색 결과가 없습니다."
        + "</div>";

      return;
    }

    filtered.forEach((airport) => {
      resultList.appendChild(
        createAirportButton(
          airport,
          "home-airport-result-item",
        ),
      );
    });
  }

  function renderAirportRegion(region) {
    state.selectedAirportRegion = region || "OTHER";

    all(".home-region-button").forEach((button) => {
      button.classList.toggle(
        "is-active",
        button.dataset.region
          === state.selectedAirportRegion,
      );
    });

    const filtered = getUniqueDisplayAirports(
      state.airports.filter((airport) => {
        return (
          getAirportRegion(airport.country)
          === state.selectedAirportRegion
        );
      }),
    );

    const list = byId("cityAirportList");
    const countElement = byId("cityAirportCount");

    if (!list || !countElement) {
      return;
    }

    list.innerHTML = "";
    countElement.textContent = `${filtered.length}개`;

    if (filtered.length === 0) {
      list.innerHTML =
        '<div class="home-airport-empty">'
        + "이 지역에 등록된 공항이 없습니다."
        + "</div>";

      return;
    }

    filtered.forEach((airport) => {
      list.appendChild(
        createAirportButton(
          airport,
          "home-city-airport-item",
        ),
      );
    });
  }

  function createAirportButton(airport, className) {
    const button = document.createElement("button");

    button.type = "button";
    button.className = className;
    button.dataset.airportId = airport.id;

    const icon = document.createElement("span");

    icon.className = "material-symbols-outlined";

    icon.setAttribute(
      "aria-hidden",
      "true",
    );

    icon.textContent = "flight";

    const copy = document.createElement("span");

    copy.className = "home-airport-result-copy";

    const name = document.createElement("strong");

    name.textContent = airport.name;

    const country = document.createElement("small");

    country.textContent =
      airport.country || "국가 정보 없음";

    const code = document.createElement("em");

    code.textContent = airport.code;

    copy.append(name, country);
    button.append(icon, copy, code);

    button.addEventListener(
      "click",
      () => chooseAirport(airport.id),
    );

    return button;
  }

  function chooseAirport(airportId) {
    const selectedAirport = getAirportById(airportId);

    if (!selectedAirport) {
      return;
    }

    const targetInput =
      state.currentAirportTarget === "departure"
        ? byId("departureAirportId")
        : byId("arrivalAirportId");

    const oppositeInput =
      state.currentAirportTarget === "departure"
        ? byId("arrivalAirportId")
        : byId("departureAirportId");

    if (!targetInput || !oppositeInput) {
      return;
    }

    if (oppositeInput.value === selectedAirport.id) {
      window.alert(
        "출발지와 도착지는 서로 달라야 합니다.",
      );

      return;
    }

    targetInput.value = selectedAirport.id;

    saveRecentAirport(selectedAirport);
    syncAirportDisplay();

    if (
      state.currentAirportTarget === "departure" &&
      !byId("arrivalAirportId").value
    ) {
      state.currentAirportTarget = "arrival";

      const title = byId("airportPanelTitle");

      if (title) {
        title.textContent = "도착지 검색";
      }

      clearAirportKeyword(false);

      all("[data-panel-open]").forEach((trigger) => {
        const active =
          trigger.id === "arrivalFieldButton";

        trigger.classList.toggle(
          "is-active",
          active,
        );

        trigger.setAttribute(
          "aria-expanded",
          String(active),
        );
      });

      const keywordInput = byId("airportKeyword");

      if (keywordInput) {
        keywordInput.focus();
      }

      /* 도착지 버튼 아래로 모달 위치를 옮긴다. */
      state.activeTriggerId = "arrivalFieldButton";

      positionSelectorPanel("arrivalFieldButton");

      return;
    }

    closeSelectorPanel();
  }

  function getAirportDisplayName(airport) {
    if (!airport) {
      return "";
    }

    const code = String(airport.code || "")
      .trim()
      .toUpperCase();

    return (
      AIRPORT_DISPLAY_NAMES[code] ||
      airport.name ||
      airport.country ||
      ""
    );
  }

  function renderAirportField({
    airport,
    codeElementId,
    nameElementId,
    fieldButtonId,
    placeholder,
  }) {
    const codeElement = byId(codeElementId);
    const nameElement = byId(nameElementId);
    const fieldButton = byId(fieldButtonId);

    if (!codeElement || !nameElement || !fieldButton) {
      return;
    }

    if (!airport) {
      codeElement.textContent = placeholder;
      nameElement.textContent = "";
      nameElement.hidden = true;

      fieldButton.classList.remove(
        "is-selected",
      );

      return;
    }

    codeElement.textContent = airport.code;

    nameElement.textContent =
      getAirportDisplayName(airport);

    nameElement.hidden = false;

    fieldButton.classList.add(
      "is-selected",
    );
  }

  function syncAirportDisplay() {
    const departureInput = byId("departureAirportId");
    const arrivalInput = byId("arrivalAirportId");

    const departureAirport = departureInput
      ? getAirportById(departureInput.value)
      : null;

    const arrivalAirport = arrivalInput
      ? getAirportById(arrivalInput.value)
      : null;

    renderAirportField({
      airport: departureAirport,
      codeElementId: "departureDisplayCode",
      nameElementId: "departureDisplayName",
      fieldButtonId: "departureFieldButton",
      placeholder: "출발지",
    });

    renderAirportField({
      airport: arrivalAirport,
      codeElementId: "arrivalDisplayCode",
      nameElementId: "arrivalDisplayName",
      fieldButtonId: "arrivalFieldButton",
      placeholder: "도착지",
    });
  }

  function swapAirports() {
    const departureInput = byId("departureAirportId");
    const arrivalInput = byId("arrivalAirportId");

    if (!departureInput || !arrivalInput) {
      return;
    }

    const temporaryValue = departureInput.value;

    departureInput.value = arrivalInput.value;
    arrivalInput.value = temporaryValue;

    syncAirportDisplay();
  }

  function getAirportRegion(country) {
    const value = String(country || "").toLowerCase();

    if (
      /(대한민국|한국|south korea|republic of korea|korea)/i
        .test(value)
    ) {
      return "KOREA";
    }

    if (
      /(일본|중국|대만|홍콩|마카오|몽골|japan|china|taiwan|hong kong|macau|mongolia)/i
        .test(value)
    ) {
      return "NORTHEAST_ASIA";
    }

    if (
      /(태국|베트남|싱가포르|필리핀|인도네시아|말레이시아|캄보디아|라오스|미얀마|브루나이|thailand|vietnam|singapore|philippines|indonesia|malaysia|cambodia|laos|myanmar|brunei)/i
        .test(value)
    ) {
      return "SOUTHEAST_ASIA";
    }

    if (
      /(미국|캐나다|멕시코|브라질|칠레|아르헨티나|페루|united states|usa|canada|mexico|brazil|chile|argentina|peru)/i
        .test(value)
    ) {
      return "AMERICAS";
    }

    if (
      /(영국|프랑스|독일|이탈리아|스페인|네덜란드|스위스|오스트리아|체코|폴란드|벨기에|포르투갈|덴마크|노르웨이|스웨덴|핀란드|그리스|터키|united kingdom|france|germany|italy|spain|netherlands|switzerland|austria|czech|poland|belgium|portugal|denmark|norway|sweden|finland|greece|turkey)/i
        .test(value)
    ) {
      return "EUROPE";
    }

    if (
      /(호주|뉴질랜드|괌|사이판|피지|australia|new zealand|guam|saipan|fiji)/i
        .test(value)
    ) {
      return "OCEANIA";
    }

    return "OTHER";
  }

  /* =========================================================
     최근 검색 공항
  ========================================================= */

  function getRecentAirports() {
    try {
      const parsed = JSON.parse(
        window.localStorage.getItem(
          RECENT_AIRPORT_KEY,
        ) || "[]",
      );

      return Array.isArray(parsed)
        ? parsed
        : [];

    } catch (error) {
      return [];
    }
  }

  function saveRecentAirport(airport) {
    const recent = getRecentAirports().filter(
      (item) =>
        String(item.id) !== airport.id,
    );

    recent.unshift({
      id: airport.id,
      code: airport.code,
      name: airport.name,
      country: airport.country,
    });

    try {
      window.localStorage.setItem(
        RECENT_AIRPORT_KEY,
        JSON.stringify(
          recent.slice(0, 5),
        ),
      );

    } catch (error) {
      // 브라우저 저장소가 차단된 환경에서는 최근 검색 저장만 생략한다.
    }
  }

  function renderRecentAirports() {
    const container = byId("recentAirportList");

    if (!container) {
      return;
    }

    const recent = getRecentAirports();

    container.innerHTML = "";

    let renderedCount = 0;

    recent.forEach((item) => {
      const airport = getAirportById(item.id);

      if (!airport) {
        return;
      }

      const button = document.createElement("button");

      button.type = "button";
      button.className = "home-recent-airport-item";

      const name = document.createElement("strong");

      name.textContent = airport.name;

      const country = document.createElement("span");

      country.textContent =
        airport.country || "국가 정보 없음";

      const code = document.createElement("em");

      code.textContent = airport.code;

      button.append(name, country, code);

      button.addEventListener(
        "click",
        () => chooseAirport(airport.id),
      );

      container.appendChild(button);

      renderedCount += 1;
    });

    if (renderedCount === 0) {
      container.innerHTML =
        '<div class="home-recent-empty">'
        + "최근 검색한 공항이 없습니다."
        + "</div>";
    }
  }

  function clearRecentAirports() {
    try {
      window.localStorage.removeItem(
        RECENT_AIRPORT_KEY,
      );

    } catch (error) {
      // 저장소 접근이 불가능해도 화면 동작은 계속한다.
    }

    renderRecentAirports();
  }

  /* =========================================================
     여행 유형과 날짜 표시
  ========================================================= */

  function handleTripTypeChange() {
    syncTripType();
    syncDateDisplay();

    if (state.activePanel === "date") {
      state.pendingReturnDate =
        getSelectedTripType() === "ONEWAY"
          ? ""
          : state.pendingReturnDate;

      renderCalendars();
    }
  }

  function syncTripType() {
    const returnInput = byId("returnDate");

    if (!returnInput) {
      return;
    }

    const isOneWay =
      getSelectedTripType() === "ONEWAY";

    returnInput.disabled = isOneWay;

    if (isOneWay) {
      returnInput.value = "";
      state.pendingReturnDate = "";
    }
  }

  function syncDateDisplay() {
    const departureInput = byId("departureDate");
    const returnInput = byId("returnDate");
    const display = byId("dateDisplayText");

    if (!departureInput || !returnInput || !display) {
      return;
    }

    const departureValue = departureInput.value;
    const returnValue = returnInput.value;
    const tripType = getSelectedTripType();

    if (!departureValue) {
      display.textContent =
        tripType === "ONEWAY"
          ? "가는 날 선택"
          : "가는 날 · 오는 날";

      return;
    }

    if (tripType === "ONEWAY") {
      display.textContent =
        formatCompactDate(
          departureValue,
          true,
        );

      return;
    }

    display.textContent = returnValue
      ? `${formatCompactDate(
          departureValue,
          true,
        )} – ${formatCompactDate(
          returnValue,
          false,
        )}`
      : `${formatCompactDate(
          departureValue,
          true,
        )} – 오는 날`;
  }

  /* =========================================================
     날짜 패널
  ========================================================= */

  function openDatePanel() {
    const departureInput = byId("departureDate");
    const returnInput = byId("returnDate");

    state.pendingDepartureDate =
      departureInput
        ? departureInput.value
        : "";

    state.pendingReturnDate =
      getSelectedTripType() === "ROUNDTRIP" &&
      returnInput
        ? returnInput.value
        : "";

    const referenceDate =
      parseIsoDate(state.pendingDepartureDate) ||
      parseIsoDate(getTodayIso()) ||
      new Date();

    state.calendarBaseMonth = new Date(
      referenceDate.getFullYear(),
      referenceDate.getMonth(),
      1,
    );

    renderCalendars();

    openSelectorPanel(
      "date",
      "dateFieldButton",
    );
  }

  function moveCalendar(amount) {
    if (!state.calendarBaseMonth) {
      state.calendarBaseMonth = new Date();
      state.calendarBaseMonth.setDate(1);
    }

    state.calendarBaseMonth = addMonths(
      state.calendarBaseMonth,
      amount,
    );

    renderCalendars();
  }

  function resetPendingDates() {
    state.pendingDepartureDate = "";
    state.pendingReturnDate = "";

    renderCalendars();
  }

  function renderCalendars() {
    if (!state.calendarBaseMonth) {
      const today =
        parseIsoDate(getTodayIso()) ||
        new Date();

      state.calendarBaseMonth = new Date(
        today.getFullYear(),
        today.getMonth(),
        1,
      );
    }

    const rightMonth = addMonths(
      state.calendarBaseMonth,
      1,
    );

    const rangeTitle = byId("calendarRangeTitle");

    if (rangeTitle) {
      rangeTitle.textContent =
        `${formatYearMonth(
          state.calendarBaseMonth,
        )} – ${formatYearMonth(
          rightMonth,
        )}`;
    }

    renderCalendarMonth(
      byId("calendarMonthLeft"),
      state.calendarBaseMonth,
    );

    renderCalendarMonth(
      byId("calendarMonthRight"),
      rightMonth,
    );

    updatePendingDateSummary();
  }

  function renderCalendarMonth(
    container,
    monthDate,
  ) {
    if (!container) {
      return;
    }

    const year = monthDate.getFullYear();
    const month = monthDate.getMonth();

    const firstDay = new Date(
      year,
      month,
      1,
    );

    const lastDay = new Date(
      year,
      month + 1,
      0,
    );

    const today =
      parseIsoDate(getTodayIso()) ||
      new Date();

    const selectedDeparture =
      parseIsoDate(
        state.pendingDepartureDate,
      );

    const selectedReturn =
      parseIsoDate(
        state.pendingReturnDate,
      );

    const title = document.createElement("div");

    title.className = "home-calendar-month-title";

    title.textContent =
      `${year}. ${String(
        month + 1,
      ).padStart(2, "0")}`;

    const weekdays =
      document.createElement("div");

    weekdays.className =
      "home-calendar-weekdays";

    WEEKDAY_NAMES.forEach(
      (dayName, index) => {
        const span =
          document.createElement("span");

        span.textContent = dayName;

        if (index === 0) {
          span.classList.add("is-sunday");

        } else if (index === 6) {
          span.classList.add("is-saturday");
        }

        weekdays.appendChild(span);
      },
    );

    const days = document.createElement("div");

    days.className =
      "home-calendar-days";

    for (
      let blankIndex = 0;
      blankIndex < firstDay.getDay();
      blankIndex += 1
    ) {
      const blank =
        document.createElement("span");

      blank.className =
        "home-calendar-blank";

      days.appendChild(blank);
    }

    for (
      let day = 1;
      day <= lastDay.getDate();
      day += 1
    ) {
      const date = new Date(
        year,
        month,
        day,
      );

      const isoDate = toIsoDate(date);

      const button =
        document.createElement("button");

      button.type = "button";
      button.className = "home-calendar-day";
      button.dataset.calendarDate = isoDate;
      button.textContent = String(day);

      button.setAttribute(
        "aria-label",
        `${year}년 ${month + 1}월 ${day}일`,
      );

      if (date.getDay() === 0) {
        button.classList.add("is-sunday");

      } else if (date.getDay() === 6) {
        button.classList.add("is-saturday");
      }

      if (isSameDate(date, today)) {
        button.classList.add("is-today");
      }

      if (
        isSameDate(
          date,
          selectedDeparture,
        )
      ) {
        button.classList.add("is-departure");
      }

      if (
        isSameDate(
          date,
          selectedReturn,
        )
      ) {
        button.classList.add("is-return");
      }

      if (
        selectedDeparture &&
        selectedReturn &&
        date > selectedDeparture &&
        date < selectedReturn
      ) {
        button.classList.add("is-in-range");
      }

      if (
        isSameDate(
          date,
          selectedDeparture,
        ) &&
        !selectedReturn
      ) {
        button.classList.add("is-single");
      }

      button.disabled =
        cloneDate(date) < cloneDate(today);

      days.appendChild(button);
    }

    container.replaceChildren(
      title,
      weekdays,
      days,
    );
  }

  function selectCalendarDate(isoDate) {
    if (!isoDate) {
      return;
    }

    if (
      getSelectedTripType() === "ONEWAY"
    ) {
      state.pendingDepartureDate = isoDate;
      state.pendingReturnDate = "";

      renderCalendars();
      return;
    }

    if (
      !state.pendingDepartureDate ||
      state.pendingReturnDate
    ) {
      state.pendingDepartureDate = isoDate;
      state.pendingReturnDate = "";

    } else if (
      isoDate < state.pendingDepartureDate
    ) {
      state.pendingDepartureDate = isoDate;
      state.pendingReturnDate = "";

    } else {
      state.pendingReturnDate = isoDate;
    }

    renderCalendars();
  }

  function updatePendingDateSummary() {
    const isOneWay =
      getSelectedTripType() === "ONEWAY";

    const departureText =
      byId("pendingDepartureText");

    const returnText =
      byId("pendingReturnText");

    const returnSummary =
      byId("pendingReturnSummary");

    const arrow =
      byId("pendingDateArrow");

    const help =
      byId("dateSelectionHelp");

    if (departureText) {
      departureText.textContent =
        formatKoreanDate(
          state.pendingDepartureDate,
          true,
        );
    }

    if (returnText) {
      returnText.textContent =
        formatKoreanDate(
          state.pendingReturnDate,
          true,
        );
    }

    if (returnSummary) {
      returnSummary.hidden = isOneWay;
    }

    if (arrow) {
      arrow.hidden = isOneWay;
    }

    if (!help) {
      return;
    }

    if (isOneWay) {
      help.textContent =
        state.pendingDepartureDate
          ? "선택한 가는 날을 적용할 수 있습니다."
          : "가는 날을 선택해 주세요.";

      return;
    }

    if (!state.pendingDepartureDate) {
      help.textContent =
        "가는 날을 먼저 선택한 뒤 오는 날을 선택해 주세요.";

    } else if (!state.pendingReturnDate) {
      help.textContent =
        "이제 오는 날을 선택해 주세요.";

    } else {
      help.textContent =
        "선택한 탑승일을 적용할 수 있습니다.";
    }
  }

  function applyDateSelection(
    openPassengerNext,
  ) {
    const tripType =
      getSelectedTripType();

    if (!state.pendingDepartureDate) {
      window.alert(
        "가는 날을 선택해 주세요.",
      );

      return;
    }

    if (
      tripType === "ROUNDTRIP" &&
      !state.pendingReturnDate
    ) {
      window.alert(
        "오는 날을 선택해 주세요.",
      );

      return;
    }

    const departureInput =
      byId("departureDate");

    const returnInput =
      byId("returnDate");

    if (!departureInput || !returnInput) {
      return;
    }

    departureInput.value =
      state.pendingDepartureDate;

    returnInput.value =
      tripType === "ROUNDTRIP"
        ? state.pendingReturnDate
        : "";

    returnInput.disabled =
      tripType === "ONEWAY";

    syncDateDisplay();

    if (openPassengerNext) {
      openPassengerPanel();

    } else {
      closeSelectorPanel();
    }
  }

  /* =========================================================
     승객
  ========================================================= */

  function openPassengerPanel() {
    syncPassengerDisplay();

    openSelectorPanel(
      "passenger",
      "passengerFieldButton",
    );
  }

  function getPassengerCount(type) {
    const input = byId(`${type}Count`);

    return input
      ? Number(input.value || 0)
      : 0;
  }

  function changePassengerCount(
    type,
    amount,
  ) {
    const input = byId(`${type}Count`);

    const valueElement =
      byId(`val${capitalize(type)}`);

    if (!input || !valueElement) {
      return;
    }

    const current =
      Number(input.value || 0);

    const next = current + amount;

    const minimum =
      type === "adult"
        ? 1
        : 0;

    if (next < minimum) {
      return;
    }

    const adult =
      type === "adult"
        ? next
        : getPassengerCount("adult");

    const child =
      type === "child"
        ? next
        : getPassengerCount("child");

    const infant =
      type === "infant"
        ? next
        : getPassengerCount("infant");

    const total =
      adult + child + infant;

    if (total > 9) {
      window.alert(
        "전체 승객은 최대 9명까지 선택할 수 있습니다.",
      );

      return;
    }

    if (infant > adult) {
      window.alert(
        "유아 수는 성인 수를 초과할 수 없습니다.",
      );

      return;
    }

    input.value = String(next);
    valueElement.textContent = String(next);

    syncPassengerDisplay();
  }

  function syncPassengerDisplay() {
    const adult =
      getPassengerCount("adult");

    const child =
      getPassengerCount("child");

    const infant =
      getPassengerCount("infant");

    const display =
      byId("passengerDisplayText");

    const parts = [`성인 ${adult}`];

    if (child > 0) {
      parts.push(`소아 ${child}`);
    }

    if (infant > 0) {
      parts.push(`유아 ${infant}`);
    }

    if (display) {
      display.textContent =
        parts.join(", ");
    }

    const adultValue =
      byId("valAdult");

    const childValue =
      byId("valChild");

    const infantValue =
      byId("valInfant");

    if (adultValue) {
      adultValue.textContent =
        String(adult);
    }

    if (childValue) {
      childValue.textContent =
        String(child);
    }

    if (infantValue) {
      infantValue.textContent =
        String(infant);
    }
  }

  /* =========================================================
     나이 계산기
  ========================================================= */

  function initializeAgeCalculator() {
    const yearSelect = byId("birthYear");
    const monthSelect = byId("birthMonth");

    if (!yearSelect || !monthSelect) {
      return;
    }

    const currentYear =
      new Date().getFullYear();

    yearSelect.innerHTML =
      '<option value="">연도</option>';

    monthSelect.innerHTML =
      '<option value="">월</option>';

    for (
      let year = currentYear;
      year >= currentYear - 100;
      year -= 1
    ) {
      yearSelect.add(
        new Option(
          String(year),
          String(year),
        ),
      );
    }

    for (
      let month = 1;
      month <= 12;
      month += 1
    ) {
      monthSelect.add(
        new Option(
          String(month),
          String(month),
        ),
      );
    }

    updateBirthDayOptions();
  }

  function updateBirthDayOptions() {
    const yearSelect = byId("birthYear");
    const monthSelect = byId("birthMonth");
    const daySelect = byId("birthDay");

    if (
      !yearSelect ||
      !monthSelect ||
      !daySelect
    ) {
      return;
    }

    const previousValue = daySelect.value;

    const year = Number(
      yearSelect.value ||
      new Date().getFullYear(),
    );

    const month = Number(
      monthSelect.value || 1,
    );

    const lastDay = new Date(
      year,
      month,
      0,
    ).getDate();

    daySelect.innerHTML =
      '<option value="">일</option>';

    for (
      let day = 1;
      day <= lastDay;
      day += 1
    ) {
      daySelect.add(
        new Option(
          String(day),
          String(day),
        ),
      );
    }

    if (
      previousValue &&
      Number(previousValue) <= lastDay
    ) {
      daySelect.value = previousValue;
    }
  }

  function toggleAgeCalculator() {
    const panel =
      byId("ageCalculatorPanel");

    const toggle =
      byId("ageCalculatorToggle");

    if (!panel || !toggle) {
      return;
    }

    const willOpen = panel.hidden;

    panel.hidden = !willOpen;

    toggle.setAttribute(
      "aria-expanded",
      String(willOpen),
    );
  }

  function calculatePassengerAge() {
    const year = Number(
      byId("birthYear")?.value || 0,
    );

    const month = Number(
      byId("birthMonth")?.value || 0,
    );

    const day = Number(
      byId("birthDay")?.value || 0,
    );

    const result =
      byId("ageCalculatorResult");

    if (!result) {
      return;
    }

    if (!year || !month || !day) {
      setAgeResult(
        "생년월일을 모두 선택해 주세요.",
        true,
      );

      return;
    }

    const birthDate = new Date(
      year,
      month - 1,
      day,
    );

    const departureDate =
      parseIsoDate(
        byId("departureDate")?.value,
      ) ||
      parseIsoDate(getTodayIso()) ||
      new Date();

    if (birthDate > departureDate) {
      setAgeResult(
        "출발일보다 늦은 생년월일은 선택할 수 없습니다.",
        true,
      );

      return;
    }

    let age =
      departureDate.getFullYear() -
      birthDate.getFullYear();

    const birthdayNotPassed =
      departureDate.getMonth() <
        birthDate.getMonth() ||
      (
        departureDate.getMonth() ===
          birthDate.getMonth() &&
        departureDate.getDate() <
          birthDate.getDate()
      );

    if (birthdayNotPassed) {
      age -= 1;
    }

    const adultAge =
      isDomesticItinerary()
        ? 13
        : 12;

    let category = "성인";

    if (age < 2) {
      category = "유아";

    } else if (age < adultAge) {
      category = "소아";
    }

    setAgeResult(
      `출발일 기준 만 ${age}세로, ${category}에 해당합니다.`,
      false,
    );
  }

  function setAgeResult(message, warning) {
    const result =
      byId("ageCalculatorResult");

    if (!result) {
      return;
    }

    result.textContent = message;

    result.classList.toggle(
      "is-warning",
      warning,
    );
  }

  function isDomesticItinerary() {
    const departure = getAirportById(
      byId("departureAirportId")?.value,
    );

    const arrival = getAirportById(
      byId("arrivalAirportId")?.value,
    );

    return (
      Boolean(departure && arrival) &&
      getAirportRegion(departure.country)
        === "KOREA" &&
      getAirportRegion(arrival.country)
        === "KOREA"
    );
  }

  /* =========================================================
     좌석 등급
  ========================================================= */

  function initializeSeatDescriptions() {
    all(".home-seat-option").forEach((button) => {
      const description =
        button.querySelector(
          ".home-seat-option-description",
        );

      if (description) {
        description.textContent =
          getSeatDescription(
            button.dataset.seatName,
          );
      }
    });
  }

  function openSeatPanel() {
    state.pendingSeatClassId =
      byId("seatClassId")?.value || "";

    updateSeatOptionSelection();

    openSelectorPanel(
      "seat",
      "seatFieldButton",
    );
  }

  function selectPendingSeatClass(button) {
    state.pendingSeatClassId =
      String(
        button.dataset.seatId || "",
      );

    updateSeatOptionSelection();
  }

  function updateSeatOptionSelection() {
    all(".home-seat-option").forEach((button) => {
      const selected =
        String(button.dataset.seatId || "")
          === state.pendingSeatClassId;

      button.classList.toggle(
        "is-selected",
        selected,
      );

      button.setAttribute(
        "aria-pressed",
        String(selected),
      );
    });

    const selected = getSeatClassById(
      state.pendingSeatClassId,
    );

    const help =
      byId("seatSelectionHelp");

    if (help) {
      help.textContent = selected
        ? `${selected.name}을(를) 선택했습니다.`
        : "좌석 등급을 선택해 주세요.";
    }
  }

  function applySeatClassSelection() {
    if (!state.pendingSeatClassId) {
      window.alert(
        "좌석 등급을 선택해 주세요.",
      );

      return;
    }

    const input = byId("seatClassId");

    if (input) {
      input.value =
        state.pendingSeatClassId;
    }

    syncSeatDisplay();
    closeSelectorPanel();
  }

  function syncSeatDisplay() {
    const selected = getSeatClassById(
      byId("seatClassId")?.value,
    );

    const display =
      byId("seatDisplayText");

    if (display) {
      display.textContent = selected
        ? selected.name
        : "좌석 등급";
    }

    const field =
      byId("seatFieldButton");

    if (field) {
      field.classList.toggle(
        "is-selected",
        Boolean(selected),
      );
    }
  }

  function getSeatDescription(name) {
    const value =
      String(name || "").toLowerCase();

    if (
      /(first|일등|퍼스트)/i.test(value)
    ) {
      return "최상위 서비스와 넓은 개인 공간";
    }

    if (
      /(business|비즈니스)/i.test(value)
    ) {
      return "여유로운 좌석과 우선 서비스";
    }

    if (
      /(premium|프리미엄)/i.test(value)
    ) {
      return "더 넓은 간격과 편안한 여행";
    }

    return "합리적인 운임의 기본 좌석";
  }

  /* =========================================================
     검색 검증
  ========================================================= */

  function validateSearchForm(event) {
    const departureId =
      byId("departureAirportId")?.value || "";

    const arrivalId =
      byId("arrivalAirportId")?.value || "";

    const departureDate =
      byId("departureDate")?.value || "";

    const returnDate =
      byId("returnDate")?.value || "";

    const seatClassId =
      byId("seatClassId")?.value || "";

    const tripType =
      getSelectedTripType();

    let message = "";

    if (!departureId || !arrivalId) {
      message =
        "출발지와 도착지를 모두 선택해 주세요.";

    } else if (departureId === arrivalId) {
      message =
        "출발지와 도착지는 서로 달라야 합니다.";

    } else if (!departureDate) {
      message =
        "가는 날을 선택해 주세요.";

    } else if (
      tripType === "ROUNDTRIP" &&
      !returnDate
    ) {
      message =
        "왕복 여정은 오는 날을 선택해 주세요.";

    } else if (
      tripType === "ROUNDTRIP" &&
      returnDate < departureDate
    ) {
      message =
        "오는 날은 가는 날 이후여야 합니다.";

    } else if (!seatClassId) {
      message =
        "좌석 등급을 선택해 주세요.";
    }

    if (message) {
      event.preventDefault();
      window.alert(message);
    }
  }

  /* =========================================================
     이벤트 슬라이더
  ========================================================= */

  function initializeEventSlider() {
    const slider = byId("mainEventSlider");

    if (!slider) {
      return;
    }

    const slides =
      all(".event-slide", slider);

    const dots =
      all(".event-dot", slider);

    const previousButton =
      slider.querySelector(".event-prev");

    const nextButton =
      slider.querySelector(".event-next");

    const dotContainer =
      slider.querySelector(".event-dots");

    if (slides.length <= 1) {
      if (previousButton) {
        previousButton.hidden = true;
      }

      if (nextButton) {
        nextButton.hidden = true;
      }

      if (dotContainer) {
        dotContainer.hidden = true;
      }

      return;
    }

    let currentIndex = 0;

    const showSlide = (index) => {
      currentIndex =
        (index + slides.length)
        % slides.length;

      slides.forEach(
        (slide, slideIndex) => {
          slide.classList.toggle(
            "active",
            slideIndex === currentIndex,
          );
        },
      );

      dots.forEach((dot, dotIndex) => {
        const active =
          dotIndex === currentIndex;

        dot.classList.toggle(
          "active",
          active,
        );

        dot.setAttribute(
          "aria-current",
          active
            ? "true"
            : "false",
        );
      });
    };

    const stopAutoSlide = () => {
      if (state.sliderTimerId !== null) {
        window.clearInterval(
          state.sliderTimerId,
        );

        state.sliderTimerId = null;
      }
    };

    const startAutoSlide = () => {
      stopAutoSlide();

      state.sliderTimerId =
        window.setInterval(
          () =>
            showSlide(currentIndex + 1),
          4500,
        );
    };

    previousButton?.addEventListener(
      "click",
      () => {
        showSlide(currentIndex - 1);
        startAutoSlide();
      },
    );

    nextButton?.addEventListener(
      "click",
      () => {
        showSlide(currentIndex + 1);
        startAutoSlide();
      },
    );

    dots.forEach((dot, index) => {
      dot.addEventListener("click", () => {
        showSlide(index);
        startAutoSlide();
      });
    });

    slider.addEventListener(
      "mouseenter",
      stopAutoSlide,
    );

    slider.addEventListener(
      "mouseleave",
      startAutoSlide,
    );

    slider.addEventListener(
      "focusin",
      stopAutoSlide,
    );

    slider.addEventListener(
      "focusout",
      startAutoSlide,
    );

    showSlide(0);
    startAutoSlide();
  }

  /* =========================================================
     날짜 유틸리티
  ========================================================= */

  function getTodayIso() {
    const configuredToday =
      byId("homePageConfig")?.dataset.today;

    if (
      configuredToday &&
      /^\d{4}-\d{2}-\d{2}$/
        .test(configuredToday)
    ) {
      return configuredToday;
    }

    return toIsoDate(new Date());
  }

  function parseIsoDate(value) {
    const normalized = String(value || "");

    if (
      !/^\d{4}-\d{2}-\d{2}$/
        .test(normalized)
    ) {
      return null;
    }

    const [year, month, day] =
      normalized.split("-").map(Number);

    const date = new Date(
      year,
      month - 1,
      day,
    );

    if (
      date.getFullYear() !== year ||
      date.getMonth() !== month - 1 ||
      date.getDate() !== day
    ) {
      return null;
    }

    return date;
  }

  function toIsoDate(date) {
    const year = date.getFullYear();

    const month = String(
      date.getMonth() + 1,
    ).padStart(2, "0");

    const day = String(
      date.getDate(),
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
  }

  function cloneDate(date) {
    return new Date(
      date.getFullYear(),
      date.getMonth(),
      date.getDate(),
    );
  }

  function addMonths(date, amount) {
    return new Date(
      date.getFullYear(),
      date.getMonth() + amount,
      1,
    );
  }

  function isSameDate(left, right) {
    return (
      Boolean(left && right) &&
      left.getFullYear()
        === right.getFullYear() &&
      left.getMonth()
        === right.getMonth() &&
      left.getDate()
        === right.getDate()
    );
  }

  function formatYearMonth(date) {
    return `${date.getFullYear()}.${String(
      date.getMonth() + 1,
    ).padStart(2, "0")}`;
  }

  function formatKoreanDate(
    value,
    includeYear,
  ) {
    const date =
      typeof value === "string"
        ? parseIsoDate(value)
        : value;

    if (!date) {
      return "선택 전";
    }

    const year = includeYear
      ? `${date.getFullYear()}.`
      : "";

    const month = String(
      date.getMonth() + 1,
    ).padStart(2, "0");

    const day = String(
      date.getDate(),
    ).padStart(2, "0");

    return (
      `${year}${month}.${day} `
      + `(${WEEKDAY_NAMES[date.getDay()]})`
    );
  }

  function formatCompactDate(
    value,
    includeYear,
  ) {
    const date = parseIsoDate(value);

    if (!date) {
      return "";
    }

    const month = String(
      date.getMonth() + 1,
    ).padStart(2, "0");

    const day = String(
      date.getDate(),
    ).padStart(2, "0");

    return includeYear
      ? `${date.getFullYear()}.${month}.${day}`
      : `${month}.${day}`;
  }

  function capitalize(value) {
    const text = String(value || "");

    return (
      text.charAt(0).toUpperCase()
      + text.slice(1)
    );
  }
})();