/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * 
 * WSO2.Telco Inc. licences this file to you under  the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.mediator.mediationrule;


import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.addressing.EndpointReference;

import com.wso2telco.dbutils.AxiataDbService;
import com.wso2telco.dbutils.Operator;
import com.wso2telco.dbutils.Operatorendpoint;
import com.wso2telco.mediator.OperatorEndpoint;
import com.wso2telco.mediator.internal.Util;
import com.wso2telco.mnc.resolver.MNCQueryClient;
import com.wso2telco.oneapivalidation.exceptions.AxiataException;

// TODO: Auto-generated Javadoc
/**
 * The Class OriginatingCountryCalculatorIDD.
 */
public class OriginatingCountryCalculatorIDD extends OriginatingCountryCalculator {
     
    //private HashMap<String, EndpointReference> apiEprMap;

    /** The error message. */
    private String ERROR_MESSAGE = "Error.No API endpoint matched your request";
    
    /** The opendpoints. */
    List<Operatorendpoint> opendpoints;
    
    /** The mnc queryclient. */
    MNCQueryClient mncQueryclient = null;

    /* (non-Javadoc)
     * @see com.wso2telco.mediator.mediationrule.OriginatingCountryCalculator#initialize()
     */
    public void initialize() throws Exception {

        Util.getPropertyFile();
        opendpoints = new AxiataDbService().operatorEndpoints();

        mncQueryclient = new MNCQueryClient();

    }

     
    /**
     * Gets the API endpoints by msisdn.
     *
     * @param userMSISDN the user msisdn
     * @param apikey the apikey
     * @param requestPathURL the request path url
     * @param isredirect the isredirect
     * @param operators the operators
     * @return the API endpoints by msisdn
     * @throws Exception the exception
     */
    public OperatorEndpoint getAPIEndpointsByMSISDN(String userMSISDN, String apikey, String requestPathURL, boolean isredirect, List<Operator> operators) throws Exception {

        String operator;
        String userName = userMSISDN.substring(1);

        //Initialize End points
        initialize();
        
        String mcc = null;        
        //mcc not known in mediator
        operator = mncQueryclient.QueryNetwork(mcc, userMSISDN);
        
        if (operator == null) {
            throw new AxiataException("SVC0001", "", new String[]{"No valid operator found for given MSISDN"});
        }

        //is operator provisioned
        Operator valid = null;
        for (Operator d : operators) {
            if (d.getOperatorname() != null && d.getOperatorname().contains(operator.toUpperCase())) {
                valid = d;
                break;
            }
        }

        if (valid == null) {
            throw new AxiataException("SVC0001", "", new String[]{"Requested service is not provisioned"});
        }

        Operatorendpoint validOperatorendpoint = getValidEndpoints(apikey, operator);
        if (validOperatorendpoint == null) {
            throw new AxiataException("SVC0001", "", new String[]{"Requested service is not provisioned"});
        }
                
        String extremeEndpoint = validOperatorendpoint.getEndpoint();
        if (!isredirect) {
            extremeEndpoint = validOperatorendpoint.getEndpoint() + requestPathURL;
        }
        EndpointReference eprMSISDN = new EndpointReference(extremeEndpoint);

        return new OperatorEndpoint(eprMSISDN, operator.toUpperCase());

    }

     
    /**
     * Gets the API endpoints by app.
     *
     * @param apiKey the api key
     * @param requestPathURL the request path url
     * @param validoperator the validoperator
     * @return the API endpoints by app
     * @throws Exception the exception
     */
    public List<OperatorEndpoint> getAPIEndpointsByApp(String apiKey, String requestPathURL, List<Operator> validoperator) throws Exception {

        List<OperatorEndpoint> endpoints = new ArrayList<OperatorEndpoint>();

        Util.getPropertyFile();
        initialize();

        List<Operatorendpoint> validendpoints = getValidEndpoints(apiKey, validoperator);
        String extremeEndpoint;

        for (Operatorendpoint oe : validendpoints) {
//            if (apiType.equals("payment") || (apiType.equals("location"))) {
//                extremeEndpoint = oe.getEndpoint();
//            } else {
                extremeEndpoint = oe.getEndpoint() + requestPathURL;

//            }

            endpoints.add(new OperatorEndpoint(new EndpointReference(extremeEndpoint), oe.getOperatorcode()));
        }

        return endpoints;
    }

    /**
     * Str_piece.
     *
     * @param str the str
     * @param separator the separator
     * @param index the index
     * @return the string
     */
    private String str_piece(String str, char separator, int index) {
        String str_result = "";
        int count = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == separator) {
                count++;
                if (count == index) {
                    break;
                }
            } else {
                if (count == index - 1) {
                    str_result += str.charAt(i);
                }
            }
        }
        return str_result;
    }

    /**
     * Gets the application property.
     *
     * @param operatorcode the operatorcode
     * @param api the api
     * @return the application property
     */
    private String getApplicationProperty(String operatorcode, String api) {
        String endpoint = null;
        for (Operatorendpoint d : opendpoints) {
            if ((d.getApi().contains(api)) && (d.getOperatorcode().contains(operatorcode))) {
                endpoint = d.getEndpoint();
                break;
            }
        }
        return endpoint;
    }

    /**
     * Gets the valid endpoints.
     *
     * @param api the api
     * @param validoperator the validoperator
     * @return the valid endpoints
     */
    private List<Operatorendpoint> getValidEndpoints(String api, List<Operator> validoperator) {
        String endpoint = null;
        List<String> validlist = new ArrayList();
        List<Operatorendpoint> validoperendpoints = new ArrayList();

        for (Operator op : validoperator) {
            validlist.add(op.getOperatorname());
        }

        for (Operatorendpoint d : opendpoints) {
            if ((d.getApi().contains(api)) && (validlist.contains(d.getOperatorcode()))) {
                validoperendpoints.add(d);
            }
        }
        return validoperendpoints;
    }
    
    /**
     * Gets the valid endpoints.
     *
     * @param api the api
     * @param validoperator the validoperator
     * @return the valid endpoints
     */
    private Operatorendpoint getValidEndpoints(String api, String validoperator) {
        String endpoint = null;
        Operatorendpoint validoperendpoint = null;

        for (Operatorendpoint d : opendpoints) {
            if ((d.getApi().contains(api)) && (validoperator.equalsIgnoreCase(d.getOperatorcode()) ) ) {
                validoperendpoint  = d;
            }
        }
        return validoperendpoint;
    }    
    
}