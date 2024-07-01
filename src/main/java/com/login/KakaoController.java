package com.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
public class KakaoController {
	private final String REST_API_KEY = "b8908d4e43f44454659c9dd7d3e9d56e";
	private final String REDIRECT_URI = "http://localhost:9999/kakao/callback";
	//kakao_login.html
	//kakao_login_result.html
	
	@GetMapping("/kakao")
	public ModelAndView kakaoLoginView(ModelAndView view) {
		String apiURL = "https://kauth.kakao.com/oauth/authorize?"
				+ "response_type=code"
				+ "&client_id=" + REST_API_KEY
				+ "&redirect_uri=" + REDIRECT_URI;
		
		view.addObject("apiURL", apiURL);
		view.setViewName("kakao_login");
		return view;
	}
	
	@GetMapping("/kakao/callback")
	public ModelAndView kakaoCallBack(ModelAndView view, HttpSession session, String code) {
		String logoutURL = "https://kauth.kakao.com/oauth/logout?"
				+ "&client_id="+REST_API_KEY
				+ "&logout_redirect_uri=http://localhost9999/kakao/logout";
		
		String apiURL = "https://kauth.kakao.com/oauth/token?";
		apiURL += "grant_type=authorization_code"
				+ "&client_id=" + REST_API_KEY
				+ "&redirect_uri=" + REDIRECT_URI
				+ "&code=" + code;
		
		String res = requestKakaoServer(apiURL, null);
		
		if(res != null && !res.equals("")) {
			JSONObject json = new JSONObject(res);
			session.setAttribute("user", res);
			session.setAttribute("accessToken", json.get("access_token"));
			if (json.has("refresh_token")) {
                session.setAttribute("refreshToken", json.get("refresh_token"));
            } else {
                session.setAttribute("refreshToken", null);
            }
		}else {
			view.addObject("res", "로그인 실패");
		}
		view.addObject("logoutURL", logoutURL);
		view.setViewName("kakao_login_result");
		
		return view;
	}
	
	@GetMapping("/kakao/profile")
	public ModelAndView getProfile(ModelAndView view, HttpSession session) {
		String token = (String) session.getAttribute("accessToken");
		String header = "Bearer " + token; 
        String apiURL = "https://kapi.kakao.com/v2/user/me";
        
        String result = requestKakaoServer(apiURL, header);
        System.out.println(result);
        view.addObject("userInfo",result);
        view.setViewName("kakao_login_result");
        
		return view;
	}
	
	@GetMapping("/kakao/refresh")
	public ModelAndView refreshToken(ModelAndView view, HttpSession session) {
		String apiURL = "https://kauth.kakao.com/oauth/token?grant_type=refresh_token"
				+ "&client_id=" + REST_API_KEY
				+ "&refresh_token=" + session.getAttribute("refreshToken");

		String res = requestKakaoServer(apiURL, null);
		
		if(res != null && !res.equals("")) {
			JSONObject json = new JSONObject(res);
			session.setAttribute("user", res);
			if (json.has("access_token")) {
                session.setAttribute("accessToken", json.get("access_token"));
            } else {
                view.addObject("res", "Access token not found");
            }
			if (json.has("refresh_token")) {
                session.setAttribute("refreshToken", json.get("refresh_token"));
            } else {
                session.setAttribute("refreshToken", null);
            }
		}else {
			view.addObject("res", "로그인 실패");
		}
		view.setViewName("kakao_login_result");
		return view;
	}
	
	@GetMapping("/kakao/delete")
	public ModelAndView deleteToken(ModelAndView view, HttpSession session) {
		String apiURL = "https://kapi.kakao.com/v1/user/unlink?grant_type=delete"
				+ "&client_id=" + REST_API_KEY
				+ "&access_token=" + session.getAttribute("accessToken");

		String res = requestKakaoServer(apiURL, null);
		System.out.println("delete : " + res);
		session.invalidate();
		view.setViewName("redirect:/kakao");
		return view;
	}
	
	@GetMapping("/kakao/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		System.out.println("logout");
		return "redirect:/kakao";
	}
	
	
	
	public String requestKakaoServer(String apiURL, String header) {
		StringBuffer res = new StringBuffer();
		try {
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			if (header != null && !header.equals("")) {
				con.setRequestProperty("Authorization", header);
			}

			int responseCode = con.getResponseCode();
			BufferedReader br;
			System.out.print("responseCode=" + responseCode);
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				res.append(inputLine);
			}
			br.close();
			if (responseCode == 200) {
				System.out.println(res.toString());
			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return res.toString();
	}
	
}









