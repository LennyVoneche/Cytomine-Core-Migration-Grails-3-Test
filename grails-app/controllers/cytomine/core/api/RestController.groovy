package cytomine.core.api

/*
* Copyright (c) 2009-2017. Authors: see NOTICE file.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import cytomine.core.Exception.CytomineException
import cytomine.core.test.HttpClient
import cytomine.core.utils.Task
import grails.converters.JSON
import org.grails.web.json.JSONArray

import javax.imageio.ImageIO
import java.awt.image.BufferedImage

 class RestController {

    def sessionFactory
//    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def springSecurityService

    static final int NOT_FOUND_CODE = 404

    def transactionService

    def currentDomain() {
        return null
    }


    //usefull for doc
//    def currentDomainName() {
//        return
//    }
    /**
     * Call add function for this service with the json
     * json parameter can be an array or a single item
     * If json is array => add multiple item
     * otherwise add single item
     * @param service Service for this domain
     * @param json JSON data
     * @return response
     */
    public Object add(def service, def json) {
        try {
            if (json instanceof JSONArray) {
                responseResult(addMultiple(service, json))
            } else {
                def result = addOne(service, json)
                if(result) {
                    responseResult(result)
                }
            }
        } catch (Exception e) {//CytomineException
            log.error("add error:" + e.msg)
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Call update function for this service with the json
     * @param service Service for this domain
     * @param json JSON data
     * @return response
     */
    public Object update(def service, def json) {
        try {
            def domain =  service.retrieve(json)
            def result = service.update(domain,json)
            responseResult(result)
        } catch (Exception e) {//CytomineException
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Call delete function for this service with the json
     * @param service Service for this domain
     * @param json JSON data
     * @return response
     */
    public Object delete(def service, def json, Task task) {
        try {
            def domain = service.retrieve(json)
            def result = service.delete(domain,transactionService.start(),task,true)
            responseResult(result)
        } catch (Exception e) {//CytomineException
            log.error(e)
            response([success: false, errors: e.msg], e.code)
        }
    }

    /**
     * Call add function for this service with the json
     * @param service Service for this domain
     * @param json JSON data
     * @return response
     */
    public Object addOne(def service, def json) {
        return service.add(json)
    }

    /**
     * Call add function for this service for each item from the json array
     * @param service Service for this domain
     * @param json JSON data
     * @return response
     */
    public Object addMultiple(def service, def json) {
        return service.addMultiple(json)
    }

    /**
     * Response this data as HTTP response
     * @param data Data ro send
     * @return response
     */
    protected def response(data) {
        withFormat {
            json {
                render data as JSON
            }
            jsonp {
                response.contentType = 'application/javascript'
                render "${params.callback}(${data as JSON})"
            }
        }
    }

    /**
     * Build a response message for an object return by a command
     * @param result Command result
     * @return response
     */
    protected def responseResult(result) {
        response.status = result.status
        withFormat {
            json { render result.data as JSON }
        }
    }

    /**
     * Response a successful HTTP message
     * @param data Message content
     */
    protected def responseSuccess(data) {
        if(data instanceof List) {
            return responseList(data)
        } else if(data instanceof Collection) {
            List list = []
            list.addAll(data)
            return responseList(list)
        }
        else {
            response(data)
        }
    }

    protected def responseList(List list) {

        Boolean datatables = (params.datatables != null)

        Integer offset = params.offset != null ? params.getInt('offset') : 0
        Integer max = (params.max != null && params.getInt('max')!=0) ? params.getInt('max') : Integer.MAX_VALUE

        List subList
        if (offset >= list.size()) {
            subList = []
        } else {
            def maxForCollection = Math.min(list.size() - offset, max)
            subList = list.subList(offset,offset + maxForCollection)
        }

        if (datatables) {
            responseSuccess ([aaData: subList, sEcho: params.sEcho , iTotalRecords: list.size(), iTotalDisplayRecords : list.size()])
        } else {
            responseSuccess ([collection: subList, offset: offset, perPage : Math.min(max, list.size()), size: list.size(), totalPages: Math.ceil(list.size()/max)])
        }

    }

    /**
     * Response an HTTP message
     * @param data Message content
     * @param code HTTP code
     */
    protected def response(data, code) {
        response.status = code
        response(data)
    }

    public def responseError(Exception e) {//CytomineException
        response([success: false, errors: e.msg], e.code)
    }

    /**
     * Build a response message for a domain not found
     * E.g. annotation 34 was not found
     * className = annotation, id = 34.
     * @param className Type of domain not found
     * @param id Domain id
     */
    protected def responseNotFound(className, id) {
        log.info "responseNotFound $className $id"
        log.error className + " Id " + id + " does not exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id : " + id)
        }
    }

    /**
     * Build a response message for a domain not found with 1 filter
     * E.g. relationterm find by relation => relationterm not found
     * className = relationterm, filter1 = relation, ids = relation.id, ...
     * @param className Type of domain not found
     * @param filter1 Type of domain for the first filter
     * @param id1 Id for the first filter
     */
    protected def responseNotFound(className, filter, id) {
        log.info className + ": " + filter + " " + id + " does not exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter + " : " + id)
        }
    }

    /**
     * Build a response message for a domain not found with 2 filter
     * E.g. relationterm find by relation + term1 => relationterm not found
     * className = relationterm, filter1 = relation, ids = relation.id, ...
     * @param className Type of domain not found
     * @param filter1 Type of domain for the first filter
     * @param id1 Id for the first filter
     * @param filter2 Type of domain for the second filter
     * @param id2 Id for the second filter
     */
    protected def responseNotFound(className, filter1, filter2, id1, id2) {
        log.info className + ": " + filter1 + " " + id1 + ", " + filter2 + " " + id2 + " does not exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter1 + " : " + id1 + " and  " + filter2 + " : " + id2)
        }
    }

    /**
     * Build a response message for a domain not found with 3 filter
     * E.g. relationterm find by relation + term1 + term 2 => relationterm not found
     * className = relationterm, filter1 = relation, ids = relation.id, ...
     * @param className Type of domain not found
     * @param filter1 Type of domain for the first filter
     * @param id1 Id for the first filter
     * @param filter2 Type of domain for the second filter
     * @param id2 Id for the second filter
     * @param filter3 Type of domain for the third filter
     * @param id3 Id for the third filter
     */
    protected def responseNotFound(className, filter1, id1, filter2, id2, filter3, id3) {
        log.info className + ": " + filter1 + " " + id1 + ", " + filter2 + " " + id2 + " and " + filter3 + " " + id3 + " does not exist"
        response.status = NOT_FOUND_CODE
        render(contentType: 'text/json') {
            errors(message: className + " not found with id " + filter1 + " : " + id1 + ",  " + filter2 + " : " + id2 + " and " + filter3 + " : " + id3)
        }
    }

    /**
     * Response an image as a HTTP response
     * @param url Image url
     */
    protected def responseImage(String url) {
        withFormat {
            png {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/png")
                }
                else {
                    HttpClient client = new HttpClient()
                    client.timeout = 60000;
                    client.connect(url, "", "")
                    byte[] imageData = client.getData()
                    //IIP Send JPEG, so we have to convert to PNG
                    InputStream input = new ByteArrayInputStream(imageData);
                    BufferedImage bufferedImage = ImageIO.read(input);
                    def out = new ByteArrayOutputStream()
                    ImageIO.write(bufferedImage, "PNG", out)
                    response.contentType = "image/png"
                    response.getOutputStream() << out.toByteArray()
                }
            }
            jpg {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/jpeg")
                } else {
                    URL source = new URL(url)
                    URLConnection connection = source.openConnection()
                    response.outputStream << connection.getInputStream()
                }
            }
        }

    }


    /**
     * Response an image as a HTTP response
     * @param bufferedImage Image
     */
    protected def responseBufferedImage(BufferedImage bufferedImage) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        withFormat {

            png {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/png")
                }
                else {
                    ImageIO.write(bufferedImage, "png", baos);
                    byte[] bytesOut = baos.toByteArray();
                    response.contentLength = baos.size();
                    response.setHeader("Connection", "Keep-Alive")
                    response.setHeader("Accept-Ranges", "bytes")
                    response.setHeader("Content-Type", "image/png")
                    response.getOutputStream() << bytesOut
                    response.getOutputStream().flush()
                }
            }
            jpg {
                if (request.method == 'HEAD') {
                    render(text: "", contentType: "image/jpeg");
                }
                else {
                    ImageIO.write(bufferedImage, "jpg", baos);
                    byte[] bytesOut = baos.toByteArray();
                    response.contentLength = baos.size();
                    response.setHeader("Connection", "Keep-Alive")
                    response.setHeader("Accept-Ranges", "bytes")
                    response.setHeader("Content-Type", "image/jpeg")
                    response.getOutputStream() << bytesOut
                    response.getOutputStream().flush()
                }
            }
        }
    }


    /**
     * Substract the collection with offset (min) and max
     * @param collection Full collection
     * @param offset Min index
     * @param max Maximum index
     * @return Substracted collection with first elem = min and last elem (= max)
     */
    protected def substract(List collection, Integer offset, Integer max) {
        if (offset >= collection.size()) {
            return []
        }
        def maxForCollection = Math.min(collection.size() - offset, max)
        return collection.subList(offset, offset + maxForCollection)
    }
}