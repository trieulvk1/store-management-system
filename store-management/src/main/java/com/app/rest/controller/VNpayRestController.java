package com.app.rest.controller;
import java.io.IOException;
import java.io.ObjectInputFilter.Config;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONObject;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.VNpayConfig;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class VNpayRestController {

  @PostMapping("/createPayment")
  public void createPayment(@RequestParam("amount") int amount,
                            @RequestParam("bankCode") String bankCode,
                            @RequestParam(value = "language", required = false, defaultValue = "vn") String language,
                            HttpServletRequest request, HttpServletResponse resp) throws IOException {
      String vnp_Version = "2.1.0";
      String vnp_Command = "pay";
      String orderType = "other";
      String vnp_TxnRef = VNpayConfig.getRandomNumber(8);
      String vnp_IpAddr = VNpayConfig.getIpAddress(request);
  
      String vnp_TmnCode = VNpayConfig.vnp_TmnCode;
  
      Map<String, String> vnp_Params = new HashMap<>();
      vnp_Params.put("vnp_Version", vnp_Version);
      vnp_Params.put("vnp_Command", vnp_Command);
      vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
      vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
      vnp_Params.put("vnp_CurrCode", "VND");
  
      if (bankCode != null && !bankCode.isEmpty()) {
          vnp_Params.put("vnp_BankCode", bankCode);
      }
      vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
      vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + vnp_TxnRef);
      vnp_Params.put("vnp_OrderType", orderType);
  
      vnp_Params.put("vnp_Locale", "vn");
  
      vnp_Params.put("vnp_ReturnUrl", VNpayConfig.vnp_ReturnUrl);
      vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
  
      Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
      String vnp_CreateDate = formatter.format(cld.getTime());
      vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
  
      cld.add(Calendar.MINUTE, 15);
      String vnp_ExpireDate = formatter.format(cld.getTime());
      vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
  
      List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
      Collections.sort(fieldNames);
      StringBuilder hashData = new StringBuilder();
      StringBuilder query = new StringBuilder();
      Iterator<String> itr = fieldNames.iterator();
      while (itr.hasNext()) {
          String fieldName = itr.next();
          String fieldValue = vnp_Params.get(fieldName);
          if ((fieldValue != null) && (!fieldValue.isEmpty())) {
              // Build hash data
              hashData.append(fieldName);
              hashData.append('=');
              hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString())); // Encode field value with UTF-8
              // Build query
              query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8.toString())); // Encode field name with UTF-8
              query.append('=');
              query.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString())); // Encode field value with UTF-8
              if (itr.hasNext()) {
                  query.append('&');
                  hashData.append('&');
              }
          }
      }
      String queryUrl = query.toString();
      String vnp_SecureHash = VNpayConfig.hmacSHA512(VNpayConfig.secretKey, hashData.toString());
      queryUrl += "&vnp_SecureHash=" + URLEncoder.encode(vnp_SecureHash, StandardCharsets.UTF_8.toString()); // Encode SecureHash with UTF-8
      String paymentUrl = VNpayConfig.vnp_PayUrl + "?" + queryUrl;
      
      // Create JsonObject manually
      JSONObject job = new JSONObject();
      job.put("code", "00");
      job.put("message", "success");
      job.put("data", paymentUrl);
  
      // Write response directly
      resp.setContentType("application/json");
      resp.setCharacterEncoding("UTF-8");
      resp.getWriter().write(job.toString());
  }
  
}
