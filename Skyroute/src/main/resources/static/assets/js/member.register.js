$(function(){
	/*-------------------
	 * 회원 가입
	 *-------------------*/
	//아이디 중복 여부 저장 변수
	let checkId = 0; //0은 중복 체크 미실행/중복, 1은 미중복
	
	//아이디 중복 체크
	$('#confirm_id').click(function(){
		if($('#id').val().trim()==''){
			$('#message_id').css('color','red')
			                .text('아이디를 입력하세요');
			$('#id').val('').focus();
			return;				
		}
		
		$('#message_id').text('');//메시지 초기화
		
		//서버와 통신
		$.ajax({
			url:'/member/confirmId/' + $('#id').val(),
			type:'get',
			dataType:'json',
			success:function(param){
				if(param.result == 'idNotFound'){
					checkId = 1;
					$('#message_id').css('color','#000')
					                .text('등록 가능 ID');
				}else if(param.result == 'idDuplicated'){
					checkId = 0;
					$('#message_id').css('color','red')
									.text('중복된 ID');
					$('#id').val('').focus();				
				}else if(param.result == 'notMatchPattern'){
					checkId = 0;
					$('#message_id').css('color','red')
									.text('영문,숫자 4~14자');
					$('#id').val('').focus();
				}else{
					checkId=0;
					alert('아이디 중복 체크 오류');
				}
			},
			error:function(){
				checkId=0;
				alert('네트워크 오류 발생');
			}
		});
	});
	
	
	//submit 이벤트 발생시 아이디, 별명 중복 체크 여부 확인
	$('#member_register').submit(function(){
		//아이디 중복 체크 필수
		if(checkId == 0){
			$('#message_id').css('color','red')
			                .text('아이디 중복 체크 필수!');
			if($('#id').val().trim()==''){
				$('#id').val('').focus();
			}
			return false;				
		}
	});	
});

