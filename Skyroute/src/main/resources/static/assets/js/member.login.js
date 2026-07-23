$(function () {
    const $form = $('#member_login');
    const $id = $('#id');

    /*
     * 관리자 로그인 페이지의 비밀번호 input은 #passwd입니다.
     * 일반 회원 로그인 페이지는 #password를 사용하므로,
     * 아래 검사를 통해 관리자 로그인 페이지에서만 실행합니다.
     */
    const $password = $('#passwd');

    if ($form.length === 0 || $id.length === 0 || $password.length === 0) {
        return;
    }

    const $idError = $('#error_id');
    const $passwordError = $('#error_passwd');

    /*
     * 서버 로그인 실패 메시지는 높이를 없애지 않고
     * 보이지만 않도록 처리합니다.
     */
    function hideServerError() {
        $('.error-invalid').addClass('is-hidden');
    }

    function clearClientErrors() {
        $idError.text('');
        $passwordError.text('');
    }

    $form.on('submit', function (event) {
        const idValue = String($id.val() || '').trim();
        const passwordValue = String($password.val() || '').trim();

        const idEmpty = idValue === '';
        const passwordEmpty = passwordValue === '';

        clearClientErrors();
        hideServerError();

        /*
         * 아이디와 비밀번호가 모두 입력됐으면
         * Spring Security 로그인 요청을 그대로 진행합니다.
         */
        if (!idEmpty && !passwordEmpty) {
            return;
        }

        event.preventDefault();

        if (idEmpty) {
            $idError.text('아이디를 입력하세요.');
        }

        if (passwordEmpty) {
            $passwordError.text('비밀번호를 입력하세요.');
        }

        if (idEmpty) {
            $id.trigger('focus');
        } else {
            $password.trigger('focus');
        }
    });

    /*
     * input 이벤트는 키보드 입력뿐 아니라
     * 붙여넣기와 브라우저 자동완성도 감지합니다.
     */
    $id.on('input', function () {
        $idError.text('');
        hideServerError();
    });

    $password.on('input', function () {
        $passwordError.text('');
        hideServerError();
    });
});