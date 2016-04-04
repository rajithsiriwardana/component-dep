/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wso2telco.oneapivalidation.impl;

import com.wso2telco.oneapivalidation.AxiataException;
import com.wso2telco.oneapivalidation.IServiceValidate;
import com.wso2telco.oneapivalidation.UrlValidator;
import com.wso2telco.oneapivalidation.ValidationNew;
import com.wso2telco.oneapivalidation.ValidationRule;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author User
 */
public class ValidateDeliverySubscription implements IServiceValidate {

    private final String[] validationRules = {"outbound", "*", "subscriptions"};

    public void validate(String json) throws AxiataException {

        String clientCorrelator = null;
        String notifyURL = null;
        String callbackData = null;
        String filterCriteria = null;
        try {
            JSONObject mainJson = new JSONObject(json);
            JSONObject deliveryReceiptSubscription = mainJson.getJSONObject("deliveryReceiptSubscription");

            if (deliveryReceiptSubscription.get("filterCriteria") != null) {
                filterCriteria = deliveryReceiptSubscription.getString("filterCriteria");
            }

            JSONObject callbackReference = deliveryReceiptSubscription.getJSONObject("callbackReference");
            if (callbackReference.get("callbackData") != null) {
                callbackData = callbackReference.getString("callbackData");
            }
            if (callbackReference.get("notifyURL") != null) {
                notifyURL = callbackReference.getString("notifyURL");
            }

        } catch (JSONException ex) {
            System.out.println("ValidateDeliverySubscription: "+ex);
            throw new AxiataException("POL0299", "Unexpected Error", new String[]{""});
        }

        ValidationRule[] rules = {
                //new ValidationRule(ValidationRule.VALIDATION_TYPE_MANDATORY_TEL, "senderAddress", senderAddress),
                new ValidationRule(ValidationRule.VALIDATION_TYPE_MANDATORY_URL, "notifyURL", notifyURL),
                new ValidationRule(ValidationRule.VALIDATION_TYPE_OPTIONAL, "clientCorrelator", clientCorrelator),
                new ValidationRule(ValidationRule.VALIDATION_TYPE_OPTIONAL, "callbackData", callbackData),
                new ValidationRule(ValidationRule.VALIDATION_TYPE_MANDATORY, "filterCriteria", filterCriteria),};
        
        ValidationNew.checkRequestParams(rules);

    }

    public void validateUrl(String pathInfo) throws AxiataException {
        String[] requestParts = null;
        if (pathInfo != null) {
            if (pathInfo.startsWith("/")) {
                pathInfo = pathInfo.substring(1);
            }
            requestParts = pathInfo.split("/");
        }

        UrlValidator.validateRequest(requestParts, validationRules);
    }

    public void validate(String[] params) throws AxiataException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}