package cytomine.core.image

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

import cytomine.core.CytomineDomain
import cytomine.core.Exception.CytomineException
import cytomine.core.Exception.ServerException
import cytomine.core.Exception.WrongArgumentException
import cytomine.core.api.UrlApi
import cytomine.core.image.acquisition.Instrument
import cytomine.core.image.server.ImageServerStorage
import cytomine.core.image.server.MimeImageServer
import cytomine.core.image.server.StorageAbstractImage
import cytomine.core.laboratory.Sample
import cytomine.core.security.SecUser
import cytomine.core.utils.JSONUtils
//import org.restapidoc.annotation.RestApiObject
//import org.restapidoc.annotation.RestApiObjectField
//import org.restapidoc.annotation.RestApiObjectFields

/**
 * An abstract image is an image that can be map with projects.
 * When an "AbstractImage" is add to a project, a "ImageInstance" is created.
 */
//@RestApiObject(name = "Abstract image", description = "A real image store on disk, see 'image instance' for an image link in a project")
class AbstractImage extends CytomineDomain implements Serializable {

//    @RestApiObjectField(description = "The image short filename (will be show in GUI)", useForCreation = false)
    String originalFilename

//    @RestApiObjectField(description = "The exact image full filename")
    String filename

//    @RestApiObjectField(description = "The instrument that digitalize the image", mandatory = false)
    Instrument scanner

//    @RestApiObjectField(description = "The source of the image (human, annimal,...)", mandatory = false)
    Sample sample

//    @RestApiObjectField(description = "The full image path directory")
    String path

//    @RestApiObjectField(description = "The image type. For creation, use the ext (not the mime id!)")
    Mime mime

//    @RestApiObjectField(description = "The image width", mandatory = false, defaultValue = "-1")
    Integer width

//    @RestApiObjectField(description = "The image height", mandatory = false, defaultValue = "-1")
    Integer height

//    @RestApiObjectField(description = "The image max zoom")
    Integer magnification

//    @RestApiObjectField(description = "The image resolution (microm per pixel)")
    Double resolution

//    @RestApiObjectField(description = "The image bit depth (bits per channel)")
    Integer bitDepth

//    @RestApiObjectField(description = "The image colorspace")
    String colorspace

//    @RestApiObjectField(description = "The image owner", mandatory = false, defaultValue = "current user")
    SecUser user //owner

    static belongsTo = Sample

//    @RestApiObjectFields(params=[
//        @RestApiObjectField(apiFieldName = "metadataUrl", description = "URL to get image file metadata",allowedType = "string",useForCreation = false),
//        @RestApiObjectField(apiFieldName = "thumb", description = "URL to get abstract image short view (htumb)",allowedType = "string",useForCreation = false)
//    ])
    static transients = ["zoomLevels", "thumbURL"]

    static mapping = {
        id generator: "assigned"
        sort "id"
        mime fetch: 'join'

    }

    static constraints = {
        originalFilename(nullable: true, blank: false, unique: false)
        filename(blank: false, unique: true)
        scanner(nullable: true)
        sample(nullable: true)
        path(nullable: false)
        mime(nullable: false)
        width(nullable: true)
        height(nullable: true)
        resolution(nullable: true)
        magnification(nullable: true)
        bitDepth(nullable: true)
        colorspace(nullable: true)
        user(nullable: true)
    }

    public beforeInsert() {
        super.beforeInsert()
        if (originalFilename == null || originalFilename == "") {
            String filename = getFilename()
            filename = filename.replace(".vips.tiff", "")
            filename = filename.replace(".vips.tif", "")
            if (filename.lastIndexOf("/") != -1 && filename.lastIndexOf("/") != filename.size())
                filename = filename.substring(filename.lastIndexOf("/")+1, filename.size())
            originalFilename = filename
        }
    }

    public beforeUpdate() {
        super.beforeInsert()
        if (originalFilename == null || originalFilename == "") {
            String filename = getFilename()
            filename = filename.replace(".vips.tiff", "")
            filename = filename.replace(".vips.tif", "")
            if (filename.lastIndexOf("/") != -1 && filename.lastIndexOf("/") != filename.size())
                filename = filename.substring(filename.lastIndexOf("/")+1, filename.size())
            originalFilename = filename
        }
    }


    /**
     * Insert JSON data into domain in param
     * @param domain Domain that must be filled
     * @param json JSON containing data
     * @return Domain with json data filled
     * @throws CytomineException Error during properties copy (wrong argument,...)
     */
    static AbstractImage insertDataIntoDomain(def json, def domain = new AbstractImage()) throws CytomineException {
        domain.id = JSONUtils.getJSONAttrLong(json,'id',null)
        domain.originalFilename = JSONUtils.getJSONAttrStr(json,'originalFilename')
        domain.filename = JSONUtils.getJSONAttrStr(json,'filename')
        domain.path = JSONUtils.getJSONAttrStr(json,'path')
        domain.height = JSONUtils.getJSONAttrInteger(json,'height',-1)
        domain.width = JSONUtils.getJSONAttrInteger(json,'width',-1)
        domain.created = JSONUtils.getJSONAttrDate(json,'created')
        domain.updated = JSONUtils.getJSONAttrDate(json,'updated')
        domain.scanner = JSONUtils.getJSONAttrDomain(json,"scanner",new Instrument(),false)
        domain.sample = JSONUtils.getJSONAttrDomain(json,"sample",new Sample(),false)
        domain.mime = JSONUtils.getJSONAttrDomain(json,"mime",new Mime(),'mimeType','String',true)
        domain.magnification = JSONUtils.getJSONAttrInteger(json,'magnification',null)
        domain.resolution = JSONUtils.getJSONAttrDouble(json,'resolution',null)
        domain.bitDepth = JSONUtils.getJSONAttrInteger(json, 'bitDepth', null)
        domain.colorspace = JSONUtils.getJSONAttrStr(json, 'colorspace', false)
        domain.deleted = JSONUtils.getJSONAttrDate(json, "deleted")

        if (domain.mime.imageServers().size() == 0) {
            throw new WrongArgumentException("Mime with id:${json.mime} has not image server")
        }
        return domain;
    }

    /**
     * Define fields available for JSON response
     * @param domain Domain source for json value
     * @return Map with fields (keys) and their values
     */
    static def getDataFromDomain(def image) {
        def returnArray = CytomineDomain.getDataFromDomain(image)
        returnArray['filename'] = image?.filename
        returnArray['originalFilename'] = image?.originalFilename
        returnArray['scanner'] = image?.scanner?.id
        returnArray['sample'] = image?.sample?.id
        returnArray['path'] = image?.path
        returnArray['mime'] = image?.mime?.mimeType
        returnArray['width'] = image?.width
        returnArray['height'] = image?.height
        returnArray['depth'] = image?.getZoomLevels()?.max
        returnArray['resolution'] = image?.resolution
        returnArray['magnification'] = image?.magnification
        returnArray['bitDepth'] = image?.bitDepth
        returnArray['colorspace'] = image?.colorspace
        returnArray['thumb'] = UrlApi.getThumbImage(image ? (long)image?.id : null, 512)
        returnArray['preview'] = UrlApi.getThumbImage(image ? (long)image?.id : null, 1024)
        returnArray['fullPath'] = image?.getAbsolutePath()
        returnArray['macroURL'] = UrlApi.getAssociatedImage(image ? (long)image?.id : null, "macro", 512)
        returnArray
    }


    def getImageServersStorage() {
        try {

            def imageServers = MimeImageServer.findAllByMime(this.getMime())?.collect {it.imageServer}.findAll{it.available}.unique()

            def storageAbstractImage = StorageAbstractImage.findAllByAbstractImage(this)?.collect { it.storage }

            if (imageServers.isEmpty() || storageAbstractImage.isEmpty()) return []
            else {
                return ImageServerStorage.createCriteria().list {
                    inList("imageServer",  imageServers)
                    inList("storage", storageAbstractImage )
                }
            }
        } catch (Exception e) {
            //may appear during tests
            //this method does not work with an unsaved domain or a domain instance with transients values
            //find another way to handle the error ?
            log.error "cannot get imageServerStorage from AbstractImage $this"
            return null
        }

    }

    def getAbsolutePath() {
        if(this.version != null) {
            def sai = StorageAbstractImage.findByAbstractImage(this);
            if(sai) return [ sai.storage.basePath, this.path].join(File.separator)
        }
    }

    def getMimeType(){
        return mime?.mimeType
    }

    def getRandomImageServerURL() {
        def imageServerStorages = getImageServersStorage()
        if (imageServerStorages == null || imageServerStorages.size() == 0) {
            throw new ServerException("no IMS found")
            //return null
        }
        def index = (Integer) Math.round(Math.random() * (imageServerStorages.size() - 1)) //select an url randomly
        return imageServerStorages[index].imageServer.url
    }

    /*def getCropURL(def boundaries) {
        def imageServerStorages = getImageServersStorage()

        if (imageServerStorages == null || imageServerStorages.size() == 0) {
            return null
        }
        def index = (Integer) Math.round(Math.random() * (imageServerStorages.size() - 1)) //select an url randomly
        Resolver resolver = Resolver.getResolver(imageServerStorages[index].imageServer.className)

        if (!resolver) return null
        def baseUrl = imageServerStorages[index].imageServer.getBaseUrl()
        Storage storage = StorageAbstractImage.findAllByAbstractImage(this).first().storage

        String basePath = storage.getBasePath()
        String path = getPath()

        boundaries.baseImageWidth =this.getWidth()
        boundaries.baseImageHeight =this.getHeight()
        resolver.getCropURL(baseUrl, [basePath, path].join(File.separator), boundaries)
    }*/

    def getZoomLevels() {
        if (!width || !height) return [min : 0, max : 9, middle : 0]
        double tmpWidth = width
        double tmpHeight = height
        def nbZoom = 0
        while (tmpWidth > 256 || tmpHeight > 256) {
            nbZoom++
            tmpWidth = tmpWidth / 2
            tmpHeight = tmpHeight / 2
        }
        return [min : 0, max : nbZoom, middle : (nbZoom / 2), overviewWidth : Math.round(tmpWidth), overviewHeight : Math.round(tmpHeight), width : width, height : height]

    }

    /**
     * Get the container domain for this domain (usefull for security)
     * @return Container of this domain
     */
    public CytomineDomain[] containers() {
        StorageAbstractImage.findAllByAbstractImage(this)?.collect { it.storage }
//        getImageServersStorage().collect {
//            it.storage
//        }
    }
}