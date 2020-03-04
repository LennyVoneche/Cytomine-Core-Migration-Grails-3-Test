package cytomine.core.middleware

import cytomine.core.image.server.ImageServer
import cytomine.core.test.HttpClient
import grails.converters.JSON
import grails.transaction.Transactional

@Transactional
class ImageServerService {

    def list() {
        ImageServer.list()
    }

    def getStorageSpaces() {
        def result = []
        String url;
        ImageServer.list().each {
            url = it.url+"/storage/size.json"

            HttpClient client = new HttpClient()

            client.connect(url,"","")

            client.get()

            String response = client.getResponseData()
            int code = client.getResponseCode()
            log.info "code=$code response=$response"
            if(code < 400){
                result << JSON.parse(response)
            }
        }

        // if dns sharding, multiple link are to the same IMS. We merge the same IMS.
        result = result.unique { it.hostname }
        return result
    }
}
