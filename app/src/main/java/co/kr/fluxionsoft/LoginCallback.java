package co.kr.fluxionsoft;

import android.os.Bundle;

import android.util.Log;



import com.facebook.AccessToken;

import com.facebook.FacebookCallback;

import com.facebook.FacebookException;

import com.facebook.GraphRequest;

import com.facebook.GraphResponse;

import com.facebook.login.LoginResult;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginCallback implements FacebookCallback<LoginResult> {


    LoginResult loginResult;

    public MainActivity getParentActivity() {
        return parentActivity;
    }

    public void setParentActivity(MainActivity parentActivity) {
        this.parentActivity = parentActivity;
    }

    // 로그인 성공 시 호출 됩니다. Access Token 발급 성공.
    MainActivity parentActivity;

    @Override

    public void onSuccess(LoginResult loginResult_) {

        Log.e("Callback :: ", "onSuccess");
        loginResult=loginResult_;
        requestMe(loginResult.getAccessToken());

    }



    // 로그인 창을 닫을 경우, 호출됩니다.

    @Override

    public void onCancel() {

        Log.e("Callback :: ", "onCancel");

    }



    // 로그인 실패 시에 호출됩니다.

    @Override

    public void onError(FacebookException error) {

        Log.e("Callback :: ", "onError : " + error.getMessage());

    }



    // 사용자 정보 요청

    public void requestMe(AccessToken token) {

        GraphRequest graphRequest = GraphRequest.newMeRequest(token,

                new GraphRequest.GraphJSONObjectCallback() {

                    @Override

                    public void onCompleted(JSONObject object, GraphResponse response) {

                        Log.e("result",object.toString());

                        try {
                            String name=object.getString("name");
                            String id=object.getString("id");
                            String email=object.getString("email");
                            JSONObject picture=object.getJSONObject("picture");
                                JSONObject data=picture.getJSONObject("data");
                                String pictureUrl=data.getString("url");

                            Map map=new HashMap();
                            map.put("userId",id);
                            map.put("name",name);
                            map.put("email",email);
                            map.put("profile_img",pictureUrl);

                            Gson gson=new Gson();
                            String json=gson.toJson(map);

                            loginFacebook(json);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                });



        Bundle parameters = new Bundle();

        parameters.putString("fields", "id,name,email,gender,birthday,picture.type(large)");

        graphRequest.setParameters(parameters);

        graphRequest.executeAsync();

    }

    public void loginFacebook(String json){
        this.parentActivity.facebookLoginOk(json);
    }

}


