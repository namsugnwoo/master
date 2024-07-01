package com.login;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {
	private final String CLIENT_ID = "FIJ5BO2Mw2CGZ2VX4lv1";
	private final String CLIENT_SECRET_ID="TPlH4HrINB";
	
	@GetMapping("/")
	public String main() {
		return "main";
	}
	
	@GetMapping("/naver")
	public ModelAndView naverLoginView(ModelAndView view, HttpSession session) throws UnsupportedEncodingException {
		String redirectURI = URLEncoder.encode("http://localhost:9999/naver/callback", "UTF-8");
		SecureRandom random = new SecureRandom();
		String state = new BigInteger(130, random).toString();
		String apiURL = "https://nid.naver.com/oauth2.0/authorize?response_type=code";
		apiURL += "&client_id=" + CLIENT_ID;
		apiURL += "&redirect_uri=" + redirectURI;
		apiURL += "&state=" + state;
		session.setAttribute("state", state);
		view.addObject("apiURL", apiURL);
		view.setViewName("naver_login");
		return view;
	}

	@GetMapping("/naver/callback")
	public ModelAndView naverCallBack(HttpSession session, ModelAndView view, String state, String code)
			throws UnsupportedEncodingException {
		String redirectURI = URLEncoder.encode("http://localhost:9999/naver/callback", "UTF-8");
		String apiURL;
		apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&";
		apiURL += "client_id=" + CLIENT_ID;
		apiURL += "&client_secret=" + CLIENT_SECRET_ID;
		apiURL += "&redirect_uri=" + redirectURI;
		apiURL += "&code=" + code;
		apiURL += "&state=" + state;
		
		String res = requestNaverServer(apiURL, null);
		
		if(res != null && !res.equals("")) {
			JSONObject json = new JSONObject(res);
			session.setAttribute("user", res);
			session.setAttribute("accessToken", json.get("access_token"));
			session.setAttribute("refreshToken", json.get("refresh_token"));
		}else {
			view.addObject("res", "로그인 실패");
		}
		
		view.setViewName("naver_login_result");
		return view;
	}

	@GetMapping("/naver/profile")
	public ModelAndView getProfile(ModelAndView view, HttpSession session) {
		String token = (String) session.getAttribute("accessToken");
		String header = "Bearer " + token; 
        String apiURL = "https://openapi.naver.com/v1/nid/me";
        
        String result = requestNaverServer(apiURL, header);
        
        view.addObject("userInfo",result);
        view.setViewName("naver_login_result");
        
		return view;
	}
	
	@GetMapping("/naver/refresh")
	public ModelAndView refreshToken(ModelAndView view, HttpSession session) {
		String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=refresh_token"
				+ "&client_id=" + CLIENT_ID
				+ "&client_secret=" + CLIENT_SECRET_ID
				+ "&refresh_token=" + session.getAttribute("refreshToken");

		String res = requestNaverServer(apiURL, null);
		
		if(res != null && !res.equals("")) {
			JSONObject json = new JSONObject(res);
			session.setAttribute("user", res);
			session.setAttribute("accessToken", json.get("access_token"));
			session.setAttribute("refreshToken", json.get("refresh_token"));
		}else {
			view.addObject("res", "로그인 실패");
		}
		view.setViewName("naver_login_result");
		return view;
	}
	
	@GetMapping("/naver/delete")
	public ModelAndView deleteToken(ModelAndView view, HttpSession session) {
		String apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=delete"
				+ "&client_id=" + CLIENT_ID
				+ "&client_secret=" + CLIENT_SECRET_ID
				+ "&access_token=" + session.getAttribute("accessToken");

		String res = requestNaverServer(apiURL, null);
		System.out.println("delete : " + res);
		session.invalidate();
		view.setViewName("redirect:/naver");
		return view;
	}
	
	@GetMapping("/naver/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/naver";
	}
 
	
	public String requestNaverServer(String apiURL, String header) {
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