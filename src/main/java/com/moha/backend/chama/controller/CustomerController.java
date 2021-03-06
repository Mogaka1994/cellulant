package com.moha.backend.chama.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moha.backend.chama.entity.Chama;
import com.moha.backend.chama.entity.Customers;
import com.moha.backend.chama.exception.NonRollbackException;
import com.moha.backend.chama.model.ChamaModel;
import com.moha.backend.chama.model.CustomerPayload;
import com.moha.backend.chama.model.PinChange;
import com.moha.backend.chama.model.SaveModel;
import com.moha.backend.chama.repository.CustomerRepository;
import com.moha.backend.chama.service.CrudService;
import com.moha.backend.chama.service.CustomerService;
import com.moha.backend.chama.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 *
 * @author moha
 */
@RestController
@RequestMapping(value ="/chama")
public class CustomerController {
    private final Logger LOG = LoggerFactory.getLogger(CustomerController.class);
    @Autowired
    CrudService crudService;
    @Autowired
    CustomerService service;
    @Autowired
    CustomerRepository cust;
    @Autowired
    Environment environment;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    Map<String, Object> responseMap = new HashMap<>();
    String payload = "{\n" +
            "    \"msisdn\":\"254714593171\",\n" +
            "    \"firstname\":\"Polycarp\",\n" +
            "    \"surname\":\"Moha\",\n" +
            "    \"gender\":\"Male\",\n" +
            "    \"idno\":\"32514541\",\n" +
            "    \"location\":\"Highrise\",\n" +
            "    \"status\":\"ACTIVE\",\n" +
            "    \"marital_status\":\"MARRIED\",\n" +
            "    \"dob\":\"19951004\",\n" +
            "    \"chama_id\":\"1\"\n" +
            "}";
    @PostMapping("/user/registration")
    public ResponseEntity<String> createCustomer(@RequestBody CustomerPayload payload) {
        try {
            LOG.info("MOGAKA");
            LOG.info("Request Data customer nam {},ID {},LOCATION {}",payload.getFirstname()+"-"+payload.getSurname(),payload.getIdno(),payload.getLocation());
            Customers customers = new Customers();
            Integer pin = ThreadLocalRandom.current().nextInt(1001, 9999);
            customers.setFirstname(payload.getFirstname());
            customers.setSurname(payload.getSurname());
            customers.setGender(payload.getGender());
            customers.setIdno(payload.getIdno());
            customers.setStatus("active");
            customers.setDateCreated(new Date());
            customers.setDob(sdf.parse(payload.getDob()));
            customers.setPin(Utils.getSHA256(String.valueOf(pin)));
            customers.setLocation(payload.getLocation());
            customers.setMarital_status(payload.getMaritalStatus());
            customers.setMsisdn(payload.getMsisdn());
            customers.setChama_id(payload.getChama_id());
            if(service.checkRegistrationstatus(payload.getMsisdn())){
                responseMap.put("responseCode","01");
                responseMap.put("responseMessage","You are already registered");
            }else {
                crudService.save(customers);
                sendSms(payload.getMsisdn(),"Thank you for choosing as your finance partner,your secret pin is "+pin);
                responseMap.put("responseCode", "00");
                responseMap.put("responseMessage", "Successfully registered,your pin is " + pin);
            }
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
        } catch (JsonProcessingException | ParseException e) {
            responseMap.put("responseCode", "01");
            responseMap.put("responseMessSage", e.getMessage());
        }
        return null;
    }
    @PostMapping(value = "/customer/pinchange")
    public ResponseEntity<String> pinChange(@RequestBody PinChange payload) throws JsonProcessingException {
        try {
            LOG.info("Awesome "+payload);
            String customer_name = service.changeCustomerPin(payload.getMsisdn(), payload.getPin(), payload.getNewpin());
            if(customer_name.equals("00")){
                responseMap.put("responseCode", "00");
                responseMap.put("responseMessage", "Change Pin request processed successfully");
                return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
            }
        } catch (JsonProcessingException e) {
            e.getLocalizedMessage();
        }
        responseMap.put("responseCode", "01");
        responseMap.put("responseMessage", "pin mismatch");
        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
    }
    @GetMapping("/customers")
    public List<Customers> getAllCustomers() {
        return cust.findAll();
    }
    @PostMapping(value = "/customer/pinauthentication")
    public ResponseEntity<String> AuthenticateCustomer(@RequestBody PinChange payload) throws JsonProcessingException {
        try {
            String customer_name = service.AuthenticateCustomer(payload.getMsisdn(),payload.getPin());
            if(customer_name!=null){
                responseMap.put("responseCode", "00");
                responseMap.put("responseMessage", customer_name+" Authenticated successfully");
                return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
            }
        } catch (JsonProcessingException e) {
            e.getMessage();
        }
        responseMap.put("responseCode", "01");
        responseMap.put("responseMessage","Authentication failed");
        return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
    }
    @PostMapping(value="/chama/registration")
    public ResponseEntity<String>registerChame(@RequestBody ChamaModel payload){
        try{
            Chama chama = new Chama();
            chama.setChama_name(payload.getName());
            chama.setChama_id(payload.getChamaId());
            crudService.save(chama);
            responseMap.put("responseCode", "00");
            responseMap.put("responseMessage", "Chama Created Successfully");
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap),HttpStatus.OK);

        }catch(JsonProcessingException e){
            try {
                responseMap.put("responseCode", "01");
                responseMap.put("responseMessage", e.getMessage());
                return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    /**
     *
     * @param payload
     * @return
     * @throws
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    @PostMapping(value="/savings")
    public ResponseEntity<String>SaveMoney(@RequestBody SaveModel payload) throws NonRollbackException, JsonProcessingException{
        try {
            service.processSavingsDeposit(payload.getMsisdn(),payload.getAmount(),payload.getReference());
            responseMap.put("responseCode", "00");
            responseMap.put("responseMessage","Request Received for Processing" );
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            responseMap.put("responseCode", "01");
            responseMap.put("responseMessage", e.getMessage());
            return new ResponseEntity<>(new ObjectMapper().writeValueAsString(responseMap), HttpStatus.OK);
        }

    }
    public void sendSms(String msisdn, String message) {
        try {
            processSms(environment.getRequiredProperty("sms.url") + msisdn + "&msg=", message);
        } catch (Exception e) {
        }
    }
    private String processSms(String endPointURL, String message) throws MalformedURLException, IOException {
        LOG.info("Sms url {} Message {} ",endPointURL,message);
        URL url = new URL(endPointURL + URLEncoder.encode(message, StandardCharsets.UTF_8.toString()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        StringBuffer response = null;
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

        }
        return response.toString();
    }
}

