package com.example.hm_project.view.activity;

import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hm_project.R;
import com.example.hm_project.data.SystemManager;
import com.google.android.material.textfield.TextInputEditText;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.callback.UnLinkResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;

public class LoginActivity extends AppCompatActivity {

    private SessionCallback sessionCallback;
    TextInputEditText TextInputEditText_ID, TextInputEditText_Password;
    RelativeLayout RelativeLayout_login, RelativeLayout_login_kakao;
    TextView FindEmail,FindPassword,SignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sessionCallback = new SessionCallback(); //세션 받아옴
        Session.getCurrentSession().addCallback(sessionCallback);//자동로그인
        Session.getCurrentSession().checkAndImplicitOpen();

        init();
    }

    private void init(){
        TextInputEditText_ID = findViewById(R.id.TextInputEditText_ID);
        TextInputEditText_Password = findViewById(R.id.TextInputEditText_Password);
        RelativeLayout_login = findViewById(R.id.RelativeLayout_login);
        RelativeLayout_login_kakao=findViewById(R.id.RelativeLayout_login_Kakao);
        FindEmail=findViewById(R.id.FindEmail);
        FindPassword=findViewById(R.id.FindPassowrd);
        SignUp = findViewById(R.id.SignUP);

        RelativeLayout_login.setClickable(true);  // 로그인을 클릭했을 때 MainActivity로 이동시킨다.
        RelativeLayout_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        RelativeLayout_login_kakao.setClickable(true);
        RelativeLayout_login_kakao.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.getCurrentSession().open(AuthType.KAKAO_LOGIN_ALL, LoginActivity.this);
            }
        });

        FindEmail.setClickable(true);
        FindEmail.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,FindEmailActivity.class);
                startActivity(intent);
            }
        });

        FindPassword.setClickable(true);
        FindPassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this,FindPasswordActivity.class);
                startActivity(intent);
            }
        });

        SignUp.setClickable(true); // Signup을 클릭했을 때 Signup화면으로 이동시킨다.
        SignUp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(sessionCallback);
    }

    private class SessionCallback implements ISessionCallback {
        @Override
        public void onSessionOpened() {
            UserManagement.getInstance().me(new MeV2ResponseCallback() {
                @Override
                public void onFailure(ErrorResult errorResult) {
                    int result = errorResult.getErrorCode();

                    if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                        Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(),"로그인 도중 오류가 발생했습니다: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Toast.makeText(getApplicationContext(),"세션이 닫혔습니다. 다시 시도해 주세요: "+errorResult.getErrorMessage(),Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(MeV2Response result) {
                    String needsScopeAutority = ""; // 정보 제공이 허용되지 않은 항목의 이름을 저장하는 변수

                    // 이메일, 성별, 연령대, 생일 정보를 제공하는 것에 동의했는지 체크
                    if(result.getKakaoAccount().needsScopeAccountEmail()) {
                        needsScopeAutority = needsScopeAutority + "이메일";
                    }
                    if(result.getKakaoAccount().needsScopeGender()) {
                        needsScopeAutority = needsScopeAutority + ", 성별";
                    }
                    if(result.getKakaoAccount().needsScopeAgeRange()) {
                        needsScopeAutority = needsScopeAutority + ", 연령대";
                    }
                    if(result.getKakaoAccount().needsScopeBirthday()) {
                        needsScopeAutority = needsScopeAutority + ", 생일";
                    }

                    if(needsScopeAutority.length() != 0) { // 정보 제공이 허용되지 않은 항목이 있다면 -> 허용되지 않은 항목을 안내하고 회원탈퇴 처리
                        if(needsScopeAutority.charAt(0) == ',') {
                            needsScopeAutority = needsScopeAutority.substring(2);
                        }
                        Toast.makeText(getApplicationContext(), needsScopeAutority+"에 대한 권한이 허용되지 않았습니다. 개인정보 제공에 동의해주세요.", Toast.LENGTH_SHORT).show(); // 개인정보 제공에 동의해달라는 Toast 메세지 띄움

                        // 회원탈퇴 처리
                        UserManagement.getInstance().requestUnlink(new UnLinkResponseCallback() {
                            @Override
                            public void onFailure(ErrorResult errorResult) {
                                int result = errorResult.getErrorCode();

                                if(result == ApiErrorCode.CLIENT_ERROR_CODE) {
                                    Toast.makeText(getApplicationContext(), "네트워크 연결이 불안정합니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "오류가 발생했습니다. 다시 시도해 주세요.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onSessionClosed(ErrorResult errorResult) {
                                Toast.makeText(getApplicationContext(), "로그인 세션이 닫혔습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onNotSignedUp() {
                                Toast.makeText(getApplicationContext(), "가입되지 않은 계정입니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onSuccess(Long result) { }
                        });
                    } else { // 모든 항목에 동의했다면 -> 유저 정보를 가져와서 MainActivity에 전달하고 MainActivity 실행.
                        Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                        SystemManager.NicName=result.getNickname();
                        SystemManager.profilePhoto=result.getProfileImagePath();
                        if (result.getKakaoAccount().hasEmail() == OptionalBoolean.TRUE)
                            SystemManager.ID=result.getKakaoAccount().getEmail();

                        startActivity(intent);
                        finish();
                    }
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException e) {
            Toast.makeText(getApplicationContext(), "로그인 도중 오류가 발생했습니다. 인터넷 연결을 확인해주세요: "+e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
}