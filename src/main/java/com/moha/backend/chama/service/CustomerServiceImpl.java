package com.moha.backend.chama.service;

import com.moha.backend.chama.entity.Transaction;
import com.moha.backend.chama.exception.NonRollbackException;
import com.moha.backend.chama.utils.BeareTokenService;
import com.moha.backend.chama.utils.Utils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 *
 * @author moha
 */
@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    CrudService crudeService;
    @Autowired
    Environment env;
    private final Logger LOG = LoggerFactory.getLogger(CustomerServiceImpl.class);

    public static final String  stk_push_shortcode = "174379";
    public static final String  passkey = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919";
    public static final String stk_end_point ="https://sandbox.192.168.100.76safaricom.co.ke/mpesa/stkpush/v1/processrequest";
    public static final String callbackurl = "http:///callback_url.php";
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerServiceImpl.class.getSimpleName());
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");
    @Override
    public String changeCustomerPin(String msisdn, String pin, String newpin) {
        String msg="";
        String sq = "SELECT pin FROM customers where msisdn='"+msisdn+"'";
        List<Object> rs = crudeService.fetchWithNativeQuery(sq, Collections.EMPTY_MAP, 0, 1);
        if(rs.get(0).toString().equals(Utils.getSHA256(pin))){
            String sql = "UPDATE customers SET pin='"+Utils.getSHA256(newpin)+"' WHERE msisdn='"+msisdn+"'";
            LOG.info("Cool "+sql);
            crudeService.executeNativeQuery(sql, Collections.EMPTY_MAP);
            msg ="00";
        }else{
            msg ="01";
        }
        LOG.info("Cool "+msg);
        return msg;
    }

    @Override
    public String AuthenticateCustomer(String msisdn, String pin) {
        try{
            String q = "SELECT pin FROM customers where msisdn='"+msisdn+"'";
            List<Object> response = crudeService.fetchWithNativeQuery(q, Collections.EMPTY_MAP, 0, 1);
            if(String.valueOf(response.get(0)).equalsIgnoreCase(Utils.getSHA256(pin))){
                String sql = "SELECT firstname FROM customers where msisdn='"+msisdn+"'";
                String msg = crudeService.fetchWithNativeQuery(sql, Collections.EMPTY_MAP, 0, 1).get(0).toString();
                return msg;
            }
        }catch(Exception e){
            return e.getLocalizedMessage();
        }
        return null;
    }

    @Override
    public String processSavingsDeposit(String msisdn, String amount, String ref) throws NonRollbackException {
        Date now = new Date(System.currentTimeMillis());
        try {
            String _timestamp = SDF.format(now);
            String requestBody = "{\n"
                    + "      \"BusinessShortCode\": \"" + stk_push_shortcode + "\",\n"
                    + "      \"Password\": \"" + getRequestPassword(stk_push_shortcode,passkey, _timestamp) + "\",\n"
                    + "      \"Timestamp\": \"" + _timestamp + "\",\n"
                    + "      \"TransactionType\": \"CustomerPayBillOnline\",\n"
                    + "      \"Amount\": \"" + amount + "\",\n"
                    + "      \"PartyA\": \"" + msisdn + "\",\n"
                    + "      \"PartyB\": \"" + stk_push_shortcode + "\",\n"
                    + "      \"PhoneNumber\": \"" + msisdn + "\",\n"
                    + "      \"CallBackURL\": \"" + callbackurl + "\",\n"
                    + "      \"AccountReference\": \"" + ref + "\",\n"
                    + "      \"TransactionDesc\": \"STK Push\"\n"
                    + "    }";
            LOGGER.info("REQBODY={}",requestBody);
            String response = postPayload(stk_end_point, requestBody, "Bearer " + BeareTokenService.token);
            LOGGER.info("response={}", msisdn, response);
            JSONObject jsonObjectResponse = new JSONObject(response);
            String MerchantRequestID = jsonObjectResponse.get("MerchantRequestID").toString();
            String CheckoutRequestID = jsonObjectResponse.get("CheckoutRequestID").toString();
            String ResponseCode = jsonObjectResponse.get("ResponseCode").toString();
            LOGGER.info("Msisdn={}|C2B STK PUSH response params:MerchantRequestID={},CheckoutRequestID={},ResponseCode={}", msisdn, MerchantRequestID, CheckoutRequestID, ResponseCode);
            if (ResponseCode.contentEquals("0")) {
                Transaction trx = new Transaction();
                trx.setTrx_type("STK_PUSH");
                trx.setSender_party(msisdn);
                trx.setReceiver_party(stk_push_shortcode);
                trx.setCompleted_date(now);
                trx.setBillrefnumber(ref);
                trx.setProcessing_status("Completed");
                trx.setAmount(amount);
                trx.setMsisdn(msisdn);
                trx.setRequest_id(MerchantRequestID);
                trx.setCheckout_id(CheckoutRequestID);
                crudeService.save(trx);
                return trx.toString();
            }
        } catch (Exception e) {
            e.getLocalizedMessage();
        }
        return null;
    }

    @Override
    public boolean checkRegistrationstatus(String msisdn) {
        String q = "Select firstname from customers where msisdn='"+msisdn+"'";
        List<Object> response = crudeService.fetchWithNativeQuery(q, Collections.EMPTY_MAP, 0, 1);
        try {
            if (String.valueOf(response.get(0)) != null || !String.valueOf(response.get(0)).equals("")) {
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    private String postPayload(String endPointURL, String requestBody, String authKey) throws IOException {
        LOGGER.info("Endpoint {} Request Body {} Auth Key {} ",endPointURL,requestBody,authKey);
        URL url = new URL(endPointURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json;charset=UTF-8");
        connection.setRequestProperty("Accept",
                "application/json;charset=UTF-8");
        connection.setRequestProperty("Authorization",
                authKey);
        connection.setRequestProperty("Content-Length", ""
                + requestBody.getBytes().length);
        connection.setRequestProperty("Content-Language", "en-US");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);

        try (DataOutputStream wr = new DataOutputStream(
                connection.getOutputStream())) {
            wr.writeBytes(requestBody);
            wr.flush();
        }
        StringBuilder response = new StringBuilder();
        try (InputStream is = connection.getInputStream();
             BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }
    private static String getRequestPassword(String shortcode, String passkey, String timestamp) {
        return Base64.getEncoder().encodeToString(new StringBuilder().append(shortcode).append(passkey).append(timestamp).toString().getBytes());
    }
}
    
    

